package com.kunal.healthkriya.ui.reminder;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.core.WindowInsetUtils;

public class MedicineReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        View root = findViewById(R.id.reminderContainer);
        WindowInsetUtils.applySystemBarPadding(root, true, true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reminderContainer, MedicineReminderFragment.newInstance(true))
                    .commit();
        }
    }
}
