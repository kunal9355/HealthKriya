package com.kunal.healthkriya.ui.mood;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kunal.healthkriya.ui.mood.entry.EntryFragment;
import com.kunal.healthkriya.ui.mood.journal.JournalFragment;
import com.kunal.healthkriya.ui.mood.analytics.AnalyticsFragment;

public class MoodPagerAdapter extends FragmentStateAdapter {

    public MoodPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new EntryFragment();
        if (position == 1) return new JournalFragment();
        return new AnalyticsFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
