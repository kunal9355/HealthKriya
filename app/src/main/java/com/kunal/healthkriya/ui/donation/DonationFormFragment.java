package com.kunal.healthkriya.ui.donation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

public class DonationFormFragment extends Fragment {
    public static final String ARG_TYPE = "arg_type";
    public static final String ARG_ACTION = "arg_action";
    public static final String ARG_CLIENT_ID = "arg_client_id";

    private DonationRepository repository;
    private String donationType = DonationEntity.TYPE_BLOOD;
    private String donationAction = DonationEntity.ACTION_REQUEST;
    private String editClientId;
    private boolean isEditMode;
    private DonationEntity editingDonation;

    private TextView txtHeaderTitle;
    private TextView txtHeaderSubtitle;
    private EditText etName;
    private EditText etBloodGroup;
    private View bloodGroupLayout;
    private EditText etMedicineName;
    private View medicineNameLayout;
    private EditText etMedicineExpiry;
    private View medicineExpiryLayout;
    private EditText etLocation;
    private EditText etContact;
    private EditText etDescription;
    private AutoCompleteTextView actUrgency;
    private SwitchCompat switchPublic;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new DonationRepository(requireContext());
        readArgs();

        txtHeaderTitle = view.findViewById(R.id.txtFormTitle);
        txtHeaderSubtitle = view.findViewById(R.id.txtFormSubtitle);
        etName = view.findViewById(R.id.etDonationName);
        bloodGroupLayout = view.findViewById(R.id.layoutBloodGroup);
        etBloodGroup = view.findViewById(R.id.etBloodGroup);
        medicineNameLayout = view.findViewById(R.id.layoutMedicineName);
        etMedicineName = view.findViewById(R.id.etMedicineName);
        medicineExpiryLayout = view.findViewById(R.id.layoutMedicineExpiry);
        etMedicineExpiry = view.findViewById(R.id.etMedicineExpiry);
        etLocation = view.findViewById(R.id.etDonationCity);
        etContact = view.findViewById(R.id.etDonationContact);
        etDescription = view.findViewById(R.id.etDonationDescription);
        actUrgency = view.findViewById(R.id.actDonationUrgency);
        switchPublic = view.findViewById(R.id.switchPublicDonation);
        btnSave = view.findViewById(R.id.btnSaveDonation);

        setupUrgencyOptions();
        setupSpecificFields();
        setupHeader();
        btnSave.setOnClickListener(v -> submitDonation());

