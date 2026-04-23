package com.kunal.healthkriya.data.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.kunal.healthkriya.data.helper.LocationHelper;
import com.kunal.healthkriya.data.helper.NotificationHelper;
import com.kunal.healthkriya.data.local.donation.DonationDao;
import com.kunal.healthkriya.data.local.donation.DonationDatabase;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.model.donation.HelpInterest;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DonationRepository {
    public static final String ACTION_DONATIONS_CHANGED = "com.kunal.healthkriya.action.DONATIONS_CHANGED";

    private static final String COLLECTION_DONATIONS = "donations";
    private static final String COLLECTION_HELP_INTERESTS = "donation_help_interests";
    private static final String COLLECTION_REPORTS = "donation_reports";

    private static final int MAX_REQUESTS_PER_DAY = 5;
    private static final long REQUEST_EXPIRY_MS = 48L * 60L * 60L * 1000L;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    private final Context appContext;
    private final DonationDao donationDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private ListenerRegistration userListener;
    private ListenerRegistration publicFeedListener;
    private ListenerRegistration helpResponseListener;

    private boolean publicFeedHydrated;
    private boolean helpResponseHydrated;

    public DonationRepository(Context context) {
        appContext = context.getApplicationContext();
        donationDao = DonationDatabase.getInstance(appContext).donationDao();
    }

    public static String newClientId() {
        return UUID.randomUUID().toString();
    }

    public void saveDonation(DonationEntity donation, SaveCallback callback) {
        persistDonation(donation, callback);
    }

    public void updateDonation(DonationEntity donation, SaveCallback callback) {
        persistDonation(donation, callback);
    }

    public void cancelDonation(String clientId, SaveCallback callback) {
        updateDonationStatus(clientId, DonationEntity.STATUS_CANCELLED, callback);
    }

    public void markDonationCompleted(String clientId, SaveCallback callback) {
        updateDonationStatus(clientId, DonationEntity.STATUS_COMPLETED, callback);
    }

    public void requestHelp(String requestId, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                String helperId = auth.getUid();
                if (helperId == null || helperId.trim().isEmpty()) {
                    postSave(callback, false, new IllegalStateException("User not logged in"));
                    return;
                }
                if (requestId == null || requestId.trim().isEmpty()) {
                    postSave(callback, false, new IllegalArgumentException("Request not found"));
                    return;
                }

                String normalizedRequestId = requestId.trim();
                DonationEntity donation = donationDao.getByClientIdSync(normalizedRequestId);
                if (donation == null) {
                    postSave(callback, false, new IllegalStateException("Request not available"));
                    return;
                }

                if (helperId.equals(donation.userId)) {
                    postSave(callback, false, new IllegalStateException("Owner cannot help own request"));
                    return;
                }
                if (!DonationEntity.ACTION_REQUEST.equals(donation.action)) {
                    postSave(callback, false, new IllegalStateException("Help is for request posts only"));
                    return;
                }
                if (!DonationEntity.STATUS_ACTIVE.equals(donation.status)) {
                    postSave(callback, false, new IllegalStateException("This request is not active"));
                    return;
                }

                String docId = buildHelpDocId(normalizedRequestId, helperId);
                long now = System.currentTimeMillis();
                Map<String, Object> payload = new HashMap<>();
                payload.put("requestId", normalizedRequestId);
                payload.put("ownerId", donation.userId);
                payload.put("helperId", helperId);
                payload.put("status", HelpInterest.STATUS_PENDING);
                payload.put("createdAt", now);
                payload.put("updatedAt", now);

                firestore.collection(COLLECTION_HELP_INTERESTS)
                        .document(docId)
                        .set(payload)
                        .addOnSuccessListener(unused -> postSave(callback, true, null))
                        .addOnFailureListener(error -> postSave(callback, false, error));
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    public void reportDonation(String requestId, String reason, SaveCallback callback) {
        String reporterId = auth.getUid();
        if (reporterId == null || reporterId.trim().isEmpty()) {
            postSave(callback, false, new IllegalStateException("User not logged in"));
            return;
        }
        if (requestId == null || requestId.trim().isEmpty()) {
            postSave(callback, false, new IllegalArgumentException("Request not found"));
            return;
        }

        String safeReason = stringOrEmpty(reason);
        if (safeReason.trim().isEmpty()) {
            safeReason = "suspicious_request";
        }

        long now = System.currentTimeMillis();
        String docId = requestId.trim() + "_" + reporterId;
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", requestId.trim());
        payload.put("reporterId", reporterId);
        payload.put("reason", safeReason);
        payload.put("status", "open");
        payload.put("createdAt", now);
        payload.put("updatedAt", now);

        firestore.collection(COLLECTION_REPORTS)
                .document(docId)
                .set(payload)
                .addOnSuccessListener(unused -> postSave(callback, true, null))
                .addOnFailureListener(error -> postSave(callback, false, error));
    }

    public void fetchHelpSummary(String requestId, HelpSummaryCallback callback) {
        if (requestId == null || requestId.trim().isEmpty()) {
            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(true, new HelpSummary(0, false), null));
            }
            return;
        }

        firestore.collection(COLLECTION_HELP_INTERESTS)
                .whereEqualTo("requestId", requestId.trim())
                .get()
                .addOnSuccessListener(snapshots ->
                        postHelpSummary(callback, parseHelpSummary(snapshots), null))
                .addOnFailureListener(error ->
                        postHelpSummary(callback, new HelpSummary(0, false), error));
    }

    public HelpSubscription listenToHelpSummary(String requestId, HelpSummaryListener listener) {
        if (requestId == null || requestId.trim().isEmpty() || listener == null) {
            return () -> {
            };
        }

        ListenerRegistration registration = firestore.collection(COLLECTION_HELP_INTERESTS)
                .whereEqualTo("requestId", requestId.trim())
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }
                    HelpSummary summary = parseHelpSummary(snapshots);
                    mainHandler.post(() -> listener.onUpdate(summary));
                });
        return registration::remove;
    }

    public LiveData<List<DonationEntity>> getUserDonations() {
        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            MutableLiveData<List<DonationEntity>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return donationDao.observeByUserId(uid);
    }

    public LiveData<List<DonationEntity>> getUserDonations(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            MutableLiveData<List<DonationEntity>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return donationDao.observeByUserId(userId.trim());
    }

    public LiveData<List<DonationEntity>> getPublicDonations() {
        expireStaleRequestsAsync();
        return donationDao.observeAllPublic();
    }

    public LiveData<DonationEntity> getDonation(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            MutableLiveData<DonationEntity> empty = new MutableLiveData<>();
            empty.setValue(null);
            return empty;
        }
        return donationDao.observeByClientId(clientId.trim());
    }

    public String getCurrentUserId() {
        return auth.getUid();
    }

    public void listenToPublicFeed() {
        stopPublicFeedListener();
        publicFeedHydrated = false;

        android.util.Log.d("DonationRepository", "Starting public feed listener...");

        publicFeedListener = firestore.collection(COLLECTION_DONATIONS)
                .whereEqualTo("isPublic", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        android.util.Log.e("DonationRepository", "Public feed listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    android.util.Log.d("DonationRepository", "Public feed update received: " + snapshots.getDocumentChanges().size() + " changes");

                    boolean shouldNotify = publicFeedHydrated;
                    publicFeedHydrated = true;

                    EXECUTOR.execute(() -> {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            handlePublicChange(change, shouldNotify);
                        }
                        notifyDonationsChanged();
                    });
                });
    }

    public void restoreFromFirebase() {
        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            listenToPublicFeed();
            return;
        }

        firestore.collection(COLLECTION_DONATIONS)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snapshot -> EXECUTOR.execute(() -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        mergeIncoming(fromDocument(doc));
                    }
                    syncPendingDonations();
                    expireStaleRequestsAsync();
                    startRealtimeSync();
                    notifyDonationsChanged();
                }))
                .addOnFailureListener(e -> startRealtimeSync());
    }

    public void startRealtimeSync() {
        stopRealtimeSync();
        expireStaleRequestsAsync();
        listenToUserDonations();
        listenToPublicFeed();
        listenToHelpResponses();
    }

    public void stopRealtimeSync() {
        stopUserListener();
        stopPublicFeedListener();
        stopHelpResponseListener();
        publicFeedHydrated = false;
        helpResponseHydrated = false;
    }

    public void clearLocalData() {
        EXECUTOR.execute(() -> {
            stopRealtimeSync();
            donationDao.clearAll();
            notifyDonationsChanged();
        });
    }

    private void updateDonationStatus(String clientId, String status, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                if (clientId == null || clientId.trim().isEmpty()) {
                    postSave(callback, false, new IllegalArgumentException("Donation not found"));
                    return;
                }

                DonationEntity existing = donationDao.getByClientIdSync(clientId.trim());
                if (existing == null) {
                    postSave(callback, false, new IllegalStateException("Donation not found"));
                    return;
                }

                String uid = auth.getUid();
                if (uid == null || uid.trim().isEmpty()) {
                    postSave(callback, false, new IllegalStateException("User not logged in"));
                    return;
                }
                if (!uid.equals(existing.userId)) {
                    postSave(callback, false, new IllegalStateException("Only owner can update status"));
                    return;
                }

                String normalizedStatus = normalizeStatus(status);
                existing.status = normalizedStatus;
                
                // Keep it public if active or in-progress
                existing.isPublic = DonationEntity.STATUS_ACTIVE.equals(normalizedStatus)
                        || DonationEntity.STATUS_IN_PROGRESS.equals(normalizedStatus);
                
                existing.updatedAt = System.currentTimeMillis();
                existing.syncStatus = DonationEntity.SYNC_PENDING;

                donationDao.insertOrUpdate(existing);
                pushDonationToFirebase(existing);
                notifyDonationsChanged();
                NotificationHelper.notifyStatusUpdate(appContext, existing.clientId, existing.status);
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    private void persistDonation(DonationEntity donation, SaveCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                DonationEntity prepared = prepareForSave(donation);
                donationDao.insertOrUpdate(prepared);
                pushDonationToFirebase(prepared);
                LocationHelper.rememberDonationContext(appContext, prepared);
                notifyDonationsChanged();
                postSave(callback, true, null);
            } catch (Exception e) {
                postSave(callback, false, e);
            }
        });
    }

    private DonationEntity prepareForSave(DonationEntity donation) {
        if (donation == null) {
            throw new IllegalArgumentException("Donation data missing");
        }

        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalStateException("User not logged in");
        }

        long now = System.currentTimeMillis();
        if (donation.clientId == null || donation.clientId.trim().isEmpty()) {
            donation.clientId = newClientId();
        }

        DonationEntity existing = donationDao.getByClientIdSync(donation.clientId);
        if (existing != null && hasText(existing.userId) && !uid.equals(existing.userId)) {
            throw new IllegalStateException("Only owner can edit donation");
        }

        donation.userId = uid;
        donation.type = normalizeType(donation.type);
        donation.action = normalizeAction(donation.action);
        if (!hasText(donation.status)) {
            donation.status = existing != null ? existing.status : DonationEntity.STATUS_ACTIVE;
        }
        donation.status = normalizeStatus(donation.status);
        donation.urgency = normalizeUrgency(donation.urgency);

        // Ensure isPublic is set correctly based on status and action if not already set
        // For 'donate' and 'request', if status is 'active' or 'in_progress', it's usually public
        // but we respect the existing value if this is an update.
        if (existing == null && (DonationEntity.STATUS_ACTIVE.equals(donation.status) 
                || DonationEntity.STATUS_IN_PROGRESS.equals(donation.status))) {
            donation.isPublic = true;
        }

        boolean isNewRequest = existing == null && DonationEntity.ACTION_REQUEST.equals(donation.action);
        if (isNewRequest) {
            int requestCountToday = donationDao.countUserRequestsSince(
                    uid,
                    DonationEntity.ACTION_REQUEST,
                    dayStart(now)
            );
            if (requestCountToday >= MAX_REQUESTS_PER_DAY) {
                throw new IllegalStateException("Daily request limit reached. Try again tomorrow.");
            }
        }

        if (donation.createdAt <= 0L) {
            if (existing != null && existing.createdAt > 0L) {
                donation.createdAt = existing.createdAt;
            } else {
                donation.createdAt = now;
            }
        }

        if (existing != null && donation.id <= 0L) {
            donation.id = existing.id;
        }
        if (donation.name == null) donation.name = "";
        if (donation.title == null || donation.title.trim().isEmpty()) {
            donation.title = buildFallbackTitle(donation);
        }
        if (donation.description == null) donation.description = "";
        if (donation.city == null) donation.city = "";
        if (donation.contact == null) donation.contact = "";
        if (donation.bloodGroup == null) donation.bloodGroup = "";
        if (donation.medicineName == null) donation.medicineName = "";
        if (donation.medicineExpiry == null) donation.medicineExpiry = "";

        donation.updatedAt = now;
        donation.syncStatus = DonationEntity.SYNC_PENDING;
        donation.deleted = false;

        if (!DonationEntity.STATUS_ACTIVE.equals(donation.status)
                && !DonationEntity.STATUS_IN_PROGRESS.equals(donation.status)) {
            donation.isPublic = false;
        }

        return donation;
    }

    private void pushDonationToFirebase(DonationEntity donation) {
        long version = donation.updatedAt;
        Map<String, Object> payload = toFirestoreMap(donation);

        firestore.collection(COLLECTION_DONATIONS)
                .document(donation.clientId)
                .set(payload)
                .addOnSuccessListener(unused ->
                        EXECUTOR.execute(() -> markSyncedIfCurrent(donation.clientId, version)))
                .addOnFailureListener(e ->
                        EXECUTOR.execute(() -> markSyncErrorIfCurrent(donation.clientId, version)));
    }

    private void listenToUserDonations() {
        String uid = auth.getUid();
        if (uid == null || uid.trim().isEmpty()) {
            return;
        }

        android.util.Log.d("DonationRepository", "Starting user donations listener for: " + uid);

        userListener = firestore.collection(COLLECTION_DONATIONS)
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        android.util.Log.e("DonationRepository", "User donations listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null) return;

                    EXECUTOR.execute(() -> {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            DonationEntity incoming = fromDocument(change.getDocument());
                            if (incoming == null) {
                                // For REMOVED events, we handle deletion locally
                                if (change.getType() == DocumentChange.Type.REMOVED) {
                                    handleLocalRemoval(change.getDocument().getId());
                                }
                                continue;
                            }
                            mergeIncoming(incoming);
                        }
                        expireStaleRequestsAsync();
                        notifyDonationsChanged();
                    });
                });
    }

    private void handleLocalRemoval(String clientId) {
        if (clientId == null) return;
        DonationEntity local = donationDao.getByClientIdSync(clientId);
        if (local != null) {
            local.isPublic = false;
            local.deleted = true; // Or just hide it
            donationDao.insertOrUpdate(local);
        }
    }

    private void listenToHelpResponses() {
        String ownerId = auth.getUid();
        if (ownerId == null || ownerId.trim().isEmpty()) {
            return;
        }

        stopHelpResponseListener();
        helpResponseHydrated = false;

        helpResponseListener = firestore.collection(COLLECTION_HELP_INTERESTS)
                .whereEqualTo("ownerId", ownerId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        return;
                    }

                    boolean shouldNotify = helpResponseHydrated;
                    helpResponseHydrated = true;

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() != DocumentChange.Type.ADDED) {
                            continue;
                        }

                        String status = stringOrEmpty(change.getDocument().getString("status"));
                        String helperId = stringOrEmpty(change.getDocument().getString("helperId"));
                        String requestId = stringOrEmpty(change.getDocument().getString("requestId"));

                        if (!isReadyHelpStatus(status) || ownerId.equals(helperId)) {
                            continue;
                        }

                        // Update local count and sync back to firestore
                        EXECUTOR.execute(() -> updateLocalHelpCount(requestId));

                        if (shouldNotify) {
                            NotificationHelper.notifyHelpResponse(appContext, requestId, helperId);
                        }
                    }
                });
    }

    private void updateLocalHelpCount(String requestId) {
        if (requestId == null) return;
        DonationEntity donation = donationDao.getByClientIdSync(requestId);
        if (donation == null) return;

        firestore.collection(COLLECTION_HELP_INTERESTS)
                .whereEqualTo("requestId", requestId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (isReadyHelpStatus(stringOrEmpty(doc.getString("status")))) {
                            count++;
                        }
                    }
                    final int finalCount = count;
                    EXECUTOR.execute(() -> {
                        DonationEntity current = donationDao.getByClientIdSync(requestId);
                        if (current != null && current.helpCount != finalCount) {
                            current.helpCount = finalCount;
                            current.updatedAt = System.currentTimeMillis();
                            current.syncStatus = DonationEntity.SYNC_PENDING;
                            donationDao.insertOrUpdate(current);
                            pushDonationToFirebase(current);
                            notifyDonationsChanged();
                        }
                    });
                });
    }

    private void syncPendingDonations() {
        List<DonationEntity> pending = donationDao.getUnsynced(DonationEntity.SYNC_SYNCED);
        for (DonationEntity donation : pending) {
            pushDonationToFirebase(donation);
        }
    }

    private void expireStaleRequestsAsync() {
        EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            long olderThan = now - REQUEST_EXPIRY_MS;
            String currentUid = auth.getUid();

            List<DonationEntity> staleItems = donationDao.getStalePublicRequests(
                    DonationEntity.ACTION_REQUEST,
                    DonationEntity.STATUS_ACTIVE,
                    olderThan
            );

            if (staleItems == null || staleItems.isEmpty()) {
                return;
            }

            for (DonationEntity staleItem : staleItems) {
                staleItem.status = DonationEntity.STATUS_EXPIRED;
                staleItem.isPublic = false;
                staleItem.updatedAt = now;
                boolean isOwner = currentUid != null && currentUid.equals(staleItem.userId);
                staleItem.syncStatus = isOwner
                        ? DonationEntity.SYNC_PENDING
                        : DonationEntity.SYNC_SYNCED;
                donationDao.insertOrUpdate(staleItem);

                if (isOwner) {
                    pushDonationToFirebase(staleItem);
                    NotificationHelper.notifyStatusUpdate(
                            appContext,
                            staleItem.clientId,
                            DonationEntity.STATUS_EXPIRED
                    );
                }
            }

            notifyDonationsChanged();
        });
    }

    private void handlePublicChange(DocumentChange change, boolean notifyOnNewRequest) {
        if (change.getType() == DocumentChange.Type.REMOVED) {
            String clientId = change.getDocument().getId();
            DonationEntity local = donationDao.getByClientIdSync(clientId);
            if (local == null) {
                return;
            }
            local.isPublic = false;
            local.updatedAt = Math.max(local.updatedAt, System.currentTimeMillis());
            local.syncStatus = DonationEntity.SYNC_SYNCED;
            donationDao.insertOrUpdate(local);
            return;
        }

        DonationEntity incoming = fromDocument(change.getDocument());
        if (incoming == null) {
            return;
        }

        mergeIncoming(incoming);
        if (notifyOnNewRequest && change.getType() == DocumentChange.Type.ADDED) {
            notifyNewRequestIfRelevant(incoming);
        }
    }

    private void mergeIncoming(DonationEntity incoming) {
        if (incoming == null || !hasText(incoming.clientId)) {
            return;
        }

        DonationEntity local = donationDao.getByClientIdSync(incoming.clientId);
        if (local != null && incoming.updatedAt < local.updatedAt) {
            return;
        }

        if (local != null) {
            incoming.id = local.id;
        }

        boolean statusChanged = local != null && !stringOrEmpty(local.status).equals(incoming.status);
        incoming.syncStatus = DonationEntity.SYNC_SYNCED;
        donationDao.insertOrUpdate(incoming);

        if (statusChanged
                && isOwnDonation(incoming.userId)
                && (DonationEntity.STATUS_COMPLETED.equals(incoming.status)
                || DonationEntity.STATUS_CANCELLED.equals(incoming.status)
                || DonationEntity.STATUS_EXPIRED.equals(incoming.status))) {
            NotificationHelper.notifyStatusUpdate(appContext, incoming.clientId, incoming.status);
        }
    }

    private DonationEntity fromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        DonationEntity entity = new DonationEntity();
        entity.clientId = doc.getId();
        entity.userId = stringOrEmpty(doc.getString("userId"));
        entity.type = normalizeType(doc.getString("type"));
        entity.action = normalizeAction(doc.getString("action"));
        entity.status = normalizeStatus(doc.getString("status"));
        entity.urgency = normalizeUrgency(doc.getString("urgency"));
        entity.isPublic = Boolean.TRUE.equals(doc.getBoolean("isPublic"));
        entity.deleted = Boolean.TRUE.equals(doc.getBoolean("deleted"));
        entity.createdAt = longOrNow(doc, "createdAt");
        entity.updatedAt = longOrNow(doc, "updatedAt");
        Long hc = doc.getLong("helpCount");
        entity.helpCount = hc != null ? hc.intValue() : 0;
        entity.syncStatus = DonationEntity.SYNC_SYNCED;
        entity.title = stringOrEmpty(doc.getString("title"));
        entity.name = stringOrEmpty(doc.getString("name"));
        entity.description = stringOrEmpty(doc.getString("description"));
        entity.city = stringOrEmpty(doc.getString("city"));
        entity.contact = stringOrEmpty(doc.getString("contact"));
        entity.bloodGroup = stringOrEmpty(doc.getString("bloodGroup"));
        entity.medicineName = stringOrEmpty(doc.getString("medicineName"));
        entity.medicineExpiry = stringOrEmpty(doc.getString("medicineExpiry"));
        return entity;
    }

    private Map<String, Object> toFirestoreMap(DonationEntity donation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", donation.clientId);
        payload.put("userId", donation.userId);
        payload.put("type", donation.type);
        payload.put("action", donation.action);
        payload.put("status", donation.status);
        payload.put("urgency", donation.urgency);
        payload.put("isPublic", donation.isPublic);
        payload.put("deleted", donation.deleted);
        payload.put("createdAt", donation.createdAt);
        payload.put("updatedAt", donation.updatedAt);
        payload.put("helpCount", donation.helpCount);
        payload.put("title", donation.title);
        payload.put("name", donation.name);
        payload.put("description", donation.description);
        payload.put("city", donation.city);
        payload.put("contact", donation.contact);
        payload.put("bloodGroup", donation.bloodGroup);
        payload.put("medicineName", donation.medicineName);
        payload.put("medicineExpiry", donation.medicineExpiry);
        return payload;
    }

    private void notifyNewRequestIfRelevant(DonationEntity donation) {
        if (donation == null) {
            return;
        }
        if (!DonationEntity.ACTION_REQUEST.equals(donation.action) && !DonationEntity.ACTION_DONATE.equals(donation.action)) {
            return;
        }
        if (!DonationEntity.STATUS_ACTIVE.equals(donation.status) || !donation.isPublic) {
            return;
        }
        if (isOwnDonation(donation.userId)) {
            return;
        }
        if (!LocationHelper.isNearbyCity(appContext, donation.city)) {
            return;
        }
        NotificationHelper.notifyNewRequest(appContext, donation);
    }

    private void markSyncedIfCurrent(String clientId, long updatedAt) {
        DonationEntity current = donationDao.getByClientIdSync(clientId);
        if (current != null
                && current.updatedAt == updatedAt
                && current.syncStatus == DonationEntity.SYNC_PENDING) {
            current.syncStatus = DonationEntity.SYNC_SYNCED;
            donationDao.insertOrUpdate(current);
            notifyDonationsChanged();
        }
    }

    private void markSyncErrorIfCurrent(String clientId, long updatedAt) {
        DonationEntity current = donationDao.getByClientIdSync(clientId);
        if (current != null
                && current.updatedAt == updatedAt
                && current.syncStatus == DonationEntity.SYNC_PENDING) {
            current.syncStatus = DonationEntity.SYNC_ERROR;
            donationDao.insertOrUpdate(current);
            notifyDonationsChanged();
        }
    }

    private void stopUserListener() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    private void stopPublicFeedListener() {
        if (publicFeedListener != null) {
            publicFeedListener.remove();
            publicFeedListener = null;
        }
    }

    private void stopHelpResponseListener() {
        if (helpResponseListener != null) {
            helpResponseListener.remove();
            helpResponseListener = null;
        }
    }

    private void notifyDonationsChanged() {
        appContext.sendBroadcast(new Intent(ACTION_DONATIONS_CHANGED));
    }

    private void postSave(SaveCallback callback, boolean success, Exception error) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onComplete(success, error));
    }

    private void postHelpSummary(HelpSummaryCallback callback, HelpSummary summary, Exception error) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onComplete(error == null, summary, error));
    }

    private String normalizeType(String type) {
        if (DonationEntity.TYPE_MEDICINE.equalsIgnoreCase(type)) {
            return DonationEntity.TYPE_MEDICINE;
        }
        return DonationEntity.TYPE_BLOOD;
    }

    private String normalizeAction(String action) {
        if (DonationEntity.ACTION_DONATE.equalsIgnoreCase(action)) {
            return DonationEntity.ACTION_DONATE;
        }
        return DonationEntity.ACTION_REQUEST;
    }

    private String normalizeStatus(String status) {
        if (DonationEntity.STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            return DonationEntity.STATUS_IN_PROGRESS;
        }
        if (DonationEntity.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            return DonationEntity.STATUS_COMPLETED;
        }
        if (DonationEntity.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return DonationEntity.STATUS_CANCELLED;
        }
        if (DonationEntity.STATUS_EXPIRED.equalsIgnoreCase(status)) {
            return DonationEntity.STATUS_EXPIRED;
        }
        return DonationEntity.STATUS_ACTIVE;
    }

    private String normalizeUrgency(String urgency) {
        if (DonationEntity.URGENCY_CRITICAL.equalsIgnoreCase(urgency)) {
            return DonationEntity.URGENCY_CRITICAL;
        }
        if (DonationEntity.URGENCY_HIGH.equalsIgnoreCase(urgency)
                || "urgent".equalsIgnoreCase(urgency)) {
            return DonationEntity.URGENCY_HIGH;
        }
        return DonationEntity.URGENCY_NORMAL;
    }

    private String stringOrEmpty(String value) {
        return value != null ? value : "";
    }

    private long longOrNow(DocumentSnapshot doc, String key) {
        Long value = doc.getLong(key);
        return value != null ? value : System.currentTimeMillis();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isOwnDonation(String userId) {
        String uid = auth.getUid();
        return uid != null && uid.equals(userId);
    }

    private long dayStart(long ts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private HelpSummary parseHelpSummary(QuerySnapshot snapshots) {
        String currentUserId = auth.getUid();
        int readyCount = 0;
        boolean currentUserRequested = false;

        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            String status = stringOrEmpty(doc.getString("status"));
            if (!isReadyHelpStatus(status)) {
                continue;
            }
            readyCount++;
            if (currentUserId != null && currentUserId.equals(doc.getString("helperId"))) {
                currentUserRequested = true;
            }
        }
        return new HelpSummary(readyCount, currentUserRequested);
    }

    private boolean isReadyHelpStatus(String status) {
        return HelpInterest.STATUS_PENDING.equalsIgnoreCase(status)
                || HelpInterest.STATUS_ACCEPTED.equalsIgnoreCase(status);
    }

    private String buildFallbackTitle(DonationEntity donation) {
        if (DonationEntity.TYPE_BLOOD.equals(donation.type)) {
            if (hasText(donation.bloodGroup)) {
                return "Blood " + donation.bloodGroup.trim();
            }
            return "Blood Support";
        }
        if (hasText(donation.medicineName)) {
            return donation.medicineName.trim();
        }
        return "Medicine Support";
    }

    private String buildHelpDocId(String requestId, String helperId) {
        return requestId + "_" + helperId;
    }

    public interface SaveCallback {
        void onComplete(boolean success, Exception error);
    }

    public interface HelpSummaryCallback {
        void onComplete(boolean success, HelpSummary summary, Exception error);
    }

    public interface HelpSummaryListener {
        void onUpdate(HelpSummary summary);
    }

    public interface HelpSubscription {
        void cancel();
    }

    public static final class HelpSummary {
        private final int readyHelpers;
        private final boolean currentUserRequested;

        public HelpSummary(int readyHelpers, boolean currentUserRequested) {
            this.readyHelpers = readyHelpers;
            this.currentUserRequested = currentUserRequested;
        }

        public int getReadyHelpers() {
            return readyHelpers;
        }

        public boolean isCurrentUserRequested() {
            return currentUserRequested;
        }
    }
}
