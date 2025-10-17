package com.samsung.remotespen.core.device.control.factory.canvas;

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
public class BleSpenCanvasFactory extends BleSpenDeviceFactory {
    private static final int MAX_ALLOWABLE_RESPONSE_DELAY_FOR_CANVAS = 700;
    private static final String TAG = "BleSpenCanvasFactory";
    private static BleSpenApplicationFeature sApplicationFeature;
    private static BleSpenDeviceFeature sDeviceFeature = new CanvasSpenDeviceFeature();
    private boolean mIsSupportFirmwareUpgrade;
    private CanvasWacomChargingDriver mWacomChargingDriver;

    public BleSpenCanvasFactory() {
        boolean isSupportFirmwareUpgradeForTablet = CommonUtils.isSupportFirmwareUpgradeForTablet();
        this.mIsSupportFirmwareUpgrade = isSupportFirmwareUpgradeForTablet;
        sApplicationFeature = new BleSpenApplicationFeature(0, R.string.remotespen_settings_spen_status_type_default_spen, true, true, true, true, isSupportFirmwareUpgradeForTablet, false, false, false, null);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        BleSpenAttachedPenAddrFinder bleSpenAttachedPenAddrFinder = new BleSpenAttachedPenAddrFinder(context, new BleSpenAttachedPenAddrFinder.AdvertisementControlListener(context.getApplicationContext()) { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasFactory.1
            private CanvasWacomChargingDriver mWacomChargingDriver;
            public final /* synthetic */ Context val$appContext;

            {
                this.val$appContext = r2;
                this.mWacomChargingDriver = (CanvasWacomChargingDriver) BleSpenCanvasFactory.this.getWacomChargingDriver(r2);
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
                Log.i(BleSpenCanvasFactory.TAG, "onPrepareTicToc : perform reset before TicToc");
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
        options.mMaxAllowableResponseDelay = MAX_ALLOWABLE_RESPONSE_DELAY_FOR_CANVAS;
        bleSpenAttachedPenAddrFinder.setFinderOptions(options);
        return bleSpenAttachedPenAddrFinder;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        if (this.mWacomChargingDriver == null) {
            this.mWacomChargingDriver = new CanvasWacomChargingDriver(context);
        }
        return this.mWacomChargingDriver;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return new BleSpenCanvasDriver(SpenModelName.CANVAS, getDeviceFeature(), null, this.mIsSupportFirmwareUpgrade ? new RainbowDialogFirmwareUpgradeDriver() : null);
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
