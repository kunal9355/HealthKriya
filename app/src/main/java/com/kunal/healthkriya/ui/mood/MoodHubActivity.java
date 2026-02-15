package com.kunal.healthkriya.ui.mood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.ui.mood.calendar.CalendarAdapter;
import com.kunal.healthkriya.ui.mood.calendar.CalendarDateModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MoodHubActivity extends AppCompatActivity {

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

        findViewById(R.id.cardEntry).setOnClickListener(v -> openMoodContainer(0));
        findViewById(R.id.cardJournal).setOnClickListener(v -> openMoodContainer(1));
        findViewById(R.id.cardAnalytics).setOnClickListener(v -> openMoodContainer(2));


        RecyclerView rvCalendar = findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<CalendarDateModel> calendarList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

        for (int i = 0; i < 14; i++) {
            String day = dayFormat.format(calendar.getTime());
            String date = dateFormat.format(calendar.getTime());
            calendarList.add(new CalendarDateModel(day, date, i == 0));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        CalendarAdapter adapter = new CalendarAdapter(calendarList);
        rvCalendar.setAdapter(adapter);


    }

    private void openMoodContainer(int startTab) {
        Intent intent = new Intent(this, MoodContainerActivity.class);
        intent.putExtra("START_TAB", startTab);
        startActivity(intent);
    }
}
