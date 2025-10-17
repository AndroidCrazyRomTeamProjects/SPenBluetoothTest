package com.samsung.remotespen.core.device.control.factory;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import java.util.UUID;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class WriteCharacteristicTransaction extends Transaction {
    private BluetoothGattCharacteristic mCharacteristic;
    private UUID mCharacteristicUuid;
    private byte[] mData;
    private UUID mServiceUuid;

    public WriteCharacteristicTransaction(UUID uuid, UUID uuid2, byte[] bArr) {
        this.mServiceUuid = uuid;
        this.mCharacteristicUuid = uuid2;
        this.mData = bArr;
    }

    public WriteCharacteristicTransaction(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.mCharacteristic = bluetoothGattCharacteristic;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        boolean requestCharacteristicWrite;
        if (bleSpenBleDriver.isDisconnected()) {
            finish(BleOpResultCode.DISCONNECTED);
        }
        IBleGatt gatt = bleSpenBleDriver.getGatt();
        if (gatt != null) {
            BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mCharacteristic;
            if (bluetoothGattCharacteristic != null) {
                requestCharacteristicWrite = bleSpenBleDriver.requestCharacteristicWrite(gatt, bluetoothGattCharacteristic);
            } else {
                requestCharacteristicWrite = bleSpenBleDriver.requestCharacteristicWrite(gatt, this.mServiceUuid, this.mCharacteristicUuid, this.mData);
            }
            if (requestCharacteristicWrite) {
                return;
            }
            Log.e(this.TAG, "WriteCharacteristicTransaction : Failed to write characteristic");
            finish(BleOpResultCode.API_CALL_FAIL);
            return;
        }
        Log.e(this.TAG, "WriteCharacteristicTransaction : Gatt is null!");
        finish(BleOpResultCode.GATT_NULL);
    }
}
