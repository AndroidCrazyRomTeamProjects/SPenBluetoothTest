package com.samsung.remotespen.core.device.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.util.ViewHelper;
import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class AlarmTimer {
    private static final String TAG = "AlarmTimer";
    private final String ACTION_ALARM;
    private final AlarmManager mAlarmManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.util.AlarmTimer.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            AlarmTimer.this.onTimerExpired(intent);
        }
    };
    private final Context mContext;
    private Listener mListener;
    private PendingIntent mPendingIntent;
    private final Object mSyncObj;
    private long mTimerStartTime;

    /* loaded from: classes.dex */
    public interface Listener {
        void onTimerExpired(long j);
    }

    public AlarmTimer(Context context, Object obj) {
        Assert.notNull(obj);
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mSyncObj = obj;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.ACTION_ALARM = applicationContext.getPackageName() + ".intent.action." + getClass().getSimpleName() + ViewHelper.QUALIFIER_DELIMITER + Integer.toHexString(hashCode()).toUpperCase();
    }

    public void reserveTimer(long j, Listener listener) {
        synchronized (this.mSyncObj) {
            if (isTimerWorking()) {
                Log.i(TAG, "reserveTimer : timer is already working. cancels previous timer");
                stopTimer(false);
            }
            this.mTimerStartTime = SystemClock.elapsedRealtime();
            this.mListener = listener;
            this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.ACTION_ALARM), 67108864);
            this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter(this.ACTION_ALARM));
            this.mAlarmManager.setExactAndAllowWhileIdle(3, SystemClock.elapsedRealtime() + j, this.mPendingIntent);
            String str = TAG;
            Log.i(str, "reserveTimer : timer reserved. time=" + (j / 1000) + "s");
        }
    }

    public void stopTimer(boolean z) {
        Listener listener;
        synchronized (this.mSyncObj) {
            if (!isTimerWorking()) {
                Log.v(TAG, "stopTimer : Timer is not working");
                return;
            }
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            PendingIntent pendingIntent = this.mPendingIntent;
            if (pendingIntent != null) {
                this.mAlarmManager.cancel(pendingIntent);
                this.mPendingIntent = null;
            }
            Log.i(TAG, "stopTimer : timer stopped");
            if (!z || (listener = this.mListener) == null) {
                return;
            }
            listener.onTimerExpired(this.mTimerStartTime);
            this.mListener = null;
        }
    }

    public boolean isTimerWorking() {
        boolean z;
        synchronized (this.mSyncObj) {
            z = this.mPendingIntent != null;
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTimerExpired(Intent intent) {
        Log.i(TAG, "onTimerExpired : timer expired");
        stopTimer(true);
    }
}
