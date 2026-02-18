package com.kunal.healthkriya.data.source;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.kunal.healthkriya.data.local.mood.MoodEntity;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMoodSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface MoodSyncListener {
        void onMoodChanged(MoodEntity mood);
    }


    public void syncMood(MoodEntity mood) {
        String uid = getUid();
        if (uid == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("moodLevel", mood.moodLevel);
        map.put("note", mood.note);
        map.put("updatedAt", mood.updatedAt);

        db.collection("users")
                .document(uid)
                .collection("moods")
                .document(mood.date)
                .set(map);
    }

    public ListenerRegistration listenToMoodChanges(MoodSyncListener listener) {
        String uid = getUid();

        if (uid == null) return null;

        return db.collection("users")
                .document(uid)
                .collection("moods")
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {

                        DocumentSnapshot doc = dc.getDocument();

                        String date = doc.getId();
                        Long moodLevelValue = doc.getLong("moodLevel");
                        if (moodLevelValue == null) continue;
                        int moodLevel = moodLevelValue.intValue();
                        String note = doc.getString("note");
                        Long updatedAt = doc.getLong("updatedAt");

                        MoodEntity entity =
                                new MoodEntity(
                                        date,
                                        moodLevel,
                                        note,
                                        updatedAt != null ? updatedAt : System.currentTimeMillis()
                                );

                        listener.onMoodChanged(entity);
                    }
                });
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }

}
