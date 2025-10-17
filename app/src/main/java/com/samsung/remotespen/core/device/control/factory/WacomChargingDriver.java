package com.samsung.remotespen.core.device.control.factory;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.util.CommonUtils;
import com.samsung.util.SpenGestureManagerWrapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes.dex */
public abstract class WacomChargingDriver {
    public static final int MIN_WAIT_TIME_AFTER_PEN_INSERTION = 1000;
    private static final String TAG = "WacomChargingDriver";
    private Context mContext;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private long mLastCommandWriteTime;
    private long mLastTransactionFinishTime;
    private final long mMinCommandWriteInterval;
    private final long mMinTransactionInterval;
    private SpenGestureManagerWrapper mSpenGestureManagerWrapper;

    public abstract void resetSpen();

    public abstract int resetWacomDsp(boolean z);

    public abstract int startCharge();

    public abstract boolean startContinuousCharge();

    public abstract void stopCharge();

    public abstract void turnOffWacomChargingModule();

    public abstract void turnOnWacomChargingModule();

    public WacomChargingDriver(Context context, long j, long j2) {
        this.mContext = context.getApplicationContext();
        this.mSpenGestureManagerWrapper = new SpenGestureManagerWrapper(this.mContext);
        this.mMinTransactionInterval = j;
        this.mMinCommandWriteInterval = j2;
    }

    public synchronized void writeBleSpenCommand(String str) {
        ensureCommandWriteInterval();
        String str2 = TAG;
        Log.i(str2, "writeBleSpenCommand : " + str);
        this.mSpenGestureManagerWrapper.writeBleSpenCommand(str);
        this.mLastCommandWriteTime = SystemClock.elapsedRealtime();
    }

    public void ensureCommandWriteInterval() {
        long min = Math.min(this.mMinCommandWriteInterval - (SystemClock.elapsedRealtime() - this.mLastCommandWriteTime), this.mMinCommandWriteInterval);
        if (min > 0) {
            String str = TAG;
            Log.v(str, "ensureCommandWriteInterval : waiting for " + min + "ms");
            CommonUtils.sleep(min);
        }
    }

    public void ensureMinTransactionInterval() {
        long min = Math.min(this.mMinTransactionInterval - (SystemClock.elapsedRealtime() - this.mLastTransactionFinishTime), this.mMinTransactionInterval);
        if (min > 0) {
            String str = TAG;
            Log.v(str, "ensureMinTransactionInterval : waiting for " + min + "ms");
            CommonUtils.sleep(min);
        }
    }

    public void ensureNotMainThread(Runnable runnable) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mExecutor.submit(runnable);
        } else {
            runnable.run();
        }
    }

    public void markEndTransaction() {
        this.mLastTransactionFinishTime = SystemClock.elapsedRealtime();
    }
}
