package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class MtuRequestTransaction extends Transaction {
    private int mMtu;

    public MtuRequestTransaction(int i) {
        this.mMtu = i;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        if (bleSpenBleDriver.isDisconnected()) {
            finish(BleOpResultCode.DISCONNECTED);
        }
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            if (gatt.requestMtu(this.mMtu)) {
                return;
            }
            Log.e(this.TAG, "MtuRequestTransaction : Failed to change MTU");
            finish(BleOpResultCode.API_CALL_FAIL);
            return;
        }
        Log.e(this.TAG, "MtuRequestTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
