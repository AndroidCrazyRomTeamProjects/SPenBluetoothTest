package com.samsung.remotespen.core.device.control.factory.ext1;

import android.content.Context;
import com.samsung.remotespen.core.device.control.behavior.GenericPenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenApplicationFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.RegulatoryInformationData;
import com.samsung.remotespen.core.device.fota.Ext1DialogFirmwareUpgradeDriver;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenExt1Factory extends BleSpenDeviceFactory {
    private static final String TAG = "BleSpenExt1Factory";
    private static BleSpenDeviceFeature sDeviceFeature = new Ext1SpenDeviceFeature();
    private static BleSpenApplicationFeature sApplicationFeature = new BleSpenApplicationFeature(R.string.s_pen_pro_model_number, R.string.s_pen_pro_model_name, true, true, true, false, true, false, true, false, new RegulatoryInformationData(R.string.s_pen_pro_fcc_certification, R.string.s_pen_pro_ic, R.string.s_pen_pro_korea, R.drawable.spen_ic_setting_japan_e_label));

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context) {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public WacomChargingDriver getWacomChargingDriver(Context context) {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory
    public BleSpenDriver createBleSpenDriver() {
        return new BleSpenExt1Driver(SpenModelName.EXT1, getDeviceFeature(), new GenericFmmDriver(), new Ext1DialogFirmwareUpgradeDriver());
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
