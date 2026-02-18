package com.kunal.healthkriya.ui.mood.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.mood.MoodEntity;
import com.kunal.healthkriya.data.repository.MoodRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsFragment extends Fragment {

    private LineChart lineChartWeekly;
    private LineChart lineChartMonthly;
    private MoodRepository repository;
    private int textPrimaryColor;
    private int textSecondaryColor;
    private int gridColor;

    private TextView txtAvgMood, txtBestDay, txtWorstDay;
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat dayLabelFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        lineChartWeekly = view.findViewById(R.id.lineChartWeekly);
        lineChartMonthly = view.findViewById(R.id.lineChartMonthly);
        txtAvgMood = view.findViewById(R.id.txtAvgMood);
        txtBestDay = view.findViewById(R.id.txtBestDay);
        txtWorstDay = view.findViewById(R.id.txtWorstDay);

        textPrimaryColor = ContextCompat.getColor(requireContext(), R.color.health_text_primary);
        textSecondaryColor = ContextCompat.getColor(requireContext(), R.color.health_text_secondary);
        gridColor = ContextCompat.getColor(requireContext(), R.color.date_chip_stroke);
        apiDateFormat.setLenient(false);

        repository = new MoodRepository(requireContext());

        loadData();

        return view;
    }

    private void loadData() {
        repository.getAllMoods(list -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (list == null || list.isEmpty()) {
                    showEmptyState();
                    return;
                }
                updateStats(list);
                setupWeeklyChart(list);
                setupMonthlyChart(list);
            });
        });
    }

    private void showEmptyState() {
        txtAvgMood.setText("Avg\n--");
        txtBestDay.setText("Best\n--");
        txtWorstDay.setText("Worst\n--");
        showNoData(lineChartWeekly, "No mood data for last 7 days");
        showNoData(lineChartMonthly, "No mood data for this month");
    }

    private void showNoData(LineChart chart, String message) {
        chart.clear();
        chart.setNoDataText(message);
        chart.setNoDataTextColor(textSecondaryColor);
        chart.setMarker(null);
        chart.invalidate();
    }

    private void updateStats(List<MoodEntity> list) {
        float total = 0;
        MoodEntity best = list.get(0);
        MoodEntity worst = list.get(0);

        for (MoodEntity m : list) {
            total += m.moodLevel;
            if (m.moodLevel > best.moodLevel) best = m;
            if (m.moodLevel < worst.moodLevel) worst = m;
        }

        float avg = total / list.size();
        String emoji = getEmoji(Math.round(avg));

        txtAvgMood.setText("Avg\n" + emoji + " " + String.format(Locale.US, "%.1f", avg));
        txtBestDay.setText("Best\n" + getDayName(best.date) + " " + getEmoji(best.moodLevel));
        txtWorstDay.setText("Worst\n" + getDayName(worst.date) + " " + getEmoji(worst.moodLevel));
    }

    private void setupWeeklyChart(List<MoodEntity> allMoods) {
        Calendar end = Calendar.getInstance();
        resetTime(end);
        Calendar start = (Calendar) end.clone();
        start.add(Calendar.DAY_OF_MONTH, -6);

        Map<String, float[]> statsByDate = new HashMap<>();
        for (MoodEntity mood : allMoods) {
            Date moodDate = parseDate(mood.date);
            if (moodDate == null) continue;

            if (!moodDate.before(start.getTime()) && !moodDate.after(end.getTime())) {
                float[] stats = statsByDate.get(mood.date);
                if (stats == null) {
                    stats = new float[]{0f, 0f};
                    statsByDate.put(mood.date, stats);
                }
                stats[0] += mood.moodLevel; // sum
                stats[1] += 1f;             // count
            }
        }

        if (statsByDate.isEmpty()) {
            showNoData(lineChartWeekly, "No mood data for last 7 days");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        Calendar cursor = (Calendar) start.clone();
        int index = 0;
        while (!cursor.after(end)) {
            String dateKey = apiDateFormat.format(cursor.getTime());
            float[] stats = statsByDate.get(dateKey);
            float avg = (stats == null || stats[1] == 0f) ? 0f : (stats[0] / stats[1]);
            entries.add(new Entry(index, avg));
            xLabels.add(dayLabelFormat.format(cursor.getTime()));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
            index++;
        }

        configureLineChart(
                lineChartWeekly,
                entries,
                xLabels,
                Color.parseColor("#4F46E5"),
                true
        );
    }

    private void setupMonthlyChart(List<MoodEntity> allMoods) {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        TreeMap<Integer, float[]> dayStats = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        for (MoodEntity mood : allMoods) {
            Date date = parseDate(mood.date);
            if (date == null) continue;

            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) != currentYear || calendar.get(Calendar.MONTH) != currentMonth) {
                continue;
            }

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            float[] stats = dayStats.get(day);
            if (stats == null) {
                stats = new float[]{0f, 0f};
                dayStats.put(day, stats);
            }
            stats[0] += mood.moodLevel; // sum
            stats[1] += 1f;             // count
        }

        if (dayStats.isEmpty()) {
            showNoData(lineChartMonthly, "No mood data for this month");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        List<String> markerDates = new ArrayList<>();
        int index = 0;
        for (Map.Entry<Integer, float[]> item : dayStats.entrySet()) {
            int day = item.getKey();
            float[] stats = item.getValue();
            float avg = stats[1] == 0f ? 0f : (stats[0] / stats[1]);

            entries.add(new Entry(index, avg));
            xLabels.add(String.valueOf(day));
            markerDates.add(String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth + 1, day));
            index++;
        }

        configureLineChart(
                lineChartMonthly,
                entries,
                xLabels,
                Color.parseColor("#10B981"),
                false
        );

        MoodValueMarker marker = new MoodValueMarker(requireContext(), markerDates, "Avg");
        marker.setChartView(lineChartMonthly);
        lineChartMonthly.setMarker(marker);
    }

    private void configureLineChart(
            LineChart chart,
            List<Entry> entries,
            List<String> xLabels,
            int lineColor,
            boolean smoothLine
    ) {
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setMode(smoothLine ? LineDataSet.Mode.CUBIC_BEZIER : LineDataSet.Mode.LINEAR);
        dataSet.setColor(lineColor);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(lineColor);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(textPrimaryColor);
        dataSet.setValueTextSize(10f);
        dataSet.setHighLightColor(textPrimaryColor);
        dataSet.setDrawHorizontalHighlightIndicator(false);

        LineData data = new LineData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format(Locale.US, "%.1f", entry.getY());
            }
        });
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textPrimaryColor);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setAxisMaximum(5.5f);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisLeft().setTextColor(textPrimaryColor);
        chart.getAxisLeft().setGridColor(gridColor);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setNoDataTextColor(textSecondaryColor);
        chart.setDrawGridBackground(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setHighlightPerDragEnabled(true);

        chart.animateX(700, Easing.EaseInOutQuad);
        chart.invalidate();
    }

    private Date parseDate(String dateStr) {
        try {
            return apiDateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private void resetTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String getEmoji(int level) {
        if (level >= 5) return "🤩";
        if (level >= 4) return "🙂";
        if (level >= 3) return "😐";
        if (level >= 2) return "😔";
        return "😫";
    }

    private String getDayName(String dateStr) {
        try {
            Date date = apiDateFormat.parse(dateStr);
            return new SimpleDateFormat("EEE", Locale.getDefault()).format(date);
        } catch (Exception e) { return ""; }
    }
}
