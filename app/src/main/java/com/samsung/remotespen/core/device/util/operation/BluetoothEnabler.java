package com.samsung.remotespen.core.device.util.operation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.crazyromteam.spenbletest.utils.Assert;

/* loaded from: classes.dex */
public class BluetoothEnabler implements BleCancellableOperation {
    private static final int DEFAULT_BLUETOOTH_TURN_ON_TIMEOUT = 30000;
    private static final String TAG = "BluetoothEnabler";
    private BroadcastReceiver mBluetoothStateChangeReceiver;
    private Context mContext;
    private Listener mFinishListener;
    private long mStartTime;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mBluetoothTurnOnTimeoutHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BluetoothEnabler.1
        @Override // java.lang.Runnable
        public void run() {
            Log.e(BluetoothEnabler.TAG, "turnOnBluetoothMode : timeout");
            BluetoothEnabler.this.invokeFinishListener(BleOpResultCode.TIMEOUT);
        }
    };

    /* loaded from: classes.dex */
    public interface Listener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    public void enableBluetoothMode(Context context, Listener listener) {
        enableBluetoothMode(context, listener, DEFAULT_BLUETOOTH_TURN_ON_TIMEOUT);
    }

    public void enableBluetoothMode(Context context, Listener listener, int i) {
        this.mFinishListener = listener;
        this.mContext = context.getApplicationContext();
        this.mStartTime = System.currentTimeMillis();
        if (this.mBluetoothStateChangeReceiver != null) {
            Log.e(TAG, "enableBluetoothMode : previous task is in progress");
            invokeFinishListener(BleOpResultCode.ALREADY_RUNNING);
        } else if (BleUtils.getBluetoothAdapter(this.mContext) == null) {
            Log.e(TAG, "turnOnBluetoothMode : failed to obtain BluetoothAdapter.");
            invokeFinishListener(BleOpResultCode.BLUETOOTH_NOT_ENABLED);
        } else if (BleUtils.isBluetoothEnabled(this.mContext)) {
            Log.d(TAG, "turnOnBluetoothMode : Bluetooth is enabled");
            invokeFinishListener(BleOpResultCode.SUCCESS);
        } else {
            registerBluetoothStateChangeReceiver();
            this.mHandler.postDelayed(this.mBluetoothTurnOnTimeoutHandler, i);
            String str = TAG;
            Log.d(str, "turnOnBluetoothMode : enabling... timeout=" + i);
            if (BleUtils.setBluetoothEnable(context)) {
                return;
            }
            Log.e(str, "enableBluetoothMode : Can't use Bluetooth");
            invokeFinishListener(BleOpResultCode.API_CALL_FAIL);
        }
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        Log.d(TAG, "cancelOperation");
        invokeFinishListener(BleOpResultCode.CANCELLED);
        if (finishListener != null) {
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeFinishListener(BleOpResultCode bleOpResultCode) {
        invokeFinishListener(new BleOpResultData(bleOpResultCode));
    }

    private void invokeFinishListener(BleOpResultData bleOpResultData) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "invokeFinishListener : result=" + resultCode.name());
        unregisterBluetoothStateChangeReceiver();
        this.mHandler.removeCallbacks(this.mBluetoothTurnOnTimeoutHandler);
        Listener listener = this.mFinishListener;
        if (listener != null) {
            listener.onFinish(bleOpResultData);
            this.mFinishListener = null;
        }
    }

    private void registerBluetoothStateChangeReceiver() {
        Assert.e(this.mBluetoothStateChangeReceiver == null, "BluetoothStateChangeReceiver is not null!");
        this.mBluetoothStateChangeReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.util.operation.BluetoothEnabler.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                BluetoothEnabler.this.onBluetoothStateChanged(intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.mContext.registerReceiver(this.mBluetoothStateChangeReceiver, intentFilter);
    }

    private void unregisterBluetoothStateChangeReceiver() {
        Log.v(TAG, "unregisterBluetoothStateChangeReceiver");
        BroadcastReceiver broadcastReceiver = this.mBluetoothStateChangeReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mBluetoothStateChangeReceiver = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBluetoothStateChanged(Intent intent) {
        String action = intent.getAction();
        String str = TAG;
        Log.v(str, "action = " + action);
        if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
            if (intExtra == 12) {
                Log.d(str, "onBluetoothStateChanged : Bluetooth is turned to ON. elapsed=" + (System.currentTimeMillis() - this.mStartTime));
                invokeFinishListener(BleOpResultCode.SUCCESS);
                return;
            } else if (intExtra == 10) {
                Log.d(str, "onBluetoothStateChanged : Bluetooth is turned to OFF. elapsed=" + (System.currentTimeMillis() - this.mStartTime));
                invokeFinishListener(BleOpResultCode.BLUETOOTH_NOT_ENABLED);
                return;
            } else {
                Log.d(str, "onBluetoothStateChanged : Bluetooth state changed : " + BleUtils.convertBtStateToString(intExtra));
                return;
            }
        }
        Assert.fail("Unexpected action : " + action);
    }
}