        if (isEditMode) {
            loadForEdit();
        }
    }

    private void readArgs() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        editClientId = args.getString(ARG_CLIENT_ID);
        isEditMode = !TextUtils.isEmpty(editClientId);
        if (isEditMode) {
            return;
        }

        String type = args.getString(ARG_TYPE);
        String action = args.getString(ARG_ACTION);
        if (DonationEntity.TYPE_MEDICINE.equals(type)) {
            donationType = DonationEntity.TYPE_MEDICINE;
        }
        if (DonationEntity.ACTION_DONATE.equals(action)) {
            donationAction = DonationEntity.ACTION_DONATE;
        }
    }

    private void loadForEdit() {
        repository.getDonation(editClientId).observe(getViewLifecycleOwner(), donation -> {
            if (donation == null) {
                return;
            }

            editingDonation = donation;
            donationType = donation.type;
            donationAction = donation.action;

            etName.setText(donation.name);
            etLocation.setText(donation.city);
            etContact.setText(donation.contact);
            etDescription.setText(donation.description);
            actUrgency.setText(urgencyToInputLabel(donation.urgency), false);
            switchPublic.setChecked(donation.isPublic);
            etBloodGroup.setText(donation.bloodGroup);
            etMedicineName.setText(donation.medicineName);
            etMedicineExpiry.setText(donation.medicineExpiry);

            setupSpecificFields();
            setupHeader();
            applyEditStatusState(donation);
        });
    }

    private void applyEditStatusState(DonationEntity donation) {
        boolean editable = DonationEntity.STATUS_ACTIVE.equals(donation.status)
                || DonationEntity.STATUS_IN_PROGRESS.equals(donation.status);
        btnSave.setEnabled(editable);
        if (!editable) {
            txtHeaderSubtitle.setText("Completed or cancelled entries cannot be edited.");
        }
    }

    private void setupHeader() {
        String item = DonationEntity.TYPE_BLOOD.equals(donationType) ? "Blood" : "Medicine";
        String action = DonationUiUtils.actionLabel(donationAction);

        if (isEditMode) {
            txtHeaderTitle.setText("Edit " + action + " " + item);
            btnSave.setText("Update Donation");
            txtHeaderSubtitle.setText("Update details and keep this card synced across dashboard and feed.");
            return;
        }

        txtHeaderTitle.setText(action + " " + item);
        btnSave.setText("Submit Donation");
        txtHeaderSubtitle.setText("Create one clean card that works in both dashboard and public feed.");
    }

    private void setupSpecificFields() {
        boolean isBlood = DonationEntity.TYPE_BLOOD.equals(donationType);
        bloodGroupLayout.setVisibility(isBlood ? View.VISIBLE : View.GONE);
        medicineNameLayout.setVisibility(isBlood ? View.GONE : View.VISIBLE);
        medicineExpiryLayout.setVisibility(isBlood ? View.GONE : View.VISIBLE);
    }

    private void setupUrgencyOptions() {
        String[] urgencyOptions = {"Normal", "Urgent", "Critical"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                urgencyOptions
        );
        actUrgency.setAdapter(adapter);
        actUrgency.setText("Normal", false);
    }

    private void submitDonation() {
        String name = textOf(etName);
        String location = textOf(etLocation);
        String contact = textOf(etContact);
        String description = textOf(etDescription);
        String urgency = inputLabelToUrgency(textOf(actUrgency));

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location required");
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Contact required");
            return;
        }

        DonationEntity entity = isEditMode && editingDonation != null
                ? editingDonation
                : new DonationEntity();

        entity.type = donationType;
        entity.action = donationAction;
        entity.name = name;
        entity.city = location;
        entity.contact = contact;
        entity.description = description;
        entity.urgency = urgency;
        entity.isPublic = switchPublic.isChecked();
        if (!isEditMode) {
            entity.status = DonationEntity.STATUS_ACTIVE;
        }

        if (DonationEntity.TYPE_BLOOD.equals(donationType)) {
            String bloodGroup = textOf(etBloodGroup);
            if (TextUtils.isEmpty(bloodGroup)) {
                etBloodGroup.setError("Blood group required");
                return;
            }
            entity.bloodGroup = bloodGroup;
            entity.medicineName = "";
            entity.medicineExpiry = "";
            entity.title = "Blood " + bloodGroup;
        } else {
            String medicineName = textOf(etMedicineName);
            if (TextUtils.isEmpty(medicineName)) {
                etMedicineName.setError("Medicine name required");
                return;
            }
            entity.medicineName = medicineName;
            entity.medicineExpiry = textOf(etMedicineExpiry);
            entity.bloodGroup = "";
            entity.title = medicineName;
        }

        DonationRepository.SaveCallback callback = (success, error) -> {
            if (!isAdded()) {
                return;
            }
            if (success) {
                Toast.makeText(
                        requireContext(),
                        isEditMode ? "Donation updated" : "Donation saved",
                        Toast.LENGTH_SHORT
                ).show();
                NavHostFragment.findNavController(this).popBackStack();
            } else {
                String errorMessage = error != null && error.getMessage() != null
                        ? error.getMessage()
                        : "Unable to save right now";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        if (isEditMode) {
            repository.updateDonation(entity, callback);
        } else {
            repository.saveDonation(entity, callback);
        }
    }

    private String urgencyToInputLabel(String urgency) {
        if (DonationEntity.URGENCY_CRITICAL.equals(urgency)) {
            return "Critical";
        }
        if (DonationEntity.URGENCY_HIGH.equals(urgency)) {
            return "Urgent";
        }
        return "Normal";
    }

    private String inputLabelToUrgency(String inputLabel) {
        if ("critical".equalsIgnoreCase(inputLabel)) {
            return DonationEntity.URGENCY_CRITICAL;
        }
        if ("urgent".equalsIgnoreCase(inputLabel) || "high".equalsIgnoreCase(inputLabel)) {
            return DonationEntity.URGENCY_HIGH;
        }
        return DonationEntity.URGENCY_NORMAL;
    }

    private String textOf(TextView view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }
}
