package com.kunal.healthkriya.ui.mood.calendar;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunal.healthkriya.R;

import java.util.List;



public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DateViewHolder> {

    private final List<CalendarDateModel> list;
    private final OnDateSelectListener listener;

    private int selectedPosition = 0;

    public interface OnDateSelectListener {
        void onDateSelected(String fullDate);
    }


    public CalendarAdapter(List<CalendarDateModel> list,
                           OnDateSelectListener listener) {
        this.list = list;
        this.listener = listener;
    }


    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {

        CalendarDateModel model = list.get(position);

        holder.txtDate.setText(model.date + "\n" + model.day);
        holder.txtDate.setBackgroundResource(
                model.isSelected ? R.drawable.bg_date_selected : R.drawable.bg_date_normal
        );

        holder.itemView.setOnClickListener(v -> {
            list.get(selectedPosition).isSelected = false;
            selectedPosition = position;
            list.get(selectedPosition).isSelected = true;
            notifyDataSetChanged();

            listener.onDateSelected(model.fullDate);
        });

        if (model.moodLevel != null) {
            holder.viewDot.setVisibility(View.VISIBLE);

            int color;
            switch (model.moodLevel) {
                case 1: color = Color.RED; break;
                case 2: color = Color.parseColor("#F97316"); break;
                case 3: color = Color.GRAY; break;
                case 4: color = Color.parseColor("#22C55E"); break;
                default: color = Color.parseColor("#16A34A");
            }

            ((GradientDrawable) holder.viewDot.getBackground())
                    .setColor(color);

        } else {
            holder.viewDot.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        View viewDot;
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            viewDot = itemView.findViewById(R.id.viewDot);
        }
    }
}
