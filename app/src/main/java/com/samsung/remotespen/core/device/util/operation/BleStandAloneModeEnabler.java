package com.samsung.remotespen.core.device.util.operation;

import android.bluetooth.BluetoothAdapter;
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
import com.samsung.util.debug.Assert;
import com.samsung.util.permission.DefinedPermission;
import com.samsung.util.permission.PermissionUtil;

/* loaded from: classes.dex */
public class BleStandAloneModeEnabler implements BleCancellableOperation {
    private static final long BLE_ON_RETRY_INTERVAL = 10000;
    private static final int DEFAULT_BLE_TURN_ON_TIMEOUT = 30000;
    private static final String TAG = "BleStandAloneModeEnabler";
    private BroadcastReceiver mBleStateChangeReceiver;
    private Context mContext;
    private Listener mFinishListener;
    private long mStartTime;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mBleTurnOnTimeoutHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler.1
        @Override // java.lang.Runnable
        public void run() {
            Log.e(BleStandAloneModeEnabler.TAG, "turnOnBleStandAloneMode : timeout");
            BleStandAloneModeEnabler.this.invokeFinishListener(BleOpResultCode.BLE_NOT_ENABLED);
        }
    };
    private Runnable mBleTurnOnRetryHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler.2
        @Override // java.lang.Runnable
        public void run() {
            Log.d(BleStandAloneModeEnabler.TAG, "turnOnBleStandAloneMode : retry handler");
            BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(BleStandAloneModeEnabler.this.mContext);
            if (bluetoothAdapter == null) {
                Log.e(BleStandAloneModeEnabler.TAG, "turnOnBleStandAloneMode : retry handler : failed to obtain BluetoothAdapter.");
                BleStandAloneModeEnabler.this.invokeFinishListener(BleOpResultCode.BLE_NOT_ENABLED);
            } else if (!bluetoothAdapter.semSetStandAloneBleMode(true)) {
                Log.e(BleStandAloneModeEnabler.TAG, "turnOnBleStandAloneMode : retry handler : Failed to enable stand alone mode.");
                BleStandAloneModeEnabler.this.invokeFinishListener(BleOpResultCode.BLE_NOT_ENABLED);
            } else {
                Log.d(BleStandAloneModeEnabler.TAG, "turnOnBleStandAloneMode : retry handler : BLE ON request success");
            }
        }
    };

    /* loaded from: classes.dex */
    public interface Listener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    public void enableBleStandAloneMode(Context context, Listener listener) {
        enableBleStandAloneMode(context, listener, DEFAULT_BLE_TURN_ON_TIMEOUT);
    }

    public void enableBleStandAloneMode(Context context, Listener listener, int i) {
        this.mFinishListener = listener;
        this.mContext = context.getApplicationContext();
        this.mStartTime = System.currentTimeMillis();
        if (!PermissionUtil.isPermissionsGranted(this.mContext, DefinedPermission.BT)) {
            Log.e(TAG, "enableBleStandAloneMode : denied bluetooth permissions are existed.");
            invokeFinishListener(BleOpResultCode.BLUETOOTH_PERMISSIONS_DENIED);
        } else if (this.mBleStateChangeReceiver != null) {
            Log.e(TAG, "enableBleStandAloneMode : previous task is in progress");
            invokeFinishListener(BleOpResultCode.ALREADY_RUNNING);
        } else {
            BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(this.mContext);
            if (bluetoothAdapter == null) {
                Log.e(TAG, "turnOnBleStandAloneMode : failed to obtain BluetoothAdapter.");
                invokeFinishListener(BleOpResultCode.BLE_NOT_ENABLED);
            } else if (BleUtils.isBleEnabled(this.mContext)) {
                String str = TAG;
                Log.v(str, "turnOnBleStandAloneMode : BLE is enabled");
                if (bluetoothAdapter.semSetStandAloneBleMode(true)) {
                    invokeFinishListener(BleOpResultCode.SUCCESS);
                    return;
                }
                Log.e(str, "turnOnBleStandAloneMode : Failed to enable stand alone mode");
                invokeFinishListener(BleOpResultCode.BLE_NOT_ENABLED);
            } else {
                registerBleStateChangeReceiver();
                this.mHandler.postDelayed(this.mBleTurnOnTimeoutHandler, i);
                String str2 = TAG;
                Log.d(str2, "turnOnBleStandAloneMode : enabling... timeout=" + i);
                if (bluetoothAdapter.semSetStandAloneBleMode(true)) {
                    return;
                }
                Log.e(str2, "turnOnBleStandAloneMode : Failed to enable stand alone mode. retrying..");
                this.mHandler.postDelayed(this.mBleTurnOnRetryHandler, BLE_ON_RETRY_INTERVAL);
            }
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
        unregisterBleStateChangeReceiver();
        this.mHandler.removeCallbacks(this.mBleTurnOnTimeoutHandler);
        this.mHandler.removeCallbacks(this.mBleTurnOnRetryHandler);
        Listener listener = this.mFinishListener;
        if (listener != null) {
            listener.onFinish(bleOpResultData);
            this.mFinishListener = null;
        }
    }

    private void registerBleStateChangeReceiver() {
        Assert.e(this.mBleStateChangeReceiver == null, "BleStateChangeReceiver is not null!");
        this.mBleStateChangeReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler.3
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                BleStandAloneModeEnabler.this.onBleStateChanged(intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBleStateChangeReceiver, intentFilter);
    }

    private void unregisterBleStateChangeReceiver() {
        BroadcastReceiver broadcastReceiver = this.mBleStateChangeReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mBleStateChangeReceiver = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBleStateChanged(Intent intent) {
        String action = intent.getAction();
        if ("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED".equals(action)) {
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10);
            if (intExtra == 15) {
                long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
                String str = TAG;
                Log.d(str, "onBleStateChanged : BLE is turned to ON. elapsed=" + currentTimeMillis);
                invokeFinishListener(BleOpResultCode.SUCCESS);
                return;
            }
            String str2 = TAG;
            Log.d(str2, "onBleStateChanged : BT state changed : " + BleUtils.convertBtStateToString(intExtra));
            return;
        }
        Assert.fail("Unexpected action : " + action);
    }
}
