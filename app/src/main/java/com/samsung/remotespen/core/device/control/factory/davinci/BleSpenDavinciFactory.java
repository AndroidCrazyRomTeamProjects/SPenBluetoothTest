package com.samsung.remotespen.core.device.control.factory.davinci;

import android.content.Context;
import android.util.Log;

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
import com.samsung.util.CommonUtils;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenDavinciFactory extends BleSpenDeviceFactory {
    private static final String TAG = "BleSpenDavinciFactory";
    private DavinciWacomChargingDriver mWacomChargingDriver;
    private static BleSpenDeviceFeature sDeviceFeature = new DavinciSpenDeviceFeature();
    private static BleSpenApplicationFeature sApplicationFeature = new BleSpenApplicationFeature(0, R.string.remotespen_settings_spen_status_type_default_spen, true, true, false, true, false, false, false, false, null);

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        return new BleSpenAttachedPenAddrFinder(context, new BleSpenAttachedPenAddrFinder.AdvertisementControlListener(context.getApplicationContext()) { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciFactory.1
            private DavinciWacomChargingDriver mWacomChargingDriver;
            public final /* synthetic */ Context val$appContext;

            {
                this.val$appContext = r2;
                this.mWacomChargingDriver = (DavinciWacomChargingDriver) BleSpenDavinciFactory.this.getWacomChargingDriver(r2);
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void enableSpenAdvertisement(boolean z) {
                if (z) {
                    this.mWacomChargingDriver.startCharge();
                } else {
                    this.mWacomChargingDriver.stopCharge();
                }
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void onPrepareTicToc() {
                Log.i(BleSpenDavinciFactory.TAG, "onPrepareTicToc : perform reset before TicToc");
                this.mWacomChargingDriver.stopCharge();
                this.mWacomChargingDriver.resetSpen();
                CommonUtils.sleep(5000L);
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void onFinishTicToc() {
                this.mWacomChargingDriver.startCharge();
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        if (this.mWacomChargingDriver == null) {
            this.mWacomChargingDriver = new DavinciWacomChargingDriver(context);
        }
        return this.mWacomChargingDriver;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return new BleSpenDavinciDriver(SpenModelName.DAVINCI, getDeviceFeature(), null, null);
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
