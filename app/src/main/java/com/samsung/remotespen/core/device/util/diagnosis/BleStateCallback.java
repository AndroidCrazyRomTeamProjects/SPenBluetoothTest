package com.samsung.remotespen.core.device.util.diagnosis;

/* loaded from: classes.dex */
public interface BleStateCallback {
    int getBatteryLevel();

    String getFirmwareVersion();

    boolean isConnected();
}
