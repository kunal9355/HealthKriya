package com.kunal.healthkriya.ui.onboarding;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class OnboardingViewModel extends ViewModel {

    public static class Page {
        public final int imageRes;
        public final String title;
        public final String desc;

        public Page(int imageRes, String title, String desc) {
            this.imageRes = imageRes;
            this.title = title;
            this.desc = desc;
        }
    }

    public List<Page> getPages() {
        List<Page> pages = new ArrayList<>();
        pages.add(new Page(
                com.kunal.healthkriya.R.drawable.ic_onboard_health,
                "All Health in One Place",
                "Track health, records and essentials securely in one app."
        ));
        pages.add(new Page(
                com.kunal.healthkriya.R.drawable.ic_onboard_reminder,
                "Smart Reminders",
                "Never miss medicines, checkups or important alerts."
        ));
        return pages;
    }
}
