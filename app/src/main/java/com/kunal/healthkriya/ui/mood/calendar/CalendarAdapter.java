package com.kunal.healthkriya.ui.mood.calendar;

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

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
