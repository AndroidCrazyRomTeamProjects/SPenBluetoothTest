package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class ReadRemoteRssiTransaction extends Transaction {
    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        if (bleSpenBleDriver.isDisconnected()) {
            finish(BleOpResultCode.DISCONNECTED);
        }
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            if (gatt.readRemoteRssi()) {
                return;
            }
            Log.e(this.TAG, "ReadRemoteRssiTransaction : Failed to read remote rssi");
            finish(BleOpResultCode.API_CALL_FAIL);
            return;
        }
        Log.e(this.TAG, "ReadRemoteRssiTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
