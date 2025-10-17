package com.samsung.remotespen.core.device.control.behavior.policy;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;

/* loaded from: classes.dex */
public class ExternalPenBehaviorPolicy extends AbsPenBehaviorPolicy {
    private static final String TAG = "ExternalPenBehaviorPolicy";

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void doReserveReconnection(int i, ReconnectionController.ReconnectReason reconnectReason) {
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public boolean isSupportReconnection() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void onBatteryStateChanged(BleSpenBatteryState bleSpenBatteryState) {
    }

    public ExternalPenBehaviorPolicy(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        super(context, bleSpenDriver, bleSpenInstanceId, callback);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void start() {
        super.start();
        Log.i(TAG, "start");
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void stop() {
        super.stop();
        Log.i(TAG, "stop");
    }
}
