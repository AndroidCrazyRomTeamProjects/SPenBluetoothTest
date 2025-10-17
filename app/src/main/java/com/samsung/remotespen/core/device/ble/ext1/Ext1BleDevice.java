package com.samsung.remotespen.core.device.ble.ext1;

import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import java.util.UUID;

/* loaded from: classes.dex */
public class Ext1BleDevice implements IBleDevice {
    private String mBdAddress;
    private Ext1BleGatt mGatt;

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public int getBondState() {
        return 12;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getName() {
        return "Virtual S Pen Pro";
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public boolean setAlias(String str) {
        return true;
    }

    public Ext1BleDevice(Context context, String str) {
        this.mBdAddress = str;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public IBleGatt connectGatt(Context context, boolean z, IBleGattCallback iBleGattCallback, int i, int i2, ParcelUuid parcelUuid, Handler handler) {
        Ext1BleGatt ext1BleGatt = new Ext1BleGatt(context, this, iBleGattCallback);
        this.mGatt = ext1BleGatt;
        ext1BleGatt.connect();
        return this.mGatt;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getAddress() {
        return this.mBdAddress;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getDeviceInfoStr() {
        return getName() + " / " + getAddress();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public String getAlias() {
        return getName();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public int getConnectionState(Context context) {
        Ext1BleGatt ext1BleGatt = this.mGatt;
        if (ext1BleGatt == null) {
            return 0;
        }
        return ext1BleGatt.getConnectionState();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleDevice
    public boolean injectCharacteristicChangeEvent(UUID uuid, byte[] bArr) {
        Ext1BleGatt ext1BleGatt = this.mGatt;
        if (ext1BleGatt == null) {
            return false;
        }
        return ext1BleGatt.injectCharacteristicChangeEvent(uuid, bArr);
    }
}
