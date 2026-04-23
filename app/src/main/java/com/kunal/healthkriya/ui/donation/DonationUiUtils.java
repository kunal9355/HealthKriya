package com.kunal.healthkriya.ui.donation;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;

public final class DonationUiUtils {

    private DonationUiUtils() {
    }

    public static String buildTitle(DonationEntity item) {
        if (item == null) {
            return "Donation";
        }
        if (hasText(item.title)) {
            return item.title.trim();
        }
        if (DonationEntity.TYPE_BLOOD.equals(item.type)) {
            if (hasText(item.bloodGroup)) {
                return "Blood " + item.bloodGroup.trim();
            }
            return "Blood Support";
        }
        if (hasText(item.medicineName)) {
            return item.medicineName.trim();
        }
        return "Medicine Support";
    }

    public static String buildTypeLabel(DonationEntity item) {
        if (item == null) {
            return "Support";
        }
        if (DonationEntity.TYPE_BLOOD.equals(item.type)) {
            return "Blood";
        }
        return "Medicine";
    }

    public static String buildLocation(DonationEntity item) {
        if (item == null || !hasText(item.city)) {
            return "Location pending";
        }
        return item.city.trim();
    }

    public static String buildRelativeTime(DonationEntity item) {
        if (item == null) {
            return "--";
        }
        long time = item.updatedAt > 0 ? item.updatedAt : item.createdAt;
        if (time <= 0) {
            return "--";
        }
        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }

    public static String actionLabel(String action) {
        if (DonationEntity.ACTION_DONATE.equals(action)) {
            return "Donate";
        }
        return "Request";
    }

    public static String urgencyLabel(String urgency) {
        if (DonationEntity.URGENCY_CRITICAL.equals(urgency)) {
            return "Critical";
        }
        if (DonationEntity.URGENCY_HIGH.equals(urgency)) {
            return "High";
        }
        return "Normal";
    }

    public static String statusLabel(String status) {
        if (DonationEntity.STATUS_IN_PROGRESS.equals(status)) {
            return "In Progress";
        }
        if (DonationEntity.STATUS_COMPLETED.equals(status)) {
            return "Completed";
        }
        if (DonationEntity.STATUS_CANCELLED.equals(status)) {
            return "Cancelled";
        }
        if (DonationEntity.STATUS_EXPIRED.equals(status)) {
            return "Expired";
        }
        return "Active";
    }

    public static void applyActionChip(TextView view, String action) {
        Context context = view.getContext();
        if (DonationEntity.ACTION_DONATE.equals(action)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_donate_badge_bg),
                    ContextCompat.getColor(context, R.color.donation_donate_badge_text)
            );
        } else {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_request_badge_bg),
                    ContextCompat.getColor(context, R.color.donation_request_badge_text)
            );
        }
    }

    public static void applyStatusChip(TextView view, String status) {
        Context context = view.getContext();
        if (DonationEntity.STATUS_COMPLETED.equals(status)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_status_completed_bg),
                    ContextCompat.getColor(context, R.color.donation_status_completed_text)
            );
        } else if (DonationEntity.STATUS_CANCELLED.equals(status)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_status_cancelled_bg),
                    ContextCompat.getColor(context, R.color.donation_status_cancelled_text)
            );
        } else if (DonationEntity.STATUS_EXPIRED.equals(status)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_status_expired_bg),
                    ContextCompat.getColor(context, R.color.donation_status_expired_text)
            );
        } else if (DonationEntity.STATUS_IN_PROGRESS.equals(status)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_status_progress_bg),
                    ContextCompat.getColor(context, R.color.donation_status_progress_text)
            );
        } else {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_status_active_bg),
                    ContextCompat.getColor(context, R.color.donation_status_active_text)
            );
        }
    }

    public static void applyUrgencyChip(TextView view, String urgency) {
        Context context = view.getContext();
        if (DonationEntity.URGENCY_CRITICAL.equals(urgency)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_urgency_critical_bg),
                    ContextCompat.getColor(context, R.color.donation_urgency_critical_text)
            );
        } else if (DonationEntity.URGENCY_HIGH.equals(urgency)) {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_urgency_high_bg),
                    ContextCompat.getColor(context, R.color.donation_urgency_high_text)
            );
        } else {
            applyChip(
                    view,
                    ContextCompat.getColor(context, R.color.donation_hint_surface),
                    ContextCompat.getColor(context, R.color.health_text_secondary)
            );
        }
    }

    public static int typeAccentColor(Context context, String type) {
        if (DonationEntity.TYPE_BLOOD.equals(type)) {
            return ContextCompat.getColor(context, R.color.donation_blood_text);
        }
        return ContextCompat.getColor(context, R.color.donation_medicine_text);
    }

    public static int typeSurfaceColor(Context context, String type) {
        if (DonationEntity.TYPE_BLOOD.equals(type)) {
            return ContextCompat.getColor(context, R.color.donation_blood_bg);
        }
        return ContextCompat.getColor(context, R.color.donation_medicine_bg);
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static void applyMatchChip(TextView view) {
        Context context = view.getContext();
        applyChip(
                view,
                ContextCompat.getColor(context, R.color.donation_match_bg),
                ContextCompat.getColor(context, R.color.donation_match_text)
        );
    }

    private static void applyChip(TextView view, int bgColor, int textColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(view.getResources().getDisplayMetrics().density * 999f);
        drawable.setColor(bgColor);
        view.setBackground(drawable);
        view.setTextColor(textColor);
    }
}
