package com.kunal.healthkriya.ui.mood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kunal.healthkriya.R;

public class MoodHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_hub);

        // Entry option
        findViewById(R.id.optionEntry).setOnClickListener(v -> openMoodContainer(0));

        // Journal option
        findViewById(R.id.optionJournal).setOnClickListener(v -> openMoodContainer(1));

        // Analytics option
        findViewById(R.id.optionAnalytics).setOnClickListener(v -> openMoodContainer(2));
    }

    private void openMoodContainer(int startTab) {
        Intent intent = new Intent(this, MoodContainerActivity.class);
        intent.putExtra("START_TAB", startTab);
        startActivity(intent);
    }
}
