package com.samsung.remotespen.core.device.ble.ext1;

import android.os.SystemClock;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IScanResult;

/* loaded from: classes.dex */
public class Ext1ScanResult implements IScanResult {
    public static final byte[] MANUFACTURE_SEPECIFIC_DATA = new byte[20];
    private static final int SCAN_RSSI = -60;
    private final Ext1BleDevice mDevice;
    private final long mTimestampInNano = SystemClock.elapsedRealtimeNanos();

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public int getRssi() {
        return SCAN_RSSI;
    }

    static {
        int[] iArr = {16, 16, 63, 66, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 20; i++) {
            MANUFACTURE_SEPECIFIC_DATA[i] = (byte) (iArr[i] & 255);
        }
    }

    public Ext1ScanResult(IBleDevice iBleDevice) {
        this.mDevice = (Ext1BleDevice) iBleDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public IBleDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public byte[] getManufacturerSpecificData(int i) {
        return MANUFACTURE_SEPECIFIC_DATA;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public long getTimestampNanos() {
        return this.mTimestampInNano;
    }
}
