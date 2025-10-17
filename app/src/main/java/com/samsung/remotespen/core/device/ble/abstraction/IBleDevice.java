package com.samsung.remotespen.core.device.ble.abstraction;

import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import java.util.UUID;

/* loaded from: classes.dex */
public interface IBleDevice {
    public static final int BOND_BONDED = 12;
    public static final int BOND_BONDING = 11;
    public static final int BOND_NONE = 10;
    public static final String EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE";
    public static final int PHY_LE_1M_MASK = 1;
    public static final int TRANSPORT_LE = 2;

    IBleGatt connectGatt(Context context, boolean z, IBleGattCallback iBleGattCallback, int i, int i2, ParcelUuid parcelUuid, Handler handler);

    String getAddress();

    String getAlias();

    int getBondState();

    int getConnectionState(Context context);

    String getDeviceInfoStr();

    String getName();

    boolean injectCharacteristicChangeEvent(UUID uuid, byte[] bArr);

    boolean setAlias(String str);
}
