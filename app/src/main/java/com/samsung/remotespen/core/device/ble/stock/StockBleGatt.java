package com.samsung.remotespen.core.device.ble.stock;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.util.debug.Assert;
import java.util.UUID;

/* loaded from: classes.dex */
public class StockBleGatt implements IBleGatt {
    private StockBleDevice mDevice;
    private BluetoothGatt mGatt;

    public StockBleGatt(StockBleDevice stockBleDevice) {
        Assert.notNull(stockBleDevice);
        this.mDevice = stockBleDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public IBleDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean requestConnectionPriority(int i) {
        return this.mGatt.requestConnectionPriority(i);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public BluetoothGattService getService(UUID uuid) {
        return this.mGatt.getService(uuid);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return this.mGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        return this.mGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        return this.mGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void writeDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor) {
        this.mGatt.writeDescriptor(bluetoothGattDescriptor);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void close() {
        this.mGatt.close();
        this.mGatt = null;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void connect() {
        this.mGatt.connect();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void disconnect() {
        this.mGatt.disconnect();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean discoverServices() {
        return this.mGatt.discoverServices();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean requestMtu(int i) {
        return this.mGatt.requestMtu(i);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean readRemoteRssi() {
        return this.mGatt.readRemoteRssi();
    }

    public void setGatt(BluetoothGatt bluetoothGatt) {
        Assert.notNull(bluetoothGatt);
        Assert.e(this.mGatt == null);
        this.mGatt = bluetoothGatt;
    }

    public BluetoothGatt getGatt() {
        return this.mGatt;
    }
}
