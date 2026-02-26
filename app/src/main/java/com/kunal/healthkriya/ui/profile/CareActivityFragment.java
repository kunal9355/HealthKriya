package com.kunal.healthkriya.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

import java.util.ArrayList;
import java.util.List;

public class CareActivityFragment extends Fragment {

    private DonationRepository donationRepository;
    private TextView txtSummary;
    private TextView txtBloodEntries;
    private TextView txtMedicineEntries;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_care_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        donationRepository = new DonationRepository(requireContext());
        txtSummary = view.findViewById(R.id.txtCareSummary);
        txtBloodEntries = view.findViewById(R.id.txtBloodEntries);
        txtMedicineEntries = view.findViewById(R.id.txtMedicineEntries);

        loadEntries();
    }

    private void loadEntries() {
        donationRepository.getAllActiveDonations(entries -> {
            if (!isAdded()) {
                return;
            }

            List<DonationEntity> blood = new ArrayList<>();
            List<DonationEntity> medicine = new ArrayList<>();
            int donateCount = 0;
            int requestCount = 0;

            for (DonationEntity entry : entries) {
                if (DonationEntity.ACTION_DONATE.equals(entry.actionType)) {
                    donateCount++;
                } else if (DonationEntity.ACTION_REQUEST.equals(entry.actionType)) {
                    requestCount++;
                }

                if (DonationEntity.CATEGORY_BLOOD.equals(entry.category)) {
                    blood.add(entry);
                } else if (DonationEntity.CATEGORY_MEDICINE.equals(entry.category)) {
                    medicine.add(entry);
                }
            }

            txtSummary.setText(
                    "Total " + entries.size() + " entries  •  "
                            + "Donate " + donateCount + "  •  "
                            + "Request " + requestCount
            );
            txtBloodEntries.setText(formatSectionEntries(blood));
            txtMedicineEntries.setText(formatSectionEntries(medicine));
        });
    }

    private String formatSectionEntries(List<DonationEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return "- No entries";
        }

        StringBuilder builder = new StringBuilder();
        for (DonationEntity entry : entries) {
            String action = DonationEntity.ACTION_DONATE.equals(entry.actionType) ? "Donate" : "Request";
            String title = safeText(entry.title, "Untitled");
            String detail = safeText(entry.detail, "");

            builder.append("- ").append(action).append(": ").append(title);
            if (!detail.isEmpty()) {
                builder.append(" (").append(detail).append(")");
            }
            builder.append(" [").append(syncLabel(entry.syncStatus)).append("]").append("\n");
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

    private String syncLabel(int status) {
        if (status == DonationEntity.SYNC_SYNCED) {
            return "Synced";
        }
        if (status == DonationEntity.SYNC_ERROR) {
            return "Error";
        }
        return "Pending";
    }
}
