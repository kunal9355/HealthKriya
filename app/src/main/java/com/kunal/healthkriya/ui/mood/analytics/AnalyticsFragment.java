package com.kunal.healthkriya.ui.mood.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private LineChart lineChart;
    private BarChart barChart;
    private MoodRepository repository;
    private int textPrimaryColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        lineChart = view.findViewById(R.id.lineChartWeekly);
        barChart = view.findViewById(R.id.barChartMonthly);
        textPrimaryColor = ContextCompat.getColor(requireContext(), R.color.health_text_primary);

        repository = new MoodRepository(requireContext());

        loadWeeklyChart();
        loadMonthlyChart();

        return view;
    }

    private void loadWeeklyChart() {
        repository.getWeeklyMoods(list -> {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            int index = 0;
            for (MoodEntity mood : list) {
                entries.add(new Entry(index, mood.moodLevel));
                labels.add(mood.date.substring(5)); // MM-dd
                index++;
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (entries.isEmpty()) {
                    lineChart.clear();
                    lineChart.setNoDataText("No mood data for this week");
                    lineChart.setNoDataTextColor(textPrimaryColor);
                    lineChart.invalidate();
                    return;
                }

                LineDataSet dataSet = new LineDataSet(entries, "Weekly Mood");
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setLineWidth(3f);
                dataSet.setCircleRadius(4f);
                dataSet.setColor(Color.parseColor("#4F46E5"));
                dataSet.setCircleColor(Color.parseColor("#4F46E5"));
                dataSet.setValueTextSize(10f);
                dataSet.setValueTextColor(textPrimaryColor);

                LineData data = new LineData(dataSet);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setTextColor(textPrimaryColor);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

                lineChart.getAxisRight().setEnabled(false);
                lineChart.getAxisLeft().setAxisMinimum(0f);
                lineChart.getAxisLeft().setAxisMaximum(5f);
                lineChart.getAxisLeft().setTextColor(textPrimaryColor);
                lineChart.getDescription().setEnabled(false);
                lineChart.getLegend().setEnabled(false);
                lineChart.setData(data);
                lineChart.animateX(900, Easing.EaseInOutQuad);
                lineChart.invalidate();
            });
        });
    }

    private void loadMonthlyChart() {
        repository.getMonthlyMoods(list -> {
            List<BarEntry> entries = new ArrayList<>();

            for (MoodEntity mood : list) {
                int day = Integer.parseInt(mood.date.substring(8, 10));
                entries.add(new BarEntry(day, mood.moodLevel));
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (entries.isEmpty()) {
                    barChart.clear();
                    barChart.setNoDataText("No mood data for this month");
                    barChart.setNoDataTextColor(textPrimaryColor);
                    barChart.invalidate();
                    return;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Monthly Mood");
                dataSet.setColor(Color.parseColor("#22C55E"));
                dataSet.setValueTextSize(10f);
                dataSet.setValueTextColor(textPrimaryColor);

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.8f);

                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setTextColor(textPrimaryColor);
                xAxis.setLabelCount(Math.min(entries.size(), 10), false);

                barChart.getAxisRight().setEnabled(false);
                barChart.getAxisLeft().setAxisMinimum(0f);
                barChart.getAxisLeft().setAxisMaximum(5f);
                barChart.getAxisLeft().setTextColor(textPrimaryColor);
                barChart.getDescription().setEnabled(false);
                barChart.getLegend().setEnabled(false);
                barChart.setData(data);
                barChart.setFitBars(true);
                barChart.animateY(900, Easing.EaseInOutQuad);
                barChart.invalidate();
            });
        });
    }
}
