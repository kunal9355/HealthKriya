package com.kunal.healthkriya.ui.emergency;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.model.EmergencyCardModel;

public class EmergencyFragment extends Fragment {

    private EmergencyViewModel viewModel;

    private View emptyState;
    private View cardContent;
    private TextView txtName;
    private TextView txtAge;
    private TextView txtBloodGroup;
    private TextView txtEmergencyContact;
    private TextView txtMedicalConditions;
    private TextView txtAllergies;
    private TextView txtMedicines;
    private TextView txtAddress;
    private MaterialButton btnCreateCard;
    private MaterialButton btnEditCard;
    private MaterialButton btnCallContact;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);

        emptyState = view.findViewById(R.id.layoutEmergencyEmptyState);
        cardContent = view.findViewById(R.id.layoutEmergencyCardContent);
        txtName = view.findViewById(R.id.cardName);
        txtAge = view.findViewById(R.id.cardAge);
        txtBloodGroup = view.findViewById(R.id.cardBloodGroup);
        txtEmergencyContact = view.findViewById(R.id.cardEmergencyContact);
        txtMedicalConditions = view.findViewById(R.id.cardMedicalConditions);
        txtAllergies = view.findViewById(R.id.cardAllergies);
        txtMedicines = view.findViewById(R.id.cardMedicines);
        txtAddress = view.findViewById(R.id.cardAddress);
        btnCreateCard = view.findViewById(R.id.btnCreateEmergencyCard);
        btnEditCard = view.findViewById(R.id.btnEditCard);
        btnCallContact = view.findViewById(R.id.btnCallEmergencyContact);

        btnCreateCard.setOnClickListener(v -> openForm());
        btnEditCard.setOnClickListener(v -> openForm());
        btnCallContact.setOnClickListener(v ->
                dialNumber(txtEmergencyContact.getText() != null
                        ? txtEmergencyContact.getText().toString()
                        : ""));
        txtEmergencyContact.setOnClickListener(v ->
                dialNumber(txtEmergencyContact.getText() != null
                        ? txtEmergencyContact.getText().toString()
                        : ""));

        viewModel.getEmergencyCard().observe(getViewLifecycleOwner(), this::renderCard);
    }

    private void renderCard(EmergencyCardModel card) {
        EmergencyCardModel emergencyCard = card != null ? card : new EmergencyCardModel();
        boolean hasData = !emergencyCard.isEmpty();

        emptyState.setVisibility(hasData ? View.GONE : View.VISIBLE);
        cardContent.setVisibility(hasData ? View.VISIBLE : View.GONE);
        btnEditCard.setVisibility(hasData ? View.VISIBLE : View.GONE);

        if (!hasData) {
            return;
        }

        txtName.setText(valueOrFallback(emergencyCard.getName(), "Not added"));
        txtAge.setText(buildAgeText(emergencyCard.getAge()));
        txtBloodGroup.setText(valueOrFallback(emergencyCard.getBloodGroup(), "--"));
        txtEmergencyContact.setText(valueOrFallback(emergencyCard.getEmergencyContact(), "Not added"));
        txtMedicalConditions.setText(valueOrFallback(
                emergencyCard.getMedicalConditions(),
                "No medical conditions added"
        ));
        txtAllergies.setText(valueOrFallback(
                emergencyCard.getAllergies(),
                "No allergies added"
        ));
        txtMedicines.setText(valueOrFallback(
                emergencyCard.getMedicines(),
                "No medicines added"
        ));
        txtAddress.setText(valueOrFallback(emergencyCard.getAddress(), "Address not added"));
        btnCallContact.setEnabled(!TextUtils.isEmpty(emergencyCard.getEmergencyContact()));
    }

    private void openForm() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_emergencyFragment_to_emergencyFormFragment);
    }

    private void dialNumber(String rawNumber) {
        if (!isAdded()) {
            return;
        }

        String safeNumber = rawNumber == null ? "" : rawNumber.replaceAll("[^0-9+]", "");
        if (safeNumber.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + safeNumber));
        startActivity(intent);
    }

    private String buildAgeText(String age) {
        if (TextUtils.isEmpty(age)) {
            return "Age not added";
        }
        return age + " years";
    }

    private String valueOrFallback(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }
}
