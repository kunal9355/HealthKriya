package com.kunal.healthkriya.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.model.UserModel;
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

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public void login(String email, String password) {
        LiveData<UserModel> source = repository.login(email, password);
        Observer<UserModel> observer = new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel user) {
                source.removeObserver(this);
                if (user != null) {
                    repository.saveUser(user);
                    authState.postValue(AuthState.LOGIN_SUCCESS);
                } else {
                    authState.postValue(AuthState.ERROR);
                }
            }
        };
        source.observeForever(observer);
    }

    public void register(String email, String password, String phone) {
        LiveData<UserModel> source = repository.register(email, password);
        Observer<UserModel> observer = new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel user) {
                source.removeObserver(this);
                if (user != null) {
                    user.setPhone(phone);
                    repository.saveUser(user);
                    authState.postValue(AuthState.REGISTER_SUCCESS);
                } else {
                    authState.postValue(AuthState.USER_EXISTS);
                }
            }
        };
        source.observeForever(observer);
    }

    public void clearAuthState() {
        authState.setValue(null);
    }
}
