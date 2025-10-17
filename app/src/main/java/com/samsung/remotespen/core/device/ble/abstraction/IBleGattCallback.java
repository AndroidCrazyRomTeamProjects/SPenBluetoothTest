package com.samsung.remotespen.core.device.ble.abstraction;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/* loaded from: classes.dex */
public class IBleGattCallback {
    public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
    }

    public void onCharacteristicRead(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
    }

    public void onCharacteristicWrite(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
    }

    public void onConnectionStateChange(IBleGatt iBleGatt, int i, int i2) {
    }

    public void onDescriptorRead(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
    }

    public void onDescriptorWrite(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
    }

    public void onMtuChanged(IBleGatt iBleGatt, int i, int i2) {
    }

    public void onReadRemoteRssi(IBleGatt iBleGatt, int i, int i2) {
    }

    public void onReliableWriteCompleted(IBleGatt iBleGatt, int i) {
    }

    public void onServicesDiscovered(IBleGatt iBleGatt, int i) {
    }
}
