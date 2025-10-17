package com.samsung.remotespen.core.device.control.factory.rainbow;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.canvas.CanvasSpenDeviceFeature;

/* loaded from: classes.dex */
public class RainbowSpenDeviceFeature extends CanvasSpenDeviceFeature {
    @Override // com.samsung.remotespen.core.device.control.factory.canvas.CanvasSpenDeviceFeature, com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public String getPenTypeString() {
        return "rainbow";
    }

    @Override // com.samsung.remotespen.core.device.control.factory.canvas.CanvasSpenDeviceFeature, com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportFmmConfigPolicy() {
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.canvas.CanvasSpenDeviceFeature, com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature
    public boolean isSupportStandbyMode() {
        return true;
    }

    static {
        BleSpenDeviceFeature.TAG = RainbowSpenDeviceFeature.class.getSimpleName();
    }
}
