package com.samsung.remotespen.core.device.ble.stock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

/* loaded from: classes.dex */
public class StockUtils {
    private static final String TAG = "StockUtils";

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "getBluetoothAdapter : failed to obtain bluetooth manager", new Exception());
            return null;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "getBluetoothAdapter : failed to obtain BluetoothAdapter.", new Exception());
            return null;
        }
        return adapter;
    }

    public static BluetoothDevice getBluetoothDevice(Context context, String str) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
        if (bluetoothAdapter == null) {
            Log.e(TAG, "getBluetoothDevice : failed to get BT adapter");
            return null;
        }
        return bluetoothAdapter.getRemoteDevice(str);
    }
}
