package com.kunal.healthkriya.ui.mood;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kunal.healthkriya.ui.mood.entry.EntryFragment;
import com.kunal.healthkriya.ui.mood.journal.JournalFragment;
import com.kunal.healthkriya.ui.mood.analytics.AnalyticsFragment;

public class MoodPagerAdapter extends FragmentStateAdapter {



    private final Bundle bundle;

    public MoodPagerAdapter(@NonNull FragmentActivity fa, Bundle bundle) {
        super(fa);
        this.bundle = bundle;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) fragment = new EntryFragment();
        else if (position == 1) fragment = new JournalFragment();
        else fragment = new AnalyticsFragment();

        fragment.setArguments(bundle);
        return fragment;

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
