package com.kunal.healthkriya.data.source;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kunal.healthkriya.data.model.UserModel;

public class FirebaseSource {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ---------------- CORE AUTH ----------------

    // LOGIN
    public LiveData<UserModel> login(String email, String password) {
        MutableLiveData<UserModel> liveData = new MutableLiveData<>();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        fetchUser(user.getUid(), liveData);
                    }
                })
                .addOnFailureListener(e -> liveData.postValue(null));

        return liveData;
    }

    // REGISTER
    public LiveData<UserModel> register(String email, String password) {
        MutableLiveData<UserModel> liveData = new MutableLiveData<>();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        UserModel model = new UserModel(user.getUid(), user.getEmail());
                        db.collection("users")
                                .document(user.getUid())
                                .set(model)
                                .addOnSuccessListener(v -> liveData.postValue(model))
                                .addOnFailureListener(e -> liveData.postValue(null));
                    }
                })
                .addOnFailureListener(e -> liveData.postValue(null));

        return liveData;
    }

    // FETCH USER FROM FIRESTORE
    private void fetchUser(String uid, MutableLiveData<UserModel> liveData) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        UserModel user = snapshot.toObject(UserModel.class);
                        liveData.postValue(user);
                    } else {
                        liveData.postValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.postValue(null));
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void logout() {
        auth.signOut();
    }


    // 🔥 UPDATE USER PROFILE IN FIRESTORE
    public MutableLiveData<Boolean> updateUserProfile(UserModel user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection("users")
                .document(user.getUid())
                .set(user)   // overwrite with latest data
                .addOnSuccessListener(v -> result.postValue(true))
                .addOnFailureListener(e -> result.postValue(false));

        return result;
    }


    // ---------------- ADVANCED AUTH ----------------

    // Verify email + phone (forgot password)
    public MutableLiveData<Boolean> verifyEmailPhone(String email, String phone) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(snapshot ->
                        result.postValue(!snapshot.isEmpty()))
                .addOnFailureListener(e ->
                        result.postValue(false));

        return result;
    }

    // Send password reset email
    public void sendPasswordResetEmail(
            String email,
            Runnable onSuccess,
            Runnable onFailure
    ) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    // Logged-in password change
    public MutableLiveData<Boolean> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null || TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            result.postValue(false);
            return result;
        }

        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            result.postValue(false);
            return result;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(v -> user.updatePassword(newPassword)
                        .addOnSuccessListener(done -> result.postValue(true))
                        .addOnFailureListener(err -> result.postValue(false)))
                .addOnFailureListener(e -> result.postValue(false));

        return result;
    }
}
