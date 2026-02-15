package com.kunal.healthkriya.ui.mood;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kunal.healthkriya.R;

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
    }

    private void openMoodContainer(int startTab) {
        Intent intent = new Intent(this, MoodContainerActivity.class);
        intent.putExtra("START_TAB", startTab);
        startActivity(intent);
    }
}
