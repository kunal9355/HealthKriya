package com.kunal.healthkriya.ui.mood;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.core.WindowInsetUtils;
import com.kunal.healthkriya.ui.mood.analytics.AnalyticsFragment;
import com.kunal.healthkriya.ui.mood.entry.EntryFragment;
import com.kunal.healthkriya.ui.mood.journal.JournalFragment;

public class MoodContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_container);

        View root = findViewById(R.id.fragmentContainer);
        WindowInsetUtils.applySystemBarPadding(root, true, true);

        int startTab = getIntent().getIntExtra("START_TAB", 0);
        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");

        Fragment fragment;
        if (startTab == 1) {
            fragment = new JournalFragment();
        } else if (startTab == 2) {
            fragment = new AnalyticsFragment();
        } else {
            fragment = new EntryFragment();
        }

        Bundle bundle = new Bundle();
        bundle.putString("SELECTED_DATE", selectedDate);
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

}
