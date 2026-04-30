package com.kunal.healthkriya.ui.emergency;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kunal.healthkriya.data.model.EmergencyCardModel;
import com.kunal.healthkriya.data.repository.AppRepository;

public class EmergencyViewModel extends ViewModel {

    private final AppRepository repository = AppRepository.getInstance();

    public LiveData<EmergencyCardModel> getEmergencyCard() {
        return repository.getEmergencyCardLive();
    }

    public EmergencyCardModel getCurrentCard() {
        EmergencyCardModel card = repository.getEmergencyCard();
        return card != null ? card : new EmergencyCardModel();
    }

    public void saveEmergencyCard(EmergencyCardModel card) {
        repository.saveEmergencyCard(card);
    }
}
