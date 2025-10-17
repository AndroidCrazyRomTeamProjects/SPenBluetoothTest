package com.samsung.remotespen.core.device.control.factory;

import com.samsung.remotespen.core.device.data.BleSpenScanFilter;

/* loaded from: classes.dex */
public abstract class BleSpenDeviceFeature {
    public static String TAG = "BleSpenDeviceFeature";

    public abstract int getMaxMotionMovementValue();

    public abstract int getMinChargeDurationForSpenBoot(boolean z);

    public abstract int getMotionStayRangeThreshold();

    public abstract String getPenTypeString();

    public abstract BleSpenScanFilter getScanFilter();

    public abstract boolean isBundleTypeSpen();

    public abstract boolean isSupportAutoConnect();

    public abstract boolean isSupportButtonDownUpEvent();

    public abstract boolean isSupportChargeState();

    public abstract boolean isSupportEasyConnect();

    public abstract boolean isSupportFmmConfigPolicy();

    public abstract boolean isSupportLed();

    public abstract boolean isSupportLogExtraction();

    public abstract boolean isSupportMotionSensor();

    public abstract boolean isSupportPenColorCode();

    public abstract boolean isSupportPenFrequencyChange();

    public abstract boolean isSupportPenTipApproachDetection();

    public abstract boolean isSupportSecondaryButton();

    public abstract boolean isSupportSpenChargeNotification();

    public abstract boolean isSupportStandbyMode();

    public abstract boolean isSupportWacomCharger();

    public abstract boolean shouldChangeSlaveLatencyOnLowBattery();

    public abstract boolean shouldPerformWacomDspResetOnLinkLossAndLowBattery();
}
