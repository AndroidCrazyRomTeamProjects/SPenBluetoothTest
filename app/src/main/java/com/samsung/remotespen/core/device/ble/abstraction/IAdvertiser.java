package com.samsung.remotespen.core.device.ble.abstraction;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import java.util.List;

/* loaded from: classes.dex */
public interface IAdvertiser {
    void onStartScan(List<ScanFilter> list, ScanSettings scanSettings, IScanCallback iScanCallback);

    void onStopScan(IScanCallback iScanCallback);
}
