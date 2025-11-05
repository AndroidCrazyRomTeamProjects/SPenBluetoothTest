package com.samsung.remotespen.core.device.chargepolicy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.control.BleSpenDeviceMainController;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.crazyromteam.spenbletest.utils.Assert;

/* compiled from: BleSpenGenericChargeController.java */
/* loaded from: classes.dex */
class BleSpenMotionSensorCalibrationController {
    private static final long MIN_CALIBRATION_INTERVAL = 28800000;
    private static final String TAG = "BleSpenMotionSensorCalibrationController";
    private BleSpenDeviceMainController mBleSpenMainController;
    private Context mContext;
    private long mLastCalibrationTime = 0;
    private boolean mIsRunning = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenMotionSensorCalibrationController.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                BleSpenMotionSensorCalibrationController.this.onScreenOff(intent);
            }
        }
    };

    public BleSpenMotionSensorCalibrationController(Context context, BleSpenDeviceMainController bleSpenDeviceMainController) {
        this.mContext = context.getApplicationContext();
        this.mBleSpenMainController = bleSpenDeviceMainController;
    }

    public void start() {
        Log.i(TAG, "start");
        Assert.e(!this.mIsRunning, "Already running!");
        this.mIsRunning = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void stop() {
        Log.i(TAG, "stop");
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mIsRunning = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onScreenOff(Intent intent) {
        int intExtra = intent.getIntExtra("why", -1);
        String str = TAG;
        Log.d(str, "onScreenOff : reason = " + intExtra);
        if (intExtra == 3) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long j = elapsedRealtime - this.mLastCalibrationTime;
            boolean z = this.mBleSpenMainController.getConnectionState() == BleSpenDeviceMainController.ConnectionState.CONNECTED;
            boolean isPowerPlugged = CommonUtils.isPowerPlugged(this.mContext);
            boolean isInserted = SpenInsertionEventDetector.getInstance(this.mContext).isInserted();
            if (j > MIN_CALIBRATION_INTERVAL && z && isPowerPlugged && isInserted) {
                Log.i(str, "onScreenOff : performs motion sensor calibration. last calibration : " + (j / 1000) + "s ago");
                this.mBleSpenMainController.performCalibration(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenMotionSensorCalibrationController.2
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j2) {
                        String str2 = BleSpenMotionSensorCalibrationController.TAG;
                        Log.i(str2, "onScreenOff : calibration request result = " + bleOpResultData.getResultCode());
                    }
                });
                this.mLastCalibrationTime = elapsedRealtime;
            }
        }
    }
}
