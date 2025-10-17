package com.samsung.remotespen.util;

import android.content.Context;
import com.samsung.android.bluetooth.SemBluetoothAudioCast;
import com.samsung.android.bluetooth.SemBluetoothCastAdapter;
import com.samsung.android.bluetooth.SemBluetoothCastDevice;
import com.samsung.android.bluetooth.SemBluetoothCastProfile;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class BluetoothAudioCastMonitor {
    private static String TAG = "BluetoothAudioCastMonitor";
    private static BluetoothAudioCastMonitor sInstance;
    private SemBluetoothCastProfile.BluetoothCastProfileListener mAudioCastProfileListener;
    private SemBluetoothAudioCast mBluetoothAudioCast;
    private Context mContext;

    public static BluetoothAudioCastMonitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BluetoothAudioCastMonitor(context);
        }
        return sInstance;
    }

    private BluetoothAudioCastMonitor(Context context) {
        SemBluetoothCastProfile.BluetoothCastProfileListener bluetoothCastProfileListener = new SemBluetoothCastProfile.BluetoothCastProfileListener() { // from class: com.samsung.remotespen.util.BluetoothAudioCastMonitor.1
            public void onServiceConnected(SemBluetoothCastProfile semBluetoothCastProfile) {
                BluetoothAudioCastMonitor.this.mBluetoothAudioCast = (SemBluetoothAudioCast) semBluetoothCastProfile;
            }

            public void onServiceDisconnected() {
                if (BluetoothAudioCastMonitor.this.mBluetoothAudioCast != null) {
                    BluetoothAudioCastMonitor.this.mBluetoothAudioCast = null;
                }
            }
        };
        this.mAudioCastProfileListener = bluetoothCastProfileListener;
        this.mContext = context;
        SemBluetoothAudioCast.getProxy(context, bluetoothCastProfileListener);
    }

    public boolean isAudioCastRunning() {
        if (SemBluetoothCastAdapter.isBluetoothCastSupported()) {
            SemBluetoothCastDevice semBluetoothCastDevice = null;
            SemBluetoothAudioCast semBluetoothAudioCast = this.mBluetoothAudioCast;
            if (semBluetoothAudioCast != null && semBluetoothAudioCast.getConnectedDevices() != null && !this.mBluetoothAudioCast.getConnectedDevices().isEmpty()) {
                semBluetoothCastDevice = (SemBluetoothCastDevice) this.mBluetoothAudioCast.getConnectedDevices().get(0);
            }
            if (semBluetoothCastDevice != null && semBluetoothCastDevice.getLocalDeviceRole() == 2) {
                Log.d(TAG, "isAudioCastRunning : Audio Cast is connected as Host role");
                return true;
            }
        }
        return false;
    }
}
