package com.kunal.healthkriya.ui.donation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

public class MedicineRequestFragment extends Fragment {

    private DonationRepository donationRepository;
    private LinearLayout layoutRecentEntries;
    private TextView tvRecentEmpty;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_medicine_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donationRepository = new DonationRepository(requireContext());

        TextInputEditText etMedicineName = view.findViewById(R.id.etMedicineNameRequest);
        TextInputEditText etLocation = view.findViewById(R.id.etMedicineLocationRequest);
        layoutRecentEntries = view.findViewById(R.id.layoutRecentEntries);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);
        MaterialButton btn = view.findViewById(R.id.btnSubmitMedicineRequest);

        loadRecentEntries();

        btn.setOnClickListener(v -> {
            String medicineName = readText(etMedicineName);
            String location = readText(etLocation);

            if (medicineName.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btn.setEnabled(false);

            DonationEntity donation = new DonationEntity(
                    DonationRepository.newClientId(),
                    DonationEntity.CATEGORY_MEDICINE,
                    DonationEntity.ACTION_REQUEST,
                    medicineName,
                    location
            );

            donationRepository.saveDonation(donation, (success, error) -> {
                if (!isAdded()) {
                    return;
                }

                btn.setEnabled(true);
                if (success) {
                    etMedicineName.setText(null);
                    etLocation.setText(null);
                    Toast.makeText(requireContext(), "Saved locally • Sync pending", Toast.LENGTH_SHORT).show();
                    loadRecentEntries();
                } else {
                    Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String readText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void loadRecentEntries() {
        donationRepository.getRecentByType(
                DonationEntity.CATEGORY_MEDICINE,
                DonationEntity.ACTION_REQUEST,
                3,
                entries -> {
                    if (!isAdded()) {
                        return;
                    }
                    DonationRecentRenderer.render(
                            requireContext(),
                            getLayoutInflater(),
                            entries,
                            layoutRecentEntries,
                            tvRecentEmpty
                    );
                }
        );
    }
}
