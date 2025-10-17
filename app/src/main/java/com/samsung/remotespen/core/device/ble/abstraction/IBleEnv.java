package com.samsung.remotespen.core.device.ble.abstraction;

import android.content.Context;

/* loaded from: classes.dex */
public interface IBleEnv {
    boolean canHandleAddress(String str);

    IAdvertiser getAdvertiser();

    IBleDevice getBluetoothDevice(Context context, String str);

    void onAttached();

    void onDetached();
}
