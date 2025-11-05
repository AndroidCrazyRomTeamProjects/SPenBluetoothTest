package com.samsung.remotespen.main.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.main.notification.NotificationController;
import com.samsung.remotespen.util.BatteryPolicy;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.usage.SAUtils;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class BundledSpenNotificationController extends NotificationController.Controller {
    private static final int NOTIFICATION_ID_CONNECTION_ERROR = 8873;
    private static final int NOTIFICATION_ID_LOWBATTERY = 8871;
    private static final String TAG = "BundledSpenNotificationController";
    private Context mContext;
    private PendingIntent mLastPendingIntent = null;
    private NotificationController.NotificationType mLastShowingNotification = NotificationController.NotificationType.HIDED;
    private NotificationManager mNotiMgr;

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, PendingIntent pendingIntent) {
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void updateFirmwareUpgradeNotification(BleSpenInstanceId bleSpenInstanceId, int i, PendingIntent pendingIntent) {
    }

    public BundledSpenNotificationController(Context context) {
        this.mContext = context;
        this.mNotiMgr = (NotificationManager) context.getSystemService("notification");
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void cancelAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.cancel(NOTIFICATION_ID_LOWBATTERY);
        notificationManager.cancel(NOTIFICATION_ID_CONNECTION_ERROR);
        setLastNotificationInfo(NotificationController.NotificationType.HIDED, null);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showLowBatteryNotification(int i, boolean z, PendingIntent pendingIntent) {
        String str = TAG;
        Log.i(str, "showLowBatteryNotification : batteryLevel is " + i);
        if (BatteryPolicy.isLessThanLowBatteryThreshold(i, true) && !z && isLowBatteryNotificationExist()) {
            return;
        }
        Context context = this.mContext;
        showNotification(createNotification(context, context.getString(R.string.remotespen_noti_title_low_battery, Integer.valueOf(i)), R.drawable.stat_notify_spen_notice, CommonUtils.isPenAttachModel(this.mContext) ? R.string.remotespen_noti_desc_low_battery_attach : R.string.remotespen_noti_desc_low_battery, pendingIntent, NotificationController.NOTIFICATION_LOW_BATTERY_CHANNEL, null), NOTIFICATION_ID_LOWBATTERY);
        setLastNotificationInfo(NotificationController.NotificationType.LOW_BATTERY, pendingIntent);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showDisconnectedNotification(PendingIntent pendingIntent) {
        Log.i(TAG, "showDisconnectedNotification");
        if (isLowBatteryNotificationExist()) {
            cancelLowBatteryNotification();
        }
        showNotification(createNotification(this.mContext, getString(R.string.remotespen_noti_channel_disconnect), R.drawable.stat_notify_pen_disconnected, CommonUtils.isPenAttachModel(this.mContext) ? R.string.remotespen_noti_desc_disconnected_attach : SettingsPreferenceManager.getInstance(this.mContext).getSpenUnlockEnabled() ? R.string.remotespen_noti_desc_disconnected_insert_spen_to_reconnect_spen_unlock : R.string.remotespen_noti_desc_disconnected_insert_spen_to_reconnect, pendingIntent, NotificationController.NOTIFICATION_CONNECTION_ERROR_CHANNEL, null), NOTIFICATION_ID_CONNECTION_ERROR);
        setLastNotificationInfo(NotificationController.NotificationType.DISCONNECTED, pendingIntent);
        Long removedSystemClockTime = SpenInsertionEventDetector.getInstance(this.mContext).getRemovedSystemClockTime();
        SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_DISCONNECTED_ALARM, null, removedSystemClockTime == null ? null : Long.valueOf(SystemClock.elapsedRealtime() - removedSystemClockTime.longValue()));
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void showConnectionFailNotification(PendingIntent pendingIntent) {
        Log.i(TAG, "showConnectionFailNotification");
        if (isLowBatteryNotificationExist()) {
            cancelLowBatteryNotification();
        }
        showNotification(createNotification(this.mContext, getString(R.string.remotespen_noti_channel_disconnect), R.drawable.stat_notify_pen_disconnected, R.string.remotespen_noti_desc_connection_failed_tap_to_reconnect, pendingIntent, NotificationController.NOTIFICATION_CONNECTION_ERROR_CHANNEL, null), NOTIFICATION_ID_CONNECTION_ERROR);
        setLastNotificationInfo(NotificationController.NotificationType.CONNECTION_FAILED, pendingIntent);
        SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_RECONNECT_ALARM, null, null);
    }

    /* renamed from: com.samsung.remotespen.main.notification.BundledSpenNotificationController$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType;

        static {
            int[] iArr = new int[NotificationController.NotificationType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType = iArr;
            try {
                iArr[NotificationController.NotificationType.LOW_BATTERY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[NotificationController.NotificationType.CONNECTION_FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[NotificationController.NotificationType.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[NotificationController.NotificationType.HIDED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void cancelLastShowingNotification() {
        if (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[this.mLastShowingNotification.ordinal()] == 1) {
            cancelLowBatteryNotification();
        } else {
            cancelConnectionErrorChannel();
        }
        setLastNotificationInfo(NotificationController.NotificationType.HIDED, null);
    }

    @Override // com.samsung.remotespen.main.notification.NotificationController.Controller
    public void refreshLastShowingNotification() {
        BleSpenInstanceId bundledSpenInstanceId = BleSpenPairedSpenManager.getInstance(this.mContext).getBundledSpenInstanceId();
        BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
        Assert.notNull(bleSpenManager);
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$main$notification$NotificationController$NotificationType[this.mLastShowingNotification.ordinal()];
        if (i == 1) {
            showLowBatteryNotification(bleSpenManager.getBatteryLevel(bundledSpenInstanceId), false, this.mLastPendingIntent);
        } else if (i == 2) {
            showConnectionFailNotification(this.mLastPendingIntent);
        } else if (i == 3 && !bleSpenManager.isDisconnectByStandbyMode(bundledSpenInstanceId)) {
            showDisconnectedNotification(this.mLastPendingIntent);
        }
    }

    private void setLastNotificationInfo(NotificationController.NotificationType notificationType, PendingIntent pendingIntent) {
        this.mLastPendingIntent = pendingIntent;
        this.mLastShowingNotification = notificationType;
    }

    private void cancelLowBatteryNotification() {
        this.mNotiMgr.cancel(NOTIFICATION_ID_LOWBATTERY);
    }

    private void cancelConnectionErrorChannel() {
        this.mNotiMgr.cancel(NOTIFICATION_ID_CONNECTION_ERROR);
    }

    private boolean isLowBatteryNotificationExist() {
        for (StatusBarNotification statusBarNotification : this.mNotiMgr.getActiveNotifications()) {
            if (statusBarNotification.getId() == NOTIFICATION_ID_LOWBATTERY) {
                Log.d(TAG, "isLowBatteryNotificationExist : true");
                return true;
            }
        }
        return false;
    }

    private void showNotification(Notification notification, int i) {
        Assert.notNull(notification);
        notification.extras.putCharSequence("android.substName", this.mContext.getString(R.string.air_action_title));
        this.mNotiMgr.notify(i, notification);
    }

    private String getString(int i) {
        return this.mContext.getString(i);
    }
}
