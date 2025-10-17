package com.samsung.remotespen.core.device.control.factory;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.CommonUtils;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Assert;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class DeviceOpenTransaction extends Transaction {
    private static String SEM_ACTION_BOND_STATE_CHANGED = null;
    private static final String TAG = "DeviceOpenTransaction";
    private boolean mAutoReconnect;
    private BroadcastReceiver mBondStateChangeReceiver;
    private Context mContext;
    private IBleDevice mDevice;
    private BleSpenBleDriver mDriver;
    private IBleGattCallback mGattCallback;
    private boolean mIsBuiltInTypeSpen;
    private boolean mIsGattConnected = false;

    static {
        try {
            SEM_ACTION_BOND_STATE_CHANGED = (String) ReflectionUtils.getStaticObjectField(BluetoothDevice.class, "SEM_ACTION_BOND_STATE_CHANGED_FROM_NEARBY_DEVICE");
        } catch (Exception unused) {
            Log.e(TAG, "static : SEM_ACTION_BOND_STATE_CHANGED_FROM_NEARBY_DEVICE is not present!");
            SEM_ACTION_BOND_STATE_CHANGED = "com.samsung.bluetooth.device.action.BOND_STATE_CHANGED_FROM_NEARBY_DEVICE";
        }
    }

    public DeviceOpenTransaction(Context context, IBleDevice iBleDevice, boolean z, boolean z2, IBleGattCallback iBleGattCallback) {
        this.mContext = context;
        this.mDevice = iBleDevice;
        this.mGattCallback = iBleGattCallback;
        this.mAutoReconnect = z;
        this.mIsBuiltInTypeSpen = z2;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        int bondState = this.mDevice.getBondState();
        String address = this.mDevice.getAddress();
        if (bondState != 12) {
            String str = TAG;
            Log.i(str, "DeviceOpenTransaction : " + address + " is not paired device. state=" + bondStateToStr(bondState));
        }
        registerBondStateChangeReceiver();
        IBleGatt connectGatt = this.mDevice.connectGatt(this.mContext, this.mAutoReconnect, this.mGattCallback, 2, 1, BleUtils.getSpenDeviceTypeUuid(this.mIsBuiltInTypeSpen), null);
        if (connectGatt != null) {
            this.mDriver = bleSpenBleDriver;
            bleSpenBleDriver.setGatt(connectGatt);
            return;
        }
        Log.e(TAG, "DeviceOpenTransaction : doWork : Failed to connect GATT");
        finish(BleOpResultCode.GATT_NULL);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void onFinish(BleOpResultData bleOpResultData, long j) {
        BroadcastReceiver broadcastReceiver = this.mBondStateChangeReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mBondStateChangeReceiver = null;
        }
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        if (resultCode != BleOpResultCode.SUCCESS) {
            String str = TAG;
            Log.e(str, "DeviceOpenTransaction : not success result code(" + resultCode + ") - closing the gatt..");
            if (this.mDriver != null) {
                CommonUtils.sleep(500L);
                this.mDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.DeviceOpenTransaction.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData2, long j2) {
                        String str2 = DeviceOpenTransaction.TAG;
                        Log.d(str2, "onFinish : driver close finished. result=" + bleOpResultData2.getResultCode());
                    }
                });
            }
        }
        super.onFinish(bleOpResultData, j);
    }

    public void onGattConnected(int i) {
        int bondState = this.mDevice.getBondState();
        String str = TAG;
        Log.i(str, "DeviceOpenTransaction : onGattConnected : bound state=" + bondStateToStr(bondState) + " gattStatus=" + i);
        this.mIsGattConnected = true;
        switch (bondState) {
            case 10:
            case 11:
                return;
            case 12:
                finish(BleOpResultCode.SUCCESS);
                return;
            default:
                Log.e(str, "DeviceOpenTransaction : unexpected bonding state : " + bondState);
                finish(BleOpResultCode.API_CALL_FAIL);
                return;
        }
    }

    private void registerBondStateChangeReceiver() {
        Assert.e(this.mBondStateChangeReceiver == null);
        this.mBondStateChangeReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.factory.DeviceOpenTransaction.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                DeviceOpenTransaction.this.onBondStateChanged(intent);
            }
        };
        this.mContext.registerReceiver(this.mBondStateChangeReceiver, new IntentFilter(SEM_ACTION_BOND_STATE_CHANGED));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBondStateChanged(Intent intent) {
        String action = intent.getAction();
        if (!SEM_ACTION_BOND_STATE_CHANGED.equals(action)) {
            String str = TAG;
            Log.e(str, "DeviceOpenTransaction : onBondStateChanged : unexpected action : " + action);
            return;
        }
        int intExtra = intent.getIntExtra(IBleDevice.EXTRA_BOND_STATE, 10);
        BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (bluetoothDevice == null) {
            Log.e(TAG, "DeviceOpenTransaction : onBondStateChanged : no device information");
            return;
        }
        String address = this.mDevice.getAddress();
        String address2 = bluetoothDevice.getAddress();
        if (!address.equals(address2)) {
            String str2 = TAG;
            Log.e(str2, "DeviceOpenTransaction : onBondStateChanged : Not my device - " + address2);
            return;
        }
        String str3 = TAG;
        Log.i(str3, "DeviceOpenTransaction : onBondStateChanged : " + bondStateToStr(intExtra) + " / " + address2);
        if (!this.mIsGattConnected) {
            Log.d(str3, "DeviceOpenTransaction : GATT is not connected yet");
        } else if (intExtra == 10) {
            finish(BleOpResultCode.BOND_FAIL);
        } else if (intExtra != 12) {
        } else {
            finish(BleOpResultCode.SUCCESS);
        }
    }

    private String bondStateToStr(int i) {
        return BleUtils.convertBondStateToString(i);
    }
}
