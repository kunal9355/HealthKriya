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

public class BloodDonateFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_blood_donate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donationRepository = new DonationRepository(requireContext());

        TextInputEditText etBloodGroup = view.findViewById(R.id.etBloodGroupDonate);
        TextInputEditText etLocation = view.findViewById(R.id.etBloodLocationDonate);
        layoutRecentEntries = view.findViewById(R.id.layoutRecentEntries);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);
        MaterialButton btn = view.findViewById(R.id.btnSubmitBloodDonate);

        loadRecentEntries();

        btn.setOnClickListener(v -> {
            String bloodGroup = readText(etBloodGroup);
            String location = readText(etLocation);

            if (bloodGroup.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btn.setEnabled(false);

            DonationEntity donation = new DonationEntity(
                    DonationRepository.newClientId(),
                    DonationEntity.CATEGORY_BLOOD,
                    DonationEntity.ACTION_DONATE,
                    bloodGroup,
                    location
            );

            donationRepository.saveDonation(donation, (success, error) -> {
                if (!isAdded()) {
                    return;
                }

                btn.setEnabled(true);
                if (success) {
                    etBloodGroup.setText(null);
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
                DonationEntity.CATEGORY_BLOOD,
                DonationEntity.ACTION_DONATE,
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
