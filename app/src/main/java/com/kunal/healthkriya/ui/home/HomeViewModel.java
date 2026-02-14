package com.kunal.healthkriya.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.model.home.HomeDataModel;
import com.kunal.healthkriya.data.repository.HomeRepository;

public class HomeViewModel extends ViewModel {

    private final HomeRepository repository = HomeRepository.getInstance();

    public LiveData<HomeDataModel> getHomeData() {
        return repository.getHomeData();
    }
}
