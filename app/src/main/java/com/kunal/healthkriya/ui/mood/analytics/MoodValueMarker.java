package com.kunal.healthkriya.ui.mood.analytics;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.kunal.healthkriya.R;

import java.util.List;
import java.util.Locale;

public class MoodValueMarker extends MarkerView {

    private final TextView txtMarker;
    private final List<String> dateLabels;
    private final String valueLabel;

    public MoodValueMarker(Context context, List<String> dateLabels, String valueLabel) {
        super(context, R.layout.view_chart_marker);
        this.dateLabels = dateLabels;
        this.valueLabel = valueLabel;
        this.txtMarker = findViewById(R.id.txtMarker);
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        int index = Math.round(entry.getX());
        String date = (index >= 0 && index < dateLabels.size()) ? dateLabels.get(index) : "--";
        txtMarker.setText(String.format(Locale.US, "%s\n%s: %.1f", date, valueLabel, entry.getY()));
        super.refreshContent(entry, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 12f);
    }
}
