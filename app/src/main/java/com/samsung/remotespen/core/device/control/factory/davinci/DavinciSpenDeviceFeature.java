package com.samsung.remotespen.core.device.control.factory.davinci;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.top.RemoteSpenMainController;

/* loaded from: classes.dex */
public class DavinciSpenDeviceFeature extends BleSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMaxMotionMovementValue() {
        return 2700;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMinChargeDurationForSpenBoot(boolean z) {
        return 30000;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMotionStayRangeThreshold() {
        return RemoteSpenMainController.GESTURE_DURATION_LIMIT;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public String getPenTypeString() {
        return "davinci";
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isBundleTypeSpen() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportAutoConnect() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportButtonDownUpEvent() {
        return true;
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
        return true;
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
        return true;
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
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean shouldPerformWacomDspResetOnLinkLossAndLowBattery() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public BleSpenScanFilter getScanFilter() {
        return new BleSpenScanFilter(BleSpenDavinciDriver.UUID_SPEN_SERVICE);
    }
}
