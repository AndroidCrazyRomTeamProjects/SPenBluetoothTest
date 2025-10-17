package com.samsung.remotespen.core.device.ble.abstraction;

/* loaded from: classes.dex */
public interface IScanCallback {
    void onScanFailed(int i);

    void onScanResult(IScanResult iScanResult);
}
