package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation;

/* loaded from: classes.dex */
public class BleSpenHardResetter extends BleSpenAsyncOperation {
    public static final int BLIND_RESET_DURATION = 35000;
    public static final int RESET_DURATION = 2000;
    private static final String TAG = "BleSpenHardResetter";
    private FinishListener mFinishListener;

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    public BleSpenHardResetter(Context context) {
        super(context, TAG);
    }

    public void performReset(WacomChargingDriver wacomChargingDriver, int i, FinishListener finishListener) {
        this.mFinishListener = finishListener;
        if (startOperation()) {
            if (!isSpenInserted()) {
                Log.e(TAG, "performReset : Spen is detached");
                finishOperation(BleOpResultCode.CANCELLED);
                return;
            }
            int i2 = i > 10 ? 2000 : 35000;
            String str = TAG;
            Log.i(str, "performReset : reset duration=" + i2);
            startTimer(i2);
            resetBleSpenAddress();
            if (i2 == 35000) {
                wacomChargingDriver.resetSpen();
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onFinishOperation(BleOpResultData bleOpResultData) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishOperation : " + resultCode.name());
        cancelTimer();
        FinishListener finishListener = this.mFinishListener;
        if (finishListener != null) {
            finishListener.onFinish(bleOpResultData);
            this.mFinishListener = null;
            return;
        }
        Log.e(str, "onFinishOperation : listener is null!");
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onTimerExpired() {
        finishOperation(BleOpResultCode.SUCCESS);
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onSpenInsertionEvent(boolean z) {
        if (z) {
            return;
        }
        Log.w(TAG, "onSpenInsertionEvent : SPen detached");
        finishOperation(BleOpResultCode.CANCELLED);
    }

    private void resetBleSpenAddress() {
        BleUtils.writeBundledBleSpenAddressToEfs(getContext(), null);
    }
}
