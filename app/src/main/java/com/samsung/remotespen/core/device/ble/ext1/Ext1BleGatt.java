package com.samsung.remotespen.core.device.ble.ext1;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import java.util.UUID;

/* loaded from: classes.dex */
public class Ext1BleGatt implements IBleGatt {
    private static final byte BATTERY_LEVEL = 90;
    private static final int RSSI = -60;
    private static final String TAG = "Ext1BleGatt";
    private final IBleGattCallback mCallback;
    private final Ext1BleDevice mDevice;
    private int mConnState = 0;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void close() {
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean requestConnectionPriority(int i) {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        return true;
    }

    public Ext1BleGatt(Context context, Ext1BleDevice ext1BleDevice, IBleGattCallback iBleGattCallback) {
        this.mDevice = ext1BleDevice;
        this.mCallback = iBleGattCallback;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public IBleDevice getDevice() {
        return this.mDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public BluetoothGattService getService(UUID uuid) {
        BluetoothGattService bluetoothGattService = new BluetoothGattService(uuid, 0);
        addCharacteristic(bluetoothGattService, BleSpenUuid.BUTTON_EVENT);
        addCharacteristic(bluetoothGattService, BleSpenUuid.BATTERY_LEVEL);
        addCharacteristic(bluetoothGattService, BleSpenUuid.BATTERY_LEVEL_RAW);
        addCharacteristic(bluetoothGattService, BleSpenUuid.FW_VER);
        addCharacteristic(bluetoothGattService, BleSpenUuid.MODE);
        addCharacteristic(bluetoothGattService, BleSpenUuid.CHARGE_STATUS);
        addCharacteristic(bluetoothGattService, BleSpenUuid.SELF_TEST);
        addCharacteristic(bluetoothGattService, BleSpenUuid.RAW_SENSOR_DATA);
        addCharacteristic(bluetoothGattService, BleSpenUuid.EASY_CONNECT_ID);
        addCharacteristic(bluetoothGattService, BleSpenUuid.FMM_CONFIG);
        addCharacteristic(bluetoothGattService, BleSpenUuid.LED_STATE);
        addCharacteristic(bluetoothGattService, BleSpenUuid.PEN_TIP_APPROACH);
        addCharacteristic(bluetoothGattService, BleSpenUuid.PEN_LOG);
        addCharacteristic(bluetoothGattService, BleSpenUuid.PEN_FREQUENCY);
        addCharacteristic(bluetoothGattService, BleSpenUuid.OBFUSCATION_TABLE);
        addCharacteristic(bluetoothGattService, BleSpenUuid.PEN_SYNC_CLOCK);
        return bluetoothGattService;
    }

    private void addCharacteristic(BluetoothGattService bluetoothGattService, UUID uuid) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(uuid, 0, 0);
        bluetoothGattCharacteristic.addDescriptor(new BluetoothGattDescriptor(BleSpenUuid.CHARACTERISTIC_CONFIG, 0));
        bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean readCharacteristic(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.1
            @Override // java.lang.Runnable
            public void run() {
                UUID uuid = bluetoothGattCharacteristic.getUuid();
                BluetoothGattCharacteristic bluetoothGattCharacteristic2 = new BluetoothGattCharacteristic(uuid, 0, 0);
                if (uuid.equals(BleSpenUuid.BATTERY_LEVEL)) {
                    bluetoothGattCharacteristic2.setValue(new byte[]{Ext1BleGatt.BATTERY_LEVEL});
                } else if (uuid.equals(BleSpenUuid.FW_VER)) {
                    bluetoothGattCharacteristic2.setValue(new byte[]{116, 101, 115, 116, 86, 101, 114});
                } else {
                    bluetoothGattCharacteristic2.setValue(new byte[0]);
                }
                Ext1BleGatt.this.mCallback.onCharacteristicRead(Ext1BleGatt.this, bluetoothGattCharacteristic2, 0);
            }
        });
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean writeCharacteristic(final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.2
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mCallback.onCharacteristicWrite(Ext1BleGatt.this, new BluetoothGattCharacteristic(bluetoothGattCharacteristic.getUuid(), 0, 0), 0);
            }
        });
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void writeDescriptor(final BluetoothGattDescriptor bluetoothGattDescriptor) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.3
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mCallback.onDescriptorWrite(Ext1BleGatt.this, bluetoothGattDescriptor, 0);
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void connect() {
        this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.4
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mConnState = 2;
                IBleGattCallback iBleGattCallback = Ext1BleGatt.this.mCallback;
                Ext1BleGatt ext1BleGatt = Ext1BleGatt.this;
                iBleGattCallback.onConnectionStateChange(ext1BleGatt, 0, ext1BleGatt.mConnState);
            }
        }, 500L);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public void disconnect() {
        if (this.mConnState != 2) {
            return;
        }
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.5
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mConnState = 0;
                IBleGattCallback iBleGattCallback = Ext1BleGatt.this.mCallback;
                Ext1BleGatt ext1BleGatt = Ext1BleGatt.this;
                iBleGattCallback.onConnectionStateChange(ext1BleGatt, 0, ext1BleGatt.mConnState);
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean discoverServices() {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.6
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mCallback.onServicesDiscovered(Ext1BleGatt.this, 0);
            }
        });
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean requestMtu(final int i) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.7
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mCallback.onMtuChanged(Ext1BleGatt.this, i, 0);
            }
        });
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGatt
    public boolean readRemoteRssi() {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.8
            @Override // java.lang.Runnable
            public void run() {
                Ext1BleGatt.this.mCallback.onReadRemoteRssi(Ext1BleGatt.this, Ext1BleGatt.RSSI, 0);
            }
        });
        return true;
    }

    public int getConnectionState() {
        return this.mConnState;
    }

    public boolean injectCharacteristicChangeEvent(final UUID uuid, final byte[] bArr) {
        if (this.mCallback == null) {
            return false;
        }
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleGatt.9
            @Override // java.lang.Runnable
            public void run() {
                if (Ext1BleGatt.this.mConnState != 2) {
                    Log.e(Ext1BleGatt.TAG, "injectCharacteristicChangeEvent : not connected");
                    return;
                }
                BluetoothGattCharacteristic bluetoothGattCharacteristic = new BluetoothGattCharacteristic(uuid, 0, 0);
                bluetoothGattCharacteristic.setValue(bArr);
                Ext1BleGatt.this.mCallback.onCharacteristicChanged(Ext1BleGatt.this, bluetoothGattCharacteristic);
            }
        });
        return true;
    }
}
