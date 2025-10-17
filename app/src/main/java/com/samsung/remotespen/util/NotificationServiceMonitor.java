package com.samsung.remotespen.util;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class NotificationServiceMonitor extends NotificationListenerService {
    public static final String TAG = NotificationServiceMonitor.class.getSimpleName();
    public static final int TRIM_LIGHT = 1;
    private UpdateListener mUpdateListener;

    /* loaded from: classes.dex */
    public interface UpdateListener {
        void onPosted(StatusBarNotification statusBarNotification);

        void onRemoved(StatusBarNotification statusBarNotification);
    }

    public void registerListener(UpdateListener updateListener) {
        if (this.mUpdateListener == null) {
            this.mUpdateListener = updateListener;
            return;
        }
        Log.e(TAG, "registerListener : fail to register listener because listener is existed.");
        Assert.fail("Fail to register listener");
    }

    public void unregisterListener() {
        this.mUpdateListener = null;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        super.onNotificationPosted(statusBarNotification);
        UpdateListener updateListener = this.mUpdateListener;
        if (updateListener != null) {
            updateListener.onPosted(statusBarNotification);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        super.onNotificationRemoved(statusBarNotification);
        UpdateListener updateListener = this.mUpdateListener;
        if (updateListener != null) {
            updateListener.onRemoved(statusBarNotification);
        }
    }

    @Override // android.service.notification.NotificationListenerService
    public void onListenerConnected() {
        Log.v(TAG, "onListenerConnected");
        setOnNotificationPostedTrimReflection();
        super.onListenerConnected();
    }

    @Override // android.service.notification.NotificationListenerService
    public void onListenerDisconnected() {
        Log.v(TAG, "onListenerDisconnected");
        super.onListenerDisconnected();
    }

    private void setOnNotificationPostedTrimReflection() {
        try {
            ReflectionUtils.invokeMethod(this, "setOnNotificationPostedTrim", 1);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "setOnNotificationPostedTrimReflection : e=" + e + ", " + e.getCause(), e);
        }
    }

    public StatusBarNotification[] getActiveNotificationsReflection() {
        try {
            return (StatusBarNotification[]) ReflectionUtils.invokeMethod(this, "getActiveNotifications", 1);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "getActiveNotificationsReflection : e=" + e + ", " + e.getCause(), e);
            return null;
        }
    }
}
