package com.kunal.healthkriya.ui.mood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_hub);

        // Setup Entry option
        TextView txtEntry = findViewById(R.id.cardEntry).findViewById(R.id.txtTitle);
        txtEntry.setText("Mood Entry");

        // Setup Journal option
        TextView txtJournal = findViewById(R.id.cardJournal).findViewById(R.id.txtTitle);
        txtJournal.setText("Daily Journal");

        // Setup Analytics option
        TextView txtAnalytics = findViewById(R.id.cardAnalytics).findViewById(R.id.txtTitle);
        txtAnalytics.setText("Mood Analysis");

        MoodRepository repository = new MoodRepository(this);


        findViewById(R.id.cardEntry).setOnClickListener(v -> openMoodContainer(0,selectedDate));
        findViewById(R.id.cardJournal).setOnClickListener(v -> openMoodContainer(1,selectedDate));
        findViewById(R.id.cardAnalytics).setOnClickListener(v -> openMoodContainer(2,selectedDate));


        RecyclerView rvCalendar = findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<CalendarDateModel> calendarList = new ArrayList<>();
        CalendarAdapter adapter = new CalendarAdapter(calendarList,
                date -> selectedDate = date);
        rvCalendar.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        selectedDate = fullFormat.format(calendar.getTime());

        TextView txtMonth = findViewById(R.id.txtMonth);
        txtMonth.setText(monthFormat.format(calendar.getTime()));

        repository.getAllMoods(moods -> {

            Map<String, Integer> moodMap = new HashMap<>();
            for (MoodEntity m : moods) {
                moodMap.put(m.date, m.moodLevel);
            }

            List<CalendarDateModel> generatedDates = new ArrayList<>();
            Calendar tempCalendar = Calendar.getInstance();

            for (int i = 0; i < 14; i++) {
                String fullDate = fullFormat.format(tempCalendar.getTime());

                generatedDates.add(
                        new CalendarDateModel(
                                dayFormat.format(tempCalendar.getTime()),
                                dateFormat.format(tempCalendar.getTime()),
                                fullDate,
                                i == 0,
                                moodMap.get(fullDate) // null if not present
                        )
                );
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            runOnUiThread(() -> {
                calendarList.clear();
                calendarList.addAll(generatedDates);
                adapter.notifyDataSetChanged();

                if (!calendarList.isEmpty()) {
                    selectedDate = calendarList.get(0).fullDate;
                }
            });
        });

    }

    private void openMoodContainer(int startTab, String date) {
        Intent intent = new Intent(this, MoodContainerActivity.class);
        intent.putExtra("START_TAB", startTab);
        intent.putExtra("SELECTED_DATE", date);
        startActivity(intent);
    }

}
