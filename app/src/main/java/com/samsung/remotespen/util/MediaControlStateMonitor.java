package com.samsung.remotespen.util;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.UserSwitchObserver;
import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import com.samsung.remotespen.util.NotificationServiceMonitor;
import com.samsung.util.ActivitySwitchDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class MediaControlStateMonitor {
    public static final String TAG = "MediaControlStateMonitor";
    private static MediaControlStateMonitor sInstance;
    private BluetoothAudioCastMonitor mBluetoothAudioCastMonitor;
    private Context mContext;
    private MediaSessionManager mMediaSessionManager;
    private StateChangeListener mStateChangeListener;
    private Handler mHandler = new Handler();
    private NotificationServiceMonitor mNotificationServiceMonitor = null;
    private ArrayList<String> mActiveNotiList = new ArrayList<>();
    private List<MediaController> mActiveSessions = new ArrayList();
    private NotificationServiceMonitor.UpdateListener mUpdateListener = new NotificationServiceMonitor.UpdateListener() { // from class: com.samsung.remotespen.util.MediaControlStateMonitor.1
        @Override // com.samsung.remotespen.util.NotificationServiceMonitor.UpdateListener
        public void onPosted(StatusBarNotification statusBarNotification) {
            String packageName = statusBarNotification.getPackageName();
            String str = MediaControlStateMonitor.TAG;
            Log.d(str, "onPosted : notificationPkgName = " + packageName);
            if (MediaControlStateMonitor.this.mActiveNotiList.contains(packageName) || !MediaControlStateMonitor.this.isActive(packageName)) {
                return;
            }
            MediaControlStateMonitor.this.mActiveNotiList.add(packageName);
            Log.d(str, "onPosted : add active noti for media control to the list, size=" + MediaControlStateMonitor.this.mActiveNotiList.size());
            MediaControlStateMonitor.this.mStateChangeListener.onAvailableStateChanged();
        }

        @Override // com.samsung.remotespen.util.NotificationServiceMonitor.UpdateListener
        public void onRemoved(StatusBarNotification statusBarNotification) {
            String packageName = statusBarNotification.getPackageName();
            if (MediaControlStateMonitor.this.mActiveNotiList.contains(packageName)) {
                MediaControlStateMonitor.this.mActiveNotiList.remove(packageName);
                if (MediaControlStateMonitor.this.mStateChangeListener == null || !MediaControlStateMonitor.this.mActiveNotiList.isEmpty()) {
                    return;
                }
                String className = ActivitySwitchDetector.getInstance(MediaControlStateMonitor.this.mContext).getTopMostActivity().getClassName();
                MediaControlStateMonitor mediaControlStateMonitor = MediaControlStateMonitor.this;
                if (mediaControlStateMonitor.isMediaControlAvailable(mediaControlStateMonitor.mContext, className)) {
                    return;
                }
                Log.d(MediaControlStateMonitor.TAG, "onRemoved : all active noti are removed in the list and current package is not foreground.");
                MediaControlStateMonitor.this.mStateChangeListener.onAvailableStateChanged();
            }
        }
    };
    private MediaSessionManager.OnActiveSessionsChangedListener mMediaSessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() { // from class: com.samsung.remotespen.util.MediaControlStateMonitor.2
        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> list) {
            Log.v(MediaControlStateMonitor.TAG, "onActiveSessionsChanged");
            MediaControlStateMonitor.this.mActiveSessions = list;
            MediaControlStateMonitor.this.mStateChangeListener.onAvailableStateChanged();
        }
    };
    private UserSwitchObserver mUserSwitchObserver = new UserSwitchObserver() { // from class: com.samsung.remotespen.util.MediaControlStateMonitor.3
        public void onForegroundProfileSwitch(int i) throws RemoteException {
            String str = MediaControlStateMonitor.TAG;
            Log.d(str, "onForegroundProfileSwitch : newProfileId = " + i);
            MediaControlStateMonitor.this.unregisterMediaSessionListener();
            MediaControlStateMonitor.this.registerMediaSessionListener(i);
        }
    };

    /* loaded from: classes.dex */
    public interface StateChangeListener {
        void onAvailableStateChanged();
    }

    public static synchronized MediaControlStateMonitor getInstance(Context context) {
        MediaControlStateMonitor mediaControlStateMonitor;
        synchronized (MediaControlStateMonitor.class) {
            if (sInstance == null) {
                sInstance = new MediaControlStateMonitor(context);
            }
            mediaControlStateMonitor = sInstance;
        }
        return mediaControlStateMonitor;
    }

    private MediaControlStateMonitor(Context context) {
        this.mBluetoothAudioCastMonitor = null;
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mBluetoothAudioCastMonitor = BluetoothAudioCastMonitor.getInstance(applicationContext);
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
    }

    public void startMonitoring() {
        registerMediaSessionListener(CommonUtils.getFocusedUserId(this.mContext));
        registerUserSwitchObserver();
    }

    public void stopMonitoring() {
        unregisterMediaSessionListener();
        unregisterUserSwitchObserver();
    }

    public void registerListener(StateChangeListener stateChangeListener) {
        if (this.mStateChangeListener == null) {
            this.mStateChangeListener = stateChangeListener;
            return;
        }
        Log.e(TAG, "registerListener : fail to register listener because listener is existed.");
        Assert.fail("Fail to register listener");
    }

    public void unregisterListener() {
        this.mStateChangeListener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerMediaSessionListener(int i) {
        try {
            ReflectionUtils.invokeMethod(this.mMediaSessionManager, "addOnActiveSessionsChangedListener", new ComponentName(this.mContext.getPackageName(), TAG), CommonUtils.getUserHandle(i), new Executor() { // from class: com.samsung.remotespen.util.MediaControlStateMonitor.4
                @Override // java.util.concurrent.Executor
                public void execute(Runnable runnable) {
                    if (MediaControlStateMonitor.this.mHandler.post(runnable)) {
                        return;
                    }
                    String str = MediaControlStateMonitor.TAG;
                    Log.e(str, "execute: " + MediaControlStateMonitor.this.mHandler + " is shutting down");
                }
            }, this.mMediaSessionListener);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "registerMediaSessionListener: e =" + e);
        }
        this.mActiveSessions = getActiveSessionsForUser(this.mContext, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterMediaSessionListener() {
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mMediaSessionListener);
        this.mActiveSessions.clear();
    }

    private void registerUserSwitchObserver() {
        try {
            ReflectionUtils.invokeMethod((IActivityManager) ReflectionUtils.invokeMethod((ActivityManager) this.mContext.getSystemService("activity"), "getService", new Object[0]), "registerUserSwitchObserver", this.mUserSwitchObserver, TAG);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "registerUserSwitchObserver: e =" + e);
        }
    }

    private void unregisterUserSwitchObserver() {
        try {
            ReflectionUtils.invokeMethod((IActivityManager) ReflectionUtils.invokeMethod((ActivityManager) this.mContext.getSystemService("activity"), "getService", new Object[0]), "unregisterUserSwitchObserver", this.mUserSwitchObserver);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "unregisterUserSwitchObserver: e =" + e);
        }
    }

    public void setNotificationServiceMonitor(NotificationServiceMonitor notificationServiceMonitor) {
        if (notificationServiceMonitor != null) {
            this.mNotificationServiceMonitor = notificationServiceMonitor;
            notificationServiceMonitor.registerListener(this.mUpdateListener);
            StatusBarNotification[] activeNotificationsReflection = this.mNotificationServiceMonitor.getActiveNotificationsReflection();
            if (activeNotificationsReflection == null) {
                Log.e(TAG, "setNotificationServiceMonitor : notifications is null");
                return;
            }
            for (StatusBarNotification statusBarNotification : activeNotificationsReflection) {
                String packageName = statusBarNotification.getPackageName();
                if (!this.mActiveNotiList.contains(packageName) && isActive(packageName)) {
                    this.mActiveNotiList.add(packageName);
                }
            }
            return;
        }
        this.mNotificationServiceMonitor.unregisterListener();
        this.mNotificationServiceMonitor = null;
        this.mActiveNotiList.clear();
    }

    public boolean isMediaControlAvailable(Context context, String str) {
        return isForegroundAppSupportsMediaButton(context, str) || this.mBluetoothAudioCastMonitor.isAudioCastRunning() || getActiveNotificationPackageName() != null;
    }

    private boolean isForegroundAppSupportsMediaButton(Context context, String str) {
        if (str == null) {
            return false;
        }
        if (this.mActiveSessions.isEmpty()) {
            Log.d(TAG, "isForegroundAppSupportsMediaButton : mediaSession is empty");
            return false;
        }
        for (MediaController mediaController : this.mActiveSessions) {
            if (mediaController.getPackageName().equals(str)) {
                String str2 = TAG;
                Log.d(str2, "isForegroundAppSupportsMediaButton : media session is exist for " + str);
                return true;
            }
        }
        return false;
    }

    public String getActiveNotificationPackageName() {
        if (this.mNotificationServiceMonitor == null) {
            Log.e(TAG, "getActiveNotificationPackageName : notification service monitor is null");
            return null;
        } else if (this.mActiveNotiList.isEmpty()) {
            Log.d(TAG, "active Notification List is null");
            return null;
        } else {
            String str = TAG;
            Log.d(str, "getActiveNotificationPackageName : noti count=" + this.mActiveNotiList.size() + ", session count=" + this.mActiveSessions.size());
            if (this.mActiveSessions.isEmpty() || this.mActiveNotiList.isEmpty()) {
                Log.d(str, "getActiveNotificationPackageName : notification list, media session list is null");
                return null;
            }
            for (MediaController mediaController : this.mActiveSessions) {
                String packageName = mediaController.getPackageName();
                Iterator<String> it = this.mActiveNotiList.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    String str2 = TAG;
                    Log.d(str2, "getActiveNotificationPackageName : notificationPkgName = " + next);
                    if (packageName.equals(next)) {
                        Log.d(str2, "getActiveNotificationPackageName : " + packageName);
                        return packageName;
                    }
                }
            }
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isActive(String str) {
        for (MediaController mediaController : this.mActiveSessions) {
            if (str.equals(mediaController.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public List<MediaController> getActiveSessions() {
        return this.mActiveSessions;
    }

    private static List<MediaController> getActiveSessionsForUser(Context context, int i) {
        MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService("media_session");
        ArrayList arrayList = new ArrayList();
        String str = TAG;
        Log.v(str, "getActiveSessionsForUser : userId = " + i);
        try {
            return (List) ReflectionUtils.invokeMethod(mediaSessionManager, "getActiveSessionsForUser", new ComponentName(context.getPackageName(), str), CommonUtils.getUserHandle(i));
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "getActiveSessionsForUser. e = " + e);
            return arrayList;
        }
    }
}
