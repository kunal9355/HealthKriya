package com.kunal.healthkriya.ui.reminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.repository.ReminderRepository;

import java.util.ArrayList;
import java.util.List;

public class WeeklyCalendarAdapter extends RecyclerView.Adapter<WeeklyCalendarAdapter.ViewHolder> {

    private final List<ReminderRepository.DailyOverview> days = new ArrayList<>();

    public void update(List<ReminderRepository.DailyOverview> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weekly_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReminderRepository.DailyOverview day = days.get(position);
        holder.tvDayLabel.setText(day.dayLabel);
        holder.tvDayDate.setText(day.dateKey.substring(Math.max(0, day.dateKey.length() - 2)));
        holder.tvDayAdherence.setText(day.adherencePercent + "%");
        holder.tvDayCount.setText(day.taken + "/" + day.total);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayLabel;
        TextView tvDayDate;
        TextView tvDayAdherence;
        TextView tvDayCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayLabel = itemView.findViewById(R.id.tvDayLabel);
            tvDayDate = itemView.findViewById(R.id.tvDayDate);
            tvDayAdherence = itemView.findViewById(R.id.tvDayAdherence);
            tvDayCount = itemView.findViewById(R.id.tvDayCount);
        }
    }
}
