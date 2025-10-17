package com.samsung.remotespen.core.device.ble.stock;

import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IScanResult;

/* loaded from: classes.dex */
public class StockScanResult implements IScanResult {
    private ScanResult mScanResult;

    public StockScanResult(ScanResult scanResult) {
        this.mScanResult = scanResult;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public IBleDevice getDevice() {
        return new StockBleDevice(this.mScanResult.getDevice());
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public byte[] getManufacturerSpecificData(int i) {
        ScanRecord scanRecord = this.mScanResult.getScanRecord();
        return scanRecord == null ? new byte[0] : scanRecord.getManufacturerSpecificData(i);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public int getRssi() {
        return this.mScanResult.getRssi();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanResult
    public long getTimestampNanos() {
        return this.mScanResult.getTimestampNanos();
    }
}
