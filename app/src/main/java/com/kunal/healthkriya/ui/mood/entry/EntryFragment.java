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

public class EntryFragment extends Fragment {


    private Button btnSaveMood;
    private ImageView selectedEmoji;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        ImageView emojiSad = view.findViewById(R.id.emojiSad);
        ImageView emojiNeutral = view.findViewById(R.id.emojiNeutral);
        ImageView emojiHappy = view.findViewById(R.id.emojiHappy);
        ImageView emojiVeryHappy = view.findViewById(R.id.emojiVeryHappy);

        ImageView emojiAngry = view.findViewById(R.id.emojiAngry);


        btnSaveMood = view.findViewById(R.id.btnSaveMood);

        View.OnClickListener emojiClickListener = v -> selectEmoji((ImageView) v);

        emojiSad.setOnClickListener(emojiClickListener);
        emojiNeutral.setOnClickListener(emojiClickListener);
        emojiHappy.setOnClickListener(emojiClickListener);
        emojiVeryHappy.setOnClickListener(emojiClickListener);
        emojiAngry.setOnClickListener(emojiClickListener);


        btnSaveMood.setOnClickListener(v ->
                Toast.makeText(getContext(), "Mood Saved (dummy)", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void selectEmoji(ImageView emoji) {

        // reset previous
        if (selectedEmoji != null) {
            selectedEmoji.setBackgroundResource(R.drawable.bg_emoji_normal);
            selectedEmoji.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
            selectedEmoji.setAlpha(0.6f);
        }

        // new selected
        selectedEmoji = emoji;

        emoji.setAlpha(1f);
        emoji.setBackgroundResource(R.drawable.bg_emoji_selected);

        emoji.animate()
                .scaleX(1.35f)
                .scaleY(1.35f)
                .setDuration(180)
                .withEndAction(() ->
                        emoji.animate()
                                .scaleX(1.25f)
                                .scaleY(1.25f)
                                .setDuration(120)
                )
                .start();

        // subtle haptic feedback
        emoji.performHapticFeedback(
                android.view.HapticFeedbackConstants.KEYBOARD_TAP
        );

        btnSaveMood.setEnabled(true);
        btnSaveMood.setAlpha(1f);
    }
}
