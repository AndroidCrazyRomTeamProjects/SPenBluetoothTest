package com.samsung.remotespen.core.device.control.factory.crown;

import android.content.Context;
import com.samsung.remotespen.core.device.control.behavior.GenericPenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenApplicationFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenCrownFactory extends BleSpenDeviceFactory {
    private static BleSpenDeviceFeature sDeviceFeature = new CrownSpenDeviceFeature();
    private static BleSpenApplicationFeature sApplicationFeature = new BleSpenApplicationFeature(0, R.string.remotespen_settings_spen_status_type_default_spen, true, false, false, false, false, false, false, false, null);

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        return new BleSpenAttachedPenAddrFinder(context, new BleSpenAttachedPenAddrFinder.AdvertisementControlListener(context.getApplicationContext()) { // from class: com.samsung.remotespen.core.device.control.factory.crown.BleSpenCrownFactory.1
            private CrownWacomChargingDriver mWacomChargingDriver;
            public final /* synthetic */ Context val$appContext;

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void onPrepareTicToc() {
            }

            {
                this.val$appContext = r2;
                this.mWacomChargingDriver = (CrownWacomChargingDriver) BleSpenCrownFactory.this.getWacomChargingDriver(r2);
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void enableSpenAdvertisement(boolean z) {
                if (z) {
                    this.mWacomChargingDriver.turnOnWacomChargingModule();
                } else {
                    this.mWacomChargingDriver.turnOffWacomChargingModule();
                }
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void onFinishTicToc() {
                this.mWacomChargingDriver.turnOnWacomChargingModule();
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        return CrownWacomChargingDriver.getInstance(context);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return new BleSpenCrownDriver(SpenModelName.CROWN, getDeviceFeature(), null, null);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDeviceFeature getDeviceFeature() {
        return sDeviceFeature;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenApplicationFeature getApplicationFeature() {
        return sApplicationFeature;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public PenBehaviorPolicyManager createPenBehaviorPolicyManager(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        return new GenericPenBehaviorPolicyManager(context, bleSpenDriver, bleSpenInstanceId, callback);
    }
}
