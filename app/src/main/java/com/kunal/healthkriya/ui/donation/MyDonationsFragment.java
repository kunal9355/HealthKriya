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
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.data.repository.DonationRepository;

import java.util.ArrayList;
import java.util.List;

public class MyDonationsFragment extends Fragment {

    private enum Filter {
        ALL,
        ACTIVE,
        COMPLETED
    }

    private DonationRepository repository;
    private DonationAdapter adapter;
    private final List<DonationEntity> allItems = new ArrayList<>();
    private Filter activeFilter = Filter.ALL;
    private TextView emptyView;
    private TextView chipAll;
    private TextView chipActive;
    private TextView chipCompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_donations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new DonationRepository(requireContext());
        emptyView = view.findViewById(R.id.emptyMyDonationList);
        chipAll = view.findViewById(R.id.chipAll);
        chipActive = view.findViewById(R.id.chipActive);
        chipCompleted = view.findViewById(R.id.chipCompleted);

        chipAll.setOnClickListener(v -> selectFilter(Filter.ALL));
        chipActive.setOnClickListener(v -> selectFilter(Filter.ACTIVE));
        chipCompleted.setOnClickListener(v -> selectFilter(Filter.COMPLETED));
        updateFilterChips();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerMyDonationsFull);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DonationAdapter(true, false, this::openDetail);
        recyclerView.setAdapter(adapter);

        repository.getUserDonations().observe(getViewLifecycleOwner(), items -> {
            allItems.clear();
            if (items != null) {
                allItems.addAll(items);
            }
            applyFilter();
        });

        repository.startRealtimeSync();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (repository != null) {
            repository.stopRealtimeSync();
        }
    }

    private void selectFilter(Filter filter) {
        activeFilter = filter;
        updateFilterChips();
        applyFilter();
    }

    private void updateFilterChips() {
        chipAll.setSelected(activeFilter == Filter.ALL);
        chipActive.setSelected(activeFilter == Filter.ACTIVE);
        chipCompleted.setSelected(activeFilter == Filter.COMPLETED);
    }

    private void applyFilter() {
        List<DonationEntity> filtered = new ArrayList<>();
        for (DonationEntity item : allItems) {
            if (item.deleted) {
                continue;
            }
            if (activeFilter == Filter.ACTIVE) {
                if (DonationEntity.STATUS_ACTIVE.equals(item.status)
                        || DonationEntity.STATUS_IN_PROGRESS.equals(item.status)) {
                    filtered.add(item);
                }
                continue;
            }
            if (activeFilter == Filter.COMPLETED) {
                if (DonationEntity.STATUS_COMPLETED.equals(item.status)
                        || DonationEntity.STATUS_CANCELLED.equals(item.status)
                        || DonationEntity.STATUS_EXPIRED.equals(item.status)) {
                    filtered.add(item);
                }
                continue;
            }
            filtered.add(item);
        }

        adapter.update(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openDetail(DonationEntity item) {
        if (item == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(DonationDetailFragment.ARG_CLIENT_ID, item.clientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_myDonationsFragment_to_donationDetailFragment, args);
    }
}
