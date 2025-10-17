package com.samsung.remotespen.core.device.control.behavior.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleConnTriggerCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.util.SpenInsertionEventDetector;

/* loaded from: classes.dex */
public class NormalBehaviorPolicy extends AbsPenBehaviorPolicy {
    private static final String TAG = "NormalBehaviorPolicy";
    private Runnable mConnectionAction;
    private BroadcastReceiver mPowerBroadcastReceiver;

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public boolean isSupportReconnection() {
        return true;
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void onBatteryStateChanged(BleSpenBatteryState bleSpenBatteryState) {
    }

    public NormalBehaviorPolicy(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        super(context, bleSpenDriver, bleSpenInstanceId, callback);
        this.mPowerBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.NormalBehaviorPolicy.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                action.hashCode();
                if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                    NormalBehaviorPolicy.this.onPowerConnectStateChanged(false);
                } else if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                    NormalBehaviorPolicy.this.onPowerConnectStateChanged(true);
                }
            }
        };
        this.mConnectionAction = new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.NormalBehaviorPolicy.2
            @Override // java.lang.Runnable
            public void run() {
                if (!NormalBehaviorPolicy.this.canConnect()) {
                    Log.d(NormalBehaviorPolicy.TAG, "mConnectionAction : Do Nothing.");
                } else if (NormalBehaviorPolicy.this.mCallback.isDisconnected()) {
                    Log.d(NormalBehaviorPolicy.TAG, "mConnectionAction : Disconnected state. Trying to connect to attached pen");
                    NormalBehaviorPolicy normalBehaviorPolicy = NormalBehaviorPolicy.this;
                    NormalBehaviorPolicy.this.mCallback.startPenConnectionTransaction(new BleConnReqData(NormalBehaviorPolicy.this.mInstanceId.getSpenModelName(), BleConnTriggerCode.PEN_INSERTION, SpenInstanceIdHelper.from(normalBehaviorPolicy.mContext, normalBehaviorPolicy.mInstanceId).getSpenAddress()));
                } else if (NormalBehaviorPolicy.this.mCallback.isConnecting()) {
                    Log.d(NormalBehaviorPolicy.TAG, "mConnectionAction : Connecting state");
                    NormalBehaviorPolicy.this.mCallback.reserveConnectionRetry();
                } else {
                    Log.d(NormalBehaviorPolicy.TAG, "mConnectionAction : Connected state");
                }
            }
        };
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void onConnectionStateChanged(BleSpenDriver.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
        super.onConnectionStateChanged(connectionState, bleStateChangeInfo);
        if (BleSpenDriver.ConnectionState.CONNECTING == connectionState || BleSpenDriver.ConnectionState.CONNECTED == connectionState) {
            String str = TAG;
            Log.d(str, "onConnectionStateChanged connectionState = " + connectionState);
            this.mCallback.setReconnectReason(false);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void doReserveReconnection(int i, ReconnectionController.ReconnectReason reconnectReason) {
        String str = TAG;
        Log.d(str, "doReserveReconnection : waitDuration = " + i + ", reason = " + reconnectReason);
        this.mCallback.setReconnectReason(true);
        this.mReconnectionController.reserveReconnection(this.mInstanceId.getSpenModelName(), SpenInstanceIdHelper.from(this.mContext, this.mInstanceId).getSpenAddress(), i, reconnectReason);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void start() {
        super.start();
        Log.i(TAG, "start");
        if (this.mCallback.isConnected()) {
            this.mCallback.setSpenOperationMode(BleSpenOperationMode.DEFAULT, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.NormalBehaviorPolicy.3
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str = NormalBehaviorPolicy.TAG;
                    Log.d(str, "start : setSpenOperationMode(" + BleSpenOperationMode.DEFAULT + ") result = " + bleOpResultData.isSuccess());
                }
            });
        }
        if (canConnect()) {
            this.mHandler.removeCallbacks(this.mConnectionAction);
            this.mHandler.post(this.mConnectionAction);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        this.mContext.registerReceiver(this.mPowerBroadcastReceiver, intentFilter);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void stop() {
        Log.i(TAG, "stop");
        this.mHandler.removeCallbacks(this.mConnectionAction);
        this.mContext.unregisterReceiver(this.mPowerBroadcastReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPowerConnectStateChanged(boolean z) {
        if (z) {
            if (!canConnect()) {
                Log.d(TAG, "onPowerConnectStateChanged : canConnect is false");
                return;
            }
            Log.d(TAG, "onPowerConnectStateChanged : Try to connect after 1000ms");
            this.mHandler.postDelayed(this.mConnectionAction, 1000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canConnect() {
        if (this.mCallback.isConnected() || this.mCallback.isConnecting()) {
            Log.d(TAG, "canConnect : already connected or connecting");
            return false;
        }
        SpenInsertionEventDetector spenInsertionEventDetector = this.mSpenInsertionEventDetector;
        if (spenInsertionEventDetector != null && !spenInsertionEventDetector.isInserted()) {
            Log.d(TAG, "canConnect : spen is detached.");
            return false;
        }
        boolean isBleSpenAutoConnectionEnabled = this.mCallback.isBleSpenAutoConnectionEnabled();
        if (isBleSpenAutoConnectionEnabled) {
            return true;
        }
        String str = TAG;
        Log.d(str, "canConnect : auto connection is disabled. isBleSpenAutoConnectionEnabled = " + isBleSpenAutoConnectionEnabled);
        return false;
    }
}
