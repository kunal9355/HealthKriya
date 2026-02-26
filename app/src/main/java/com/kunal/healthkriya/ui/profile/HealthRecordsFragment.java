package com.kunal.healthkriya.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.reminder.ReminderEntity;
import com.kunal.healthkriya.data.model.UserModel;
import com.kunal.healthkriya.data.repository.ReminderRepository;

import java.util.ArrayList;
import java.util.List;

public class HealthRecordsFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ReminderRepository reminderRepository;

    private UserModel currentUser;
    private List<ReminderEntity> reminders = new ArrayList<>();

    private TextView txtOngoingMedicines;
    private TextView txtConditions;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_health_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        reminderRepository = new ReminderRepository(requireContext());

        txtOngoingMedicines = view.findViewById(R.id.txtOngoingMedicines);
        txtConditions = view.findViewById(R.id.txtConditions);

        observeUser();
        loadReminders();
    }

    private void observeUser() {
        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            currentUser = user;
            renderData();
        });
    }

    private void loadReminders() {
        reminderRepository.getReminders(items -> {
            if (!isAdded()) {
                return;
            }
            reminders = items != null ? items : new ArrayList<>();
            renderData();
        });
    }

    private void renderData() {
        if (!isAdded()) {
            return;
        }
        txtOngoingMedicines.setText(buildMedicinesText());
        txtConditions.setText(buildConditionsText());
    }

    private String buildMedicinesText() {
        StringBuilder builder = new StringBuilder();
        int activeCount = 0;
        for (ReminderEntity reminder : reminders) {
            if (reminder.deleted || !reminder.active) {
                continue;
            }
            activeCount++;
            String name = safeText(reminder.name, "Medicine");
            String dosage = safeText(reminder.dosage, "");
            builder.append("- ").append(name);
            if (!dosage.isEmpty()) {
                builder.append(" • ").append(dosage);
            }
            builder.append("\n");
        }
        if (activeCount == 0) {
            return "- No ongoing medicines";
        }
        return builder.toString().trim();
    }

    private String buildConditionsText() {
        String conditions = currentUser != null ? safeText(currentUser.getMedicalConditions(), "") : "";
        if (conditions.isEmpty()) {
            return "- Not added";
        }

        String[] parts = conditions.split("[,\\n]");
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String part : parts) {
            String clean = part == null ? "" : part.trim();
            if (!clean.isEmpty()) {
                count++;
                builder.append("- ").append(clean).append("\n");
            }
        }
        if (count == 0) {
            return "- " + conditions.trim();
        }
        return builder.toString().trim();
    }

    private String safeText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String clean = value.trim();
        return clean.isEmpty() ? fallback : clean;
    }
}
