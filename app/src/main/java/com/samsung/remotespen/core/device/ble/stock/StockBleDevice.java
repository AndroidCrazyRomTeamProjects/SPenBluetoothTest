package com.samsung.remotespen.core.device.ble.stock;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.util.TestHelper;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.UUID;

/* loaded from: classes.dex */
public class StockBleDevice implements IBleDevice {
    private static final String TAG = "StockBleDevice";
    private final BluetoothDevice mDevice;

    public StockBleDevice(BluetoothDevice bluetoothDevice) {
        this.mDevice = bluetoothDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public IBleGatt connectGatt(Context context, boolean z, final IBleGattCallback iBleGattCallback, int i, int i2, ParcelUuid parcelUuid, Handler handler) {
        BluetoothGatt connectGatt;
        final StockBleGatt stockBleGatt = new StockBleGatt(this);
        BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() { // from class: com.samsung.remotespen.core.device.ble.stock.StockBleDevice.1
            @Override // android.bluetooth.BluetoothGattCallback
            public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i3, int i4) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onConnectionStateChange(stockBleGatt, i3, i4);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onServicesDiscovered(stockBleGatt, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onCharacteristicRead(stockBleGatt, bluetoothGattCharacteristic, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onCharacteristicWrite(stockBleGatt, bluetoothGattCharacteristic, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                iBleGattCallback.onCharacteristicChanged(stockBleGatt, bluetoothGattCharacteristic);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onDescriptorRead(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onDescriptorRead(stockBleGatt, bluetoothGattDescriptor, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onDescriptorWrite(stockBleGatt, bluetoothGattDescriptor, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onReliableWriteCompleted(BluetoothGatt bluetoothGatt, int i3) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onReliableWriteCompleted(stockBleGatt, i3);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onReadRemoteRssi(BluetoothGatt bluetoothGatt, int i3, int i4) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onReadRemoteRssi(stockBleGatt, i3, i4);
            }

            @Override // android.bluetooth.BluetoothGattCallback
            public void onMtuChanged(BluetoothGatt bluetoothGatt, int i3, int i4) {
                Assert.e(bluetoothGatt == stockBleGatt.getGatt());
                iBleGattCallback.onMtuChanged(stockBleGatt, i3, i4);
            }
        };
        try {
            if (TestHelper.isRoboUnitTest()) {
                connectGatt = this.mDevice.connectGatt(context, z, bluetoothGattCallback, 2);
            } else {
                connectGatt = this.mDevice.semConnectGatt(context, z, i, i2, parcelUuid, null, bluetoothGattCallback);
            }
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "connectGatt : e=" + e);
            connectGatt = this.mDevice.connectGatt(context, z, bluetoothGattCallback, 2);
        }
        if (connectGatt == null) {
            return null;
        }
        stockBleGatt.setGatt(connectGatt);
        return stockBleGatt;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public int getBondState() {
        return this.mDevice.getBondState();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getAddress() {
        return this.mDevice.getAddress();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getName() {
        return this.mDevice.getName();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getDeviceInfoStr() {
        StringBuilder sb = new StringBuilder();
        String name = getName();
        sb.append("DEVICE : ");
        if (name == null) {
            name = "no-name";
        }
        sb.append(name);
        sb.append(" " + getAddress());
        return sb.toString();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public boolean setAlias(String str) {
        try {
            return this.mDevice.semSetAlias(str);
        } catch (SecurityException e) {
            String str2 = TAG;
            Log.e(str2, "setAlias :" + e.toString());
            return false;
        }
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getAlias() {
        try {
            return this.mDevice.semGetAlias();
        } catch (SecurityException e) {
            String str = TAG;
            Log.e(str, "getAlias :" + e.toString());
            return null;
        }
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public int getConnectionState(Context context) {
        return ((BluetoothManager) context.getSystemService("bluetooth")).getConnectionState(this.mDevice, 8);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public boolean injectCharacteristicChangeEvent(UUID uuid, byte[] bArr) {
        Assert.fail("injectCharacteristicChangeEvent : not supported");
        return false;
    }
}
