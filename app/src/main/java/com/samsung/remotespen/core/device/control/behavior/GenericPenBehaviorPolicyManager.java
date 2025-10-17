package com.samsung.remotespen.core.device.control.behavior;

import android.content.Context;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.ExternalPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.NormalBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;

/* loaded from: classes.dex */
public class GenericPenBehaviorPolicyManager extends PenBehaviorPolicyManager {
    private static final String TAG = "GenericPenBehaviorPolicyManager";
    private AbsPenBehaviorPolicy mBehaviorPolicy;

    public GenericPenBehaviorPolicyManager(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        if (BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName()).getDeviceFeature().isSupportEasyConnect()) {
            this.mBehaviorPolicy = new ExternalPenBehaviorPolicy(context, bleSpenDriver, bleSpenInstanceId, callback);
        } else {
            this.mBehaviorPolicy = new NormalBehaviorPolicy(context, bleSpenDriver, bleSpenInstanceId, callback);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager
    public void start() {
        this.mBehaviorPolicy.start();
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager
    public void stop() {
        this.mBehaviorPolicy.stop();
    }
}
