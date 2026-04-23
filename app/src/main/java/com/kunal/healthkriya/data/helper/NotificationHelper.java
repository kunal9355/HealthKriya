package com.kunal.healthkriya.data.helper;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.kunal.healthkriya.MainActivity;
import com.kunal.healthkriya.R;
import com.kunal.healthkriya.data.local.donation.DonationEntity;
import com.kunal.healthkriya.ui.donation.DonationUiUtils;

public final class NotificationHelper {

    private static final String CHANNEL_DONATION = "donation_alerts";
    private static final String PREF = "donation_notifications";
    private static final String KEY_EVENT_PREFIX = "event_";

    private NotificationHelper() {
    }

    public static void initialize(Context context) {
        if (context == null) {
            return;
        }
        ensureChannel(context.getApplicationContext());
    }

    public static void notifyNewRequest(Context context, DonationEntity donation) {
        if (context == null || donation == null) {
            return;
        }

        String eventKey = "new_request_" + donation.clientId + "_" + donation.updatedAt;
        if (!shouldNotify(context, eventKey, 10_000L)) {
            return;
        }

        boolean critical = DonationEntity.URGENCY_CRITICAL.equals(donation.urgency);
        String title = critical ? "Urgent request near you" : "New public request nearby";
        String text = DonationUiUtils.buildTitle(donation) + " in " + DonationUiUtils.buildLocation(donation);
        post(context, title, text, eventKey.hashCode());
    }

    public static void notifyHelpResponse(Context context, String requestId, String helperId) {
        if (context == null) {
            return;
        }
        String safeRequestId = requestId != null ? requestId : "request";
        String safeHelperId = helperId != null ? helperId : "helper";
        String eventKey = "help_response_" + safeRequestId + "_" + safeHelperId;
        if (!shouldNotify(context, eventKey, 10_000L)) {
            return;
        }

        post(
                context,
                "Someone is ready to help you",
                "A helper responded to your request.",
                eventKey.hashCode()
        );
    }

    public static void notifyStatusUpdate(Context context, String requestId, String status) {
        if (context == null) {
            return;
        }
        String safeRequestId = requestId != null ? requestId : "request";
        String safeStatus = status != null ? status : "updated";
        String eventKey = "status_update_" + safeRequestId + "_" + safeStatus;
        if (!shouldNotify(context, eventKey, 10_000L)) {
            return;
        }

        String text;
        if (DonationEntity.STATUS_COMPLETED.equalsIgnoreCase(safeStatus)) {
            text = "Your request is completed.";
        } else if (DonationEntity.STATUS_CANCELLED.equalsIgnoreCase(safeStatus)) {
            text = "Your request has been cancelled.";
        } else if (DonationEntity.STATUS_EXPIRED.equalsIgnoreCase(safeStatus)) {
            text = "Your request expired due to inactivity.";
        } else {
            text = "Your request status was updated.";
        }

        post(context, "Donation status update", text, eventKey.hashCode());
    }

    private static void post(Context context, String title, String text, int notificationId) {
        Context appContext = context.getApplicationContext();
        ensureChannel(appContext);
        if (!hasNotificationPermission(appContext)) {
            return;
        }

        Intent openAppIntent = new Intent(appContext, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                appContext,
                Math.abs(notificationId),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_DONATION)
                .setSmallIcon(R.drawable.ic_medical)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(appContext).notify(Math.abs(notificationId), builder.build());
    }

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        NotificationChannel channel = manager.getNotificationChannel(CHANNEL_DONATION);
        if (channel != null) {
            return;
        }

        NotificationChannel createdChannel = new NotificationChannel(
                CHANNEL_DONATION,
                "Donation Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        createdChannel.setDescription("Realtime donation and help updates");
        manager.createNotificationChannel(createdChannel);
    }

    private static boolean shouldNotify(Context context, String eventKey, long minIntervalMs) {
        long now = System.currentTimeMillis();
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
        long lastTs = prefs.getLong(KEY_EVENT_PREFIX + eventKey, 0L);
        if (now - lastTs < minIntervalMs) {
            return false;
        }
        prefs.edit().putLong(KEY_EVENT_PREFIX + eventKey, now).apply();
        return true;
    }

    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
