package com.kunal.healthkriya.ui.mood.entry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;

public class EntryFragment extends Fragment {

    private MoodRepository repository;

    private Button btnSaveMood;
    private EditText etNote;
    private ImageView selectedEmojiView;
    private ImageView emojiAngry, emojiSad, emojiNeutral, emojiHappy, emojiVeryHappy;
    private TextView txtDate,txtStreak;

    private boolean isUpdateMode = false;
    private String selectedDate;
    private int selectedMood = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        // Initialize views
        emojiAngry = view.findViewById(R.id.emojiAngry);
        emojiSad = view.findViewById(R.id.emojiSad);
        emojiNeutral = view.findViewById(R.id.emojiNeutral);
        emojiHappy = view.findViewById(R.id.emojiHappy);
        emojiVeryHappy = view.findViewById(R.id.emojiVeryHappy);
        
        etNote = view.findViewById(R.id.etNote);
        btnSaveMood = view.findViewById(R.id.btnSaveMood);
        txtDate = view.findViewById(R.id.txtSelectedDate);
        txtStreak = view.findViewById(R.id.txtStreak);
        repository = new MoodRepository(requireContext());
        refreshStreak();

        // Reset state
        isUpdateMode = false;
        selectedMood = -1;
        selectedEmojiView = null;
        btnSaveMood.setText("Save Mood");
        btnSaveMood.setEnabled(false);

        etNote.setText("");
        clearEmojiSelection();


        // Get date from arguments
        if (getArguments() != null) {
            selectedDate = getArguments().getString("SELECTED_DATE");
        }

        if (selectedDate != null && !selectedDate.isEmpty()) {
            txtDate.setText("Date: " + selectedDate);
            checkExistingEntry();
        } else {
            txtDate.setText("Date: Not selected");
        }

        // Click listeners
        emojiAngry.setOnClickListener(v -> onEmojiClicked(emojiAngry, 1));
        emojiSad.setOnClickListener(v -> onEmojiClicked(emojiSad, 2));
        emojiNeutral.setOnClickListener(v -> onEmojiClicked(emojiNeutral, 3));
        emojiHappy.setOnClickListener(v -> onEmojiClicked(emojiHappy, 4));
        emojiVeryHappy.setOnClickListener(v -> onEmojiClicked(emojiVeryHappy, 5));

        btnSaveMood.setOnClickListener(v -> saveMood());

        return view;
    }

    private void checkExistingEntry() {
        if (repository == null || selectedDate == null) return;

        repository.getMoodByDate(selectedDate, mood -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (mood != null) {
                    isUpdateMode = true;
                    selectedMood = mood.moodLevel;
                    etNote.setText(mood.note);
                    btnSaveMood.setText("Update Mood");
                    preSelectEmoji(mood.moodLevel);
                } else {
                    isUpdateMode = false;
                    btnSaveMood.setText("Save Mood");
                    btnSaveMood.setEnabled(selectedMood != -1);
                }
            });
        });

    }

    private void clearEmojiSelection() {
        selectedEmojiView = null;
        emojiAngry.setAlpha(0.6f);
        emojiSad.setAlpha(0.6f);
        emojiNeutral.setAlpha(0.6f);
        emojiHappy.setAlpha(0.6f);
        emojiVeryHappy.setAlpha(0.6f);

        emojiAngry.setScaleX(1f);
        emojiAngry.setScaleY(1f);
        emojiSad.setScaleX(1f);
        emojiSad.setScaleY(1f);
        emojiNeutral.setScaleX(1f);
        emojiNeutral.setScaleY(1f);
        emojiHappy.setScaleX(1f);
        emojiHappy.setScaleY(1f);
        emojiVeryHappy.setScaleX(1f);
        emojiVeryHappy.setScaleY(1f);
    }


    private void onEmojiClicked(ImageView emojiView, int moodLevel) {
        selectedMood = moodLevel;
        updateEmojiSelectionUI(emojiView);
        
        btnSaveMood.setEnabled(true);
        btnSaveMood.setAlpha(1f);
    }

    private void preSelectEmoji(int moodLevel) {
        selectedMood = moodLevel;
        ImageView target = null;
        switch (moodLevel) {
            case 1: target = emojiAngry; break;
            case 2: target = emojiSad; break;
            case 3: target = emojiNeutral; break;
            case 4: target = emojiHappy; break;
            case 5: target = emojiVeryHappy; break;
        }
        if (target != null) {
            updateEmojiSelectionUI(target);
            btnSaveMood.setEnabled(true);
            btnSaveMood.setAlpha(1f);
        }
    }

    private void updateEmojiSelectionUI(ImageView emojiView) {
        // Reset previous selection
        if (selectedEmojiView != null) {
            selectedEmojiView.setBackgroundResource(R.drawable.bg_emoji_normal);
            selectedEmojiView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
            selectedEmojiView.setAlpha(0.6f);
        }

        // Apply new selection
        selectedEmojiView = emojiView;
        selectedEmojiView.setAlpha(1f);
        selectedEmojiView.setBackgroundResource(R.drawable.bg_emoji_selected);

        selectedEmojiView.animate()
                .scaleX(1.35f)
                .scaleY(1.35f)
                .setDuration(180)
                .withEndAction(() ->
                        selectedEmojiView.animate()
                                .scaleX(1.25f)
                                .scaleY(1.25f)
                                .setDuration(120)
                )
                .start();

        selectedEmojiView.performHapticFeedback(
                android.view.HapticFeedbackConstants.KEYBOARD_TAP
        );
    }




    private void saveMood() {
        if (selectedMood == -1) {
            Toast.makeText(getContext(), "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(getContext(), "No date selected", Toast.LENGTH_SHORT).show();
            return;
        }

        MoodEntity entity = new MoodEntity(
                selectedDate,
                selectedMood,
                etNote.getText().toString()
        );
        btnSaveMood.setEnabled(false);
        repository.saveMood(entity, (success, error) -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                btnSaveMood.setEnabled(true);
                if (success) {
                    isUpdateMode = true;
                    btnSaveMood.setText("Update Mood");
                    Toast.makeText(getContext(), "Mood Saved", Toast.LENGTH_SHORT).show();
                    refreshStreak();
                } else {
                    Toast.makeText(getContext(), "Save failed, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void refreshStreak() {
        if (repository == null) return;

        repository.getCurrentStreak(streak -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    txtStreak.setText("🔥 " + streak + " day streak")
            );
        });
    }



}
