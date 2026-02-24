package com.kunal.healthkriya.ui.reminder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.kunal.healthkriya.R;

public class MedicineReminderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_reminder);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.reminderContainer, MedicineReminderFragment.newInstance(true))
                    .commit();
        }
    }
}
