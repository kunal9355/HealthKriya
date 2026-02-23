package com.kunal.healthkriya.ui.mood.journal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;

public class JournalDetailActivity extends AppCompatActivity {

    private MoodRepository repository;
    private String selectedDate;

    private TextView txtDate;
    private TextView txtMood;
    private TextView txtNote;
    private TextView txtSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_detail);

        repository = new MoodRepository(this);

        txtDate = findViewById(R.id.txtDate);
        txtMood = findViewById(R.id.txtMood);
        txtNote = findViewById(R.id.txtNote);
        txtSync = findViewById(R.id.txtSync);

        selectedDate = getIntent().getStringExtra("date");
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            finish();
            return;
        }

        txtDate.setText(selectedDate);
        loadMoodDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMoodDetail();
    }

    private void loadMoodDetail() {
        repository.getMoodByDate(selectedDate, mood -> runOnUiThread(() -> renderMood(mood)));
    }

    private void renderMood(MoodEntity mood) {
        if (mood == null) {
            txtMood.setText("No active mood");
            txtNote.setText("This entry was deleted or not found.");
            txtSync.setText("Status: --");
            return;
        }

        txtDate.setText(mood.date);
        txtMood.setText(getEmoji(mood.moodLevel) + "  " + getMoodLabel(mood.moodLevel));
        txtNote.setText(
                mood.note == null || mood.note.trim().isEmpty()
                        ? "No note added"
                        : mood.note.trim()
        );
        txtSync.setText("Status: " + getSyncLabel(mood.syncStatus));
    }

    private String getEmoji(int moodLevel) {
        switch (moodLevel) {
            case 1:
                return "😡";
            case 2:
                return "😞";
            case 3:
                return "😐";
            case 4:
                return "🙂";
            default:
                return "😄";
        }
    }

    private String getMoodLabel(int moodLevel) {
        switch (moodLevel) {
            case 1:
                return "Very Low";
            case 2:
                return "Low";
            case 3:
                return "Neutral";
            case 4:
                return "Good";
            default:
                return "Great";
        }
    }

    private String getSyncLabel(int syncStatus) {
        if (syncStatus == MoodEntity.SYNC_SYNCED) return "Synced";
        if (syncStatus == MoodEntity.SYNC_ERROR) return "Error";
        return "Pending";
    }
}
