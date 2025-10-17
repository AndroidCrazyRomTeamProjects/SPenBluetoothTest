package com.samsung.remotespen.core.device.control.factory.ext1;

import android.bluetooth.le.ScanFilter;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.top.RemoteSpenMainController;

/* loaded from: classes.dex */
public class Ext1SpenDeviceFeature extends BleSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMaxMotionMovementValue() {
        return 1350;
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
        return "ext1";
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isBundleTypeSpen() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportAutoConnect() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportButtonDownUpEvent() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportChargeState() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportEasyConnect() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportFmmConfigPolicy() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportLed() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportLogExtraction() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportMotionSensor() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportPenColorCode() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportPenFrequencyChange() {
        return true;
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

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public BleSpenScanFilter getScanFilter() {
        return new BleSpenScanFilter(new BleSpenScanFilter.FilterBuilder() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.Ext1SpenDeviceFeature.1
            @Override // com.samsung.remotespen.core.device.data.BleSpenScanFilter.FilterBuilder
            public void applyFilter(ScanFilter.Builder builder) {
                builder.setManufacturerData(BleUtils.getSamsungManufacturerId(), new byte[]{16});
            }
        });
    }
}
