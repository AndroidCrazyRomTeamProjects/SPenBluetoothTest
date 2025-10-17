package com.samsung.remotespen.core.device.control.factory.canvas;

import android.bluetooth.le.ScanFilter;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.top.RemoteSpenMainController;

/* loaded from: classes.dex */
public class CanvasSpenDeviceFeature extends BleSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMaxMotionMovementValue() {
        return 1350;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMinChargeDurationForSpenBoot(boolean z) {
        return z ? 30000 : 50000;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public int getMotionStayRangeThreshold() {
        return RemoteSpenMainController.GESTURE_DURATION_LIMIT;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public String getPenTypeString() {
        return "canvas";
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
        return new BleSpenScanFilter(new BleSpenScanFilter.FilterBuilder() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.CanvasSpenDeviceFeature.1
            @Override // com.samsung.remotespen.core.device.data.BleSpenScanFilter.FilterBuilder
            public void applyFilter(ScanFilter.Builder builder) {
                builder.setManufacturerData(BleUtils.getSamsungManufacturerId(), new byte[]{1, 83, 80, 69, 78, 48, 50});
            }
        });
    }
}
