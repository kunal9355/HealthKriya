package com.kunal.healthkriya.ui.donation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.kunal.healthkriya.R;

public class DonationOptionFragment extends Fragment {

    private String mode = DonationFragment.MODE_BLOOD;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_donation_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String incomingMode = args.getString(DonationFragment.ARG_MODE, DonationFragment.MODE_BLOOD);
            if (DonationFragment.MODE_MEDICINE.equals(incomingMode)) {
                mode = DonationFragment.MODE_MEDICINE;
            }
        }

        TextView txtTitle = view.findViewById(R.id.txtOptionTitle);
        TextView txtSubTitle = view.findViewById(R.id.txtOptionSubtitle);
        MaterialButton btnDonate = view.findViewById(R.id.btnOptionDonate);
        MaterialButton btnRequest = view.findViewById(R.id.btnOptionRequest);

        if (DonationFragment.MODE_MEDICINE.equals(mode)) {
            txtTitle.setText("Medicine Support");
            txtSubTitle.setText("Choose if you want to donate medicines or request help.");
            btnDonate.setText("Donate Medicine");
            btnRequest.setText("Request Medicine");

            btnDonate.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_donationOptionFragment_to_medicineDonateFragment));

            btnRequest.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_donationOptionFragment_to_medicineRequestFragment));
        } else {
            txtTitle.setText("Blood Support");
            txtSubTitle.setText("Choose if you want to donate blood or raise a blood request.");
            btnDonate.setText("Donate Blood");
            btnRequest.setText("Request Blood");

            btnDonate.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_donationOptionFragment_to_bloodDonateFragment));

            btnRequest.setOnClickListener(v -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_donationOptionFragment_to_bloodRequestFragment));
        }
    }
}
