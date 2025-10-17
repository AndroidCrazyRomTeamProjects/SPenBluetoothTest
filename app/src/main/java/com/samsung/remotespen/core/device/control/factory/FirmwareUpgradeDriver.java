package com.samsung.remotespen.core.device.control.factory;

import com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener;
import java.io.File;

/* loaded from: classes.dex */
public interface FirmwareUpgradeDriver {
    void startFirmwareUpgrade(BleSpenDriver bleSpenDriver, File file, BleSpenFirmwareUpgradeListener bleSpenFirmwareUpgradeListener);
}
