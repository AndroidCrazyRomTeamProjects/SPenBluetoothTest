package com.samsung.remotespen.main.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.main.notification.NotificationController;
import com.samsung.remotespen.util.BatteryPolicy;
import com.samsung.util.debug.Assert;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class UnbundledSpenNotificationController extends NotificationController.Controller {
    private static final int NOTIFICATION_ID_UNBUNDLED_CONNECTION_ERROR = 18873;
    private static final int NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE = 18872;
    private static final int NOTIFICATION_ID_UNBUNDLED_LOWBATTERY = 18871;
    private static final String TAG = "UnbundledSpenNotificationController";
    private Context mContext;
    private NotificationCompat.Builder mFirmwareNotificationBuilder;
    private PendingIntent mLastPendingIntent;
    private NotificationController.NotificationType mLastShowingNotification;
    private NotificationManager mNotiMgr;

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showConnectionFailNotification(PendingIntent pendingIntent) {
    }

    public UnbundledSpenNotificationController(Context context) {
        this.mContext = context;
        this.mNotiMgr = (NotificationManager) context.getSystemService("notification");
        init();
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void cancelAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.cancel(NOTIFICATION_ID_UNBUNDLED_LOWBATTERY);
        notificationManager.cancel(NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE);
        notificationManager.cancel(NOTIFICATION_ID_UNBUNDLED_CONNECTION_ERROR);
        init();
    }

    private void init() {
        this.mFirmwareNotificationBuilder = null;
        setLastNotificationInfo(NotificationController.NotificationType.HIDED, null);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, PendingIntent pendingIntent) {
        NotificationCompat.Builder progress = new NotificationCompat.Builder(this.mContext, NotificationController.NOTIFICATION_FIRMWARE_UPGRADE_CHANNEL).setSmallIcon(R.drawable.stat_notify_spen_notice).setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomContentView(new RemoteViews(this.mContext.getPackageName(), (int) R.layout.firmware_upgrade_notification_layout)).setOnlyAlertOnce(true).setShowWhen(false).setContentIntent(pendingIntent).setProgress(100, 0, false);
        this.mFirmwareNotificationBuilder = progress;
        Notification build = progress.build();
        build.flags |= 32;
        showNotification(build, NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE);
        setLastNotificationInfo(NotificationController.NotificationType.FIRMWARE_UPGRADE, pendingIntent);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void updateFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, int i, PendingIntent pendingIntent) {
        if (this.mFirmwareNotificationBuilder == null) {
            showFirmwareUpgradeNotification(bleSpenInstanceId, pendingIntent);
        }
        this.mFirmwareNotificationBuilder.setProgress(100, i, false);
        Notification build = this.mFirmwareNotificationBuilder.build();
        build.flags |= 32;
        showNotification(build, NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showDisconnectedNotification(PendingIntent pendingIntent) {
        if (isLowBatteryNotificationExist()) {
            cancelLowBatteryNotification();
        }
        showNotification(createNotification(this.mContext, getString(R.string.s_pen_pro_disconnected), R.drawable.stat_notify_pen_disconnected, R.string.remotespen_manual_pairing_reconnection, pendingIntent, NotificationController.NOTIFICATION_CONNECTION_ERROR_CHANNEL, null), NOTIFICATION_ID_UNBUNDLED_CONNECTION_ERROR);
        setLastNotificationInfo(NotificationController.NotificationType.DISCONNECTED, pendingIntent);
    }

    /* renamed from: com.samsung.remotespen.main.notification.UnbundledSpenNotificationController$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType;

        static {
            int[] iArr = new int[NotificationController.NotificationType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType = iArr;
            try {
                iArr[NotificationController.NotificationType.FIRMWARE_UPGRADE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[NotificationController.NotificationType.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[NotificationController.NotificationType.HIDED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void cancelLastShowingNotification() {
        if (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[this.mLastShowingNotification.ordinal()] == 1) {
            cancelFirmwareUpgradeNotification();
        } else {
            cancelDisconnectedNotification();
        }
        setLastNotificationInfo(NotificationController.NotificationType.HIDED, null);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void refreshLastShowingNotification() {
        if (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[this.mLastShowingNotification.ordinal()] != 2) {
            return;
        }
        showDisconnectedNotification(this.mLastPendingIntent);
    }

    private void setLastNotificationInfo(NotificationController.NotificationType notificationType, PendingIntent pendingIntent) {
        this.mLastPendingIntent = pendingIntent;
        this.mLastShowingNotification = notificationType;
    }

    private void cancelFirmwareUpgradeNotification() {
        this.mFirmwareNotificationBuilder = null;
        this.mNotiMgr.cancel(NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE);
    }

    private void cancelDisconnectedNotification() {
        this.mNotiMgr.cancel(NOTIFICATION_ID_UNBUNDLED_CONNECTION_ERROR);
    }

    private void cancelLowBatteryNotification() {
        this.mNotiMgr.cancel(NOTIFICATION_ID_UNBUNDLED_LOWBATTERY);
    }

    private void showNotification(Notification notification, int i) {
        String string;
        Assert.notNull(notification);
        if (i == NOTIFICATION_ID_UNBUNDLED_FIRMWARE_UPGRADE) {
            string = this.mContext.getString(R.string.pen_settings_title);
        } else {
            string = this.mContext.getString(R.string.air_action_title);
        }
        notification.extras.putCharSequence("android.substName", string);
        this.mNotiMgr.notify(i, notification);
    }

    private String getString(int i) {
        return this.mContext.getString(i);
    }

    private boolean isLowBatteryNotificationExist() {
        StatusBarNotification[] activeNotifications;
        for (StatusBarNotification statusBarNotification : this.mNotiMgr.getActiveNotifications()) {
            if (this.mLastShowingNotification == NotificationController.NotificationType.LOW_BATTERY && statusBarNotification.getId() == NOTIFICATION_ID_UNBUNDLED_LOWBATTERY) {
                Log.d(TAG, "isLowBatteryNotificationExist : true");
                return true;
            }
        }
        return false;
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showLowBatteryNotification(int i, boolean z, PendingIntent pendingIntent) {
        String str = TAG;
        Log.i(str, "showLowBatteryNotification : batteryLevel is " + i);
        if (!BatteryPolicy.isLessThanLowBatteryThreshold(i, false) || z || isLowBatteryNotificationExist()) {
            cancelDisconnectedNotification();
            Context context = this.mContext;
            showNotification(createNotification(context, context.getString(R.string.remotespen_noti_title_low_battery_unbunlded, BatteryPolicy.getPercentageString(i)), R.drawable.stat_notify_spen_notice, R.string.remotespen_noti_desc_low_battery_unbunlded, pendingIntent, NotificationController.NOTIFICATION_LOW_BATTERY_CHANNEL, null), NOTIFICATION_ID_UNBUNDLED_LOWBATTERY);
            setLastNotificationInfo(NotificationController.NotificationType.LOW_BATTERY, pendingIntent);
        }
    }
}
