package com.samsung.remotespen.main.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.crazyromteam.spenbletest.utils.Assert;

/* loaded from: classes.dex */
public class NotificationController {
    public static final String NOTIFICATION_CONNECTION_ERROR_CHANNEL = "blespensvc_connection_error";
    public static final String NOTIFICATION_FIRMWARE_UPGRADE_CHANNEL = "blespensvc_firmware";
    public static final String NOTIFICATION_LOW_BATTERY_CHANNEL = "blespensvc_lowbattery";
    private static final long[] SEM_VIBRATION_COMMON_E = {0, 10, 60, 15};
    private static final String TAG = "NotificationController";
    private static Controller sBundledInstance;
    private static Controller sUnbundledInstance;

    /* loaded from: classes.dex */
    public enum NotificationType {
        LOW_BATTERY,
        CONNECTION_FAILED,
        DISCONNECTED,
        FIRMWARE_UPGRADE,
        HIDED
    }

    /* loaded from: classes.dex */
    public static abstract class Controller {
        public abstract void cancelAllNotifications(Context context);

        public abstract void cancelLastShowingNotification();

        public abstract void refreshLastShowingNotification();

        public abstract void showConnectionFailNotification(PendingIntent pendingIntent);

        public abstract void showDisconnectedNotification(PendingIntent pendingIntent);

        public abstract void showFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, PendingIntent pendingIntent);

        public abstract void showLowBatteryNotification(int i, boolean z, PendingIntent pendingIntent);

        public abstract void updateFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, int i, PendingIntent pendingIntent);

        public Notification createNotification(Context context, String str, int i, int i2, PendingIntent pendingIntent, String str2, Notification.Action action) {
            Assert.notNull(str);
            Assert.e(i > 0);
            Assert.e(i2 > 0);
            if (pendingIntent == null) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 67108864);
            }
            Bundle bundle = new Bundle();
            bundle.putCharSequence("dex_exclude", "dual");
            Notification.Builder contentIntent = new Notification.Builder(context, str2).setContentTitle(str).setSmallIcon(i).setAutoCancel(true).setShowWhen(true).addExtras(bundle).setContentIntent(pendingIntent);
            if (action != null) {
                contentIntent.addAction(action);
            }
            if (i2 > 0) {
                String string = context.getString(i2);
                contentIntent.setContentText(string);
                contentIntent.setStyle(new Notification.BigTextStyle().bigText(string));
            }
            return contentIntent.build();
        }
    }

    public static synchronized Controller getController(Context context, BleSpenInstanceId bleSpenInstanceId) {
        synchronized (NotificationController.class) {
            if (bleSpenInstanceId.isBundledSpen()) {
                if (sBundledInstance == null) {
                    sBundledInstance = new BundledSpenNotificationController(context);
                }
                return sBundledInstance;
            }
            if (sUnbundledInstance == null) {
                sUnbundledInstance = new UnbundledSpenNotificationController(context);
            }
            return sUnbundledInstance;
        }
    }

    public static void createAllNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(createLowBatteryChannel(context));
        notificationManager.createNotificationChannel(createConnectionErrorChannel(context));
        notificationManager.createNotificationChannel(createFirmwareUpgradeChannel(context));
    }

    public static void cancelAllNotifications(Context context) {
        Controller controller = sBundledInstance;
        if (controller != null) {
            controller.cancelAllNotifications(context);
        }
        Controller controller2 = sUnbundledInstance;
        if (controller2 != null) {
            controller2.cancelAllNotifications(context);
        }
    }

    private static NotificationChannel createLowBatteryChannel(Context context) {
        return new NotificationChannel(NOTIFICATION_LOW_BATTERY_CHANNEL, context.getString(R.string.remotespen_noti_channel_low_battery), 2);
    }

    private static NotificationChannel createConnectionErrorChannel(Context context) {
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CONNECTION_ERROR_CHANNEL, context.getString(R.string.remotespen_noti_channel_disconnect), 3);
        notificationChannel.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/raw/pen_disconnected"), Notification.AUDIO_ATTRIBUTES_DEFAULT);
        notificationChannel.setVibrationPattern(SEM_VIBRATION_COMMON_E);
        return notificationChannel;
    }

    private static NotificationChannel createFirmwareUpgradeChannel(Context context) {
        return new NotificationChannel(NOTIFICATION_FIRMWARE_UPGRADE_CHANNEL, context.getString(R.string.remotespen_noti_channel_miscellaneous), 2);
    }
}
