package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import java.util.UUID;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class ReadCharacteristicTransaction extends Transaction {
    private UUID mCharacteristicUuid;
    private UUID mServiceUuid;

    public ReadCharacteristicTransaction(UUID uuid, UUID uuid2) {
        this.mServiceUuid = uuid;
        this.mCharacteristicUuid = uuid2;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        if (bleSpenBleDriver.isDisconnected()) {
            finish(BleOpResultCode.DISCONNECTED);
        }
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            if (bleSpenBleDriver.requestCharacteristicRead(gatt, this.mServiceUuid, this.mCharacteristicUuid)) {
                return;
            }
            Log.e(this.TAG, "ReadCharacteristicTransaction : Failed to read characteristic");
            finish(BleOpResultCode.API_CALL_FAIL);
            return;
        }
        Log.e(this.TAG, "ReadCharacteristicTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
