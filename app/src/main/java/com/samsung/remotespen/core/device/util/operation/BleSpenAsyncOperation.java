package com.samsung.remotespen.core.device.util.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.util.SpenInsertionEventDetector;

/* loaded from: classes.dex */
public abstract class BleSpenAsyncOperation implements BleCancellableOperation {
    private String TAG;
    private Context mContext;
    public SpenInsertionEventDetector mSpenInsertionEventDetector;
    private long mStartTime;
    private Runnable mTimeoutListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mInProgress = false;
    private SpenInsertionEventDetector.Listener mSpenInsertionEventListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation.1
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            BleSpenAsyncOperation.this.onSpenInsertionEvent(z);
        }
    };

    public abstract void onFinishOperation(BleOpResultData bleOpResultData);

    public void onSpenInsertionEvent(boolean z) {
    }

    public void onTimerExpired() {
    }

    public BleSpenAsyncOperation(Context context, String str) {
        this.TAG = BleSpenAsyncOperation.class.getSimpleName();
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(applicationContext);
        if (str != null) {
            this.TAG = str;
        }
    }

    public boolean startOperation() {
        this.mStartTime = System.currentTimeMillis();
        this.mSpenInsertionEventDetector.registerListener(this.mSpenInsertionEventListener);
        if (this.mInProgress) {
            Log.e(this.TAG, "start : the operation is already in progress");
            finishOperation(BleOpResultCode.ALREADY_RUNNING);
            return false;
        }
        this.mInProgress = true;
        return true;
    }

    public void finishOperation(BleOpResultCode bleOpResultCode) {
        finishOperation(new BleOpResultData(bleOpResultCode));
    }

    private void finishOperation(BleOpResultData bleOpResultData) {
        this.mInProgress = false;
        this.mSpenInsertionEventDetector.unregisterListener(this.mSpenInsertionEventListener);
        cancelTimer();
        onFinishOperation(bleOpResultData);
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        finishOperation(BleOpResultCode.CANCELLED);
        if (finishListener != null) {
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isSpenInserted() {
        return this.mSpenInsertionEventDetector.isInserted();
    }

    public void startTimer(int i) {
        if (this.mTimeoutListener != null) {
            Log.e(this.TAG, "startTimer : timer is already running!");
            return;
        }
        Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation.2
            @Override // java.lang.Runnable
            public void run() {
                BleSpenAsyncOperation.this.mTimeoutListener = null;
                BleSpenAsyncOperation.this.onTimerExpired();
            }
        };
        this.mTimeoutListener = runnable;
        this.mHandler.postDelayed(runnable, i);
    }

    public void cancelTimer() {
        Runnable runnable = this.mTimeoutListener;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mTimeoutListener = null;
        }
    }

    public long getElapsedTime() {
        if (this.mStartTime == 0) {
            return 0L;
        }
        return System.currentTimeMillis() - this.mStartTime;
    }
}
