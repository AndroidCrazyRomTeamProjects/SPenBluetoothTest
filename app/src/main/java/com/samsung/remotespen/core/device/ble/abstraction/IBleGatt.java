package com.samsung.remotespen.core.device.ble.abstraction;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import java.util.UUID;

/* loaded from: classes.dex */
public interface IBleGatt {
    public static final int CONNECTION_PRIORITY_BALANCED = 0;
    public static final int CONNECTION_PRIORITY_HIGH = 1;
    public static final int GATT_SUCCESS = 0;

    void close();

    void connect();

    void disconnect();

    boolean discoverServices();

    IBleDevice getDevice();

    BluetoothGattService getService(UUID uuid);

    boolean readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic);

    boolean readRemoteRssi();

    boolean requestConnectionPriority(int i);

    boolean requestMtu(int i);

    boolean setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z);

    boolean writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic);

    void writeDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor);
}
