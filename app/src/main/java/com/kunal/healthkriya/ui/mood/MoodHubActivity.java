package com.kunal.healthkriya.ui.mood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.core.WindowInsetUtils;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;
import com.kunal.healthkriya.ui.mood.calendar.CalendarAdapter;
import com.kunal.healthkriya.ui.mood.calendar.CalendarDateModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodHubActivity extends AppCompatActivity {


    private String selectedDate; // yyyy-MM-dd
    private MoodRepository repository;
    private final List<CalendarDateModel> calendarList = new ArrayList<>();
    private CalendarAdapter adapter;
    private SimpleDateFormat dayFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat fullFormat;
    private MaterialCardView cardDayPreview;
    private TextView txtPreviewDate;
    private TextView txtPreviewMood;
    private TextView txtPreviewNote;

    private String getEmoji(int level) {
        switch (level) {
            case 1: return "😡";
            case 2: return "😞";
            case 3: return "😐";
            case 4: return "🙂";
            default: return "😄";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_hub);
        View root = findViewById(R.id.rootMoodHub);
        WindowInsetUtils.applySystemBarPadding(root, true, true);

        // Setup Entry option
        TextView txtEntry = findViewById(R.id.cardEntry).findViewById(R.id.txtTitle);
        txtEntry.setText("Mood Entry");

        // Setup Journal option
        TextView txtJournal = findViewById(R.id.cardJournal).findViewById(R.id.txtTitle);
        txtJournal.setText("Daily Journal");

        // Setup Analytics option
        TextView txtAnalytics = findViewById(R.id.cardAnalytics).findViewById(R.id.txtTitle);
        txtAnalytics.setText("Mood Analysis");

        repository = new MoodRepository(this);


        findViewById(R.id.cardEntry).setOnClickListener(v -> openMoodContainer(0,selectedDate));
        findViewById(R.id.cardJournal).setOnClickListener(v -> openMoodContainer(1,selectedDate));
        findViewById(R.id.cardAnalytics).setOnClickListener(v -> openMoodContainer(2,selectedDate));

        cardDayPreview = findViewById(R.id.cardDayPreview);
        txtPreviewDate = findViewById(R.id.txtPreviewDate);
        txtPreviewMood = findViewById(R.id.txtPreviewMood);
        txtPreviewNote = findViewById(R.id.txtPreviewNote);

        RecyclerView rvCalendar = findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        adapter = new CalendarAdapter(calendarList,
                date -> {
                    selectedDate = date;
                    loadDayPreview();
                });
        rvCalendar.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        fullFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        selectedDate = fullFormat.format(calendar.getTime());

        TextView txtMonth = findViewById(R.id.txtMonth);
        txtMonth.setText(monthFormat.format(calendar.getTime()));

        loadCalendarDates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCalendarDates();
    }

    private void loadCalendarDates() {
        if (repository == null || adapter == null || fullFormat == null) return;

        repository.getAllMoods(moods -> {

            Map<String, Integer> moodMap = new HashMap<>();
            for (MoodEntity m : moods) {
                moodMap.put(m.date, m.moodLevel);
            }

            List<CalendarDateModel> generatedDates = new ArrayList<>();
            Calendar tempCalendar = Calendar.getInstance();
            String targetDate = selectedDate;
            if (targetDate == null || targetDate.isEmpty()) {
                targetDate = fullFormat.format(tempCalendar.getTime());
            }
            boolean hasSelected = false;

            for (int i = 0; i < 14; i++) {
                String dateValue = fullFormat.format(tempCalendar.getTime());
                boolean isSelected = dateValue.equals(targetDate);
                if (isSelected) hasSelected = true;

                generatedDates.add(
                        new CalendarDateModel(
                                dayFormat.format(tempCalendar.getTime()),
                                dateFormat.format(tempCalendar.getTime()),
                                dateValue,
                                isSelected,
                                moodMap.get(dateValue) // null if not present
                        )
                );
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (!hasSelected && !generatedDates.isEmpty()) {
                generatedDates.get(0).isSelected = true;
                targetDate = generatedDates.get(0).fullDate;
            }
            String finalTargetDate = targetDate;

            runOnUiThread(() -> {
                calendarList.clear();
                calendarList.addAll(generatedDates);
                adapter.notifyDataSetChanged();
                selectedDate = finalTargetDate;
                loadDayPreview();
            });
        });
    }

    private void loadDayPreview() {
        if (repository == null || selectedDate == null || selectedDate.isEmpty()) return;

        repository.getMoodByDate(selectedDate, mood -> runOnUiThread(() -> {
            cardDayPreview.setVisibility(View.VISIBLE);

            if (mood != null) {
                txtPreviewDate.setText(mood.date);
                txtPreviewMood.setText(getEmoji(mood.moodLevel));
                txtPreviewNote.setText(
                        mood.note == null || mood.note.trim().isEmpty()
                                ? "No note for this day"
                                : mood.note.trim()
                );
            } else {
                txtPreviewDate.setText(selectedDate);
                txtPreviewMood.setText("");
                txtPreviewNote.setText("No entry for this day");
            }
        }));
    }

    private void openMoodContainer(int startTab, String date) {
        Intent intent = new Intent(this, MoodContainerActivity.class);
        intent.putExtra("START_TAB", startTab);
        intent.putExtra("SELECTED_DATE", date);
        startActivity(intent);
    }

}
