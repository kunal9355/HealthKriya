package com.kunal.healthkriya.ui.mood.journal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private enum FilterType {
        ALL,
        LAST_7_DAYS,
        THIS_MONTH
    }

    private MoodRepository repository;
    private JournalAdapter adapter;

    private final List<MoodEntity> allMoods = new ArrayList<>();
    private FilterType activeFilter = FilterType.ALL;

    private View emptyStateView;
    private TextView txtSummary;
    private TextView btnAll;
    private TextView btnLast7;
    private TextView btnMonth;

    private final SimpleDateFormat storageFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat monthFormat =
            new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        repository = new MoodRepository(requireContext());

        RecyclerView rvJournal = view.findViewById(R.id.rvJournal);
        txtSummary = view.findViewById(R.id.txtSummary);
        emptyStateView = view.findViewById(R.id.layoutEmptyState);
        btnAll = view.findViewById(R.id.btnAll);
        btnLast7 = view.findViewById(R.id.btnLast7);
        btnMonth = view.findViewById(R.id.btnMonth);

        adapter = new JournalAdapter(new ArrayList<>(), new JournalAdapter.OnItemActionListener() {
            @Override
            public void onItemClick(MoodEntity mood) {
                openDetailPage(mood.date);
            }

            @Override
            public void onItemDelete(MoodEntity mood) {
                deleteMoodWithUndo(view, mood);
            }
        });

        rvJournal.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJournal.setAdapter(adapter);

        btnAll.setOnClickListener(v -> selectFilter(FilterType.ALL));
        btnLast7.setOnClickListener(v -> selectFilter(FilterType.LAST_7_DAYS));
        btnMonth.setOnClickListener(v -> selectFilter(FilterType.THIS_MONTH));

        updateFilterButtons();
        loadMoods();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMoods();
    }

    private void loadMoods() {
        if (repository == null) return;

        repository.getAllMoods(moods -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                allMoods.clear();
                if (moods != null) {
                    allMoods.addAll(moods);
                }

                Collections.sort(allMoods, (o1, o2) -> o2.date.compareTo(o1.date));
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        List<MoodEntity> filtered = new ArrayList<>();
        Date today = new Date();

        String startDate = null;
        String endDate = storageFormat.format(today);
        String currentMonth = monthFormat.format(today);

        if (activeFilter == FilterType.LAST_7_DAYS) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            startDate = storageFormat.format(calendar.getTime());
        }

        for (MoodEntity mood : allMoods) {
            if (activeFilter == FilterType.ALL) {
                filtered.add(mood);
                continue;
            }

            if (activeFilter == FilterType.THIS_MONTH) {
                if (mood.date != null && mood.date.startsWith(currentMonth)) {
                    filtered.add(mood);
                }
                continue;
            }

            if (mood.date != null
                    && startDate != null
                    && mood.date.compareTo(startDate) >= 0
                    && mood.date.compareTo(endDate) <= 0) {
                filtered.add(mood);
            }
        }

        adapter.update(filtered);
        updateSummary(filtered);
        emptyStateView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateSummary(List<MoodEntity> moods) {
        int pendingCount = 0;
        int errorCount = 0;

        for (MoodEntity mood : moods) {
            if (mood.syncStatus == MoodEntity.SYNC_PENDING) pendingCount++;
            if (mood.syncStatus == MoodEntity.SYNC_ERROR) errorCount++;
        }

        String summary = moods.size() + " entries";
        if (pendingCount > 0) summary += " • " + pendingCount + " pending";
        if (errorCount > 0) summary += " • " + errorCount + " error";

        txtSummary.setText(summary);
    }

    private void selectFilter(FilterType filterType) {
        activeFilter = filterType;
        updateFilterButtons();
        applyFilter();
    }

    private void updateFilterButtons() {
        btnAll.setSelected(activeFilter == FilterType.ALL);
        btnLast7.setSelected(activeFilter == FilterType.LAST_7_DAYS);
        btnMonth.setSelected(activeFilter == FilterType.THIS_MONTH);
    }

    private void deleteMoodWithUndo(View root, MoodEntity mood) {
        if (repository == null) return;

        repository.softDeleteMood(mood.date, (success, error) -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!success) {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadMoods();
                Snackbar.make(root, "Entry deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> undoDelete(mood.date))
                        .show();
            });
        });
    }

    private void undoDelete(String date) {
        if (repository == null) return;

        repository.undoDeleteMood(date, (success, error) -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!success) {
                    Toast.makeText(requireContext(), "Undo failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadMoods();
            });
        });
    }

    private void openDetailPage(String date) {
        Intent intent = new Intent(requireContext(), JournalDetailActivity.class);
        intent.putExtra("date", date);
        startActivity(intent);
    }
}
