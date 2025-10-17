package com.samsung.remotespen.core.device.control.factory.great;

import android.content.Context;
import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenApplicationFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;

/* loaded from: classes.dex */
public class BleSpenGreatFactory extends BleSpenDeviceFactory {
    private static BleSpenDeviceFeature sDeviceFeature = new GreatSpenDeviceFeature();
    private static BleSpenApplicationFeature sApplicationFeature = new BleSpenApplicationFeature(0, R.string.remotespen_settings_spen_status_type_default_spen, false, false, false, false, false, false, false, false, null);

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public PenBehaviorPolicyManager createPenBehaviorPolicyManager(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDeviceFeature getDeviceFeature() {
        return sDeviceFeature;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenApplicationFeature getApplicationFeature() {
        return sApplicationFeature;
    }
}
