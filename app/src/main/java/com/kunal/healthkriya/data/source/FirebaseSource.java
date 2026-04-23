package com.kunal.healthkriya.data.source;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kunal.healthkriya.data.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class FirebaseSource {

    private static final String TAG = "FirebaseSource";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // LOGIN
    public LiveData<UserModel> login(String email, String password) {
        MutableLiveData<UserModel> liveData = new MutableLiveData<>();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        fetchOrCreateUser(user, liveData);
                    } else {
                        liveData.postValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed: " + e.getMessage());
                    liveData.postValue(null);
                });

        return liveData;
    }

    // REGISTER - Updated to include phone
    public LiveData<UserModel> register(String email, String password, String phone) {
        MutableLiveData<UserModel> liveData = new MutableLiveData<>();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        UserModel model = new UserModel(user.getUid(), user.getEmail());
                        model.setPhone(phone); // Save phone number
                        
                        db.collection("users")
                                .document(user.getUid())
                                .set(model)
                                .addOnSuccessListener(v -> liveData.postValue(model))
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Firestore user creation failed: " + e.getMessage());
                                    liveData.postValue(null);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Auth registration failed: " + e.getMessage());
                    liveData.postValue(null);
                });

        return liveData;
    }

    public void validateCurrentSession(SessionValidationCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            postSessionValidation(callback, false);
            return;
        }

        user.reload()
                .addOnSuccessListener(unused -> postSessionValidation(callback, auth.getCurrentUser() != null))
                .addOnFailureListener(error -> {
                    if (error instanceof FirebaseAuthInvalidUserException) {
                        auth.signOut();
                        postSessionValidation(callback, false);
                        return;
                    }
                    postSessionValidation(callback, true);
                });
    }

    public void deleteCurrentUser(String currentPassword, ActionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            postAction(callback, false, "No user logged in");
            return;
        }

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            postAction(callback, false, "Account email missing");
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            postAction(callback, false, "Current password is required");
            return;
        }

        String uid = user.getUid();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> deleteFirestoreAccountData(uid)
                        .addOnSuccessListener(done -> user.delete()
                                .addOnSuccessListener(v -> {
                                    auth.signOut();
                                    postAction(callback, true, "Account deleted");
                                })
                                .addOnFailureListener(e ->
                                        postAction(callback, false, parseDeleteError(e))))
                        .addOnFailureListener(e ->
                                postAction(callback, false, "Unable to delete Firestore data")))
                .addOnFailureListener(error -> {
                    if (error instanceof FirebaseAuthInvalidCredentialsException) {
                        postAction(callback, false, "Wrong password. Please try again.");
                        return;
                    }
                    postAction(callback, false, "Re-authentication failed");
                });
    }

    private Task<Void> deleteFirestoreAccountData(String uid) {
        List<Task<Void>> tasks = new ArrayList<>();
        tasks.add(deleteSubCollection(uid, "moods"));
        tasks.add(deleteSubCollection(uid, "reminders"));
        tasks.add(deleteCollectionByField("donations", "userId", uid));
        tasks.add(deleteCollectionByField("donation_help_interests", "ownerId", uid));
        tasks.add(deleteCollectionByField("donation_help_interests", "helperId", uid));
        tasks.add(db.collection("users").document(uid).delete());
        return Tasks.whenAll(tasks);
    }

    private Task<Void> deleteSubCollection(String uid, String subCollection) {
        return db.collection("users")
                .document(uid)
                .collection(subCollection)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException());
                    }
                    QuerySnapshot snapshots = task.getResult();
                    List<Task<Void>> deletes = new ArrayList<>();
                    if (snapshots != null) {
                        snapshots.getDocuments().forEach(document -> deletes.add(document.getReference().delete()));
                    }
                    return deletes.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(deletes);
                });
    }

    private Task<Void> deleteCollectionByField(String collection, String field, String value) {
        return db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException());
                    }
                    QuerySnapshot snapshots = task.getResult();
                    List<Task<Void>> deletes = new ArrayList<>();
                    if (snapshots != null) {
                        snapshots.getDocuments().forEach(document -> deletes.add(document.getReference().delete()));
                    }
                    return deletes.isEmpty() ? Tasks.forResult(null) : Tasks.whenAll(deletes);
                });
    }

    private void fetchOrCreateUser(FirebaseUser firebaseUser, MutableLiveData<UserModel> liveData) {
        String uid = firebaseUser.getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        UserModel user = snapshot.toObject(UserModel.class);
                        if (user != null) {
                            if (TextUtils.isEmpty(user.getUid())) user.setUid(uid);
                            if (TextUtils.isEmpty(user.getEmail())) user.setEmail(firebaseUser.getEmail());
                            liveData.postValue(user);
                        } else {
                            createMinimalUser(firebaseUser, liveData);
                        }
                    } else {
                        createMinimalUser(firebaseUser, liveData);
                    }
                })
                .addOnFailureListener(e -> createMinimalUser(firebaseUser, liveData));
    }

    private void createMinimalUser(FirebaseUser firebaseUser, MutableLiveData<UserModel> liveData) {
        UserModel user = new UserModel(firebaseUser.getUid(), firebaseUser.getEmail());
        db.collection("users")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(unused -> liveData.postValue(user))
                .addOnFailureListener(e -> liveData.postValue(user));
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }

    public MutableLiveData<Boolean> updateUserProfile(UserModel user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(v -> result.postValue(true))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }

    public MutableLiveData<Boolean> verifyEmailPhone(String email, String phone) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(snapshot -> result.postValue(!snapshot.isEmpty()))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }

    public void sendPasswordResetEmail(String email, Runnable onSuccess, Runnable onFailure) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    public MutableLiveData<Boolean> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            result.postValue(false);
            return result;
        }
        String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(v -> user.updatePassword(newPassword)
                        .addOnSuccessListener(done -> result.postValue(true))
                        .addOnFailureListener(err -> result.postValue(false)))
                .addOnFailureListener(e -> result.postValue(false));
        return result;
    }

    private void postSessionValidation(SessionValidationCallback callback, boolean valid) {
        if (callback != null) mainHandler.post(() -> callback.onResult(valid));
    }

    private void postAction(ActionCallback callback, boolean success, String message) {
        if (callback != null) mainHandler.post(() -> callback.onComplete(success, message));
    }

    private String parseDeleteError(Exception error) {
        return (error instanceof FirebaseAuthInvalidUserException) ? "User no longer exists" : "Unable to delete account";
    }

    public interface SessionValidationCallback { void onResult(boolean valid); }
    public interface ActionCallback { void onComplete(boolean success, String message); }
}
