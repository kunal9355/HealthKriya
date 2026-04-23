package com.kunal.healthkriya.ui.donation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
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

public class DonationFragment extends Fragment {

    private DonationRepository repository;
    private DonationAdapter myDonationsAdapter;
    private DonationAdapter publicFeedAdapter;

    private TextView emptyMyDonations;
    private TextView emptyPublicFeed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new DonationRepository(requireContext());

        bindQuickActionCard(view.findViewById(R.id.actionDonateBlood),
                "Donate Blood",
                "Share availability",
                R.drawable.ic_donate,
                DonationEntity.TYPE_BLOOD);
        bindQuickActionCard(view.findViewById(R.id.actionRequestBlood),
                "Request Blood",
                "Raise urgent need",
                R.drawable.ic_medical,
                DonationEntity.TYPE_BLOOD);
        bindQuickActionCard(view.findViewById(R.id.actionDonateMedicine),
                "Donate Medicine",
                "Pass essentials forward",
                R.drawable.ic_medicine,
                DonationEntity.TYPE_MEDICINE);
        bindQuickActionCard(view.findViewById(R.id.actionRequestMedicine),
                "Request Medicine",
                "Ask community support",
                R.drawable.ic_pill,
                DonationEntity.TYPE_MEDICINE);

        view.findViewById(R.id.actionDonateBlood).setOnClickListener(v ->
                openForm(DonationEntity.TYPE_BLOOD, DonationEntity.ACTION_DONATE));
        view.findViewById(R.id.actionRequestBlood).setOnClickListener(v ->
                openForm(DonationEntity.TYPE_BLOOD, DonationEntity.ACTION_REQUEST));
        view.findViewById(R.id.actionDonateMedicine).setOnClickListener(v ->
                openForm(DonationEntity.TYPE_MEDICINE, DonationEntity.ACTION_DONATE));
        view.findViewById(R.id.actionRequestMedicine).setOnClickListener(v ->
                openForm(DonationEntity.TYPE_MEDICINE, DonationEntity.ACTION_REQUEST));

        emptyMyDonations = view.findViewById(R.id.emptyMyDonations);
        emptyPublicFeed = view.findViewById(R.id.emptyPublicFeed);

        RecyclerView recyclerMyDonations = view.findViewById(R.id.recyclerMyDonations);
        RecyclerView recyclerPublicFeed = view.findViewById(R.id.recyclerPublicPreview);

        myDonationsAdapter = new DonationAdapter(true, true, this::openDetail);
        publicFeedAdapter = new DonationAdapter(false, false, true, this::openDetail);

        recyclerMyDonations.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerMyDonations.setAdapter(myDonationsAdapter);

        recyclerPublicFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPublicFeed.setNestedScrollingEnabled(false);
        recyclerPublicFeed.setAdapter(publicFeedAdapter);

        view.findViewById(R.id.btnViewAllMine).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_donationFragment_to_myDonationsFragment));

        view.findViewById(R.id.btnViewAllPublic).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_donationFragment_to_publicHelpFeedFragment));

        observeData();
    }

    private void observeData() {
        repository.getUserDonations().observe(getViewLifecycleOwner(), items -> {
            List<DonationEntity> preview = take(items, 6);
            myDonationsAdapter.update(preview);
            emptyMyDonations.setVisibility(preview.isEmpty() ? View.VISIBLE : View.GONE);
        });

        repository.getPublicDonations().observe(getViewLifecycleOwner(), items -> {
            List<DonationEntity> ranked = filterPublicRequests(items);
            sortByPriority(ranked);
            Set<String> matchIds = collectBestMatches(ranked);
            List<DonationEntity> preview = take(ranked, 4);
            publicFeedAdapter.setBestMatchClientIds(matchIds);
            publicFeedAdapter.update(preview);
            emptyPublicFeed.setVisibility(preview.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void bindQuickActionCard(View cardView,
                                     String title,
                                     String subtitle,
                                     int iconRes,
                                     String type) {
        MaterialCardView card = (MaterialCardView) cardView;
        ImageView icon = card.findViewById(R.id.imgActionIcon);
        TextView titleView = card.findViewById(R.id.txtActionTitle);
        TextView subtitleView = card.findViewById(R.id.txtActionSubtitle);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        icon.setImageResource(iconRes);
        icon.setColorFilter(DonationUiUtils.typeAccentColor(requireContext(), type));
        card.setCardBackgroundColor(DonationUiUtils.typeSurfaceColor(requireContext(), type));
    }

    private void openForm(String type, String action) {
        Bundle args = new Bundle();
        args.putString(DonationFormFragment.ARG_TYPE, type);
        args.putString(DonationFormFragment.ARG_ACTION, action);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_donationFragment_to_donationFormFragment, args);
    }

    private void openDetail(DonationEntity item) {
        if (item == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(DonationDetailFragment.ARG_CLIENT_ID, item.clientId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_donationFragment_to_donationDetailFragment, args);
    }

    private List<DonationEntity> filterPublicRequests(List<DonationEntity> items) {
        List<DonationEntity> filtered = new ArrayList<>();
        if (items == null) {
            return filtered;
        }
        for (DonationEntity item : items) {
            boolean isActive = DonationEntity.STATUS_ACTIVE.equals(item.status)
                    || DonationEntity.STATUS_IN_PROGRESS.equals(item.status);
            if (isActive && !item.deleted && item.isPublic) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private void sortByPriority(List<DonationEntity> items) {
        Collections.sort(items, Comparator
                .comparingInt((DonationEntity item) -> priority(item)).reversed()
                .thenComparingLong(item -> item.updatedAt).reversed());
    }

    private int priority(DonationEntity item) {
        int score = 0;
        if (DonationEntity.URGENCY_CRITICAL.equals(item.urgency)) {
            score += 3;
        } else if (DonationEntity.URGENCY_HIGH.equals(item.urgency)) {
            score += 2;
        }
        score += LocationHelper.matchScore(requireContext(), item);
        return score;
    }

    private Set<String> collectBestMatches(List<DonationEntity> items) {
        Set<String> result = new HashSet<>();
        for (DonationEntity item : items) {
            if (item.clientId != null && LocationHelper.isBestMatch(requireContext(), item)) {
                result.add(item.clientId);
            }
        }
        return result;
    }

    private List<DonationEntity> take(List<DonationEntity> items, int limit) {
        List<DonationEntity> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (DonationEntity item : items) {
            if (result.size() >= limit) {
                break;
            }
            result.add(item);
        }
        return result;
    }
}
