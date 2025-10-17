package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class BleSpenReconnector implements BleCancellableOperation {
    private static final int RETRY_INTERVAL = 1000;
    private static final String TAG = "BleSpenReconnector";
    private BleSpenDriver mBleSpenDriver;
    private BleCancellableOperation.FinishListener mCancelFinishListener;
    private ConnectionListener mConnectionFinishListener;
    private Context mContext;
    private BleCancellableOperation mCurWorkingOp;
    private long mStartTime;
    private long mTimeout;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsCancelled = false;
    private int mConnTriedCount = 0;
    private Runnable mReconnectionHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.1
        @Override // java.lang.Runnable
        public void run() {
            Log.d(BleSpenReconnector.TAG, "Reconnection : Retrying to connect...");
            BleSpenReconnector.this.tryToConnect();
        }
    };

    /* loaded from: classes.dex */
    public interface ConnectionListener {
        void onFinish(BleOpResultData bleOpResultData, long j);
    }

    public BleSpenReconnector(Context context, BleSpenDriver bleSpenDriver) {
        this.mContext = context.getApplicationContext();
        this.mBleSpenDriver = bleSpenDriver;
    }

    public void startReconnection(long j, ConnectionListener connectionListener) {
        Log.i(TAG, "startReconnection");
        this.mConnTriedCount = 0;
        this.mConnectionFinishListener = connectionListener;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mTimeout = j;
        tryToConnect();
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        if (isInProgress()) {
            this.mIsCancelled = true;
            boolean z = this.mCancelFinishListener == null;
            Assert.e(z, "Cancel listener is already registered : " + this.mCancelFinishListener);
            this.mCancelFinishListener = finishListener;
            if (this.mCurWorkingOp != null) {
                String str = TAG;
                Log.d(str, "cancelOperation : cancelling " + this.mCurWorkingOp.getClass().getSimpleName());
                this.mCurWorkingOp.cancelOperation(null);
                return;
            }
            Log.d(TAG, "cancelOperation");
            return;
        }
        Log.e(TAG, "cancelOperation : connection is not started");
        if (finishListener != null) {
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
        }
    }

    private boolean isInProgress() {
        return this.mStartTime > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveReconnection() {
        Log.d(TAG, "reserveReconnection");
        cancelReconnection(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                Log.d(BleSpenReconnector.TAG, "reserveReconnection : reconnection cancel finished");
                BleSpenReconnector.this.mHandler.postDelayed(BleSpenReconnector.this.mReconnectionHandler, 1000L);
            }
        });
    }

    private void cancelReconnection(BleSpenDriver.OperationFinishListener operationFinishListener) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mHandler.removeCallbacks(this.mReconnectionHandler);
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), SystemClock.elapsedRealtime() - elapsedRealtime);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tryToConnect() {
        int connectionTimeoutDuration = getConnectionTimeoutDuration();
        String str = TAG;
        Log.i(str, "tryToConnect : timeout=" + connectionTimeoutDuration + " trycount=" + (this.mConnTriedCount + 1));
        if (isCancelled()) {
            Log.e(str, "tryToConnect: connection cancelled");
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else if (this.mBleSpenDriver.isDisconnected()) {
            Log.e(str, "tryToConnect: BLE disconnected");
            invokeFinishListener(BleOpResultCode.DISCONNECTED);
        } else if (connectionTimeoutDuration < 0) {
            Log.e(str, "tryToConnect: connection time limitation expired");
            invokeFinishListener(BleOpResultCode.TIMEOUT);
        } else {
            this.mConnTriedCount++;
            final BleSpenConnSetupSeqHelper bleSpenConnSetupSeqHelper = new BleSpenConnSetupSeqHelper(this.mContext, this.mBleSpenDriver);
            markOperationStart(bleSpenConnSetupSeqHelper);
            bleSpenConnSetupSeqHelper.doReconnectionSetupSequence(new BleSpenConnSetupSeqHelper.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.3
                @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper.FinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    BleSpenReconnector.this.markOperationFinish(bleSpenConnSetupSeqHelper);
                    BleOpResultCode resultCode = bleOpResultData.getResultCode();
                    String str2 = BleSpenReconnector.TAG;
                    Log.d(str2, "onFinishConnSetupSeq : " + resultCode.name() + " " + bleOpResultData);
                    if (resultCode == BleOpResultCode.SUCCESS) {
                        BleSpenReconnector.this.invokeFinishListener(bleOpResultData);
                        return;
                    }
                    String str3 = BleSpenReconnector.TAG;
                    Log.d(str3, "onFinishConnSetupSeq : Conn seq failed - " + resultCode.name() + " retrying..");
                    if (BleSpenReconnector.this.isCancelled()) {
                        BleSpenReconnector.this.invokeFinishListener(BleOpResultCode.CANCELLED);
                    } else {
                        BleSpenReconnector.this.reserveReconnection();
                    }
                }
            });
        }
    }

    private int getConnectionTimeoutDuration() {
        return getElapsedTime() >= this.mTimeout ? -1 : 10000;
    }

    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - this.mStartTime;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeFinishListener(BleOpResultCode bleOpResultCode) {
        invokeFinishListener(new BleOpResultData(bleOpResultCode));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeFinishListener(final BleOpResultData bleOpResultData) {
        final long elapsedTime = getElapsedTime();
        this.mIsCancelled = false;
        this.mStartTime = 0L;
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.4
            @Override // java.lang.Runnable
            public void run() {
                if (BleSpenReconnector.this.mConnectionFinishListener != null) {
                    BleSpenReconnector.this.mConnectionFinishListener.onFinish(bleOpResultData, elapsedTime);
                }
                if (BleSpenReconnector.this.mCancelFinishListener != null) {
                    BleSpenReconnector.this.mCancelFinishListener.onFinish(bleOpResultData);
                    BleSpenReconnector.this.mCancelFinishListener = null;
                }
            }
        };
        if (bleOpResultData.getResultCode() != BleOpResultCode.SUCCESS) {
            cancelReconnection(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.5
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData2, long j) {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isCancelled() {
        boolean z = true;
        boolean z2 = !BleUtils.isBleEnabled(this.mContext);
        if (!this.mIsCancelled && !z2) {
            z = false;
        }
        if (z) {
            Log.e(TAG, "isCancelled : cancelled.  cancelFlag:" + this.mIsCancelled + "  bleDisabled:" + z2);
        }
        return z;
    }

    private void markOperationStart(BleCancellableOperation bleCancellableOperation) {
        if (this.mCurWorkingOp != null) {
            String str = TAG;
            Log.e(str, "markOperationStart : prev op(" + this.mCurWorkingOp.getClass().getSimpleName() + ") is still working! newOp=" + bleCancellableOperation.getClass().getSimpleName(), new Exception());
            return;
        }
        this.mCurWorkingOp = bleCancellableOperation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void markOperationFinish(BleCancellableOperation bleCancellableOperation) {
        BleCancellableOperation bleCancellableOperation2 = this.mCurWorkingOp;
        if (bleCancellableOperation2 == null) {
            String str = TAG;
            Log.e(str, "markOperationFinish : cur op is null. So cannot finish the " + bleCancellableOperation.getClass().getSimpleName() + " operation", new Exception());
        } else if (bleCancellableOperation2 != bleCancellableOperation) {
            String str2 = TAG;
            Log.e(str2, "markOperationFinish : prev op(" + this.mCurWorkingOp.getClass().getSimpleName() + ") is still working! opToFinish=" + bleCancellableOperation.getClass().getSimpleName(), new Exception());
        } else {
            this.mCurWorkingOp = null;
        }
    }
}
