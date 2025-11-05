package com.samsung.remotespen.core.device.ble.ext1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleEnv;
import com.samsung.remotespen.external.BindingApiConstants;
import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class Ext1BleEnv implements IBleEnv {
    private static final String TAG = "Ext1BleEnv";
    private final IAdvertiser mAdvertiser;
    private final Context mContext;
    private final Ext1BleDevice mDevice;
    private boolean mIsAttached = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public Ext1BleEnv(Context context, String str) {
        this.mContext = context.getApplicationContext();
        Ext1BleDevice ext1BleDevice = new Ext1BleDevice(context, str);
        this.mDevice = ext1BleDevice;
        this.mAdvertiser = new Ext1Advertiser(ext1BleDevice);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public IBleDevice getBluetoothDevice(Context context, String str) {
        boolean equals = this.mDevice.getAddress().equals(str);
        Assert.e(equals, "BD address not matched : " + this.mDevice.getAddress() + ", " + str);
        return this.mDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public IAdvertiser getAdvertiser() {
        return this.mAdvertiser;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public boolean canHandleAddress(String str) {
        return str.equals(this.mDevice.getAddress());
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public void onAttached() {
        Log.d(TAG, "onAttached");
        this.mIsAttached = true;
        this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleEnv.1
            @Override // java.lang.Runnable
            public void run() {
                if (Ext1BleEnv.this.mIsAttached) {
                    Ext1BleEnv.this.sendEasyConnectRequest();
                }
            }
        }, 1000L);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleEnv
    public void onDetached() {
        this.mIsAttached = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendEasyConnectRequest() {
        Log.d(TAG, "sendEasyConnectRequest");
        ServiceConnection serviceConnection = new ServiceConnection() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1BleEnv.2
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Bundle bundle = new Bundle();
                bundle.putString(BindingApiConstants.BUNDLE_KEY_CMD, "connectSpen");
                bundle.putString("bdAddress", Ext1BleEnv.this.mDevice.getAddress());
                bundle.putByteArray("adData", Ext1ScanResult.MANUFACTURE_SEPECIFIC_DATA);
                Message obtain = Message.obtain();
                obtain.setData(bundle);
                try {
                    new Messenger(iBinder).send(obtain);
                } catch (RemoteException e) {
                    String str = Ext1BleEnv.TAG;
                    Log.e(str, "onServiceConnected : e=" + e, e);
                }
                Ext1BleEnv.this.mContext.unbindService(this);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                String str = Ext1BleEnv.TAG;
                Log.d(str, "onServiceDisconnected : " + componentName);
            }
        };
        try {
            Intent intent = new Intent();
            intent.setClassName(this.mContext.getPackageName(), "com.samsung.remotespen.external.RemoteSpenInternalBindingService");
            this.mContext.bindService(intent, serviceConnection, 1);
        } catch (SecurityException e) {
            String str = TAG;
            Log.e(str, "sendEasyConnectRequest : Failed to start Ble Spen service " + e);
        }
    }
}
