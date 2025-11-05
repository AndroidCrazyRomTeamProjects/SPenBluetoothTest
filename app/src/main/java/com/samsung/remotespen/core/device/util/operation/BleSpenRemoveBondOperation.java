package com.samsung.remotespen.core.device.util.operation;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.crazyromteam.spenbletest.utils.Assert;

/* loaded from: classes.dex */
public class BleSpenRemoveBondOperation extends BleSpenAsyncOperation {
    private static final String TAG = "BleSpenRemoveBondOperation";
    private static final int TIMEOUT = 10000;
    private BroadcastReceiver mBondStateChangeReceiver;
    private IBleDevice mDevice;
    private FinishListener mFinishListener;

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    public BleSpenRemoveBondOperation(Context context) {
        super(context, TAG);
    }

    public void removeBond(String str, FinishListener finishListener) {
        String str2 = TAG;
        Log.d(str2, "removeBond");
        Context context = getContext();
        this.mFinishListener = finishListener;
        if (startOperation()) {
            startTimer(TIMEOUT);
            IBleDevice bluetoothDevice = BleEnvManager.getInstance(context).getBluetoothDevice(context, str);
            this.mDevice = bluetoothDevice;
            if (bluetoothDevice == null) {
                Log.e(str2, "removeBond : device is null");
                finishOperation(BleOpResultCode.API_CALL_FAIL);
            } else if (bluetoothDevice.getBondState() == 10) {
                Log.v(str2, "already BOND_NONE");
                finishOperation(BleOpResultCode.SUCCESS);
            } else {
                String address = this.mDevice.getAddress();
                if (address == null) {
                    Log.e(str2, "address is null");
                    finishOperation(BleOpResultCode.UNBOND_FAIL);
                    return;
                }
                registerBondStateChangeReceiver();
                BleUtils.removeBond(context, address);
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onFinishOperation(BleOpResultData bleOpResultData) {
        Context context = getContext();
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishOperation : " + resultCode.name());
        BroadcastReceiver broadcastReceiver = this.mBondStateChangeReceiver;
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
            this.mBondStateChangeReceiver = null;
        }
        cancelTimer();
        FinishListener finishListener = this.mFinishListener;
        if (finishListener != null) {
            finishListener.onFinish(bleOpResultData);
            this.mFinishListener = null;
            return;
        }
        Log.e(str, "onFinishOperation : listener is null!");
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onTimerExpired() {
        Log.e(TAG, "onTimerExpired : timer expired");
        finishOperation(BleOpResultCode.TIMEOUT);
    }

    private void registerBondStateChangeReceiver() {
        Assert.e(this.mBondStateChangeReceiver == null);
        this.mBondStateChangeReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenRemoveBondOperation.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                BleSpenRemoveBondOperation.this.onBondStateChanged(intent);
            }
        };
        getContext().registerReceiver(this.mBondStateChangeReceiver, new IntentFilter("com.samsung.bluetooth.device.action.BOND_STATE_CHANGED_FROM_NEARBY_DEVICE"));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBondStateChanged(Intent intent) {
        String action = intent.getAction();
        if (!"com.samsung.bluetooth.device.action.BOND_STATE_CHANGED_FROM_NEARBY_DEVICE".equals(action)) {
            String str = TAG;
            Log.e(str, "onBondStateChanged : unexpected action : " + action);
            return;
        }
        int intExtra = intent.getIntExtra(IBleDevice.EXTRA_BOND_STATE, 10);
        int intExtra2 = intent.getIntExtra("android.bluetooth.device.extra.PREVIOUS_BOND_STATE", 10);
        BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (bluetoothDevice == null) {
            Log.e(TAG, "onBondStateChanged : no device information");
            return;
        }
        String address = this.mDevice.getAddress();
        String address2 = bluetoothDevice.getAddress();
        if (!address.equals(address2)) {
            String str2 = TAG;
            Log.e(str2, "onBondStateChanged : Not my device - " + address2);
            return;
        }
        String str3 = TAG;
        Log.d(str3, "onBondStateChanged : " + BleUtils.convertBondStateToString(intExtra2) + " -> " + BleUtils.convertBondStateToString(intExtra) + " / " + address2);
        if (intExtra != 10) {
            return;
        }
        finishOperation(BleOpResultCode.SUCCESS);
    }
}
