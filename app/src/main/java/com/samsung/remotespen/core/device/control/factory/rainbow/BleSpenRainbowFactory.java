package com.samsung.remotespen.core.device.control.factory.rainbow;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.RainbowPenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenApplicationFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.fota.RainbowDialogFirmwareUpgradeDriver;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;
import com.samsung.util.CommonUtils;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenRainbowFactory extends BleSpenDeviceFactory {
    private static final int MAX_ALLOWABLE_RESPONSE_DELAY_FOR_RAINBOW = 700;
    private static final String TAG = "BleSpenRainbowFactory";
    private RainbowWacomChargingDriver mWacomChargingDriver;
    private static BleSpenDeviceFeature sDeviceFeature = new RainbowSpenDeviceFeature();
    private static BleSpenApplicationFeature sApplicationFeature = new BleSpenApplicationFeature(0, R.string.remotespen_settings_spen_status_type_default_spen, true, true, true, true, true, false, false, true, null);

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        BleSpenAttachedPenAddrFinder bleSpenAttachedPenAddrFinder = new BleSpenAttachedPenAddrFinder(context, new BleSpenAttachedPenAddrFinder.AdvertisementControlListener(context.getApplicationContext()) { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowFactory.1
            private RainbowWacomChargingDriver mWacomChargingDriver;
            public final /* synthetic */ Context val$appContext;

            {
                this.val$appContext = r2;
                this.mWacomChargingDriver = (RainbowWacomChargingDriver) BleSpenRainbowFactory.this.getWacomChargingDriver(r2);
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
                Log.i(BleSpenRainbowFactory.TAG, "onPrepareTicToc : perform reset before TicToc");
                this.mWacomChargingDriver.stopCharge();
                this.mWacomChargingDriver.resetSpen();
                CommonUtils.sleep(5000L);
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.AdvertisementControlListener
            public void onFinishTicToc() {
                this.mWacomChargingDriver.startCharge();
            }
        });
        BleSpenAttachedPenAddrFinder.Options options = new BleSpenAttachedPenAddrFinder.Options();
        options.mMaxAllowableResponseDelay = MAX_ALLOWABLE_RESPONSE_DELAY_FOR_RAINBOW;
        bleSpenAttachedPenAddrFinder.setFinderOptions(options);
        return bleSpenAttachedPenAddrFinder;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        if (this.mWacomChargingDriver == null) {
            this.mWacomChargingDriver = new RainbowWacomChargingDriver(context);
        }
        return this.mWacomChargingDriver;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return new BleSpenRainbowDriver(SpenModelName.RAINBOW, getDeviceFeature(), null, new RainbowDialogFirmwareUpgradeDriver());
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
        return new RainbowPenBehaviorPolicyManager(context, bleSpenDriver, bleSpenInstanceId, callback);
    }
}
