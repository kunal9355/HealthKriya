package com.kunal.healthkriya.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.kunal.healthkriya.data.local.donation.DonationDao;
import com.kunal.healthkriya.data.local.donation.DonationDatabase;
import com.kunal.healthkriya.data.local.donation.DonationEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DonationRepository {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    private final DonationDao donationDao;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ListenerRegistration firebaseListener;

    public DonationRepository(Context context) {
        donationDao = DonationDatabase.getInstance(context).donationDao();
    }

    public static String newClientId() {
        return UUID.randomUUID().toString();
    }

    public void saveDonation(DonationEntity donation, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                donation.deleted = false;
                donation.updatedAt = System.currentTimeMillis();
                donation.syncStatus = DonationEntity.SYNC_PENDING;
                donationDao.insertOrUpdate(donation);

                pushDonationToFirebase(donation);
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void getAllActiveDonations(DonationsCallback callback) {
        EXECUTOR.execute(() -> {
            List<DonationEntity> items = donationDao.getAllActive();
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(items));
            }
        });
    }

    public void getRecentByType(String category, String actionType, int limit, DonationsCallback callback) {
        EXECUTOR.execute(() -> {
            List<DonationEntity> items = donationDao.getRecentByType(category, actionType, limit);
            if (callback != null) {
                mainHandler.post(() -> callback.onResult(items));
            }
        });
    }

    public void restoreFromFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        firestore.collection("users")
                .document(uid)
                .collection("donations")
                .get()
                .addOnSuccessListener(snapshots -> EXECUTOR.execute(() -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DonationEntity incoming = fromDocument(doc);
                        if (incoming != null) {
                            mergeIncoming(incoming);
                        }
                    }
                    syncPendingDonations();
                    startRealtimeSync();
                }))
                .addOnFailureListener(e -> startRealtimeSync());
    }

    public void startRealtimeSync() {
        stopRealtimeSync();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        firebaseListener = firestore.collection("users")
                .document(uid)
                .collection("donations")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    EXECUTOR.execute(() -> {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            DonationEntity incoming = fromDocument(change.getDocument());
                            if (incoming != null) {
                                mergeIncoming(incoming);
                            }
                        }
                    });
                });
    }

    public void stopRealtimeSync() {
        if (firebaseListener != null) {
            firebaseListener.remove();
            firebaseListener = null;
        }
    }

    private void syncPendingDonations() {
        List<DonationEntity> unsynced = donationDao.getUnsynced(DonationEntity.SYNC_SYNCED);
        for (DonationEntity donation : unsynced) {
            pushDonationToFirebase(donation);
        }
    }

    private void pushDonationToFirebase(DonationEntity donation) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            return;
        }

        long version = donation.updatedAt;
        Map<String, Object> map = new HashMap<>();
        map.put("category", donation.category);
        map.put("actionType", donation.actionType);
        map.put("title", donation.title);
        map.put("detail", donation.detail);
        map.put("updatedAt", donation.updatedAt);
        map.put("deleted", donation.deleted);

        firestore.collection("users")
                .document(uid)
                .collection("donations")
                .document(donation.clientId)
                .set(map)
                .addOnSuccessListener(unused ->
                        EXECUTOR.execute(() -> markSyncStatusIfCurrent(
                                donation.clientId,
                                version,
                                DonationEntity.SYNC_SYNCED
                        ))
                )
                .addOnFailureListener(e ->
                        EXECUTOR.execute(() -> markSyncStatusIfCurrent(
                                donation.clientId,
                                version,
                                DonationEntity.SYNC_ERROR
                        ))
                );
    }

    private void markSyncStatusIfCurrent(String clientId, long updatedAt, int syncStatus) {
        DonationEntity current = donationDao.getByClientIdSync(clientId);
        if (current == null) {
            return;
        }
        if (current.updatedAt == updatedAt && current.syncStatus == DonationEntity.SYNC_PENDING) {
            current.syncStatus = syncStatus;
            donationDao.insertOrUpdate(current);
        }
    }

    private void mergeIncoming(DonationEntity incoming) {
        DonationEntity local = donationDao.getByClientIdSync(incoming.clientId);
        if (local == null || incoming.updatedAt > local.updatedAt) {
            donationDao.insertOrUpdate(incoming);
            return;
        }

        if (incoming.updatedAt == local.updatedAt) {
            boolean changed =
                    local.syncStatus != incoming.syncStatus
                            || local.deleted != incoming.deleted
                            || !Objects.equals(local.category, incoming.category)
                            || !Objects.equals(local.actionType, incoming.actionType)
                            || !Objects.equals(local.title, incoming.title)
                            || !Objects.equals(local.detail, incoming.detail);
            if (changed) {
                donationDao.insertOrUpdate(incoming);
            }
        }
    }

    private DonationEntity fromDocument(DocumentSnapshot doc) {
        String clientId = doc.getId();
        String category = doc.getString("category");
        String actionType = doc.getString("actionType");
        String title = doc.getString("title");
        String detail = doc.getString("detail");
        Long updatedAt = doc.getLong("updatedAt");
        Boolean deleted = doc.getBoolean("deleted");

        return new DonationEntity(
                clientId,
                safeText(category),
                safeText(actionType),
                safeText(title),
                safeText(detail),
                updatedAt != null ? updatedAt : System.currentTimeMillis(),
                doc.getMetadata().hasPendingWrites()
                        ? DonationEntity.SYNC_PENDING
                        : DonationEntity.SYNC_SYNCED,
                deleted != null && deleted
        );
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void postSave(SaveCallback callback, boolean success, Exception error) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onComplete(success, error));
    }

    public interface SaveCallback {
        void onComplete(boolean success, Exception error);
    }

    public interface DonationsCallback {
        void onResult(List<DonationEntity> donations);
    }
}
