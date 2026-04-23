package com.kunal.healthkriya.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.model.auth.AuthResult;
import com.kunal.healthkriya.data.repository.AppRepository;

public class AuthViewModel extends ViewModel {

    public enum AuthState {
        LOGIN_SUCCESS,
        REGISTER_SUCCESS,
        USER_EXISTS,
        ERROR
    }

    private final AppRepository repository = AppRepository.getInstance();
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private final MutableLiveData<String> authMessage = new MutableLiveData<>();

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<String> getAuthMessage() {
        return authMessage;
    }

    public void login(String email, String password) {
        LiveData<AuthResult> source = repository.login(email, password);
        Observer<AuthResult> observer = new Observer<AuthResult>() {
            @Override
            public void onChanged(AuthResult result) {
                source.removeObserver(this);
                if (result != null && result.isSuccess()) {
                    UserModel user = result.getUser();
                    repository.saveUser(user);
                    authState.postValue(AuthState.LOGIN_SUCCESS);
                    authMessage.postValue(null);
                    return;
                }

                authMessage.postValue(result != null ? result.getMessage() : "Authentication failed");
                authState.postValue(AuthState.ERROR);
            }
        };
        source.observeForever(observer);
    }

    public void register(String email, String password, String phone) {
        LiveData<AuthResult> source = repository.register(email, password);
        Observer<AuthResult> observer = new Observer<AuthResult>() {
            @Override
            public void onChanged(AuthResult result) {
                source.removeObserver(this);
                if (result != null && result.isSuccess()) {
                    UserModel user = result.getUser();
                    user.setPhone(phone);
                    repository.saveUser(user);
                    authState.postValue(AuthState.REGISTER_SUCCESS);
                    authMessage.postValue(null);
                    return;
                }

                String code = result != null ? result.getErrorCode() : "";
                if ("user_exists".equals(code)) {
                    authState.postValue(AuthState.USER_EXISTS);
                } else {
                    authState.postValue(AuthState.ERROR);
                }
                authMessage.postValue(result != null ? result.getMessage() : "Unable to create account");
            }
        };
        source.observeForever(observer);
    }

    public void clearAuthState() {
        authState.setValue(null);
        authMessage.setValue(null);
    }
}
