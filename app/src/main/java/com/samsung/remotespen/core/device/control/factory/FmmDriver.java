package com.samsung.remotespen.core.device.control.factory;

import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.FmmConfig;
import java.util.UUID;

/* loaded from: classes.dex */
public abstract class FmmDriver {
    public boolean isSupportPolicy;

    public abstract void requestGetFmmConfig(UUID uuid, BleSpenBleDriver bleSpenBleDriver, BleSpenDriver.OperationFinishListener operationFinishListener);

    public abstract void requestSetFmmConfig(UUID uuid, BleSpenBleDriver bleSpenBleDriver, FmmConfig fmmConfig, BleSpenDriver.OperationFinishListener operationFinishListener);
}
