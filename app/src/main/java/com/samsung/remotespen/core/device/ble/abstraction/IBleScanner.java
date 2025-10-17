package com.samsung.remotespen.core.device.ble.abstraction;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import java.util.List;

/* loaded from: classes.dex */
public interface IBleScanner {
    void startScan(List<ScanFilter> list, ScanSettings scanSettings, IScanCallback iScanCallback);

    void stopScan(IScanCallback iScanCallback);
}
