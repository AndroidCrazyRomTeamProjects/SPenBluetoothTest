package com.samsung.remotespen.core.device.ble.stock;

import android.content.Context;
import com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleEnv;

/* loaded from: classes.dex */
public class StockBleEnv implements IBleEnv {
    private StockBleAdvertiser mAdvertiser;

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public boolean canHandleAddress(String str) {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public void onAttached() {
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public void onDetached() {
    }

    public StockBleEnv(Context context) {
        this.mAdvertiser = new StockBleAdvertiser(context);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public IBleDevice getBluetoothDevice(Context context, String str) {
        return new StockBleDevice(StockUtils.getBluetoothDevice(context, str));
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public IAdvertiser getAdvertiser() {
        return this.mAdvertiser;
    }
}
