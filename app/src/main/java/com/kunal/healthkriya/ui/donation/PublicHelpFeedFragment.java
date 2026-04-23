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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.helper.LocationHelper;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PublicHelpFeedFragment extends Fragment {

    private DonationRepository repository;
    private DonationAdapter adapter;
    private final List<DonationEntity> allItems = new ArrayList<>();

    private String selectedType = "all";
    private String selectedUrgency = "all";
    private boolean cityOnlyFilter;

    private TextView emptyView;
    private TextView emergencyBanner;
    private TextView chipAllTypes;
    private TextView chipBlood;
    private TextView chipMedicine;
    private TextView chipAllUrgency;
    private TextView chipHigh;
    private TextView chipCritical;
    private TextView chipNearbyCity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_public_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new DonationRepository(requireContext());
        emptyView = view.findViewById(R.id.emptyPublicFeed);
        emergencyBanner = view.findViewById(R.id.txtEmergencyBanner);

        chipAllTypes = view.findViewById(R.id.chipTypeAll);
        chipBlood = view.findViewById(R.id.chipTypeBlood);
        chipMedicine = view.findViewById(R.id.chipTypeMedicine);
        chipAllUrgency = view.findViewById(R.id.chipUrgencyAll);
        chipHigh = view.findViewById(R.id.chipUrgencyHigh);
        chipCritical = view.findViewById(R.id.chipUrgencyCritical);
        chipNearbyCity = view.findViewById(R.id.chipNearbyCity);

        setupFilters();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerPublicFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DonationAdapter(false, false, true, this::openDetail);
        recyclerView.setAdapter(adapter);

        repository.getPublicDonations().observe(getViewLifecycleOwner(), items -> {
            allItems.clear();
            if (items != null) {
                allItems.addAll(items);
            }
            applyFilters();
        });
    }

    private void setupFilters() {
        chipAllTypes.setSelected(true);
        chipAllUrgency.setSelected(true);

        chipAllTypes.setOnClickListener(v -> {
            selectedType = "all";
            updateTypeChips();
            applyFilters();
        });
        chipBlood.setOnClickListener(v -> {
            selectedType = DonationEntity.TYPE_BLOOD;
            updateTypeChips();
            applyFilters();
        });
        chipMedicine.setOnClickListener(v -> {
            selectedType = DonationEntity.TYPE_MEDICINE;
            updateTypeChips();
            applyFilters();
        });

        chipAllUrgency.setOnClickListener(v -> {
            selectedUrgency = "all";
            updateUrgencyChips();
            applyFilters();
        });
        chipHigh.setOnClickListener(v -> {
            selectedUrgency = DonationEntity.URGENCY_HIGH;
            updateUrgencyChips();
            applyFilters();
        });
        chipCritical.setOnClickListener(v -> {
            selectedUrgency = DonationEntity.URGENCY_CRITICAL;
            updateUrgencyChips();
            applyFilters();
        });

        chipNearbyCity.setOnClickListener(v -> {
            cityOnlyFilter = !cityOnlyFilter;
            chipNearbyCity.setSelected(cityOnlyFilter);
            applyFilters();
        });
    }

    private void updateTypeChips() {
        chipAllTypes.setSelected("all".equals(selectedType));
        chipBlood.setSelected(DonationEntity.TYPE_BLOOD.equals(selectedType));
        chipMedicine.setSelected(DonationEntity.TYPE_MEDICINE.equals(selectedType));
    }

    private void updateUrgencyChips() {
        chipAllUrgency.setSelected("all".equals(selectedUrgency));
        chipHigh.setSelected(DonationEntity.URGENCY_HIGH.equals(selectedUrgency));
        chipCritical.setSelected(DonationEntity.URGENCY_CRITICAL.equals(selectedUrgency));
    }

    private void applyFilters() {
        List<DonationEntity> filtered = new ArrayList<>();
        Set<String> bestMatchIds = new HashSet<>();
        boolean hasCritical = false;

        for (DonationEntity item : allItems) {
            boolean isActive = DonationEntity.STATUS_ACTIVE.equals(item.status)
                    || DonationEntity.STATUS_IN_PROGRESS.equals(item.status);

            if (item.deleted || !item.isPublic || !isActive) {
                continue;
            }
            if (!"all".equals(selectedType) && !selectedType.equals(item.type)) {
                continue;
            }
            if (!"all".equals(selectedUrgency) && !selectedUrgency.equals(item.urgency)) {
                continue;
            }
            if (cityOnlyFilter && !LocationHelper.isNearbyCity(requireContext(), item.city)) {
                continue;
            }

            filtered.add(item);
            if (LocationHelper.isBestMatch(requireContext(), item) && item.clientId != null) {
                bestMatchIds.add(item.clientId);
            }
            if (DonationEntity.URGENCY_CRITICAL.equals(item.urgency)) {
                hasCritical = true;
            }
        }

        Collections.sort(filtered, Comparator
                .comparingInt((DonationEntity item) -> sortPriority(item)).reversed()
                .thenComparingLong(item -> item.updatedAt).reversed());

        emergencyBanner.setVisibility(hasCritical ? View.VISIBLE : View.GONE);
        adapter.setBestMatchClientIds(bestMatchIds);
        adapter.update(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int sortPriority(DonationEntity item) {
        int score = LocationHelper.matchScore(requireContext(), item);
        if (DonationEntity.URGENCY_CRITICAL.equals(item.urgency)) {
            score += 4;
        } else if (DonationEntity.URGENCY_HIGH.equals(item.urgency)) {
            score += 2;
        }
        return score;
    }

    private void openDetail(DonationEntity item) {
        if (item == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(DonationDetailFragment.ARG_CLIENT_ID, item.clientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_publicHelpFeedFragment_to_donationDetailFragment, args);
    }
}
