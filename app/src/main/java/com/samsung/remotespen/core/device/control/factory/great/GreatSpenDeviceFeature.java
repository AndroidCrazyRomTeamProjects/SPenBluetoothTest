package com.samsung.remotespen.core.device.control.factory.great;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;

/* loaded from: classes.dex */
public class GreatSpenDeviceFeature extends BleSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMaxMotionMovementValue() {
        return 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMinChargeDurationForSpenBoot(boolean z) {
        return 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMotionStayRangeThreshold() {
        return 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public String getPenTypeString() {
        return "generic non-BLE Spen device";
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public BleSpenScanFilter getScanFilter() {
        return null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isBundleTypeSpen() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportAutoConnect() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportButtonDownUpEvent() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportChargeState() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportEasyConnect() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportFmmConfigPolicy() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportLed() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportLogExtraction() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportMotionSensor() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportPenColorCode() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportPenFrequencyChange() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportPenTipApproachDetection() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportSecondaryButton() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportSpenChargeNotification() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportStandbyMode() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportWacomCharger() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean shouldChangeSlaveLatencyOnLowBattery() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean shouldPerformWacomDspResetOnLinkLossAndLowBattery() {
        return false;
    }
}
