package com.samsung.remotespen.core.device.ble.abstraction;

/* loaded from: classes.dex */
public interface IScanResult {
    IBleDevice getDevice();

    byte[] getManufacturerSpecificData(int i);

    int getRssi();

    long getTimestampNanos();
}
