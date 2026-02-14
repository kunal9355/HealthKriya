package com.kunal.healthkriya.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.repository.AppRepository;

public class ProfileViewModel extends ViewModel {

    private final AppRepository repository = AppRepository.getInstance();

    public LiveData<UserModel> getUser() {
        return repository.getCurrentUserLive();
    }

    public void logout() {
        repository.logout();
    }
}
