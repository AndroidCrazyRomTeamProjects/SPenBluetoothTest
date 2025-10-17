package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class ConnectTransaction extends Transaction {
    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            gatt.connect();
            return;
        }
        Log.e(this.TAG, "ConnectTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
