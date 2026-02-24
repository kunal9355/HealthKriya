package com.kunal.healthkriya.ui.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.reminder.ReminderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    public interface ReminderClickListener {
        void onReminderClick(ReminderEntity reminder);
    }

    private final List<ReminderEntity> reminderList;
    private final ReminderClickListener clickListener;

    public MedicineAdapter(List<ReminderEntity> reminderList, ReminderClickListener clickListener) {
        this.reminderList = reminderList;
        this.clickListener = clickListener;
    }

    public void update(List<ReminderEntity> newList) {
        reminderList.clear();
        if (newList != null) {
            reminderList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public ReminderEntity getItem(int position) {
        if (position < 0 || position >= reminderList.size()) {
            return null;
        }
        return reminderList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReminderEntity reminder = reminderList.get(position);
        long now = System.currentTimeMillis();

        holder.tvName.setText(reminder.name);
        holder.tvDosage.setText(reminder.dosage);
        holder.tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", reminder.hour, reminder.minute));

        ReminderStatusUtil.Status status = ReminderStatusUtil.getStatus(reminder, now);
        holder.tvStatus.setText("Status: " + ReminderStatusUtil.asLabel(status));
        bindStatusColor(holder.tvStatus, status);

        holder.tvSync.setText("Sync: " + syncLabel(reminder.syncStatus));
        bindSyncColor(holder.tvSync, reminder.syncStatus);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReminderClick(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    private void bindStatusColor(TextView tvStatus, ReminderStatusUtil.Status status) {
        Context context = tvStatus.getContext();
        int color;
        if (status == ReminderStatusUtil.Status.TAKEN) {
            color = ContextCompat.getColor(context, R.color.green);
        } else if (status == ReminderStatusUtil.Status.MISSED) {
            color = ContextCompat.getColor(context, R.color.red);
        } else {
            color = ContextCompat.getColor(context, R.color.orange);
        }
        tvStatus.setTextColor(color);
    }

    private void bindSyncColor(TextView tvSync, int syncStatus) {
        Context context = tvSync.getContext();
        int color;
        if (syncStatus == ReminderEntity.SYNC_SYNCED) {
            color = ContextCompat.getColor(context, R.color.journal_sync_synced_text);
        } else if (syncStatus == ReminderEntity.SYNC_ERROR) {
            color = ContextCompat.getColor(context, R.color.journal_sync_error_text);
        } else {
            color = ContextCompat.getColor(context, R.color.journal_sync_pending_text);
        }
        tvSync.setTextColor(color);
    }

    private String syncLabel(int syncStatus) {
        if (syncStatus == ReminderEntity.SYNC_SYNCED) return "Synced";
        if (syncStatus == ReminderEntity.SYNC_ERROR) return "Error";
        return "Pending";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvDosage;
        TextView tvTime;
        TextView tvStatus;
        TextView tvSync;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSync = itemView.findViewById(R.id.tvSync);
        }
    }
}
