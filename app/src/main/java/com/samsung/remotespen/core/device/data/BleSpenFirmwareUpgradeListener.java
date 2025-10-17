package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public interface BleSpenFirmwareUpgradeListener {
    void onFinish(BleOpResultCode bleOpResultCode);

    void onPrepared();

    void onProgress(int i);
}
