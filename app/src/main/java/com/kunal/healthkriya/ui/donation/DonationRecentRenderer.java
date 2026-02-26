package com.kunal.healthkriya.ui.donation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public final class DonationRecentRenderer {

    private DonationRecentRenderer() {
    }

    public static void render(
            Context context,
            LayoutInflater inflater,
            List<DonationEntity> entries,
            LinearLayout container,
            TextView emptyView
    ) {
        container.removeAllViews();
        boolean hasEntries = entries != null && !entries.isEmpty();
        emptyView.setVisibility(hasEntries ? View.GONE : View.VISIBLE);
        if (!hasEntries) {
            return;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        for (DonationEntity entry : entries) {
            View item = inflater.inflate(R.layout.item_donation_recent_entry, container, false);
            TextView tvTitle = item.findViewById(R.id.tvRecentTitle);
            TextView tvDetail = item.findViewById(R.id.tvRecentDetail);
            TextView tvMeta = item.findViewById(R.id.tvRecentMeta);

            tvTitle.setText(entry.title);
            tvDetail.setText(entry.detail);
            tvMeta.setText(timeFormat.format(entry.updatedAt) + "  •  " + getSyncLabel(entry.syncStatus));
            tvMeta.setTextColor(getSyncColor(context, entry.syncStatus));

            container.addView(item);
        }
    }

    private static String getSyncLabel(int status) {
        if (status == DonationEntity.SYNC_SYNCED) {
            return "Synced";
        }
        if (status == DonationEntity.SYNC_ERROR) {
            return "Error";
        }
        return "Pending";
    }

    private static int getSyncColor(Context context, int status) {
        if (status == DonationEntity.SYNC_SYNCED) {
            return context.getColor(R.color.journal_sync_synced_text);
        }
        if (status == DonationEntity.SYNC_ERROR) {
            return context.getColor(R.color.journal_sync_error_text);
        }
        return context.getColor(R.color.journal_sync_pending_text);
    }
}
