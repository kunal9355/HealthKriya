package com.kunal.healthkriya.ui.mood.journal;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalAdapter
        extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {

    private List<MoodEntity> list;
    private final OnItemActionListener listener;
    private final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public interface OnItemActionListener {
        void onItemClick(MoodEntity mood);
        void onItemDelete(MoodEntity mood);
    }

    public JournalAdapter(List<MoodEntity> list,
                          OnItemActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void update(List<MoodEntity> newList) {
        list = newList == null ? new ArrayList<>() : new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {

        MoodEntity mood = list.get(pos);

        h.txtDate.setText(formatDate(mood.date));
        h.txtNote.setText((mood.note == null || mood.note.trim().isEmpty())
                ? "No note added"
                : mood.note.trim());
        h.txtMoodEmoji.setText(getEmoji(mood.moodLevel));
        h.txtMoodLabel.setText(getMoodLabel(mood.moodLevel));
        h.moodDot.setBackgroundTintList(ColorStateList.valueOf(getMoodColor(h, mood.moodLevel)));

        if (mood.syncStatus == MoodEntity.SYNC_SYNCED) {
            h.txtSync.setText("Synced");
            h.txtSync.setBackgroundResource(R.drawable.bg_sync_status_synced);
            h.txtSync.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.journal_sync_synced_text));
        } else if (mood.syncStatus == MoodEntity.SYNC_ERROR) {
            h.txtSync.setText("Error");
            h.txtSync.setBackgroundResource(R.drawable.bg_sync_status_error);
            h.txtSync.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.journal_sync_error_text));
        } else {
            h.txtSync.setText("Pending");
            h.txtSync.setBackgroundResource(R.drawable.bg_sync_status_pending);
            h.txtSync.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.journal_sync_pending_text));
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(mood));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemDelete(mood);
            return true;
        });
    }

    private String getEmoji(int level) {
        switch (level) {
            case 1: return "😡";
            case 2: return "😞";
            case 3: return "😐";
            case 4: return "🙂";
            default: return "😄";
        }
    }

    private String getMoodLabel(int level) {
        switch (level) {
            case 1: return "Very Low";
            case 2: return "Low";
            case 3: return "Neutral";
            case 4: return "Good";
            default: return "Great";
        }
    }

    private int getMoodColor(ViewHolder holder, int level) {
        int colorRes;
        switch (level) {
            case 1:
                colorRes = R.color.journal_mood_1;
                break;
            case 2:
                colorRes = R.color.journal_mood_2;
                break;
            case 3:
                colorRes = R.color.journal_mood_3;
                break;
            case 4:
                colorRes = R.color.journal_mood_4;
                break;
            default:
                colorRes = R.color.journal_mood_5;
                break;
        }
        return ContextCompat.getColor(holder.itemView.getContext(), colorRes);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null) return "--";
        try {
            Date parsed = sourceFormat.parse(rawDate);
            if (parsed == null) return rawDate;
            return displayFormat.format(parsed);
        } catch (ParseException ignored) {
            return rawDate;
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtNote, txtMoodEmoji, txtMoodLabel, txtSync;
        View moodDot;

        ViewHolder(View v) {
            super(v);
            txtDate = v.findViewById(R.id.txtDate);
            txtNote = v.findViewById(R.id.txtNote);
            txtMoodEmoji = v.findViewById(R.id.txtMoodEmoji);
            txtMoodLabel = v.findViewById(R.id.txtMoodLabel);
            txtSync = v.findViewById(R.id.txtSync);
            moodDot = v.findViewById(R.id.viewMoodDot);
        }
    }
}
