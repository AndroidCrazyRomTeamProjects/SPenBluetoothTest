package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class DisconnectTransaction extends Transaction {
    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            if (bleSpenBleDriver.getGattConnectionState() == 0) {
                Log.d(this.TAG, "DisconnectTransaction : already disconnected");
                finish(BleOpResultCode.SUCCESS);
                return;
            }
            gatt.disconnect();
            return;
        }
        Log.e(this.TAG, "DisconnectTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
