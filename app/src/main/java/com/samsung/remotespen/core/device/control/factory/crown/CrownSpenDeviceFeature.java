package com.samsung.remotespen.core.device.control.factory.crown;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;

/* loaded from: classes.dex */
public class CrownSpenDeviceFeature extends BleSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMaxMotionMovementValue() {
        return 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMinChargeDurationForSpenBoot(boolean z) {
        return 30000;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMotionStayRangeThreshold() {
        return 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public String getPenTypeString() {
        return "crown";
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
        return true;
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
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean shouldChangeSlaveLatencyOnLowBattery() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean shouldPerformWacomDspResetOnLinkLossAndLowBattery() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public BleSpenScanFilter getScanFilter() {
        return new BleSpenScanFilter(BleSpenCrownDriver.UUID_SPEN_SERVICE);
    }
}
