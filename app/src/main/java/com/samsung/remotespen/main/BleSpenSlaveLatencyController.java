package com.samsung.remotespen.main;

import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;

/* loaded from: classes.dex */
public class BleSpenSlaveLatencyController {
    private static final int SLAVE_LATENCY_ON_DEFAULT = 9;
    private static final int SLAVE_LATENCY_ON_LOW_BATTERY = 3;
    private static final String TAG = "BleSpenSlaveLatencyController";
    private static final int TRIGGER_BATTERY_LEVEL = 10;
    private BleSpenManager mBleSpenManager;
    private BleSpenInstanceId mSpenInstanceId;

    public BleSpenSlaveLatencyController(BleSpenManager bleSpenManager, BleSpenInstanceId bleSpenInstanceId) {
        this.mBleSpenManager = bleSpenManager;
        this.mSpenInstanceId = bleSpenInstanceId;
    }

    public void onBatteryLevelChanged(int i, int i2) {
        Integer num;
        if (i <= 10 && i2 > 10) {
            num = 3;
        } else {
            num = (i <= 10 || i2 > 10) ? null : 9;
        }
        if (num != null) {
            String str = TAG;
            Log.d(str, "onFinish : Change the slave latency to " + num + "s, battery=" + i2 + "->" + i);
            setSlaveLatency(num.intValue());
        }
    }

    public void setDefaultSlaveLatency() {
        Log.v(TAG, "setDefaultSlaveLatency");
        setSlaveLatency(9);
    }

    private void setSlaveLatency(final int i) {
        BleSpenManager.ConnectionState connectionState = this.mBleSpenManager.getConnectionState(this.mSpenInstanceId);
        if (connectionState != BleSpenManager.ConnectionState.CONNECTED) {
            String str = TAG;
            Log.e(str, "setSlaveLatency : not connected state : " + connectionState);
            return;
        }
        this.mBleSpenManager.setSlaveLatency(this.mSpenInstanceId, i, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.main.BleSpenSlaveLatencyController.1
            @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
            public void onFinish(BleSpenManager.OpResultData opResultData) {
                String str2 = BleSpenSlaveLatencyController.TAG;
                Log.d(str2, "onFinish : Slave latency(" + i + "s) result = " + opResultData.getResultCode());
            }
        });
    }
}
