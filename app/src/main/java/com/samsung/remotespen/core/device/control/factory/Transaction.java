package com.samsung.remotespen.core.device.control.factory;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public abstract class Transaction {
    private BleSpenDriver.OperationFinishListener mFinishListener;
    private long mFinishTime;
    private long mStartTime;
    public String TAG = getClass().getSimpleName();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeoutHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.Transaction.1
        @Override // java.lang.Runnable
        public void run() {
            Transaction.this.onTimeout();
        }
    };

    public abstract void doWork(BleSpenBleDriver bleSpenBleDriver);

    public synchronized void start(BleSpenBleDriver bleSpenBleDriver, BleSpenDriver.OperationFinishListener operationFinishListener, int i) {
        String str = this.TAG;
        Log.d(str, getName() + " : start : timeout=" + i);
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mFinishTime = 0L;
        this.mHandler.postDelayed(this.mTimeoutHandler, (long) i);
        this.mFinishListener = operationFinishListener;
        doWork(bleSpenBleDriver);
    }

    public void finish(BleOpResultCode bleOpResultCode) {
        finish(new BleOpResultData(bleOpResultCode));
    }

    public synchronized void finish(final BleOpResultData bleOpResultData) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        if (this.mFinishTime > 0) {
            String str = this.TAG;
            Log.e(str, "finish : already finished transaction! resultCode=" + resultCode, new Exception());
            return;
        }
        this.mFinishTime = SystemClock.elapsedRealtime();
        this.mHandler.removeCallbacks(this.mTimeoutHandler);
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.Transaction.2
            @Override // java.lang.Runnable
            public void run() {
                Transaction transaction = Transaction.this;
                transaction.onFinish(bleOpResultData, transaction.getElapsedTime());
            }
        });
    }

    public synchronized boolean isWorking() {
        boolean z;
        if (this.mStartTime > 0) {
            z = this.mFinishTime == 0;
        }
        return z;
    }

    public synchronized boolean isFinished() {
        boolean z;
        if (this.mStartTime > 0) {
            z = this.mFinishTime > 0;
        }
        return z;
    }

    public synchronized long getElapsedTime() {
        if (isFinished()) {
            return this.mFinishTime - this.mStartTime;
        }
        return -1L;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void onFinish(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        if (resultCode != BleOpResultCode.SUCCESS) {
            String str = this.TAG;
            Log.e(str, getName() + " : onFinish - " + resultCode.name() + " " + j + "ms");
        }
        BleSpenDriver.OperationFinishListener operationFinishListener = this.mFinishListener;
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(bleOpResultData, j);
        }
    }

    public void onTimeout() {
        finish(BleOpResultCode.TIMEOUT);
    }
}
