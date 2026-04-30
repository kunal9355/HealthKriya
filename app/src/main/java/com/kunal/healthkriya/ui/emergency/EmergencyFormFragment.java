package com.kunal.healthkriya.ui.emergency;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.model.EmergencyCardModel;

import java.util.Locale;

public class EmergencyFormFragment extends Fragment {

    private EmergencyViewModel viewModel;

    private TextView txtTitle;
    private TextView txtSubtitle;
    private EditText edtName;
    private EditText edtAge;
    private EditText edtBloodGroup;
    private EditText edtEmergencyContact;
    private EditText edtConditions;
    private EditText edtAllergies;
    private EditText edtMedicines;
    private EditText edtAddress;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_emergency_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);

        txtTitle = view.findViewById(R.id.txtEmergencyFormTitle);
        txtSubtitle = view.findViewById(R.id.txtEmergencyFormSubtitle);
        edtName = view.findViewById(R.id.edtEmergencyName);
        edtAge = view.findViewById(R.id.edtEmergencyAge);
        edtBloodGroup = view.findViewById(R.id.edtEmergencyBloodGroup);
        edtEmergencyContact = view.findViewById(R.id.edtEmergencyContact);
        edtConditions = view.findViewById(R.id.edtEmergencyConditions);
        edtAllergies = view.findViewById(R.id.edtEmergencyAllergies);
        edtMedicines = view.findViewById(R.id.edtEmergencyMedicines);
        edtAddress = view.findViewById(R.id.edtEmergencyAddress);
        btnSave = view.findViewById(R.id.btnSaveEmergencyCard);

        EmergencyCardModel existing = viewModel.getCurrentCard();
        boolean hasExistingCard = existing != null && !existing.isEmpty();
        applyMode(hasExistingCard);
        prefill(existing);

        btnSave.setOnClickListener(v -> saveCard());
    }

    private void applyMode(boolean hasExistingCard) {
        txtTitle.setText(hasExistingCard ? "Edit Emergency Card" : "Create Emergency Card");
        txtSubtitle.setText(hasExistingCard
                ? "Update your life-saving details so the latest info is always ready."
                : "Add the most important details that someone can read quickly in an emergency.");
        btnSave.setText(hasExistingCard ? "Update Card" : "Save Card");
    }

    private void prefill(EmergencyCardModel card) {
        if (card == null) {
            return;
        }
        edtName.setText(card.getName());
        edtAge.setText(card.getAge());
        edtBloodGroup.setText(card.getBloodGroup());
        edtEmergencyContact.setText(card.getEmergencyContact());
        edtConditions.setText(card.getMedicalConditions());
        edtAllergies.setText(card.getAllergies());
        edtMedicines.setText(card.getMedicines());
        edtAddress.setText(card.getAddress());
    }

    private void saveCard() {
        String name = valueOf(edtName);
        String age = valueOf(edtAge);
        String bloodGroup = normalizeBloodGroup(valueOf(edtBloodGroup));
        String emergencyContact = sanitizePhoneNumber(valueOf(edtEmergencyContact));
        String conditions = valueOf(edtConditions);
        String allergies = valueOf(edtAllergies);
        String medicines = valueOf(edtMedicines);
        String address = valueOf(edtAddress);

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(age)) {
            edtAge.setError("Age is required");
            edtAge.requestFocus();
            return;
        }
        if (!isValidAge(age)) {
            edtAge.setError("Enter a valid age");
            edtAge.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(bloodGroup)) {
            edtBloodGroup.setError("Blood group is required");
            edtBloodGroup.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(emergencyContact)) {
            edtEmergencyContact.setError("Emergency contact is required");
            edtEmergencyContact.requestFocus();
            return;
        }
        if (!hasValidPhoneLength(emergencyContact)) {
            edtEmergencyContact.setError("Enter a valid contact number");
            edtEmergencyContact.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            edtAddress.setError("Address is required");
            edtAddress.requestFocus();
            return;
        }

        EmergencyCardModel card = new EmergencyCardModel();
        card.setName(name);
        card.setAge(age);
        card.setBloodGroup(bloodGroup);
        card.setEmergencyContact(emergencyContact);
        card.setMedicalConditions(conditions);
        card.setAllergies(allergies);
        card.setMedicines(medicines);
        card.setAddress(address);

        viewModel.saveEmergencyCard(card);
        Toast.makeText(requireContext(), "Emergency card saved", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(this).popBackStack();
    }

    private String valueOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private boolean isValidAge(String age) {
        try {
            int value = Integer.parseInt(age);
            return value > 0 && value <= 120;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean hasValidPhoneLength(String phone) {
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10;
    }

    private String sanitizePhoneNumber(String rawPhone) {
        return rawPhone.replaceAll("[^0-9+]", "");
    }

    private String normalizeBloodGroup(String rawBloodGroup) {
        return rawBloodGroup.toUpperCase(Locale.US);
    }
}
