package com.samsung.remotespen.core.device.control.behavior.policy;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleConnTriggerCode;
import com.samsung.remotespen.core.device.data.BleDisconnReqData;
import com.samsung.remotespen.core.device.data.BleDisconnTriggerCode;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.core.device.util.AlarmTimer;
import com.samsung.remotespen.util.SpenInsertionEventDetector;

/* loaded from: classes.dex */
public class StandbyBehaviorPolicy extends AbsPenBehaviorPolicy {
    private static final long RECONNECTION_TIME_OUT = 180000;
    private static final long RECONNECTION_TRIGGER_PERIOD = 45000;
    private static final String TAG = "StandbyBehaviorPolicy";
    private Runnable mConnectionAction;
    private SpenInsertionEventDetector.Listener mInsertionEventListener;
    private ReconnectionController.ReconnectReason mReconnectReason;
    private AlarmTimer mRetryReconnectionTimer;
    private Object mSyncObj;
    private WacomChargingDriver mWacomChargingDriver;

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public boolean isSupportReconnection() {
        return true;
    }

    public StandbyBehaviorPolicy(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        super(context, bleSpenDriver, bleSpenInstanceId, callback);
        this.mSyncObj = new Object();
        this.mConnectionAction = new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.1
            @Override // java.lang.Runnable
            public void run() {
                if (!StandbyBehaviorPolicy.this.canConnect()) {
                    Log.d(StandbyBehaviorPolicy.TAG, "mConnectionAction : Do Nothing.");
                    return;
                }
                BleConnTriggerCode bleConnTriggerCode = StandbyBehaviorPolicy.this.mSpenInsertionEventDetector.isInserted() ? BleConnTriggerCode.PEN_INSERTION : BleConnTriggerCode.PEN_DETACH;
                if (StandbyBehaviorPolicy.this.mCallback.isDisconnected()) {
                    String str = StandbyBehaviorPolicy.TAG;
                    Log.d(str, "mConnectionAction : Disconnected state. Trying to connect to attached pen : triggerCode = " + bleConnTriggerCode);
                    StandbyBehaviorPolicy standbyBehaviorPolicy = StandbyBehaviorPolicy.this;
                    StandbyBehaviorPolicy.this.mCallback.startPenConnectionTransaction(new BleConnReqData(StandbyBehaviorPolicy.this.mInstanceId.getSpenModelName(), bleConnTriggerCode, SpenInstanceIdHelper.from(standbyBehaviorPolicy.mContext, standbyBehaviorPolicy.mInstanceId).getSpenAddress()));
                } else if (StandbyBehaviorPolicy.this.mCallback.isConnecting()) {
                    Log.d(StandbyBehaviorPolicy.TAG, "mConnectionAction : Connecting state");
                    StandbyBehaviorPolicy.this.mCallback.reserveConnectionRetry();
                } else {
                    Log.d(StandbyBehaviorPolicy.TAG, "mConnectionAction : Connected state");
                }
            }
        };
        this.mInsertionEventListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.2
            @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
            public void onInsertEvent(boolean z) {
                if (StandbyBehaviorPolicy.this.canConnect()) {
                    String str = StandbyBehaviorPolicy.TAG;
                    Log.d(str, "onSpenInsertEvent : Try to connect. inserted = " + z);
                    StandbyBehaviorPolicy standbyBehaviorPolicy = StandbyBehaviorPolicy.this;
                    standbyBehaviorPolicy.mHandler.removeCallbacks(standbyBehaviorPolicy.mConnectionAction);
                    StandbyBehaviorPolicy standbyBehaviorPolicy2 = StandbyBehaviorPolicy.this;
                    standbyBehaviorPolicy2.mHandler.post(standbyBehaviorPolicy2.mConnectionAction);
                }
            }
        };
        this.mWacomChargingDriver = BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName()).getWacomChargingDriver(context);
        this.mRetryReconnectionTimer = new AlarmTimer(this.mContext, this.mSyncObj);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void start() {
        super.start();
        String str = TAG;
        Log.i(str, "start");
        this.mSpenInsertionEventDetector.registerListener(this.mInsertionEventListener);
        if (this.mCallback.isConnected()) {
            this.mCallback.setSpenOperationMode(BleSpenOperationMode.STANDBY, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.3
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str2 = StandbyBehaviorPolicy.TAG;
                    Log.d(str2, "start : setSpenOperationMode(" + BleSpenOperationMode.STANDBY + ") result = " + bleOpResultData.isSuccess());
                }
            });
        }
        if (this.mSpenInsertionEventDetector.isInserted() && canConnect()) {
            Log.d(str, "start : connect");
            this.mHandler.removeCallbacks(this.mConnectionAction);
            this.mHandler.post(this.mConnectionAction);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void stop() {
        super.stop();
        Log.i(TAG, "stop");
        this.mSpenInsertionEventDetector.unregisterListener(this.mInsertionEventListener);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void onBatteryStateChanged(BleSpenBatteryState bleSpenBatteryState) {
        String str = TAG;
        Log.i(str, "onBatteryStateChanged : state = " + bleSpenBatteryState);
        Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.4
            @Override // java.lang.Runnable
            public void run() {
                if (StandbyBehaviorPolicy.this.mSpenInsertionEventDetector.isInserted()) {
                    StandbyBehaviorPolicy.this.mCallback.disconnect(new BleDisconnReqData(BleDisconnTriggerCode.STANDBY), new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.4.1
                        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                        public void onFinish(BleOpResultData bleOpResultData, long j) {
                            StandbyBehaviorPolicy.this.mCallback.closeBleSpenDriver(new BleOpResultData(BleOpResultCode.SUCCESS), null);
                        }
                    });
                }
            }
        };
        if (BleSpenBatteryState.FULL.equals(bleSpenBatteryState)) {
            this.mHandler.post(runnable);
        } else if (BleSpenBatteryState.NOT_FULL.equals(bleSpenBatteryState)) {
            Log.i(str, "charging for standby policy retry 100% charging for standby mode");
            this.mWacomChargingDriver.startCharge();
        }
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void onConnectionStateChanged(BleSpenDriver.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
        super.onConnectionStateChanged(connectionState, bleStateChangeInfo);
        if (BleSpenDriver.ConnectionState.CONNECTED == connectionState) {
            String str = TAG;
            Log.d(str, "onConnectionStateChanged connectionState = " + connectionState);
            cancelReconnectTimer();
        }
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy
    public void doReserveReconnection(int i, ReconnectionController.ReconnectReason reconnectReason) {
        Log.d(TAG, "doReserveReconnection");
        this.mCallback.setReconnectReason(true);
        this.mReconnectReason = reconnectReason;
        reserveReconnect(i);
        this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.5
            @Override // java.lang.Runnable
            public void run() {
                StandbyBehaviorPolicy.this.cancelReconnectTimer();
            }
        }, RECONNECTION_TIME_OUT);
        reserveReconnectTimer();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveReconnect(int i) {
        String str = TAG;
        Log.d(str, "reserveReconnect : waitDuration = " + i + ", reason = " + this.mReconnectReason);
        this.mReconnectionController.reserveReconnection(this.mInstanceId.getSpenModelName(), SpenInstanceIdHelper.from(this.mContext, this.mInstanceId).getSpenAddress(), i, this.mReconnectReason);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelReconnectTimer() {
        Log.d(TAG, "cancelReconnectTimer");
        this.mRetryReconnectionTimer.stopTimer(false);
        this.mReconnectReason = null;
        this.mCallback.setReconnectReason(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveReconnectTimer() {
        Log.d(TAG, "reserveReconnectTimer");
        this.mRetryReconnectionTimer.reserveTimer(RECONNECTION_TRIGGER_PERIOD, new AlarmTimer.Listener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.6
            @Override // com.samsung.remotespen.core.device.util.AlarmTimer.Listener
            public void onTimerExpired(long j) {
                StandbyBehaviorPolicy.this.reserveReconnect(0);
                StandbyBehaviorPolicy.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy.6.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (StandbyBehaviorPolicy.this.mReconnectReason != null) {
                            StandbyBehaviorPolicy.this.reserveReconnectTimer();
                        }
                    }
                });
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canConnect() {
        if (this.mCallback.isConnected() || this.mCallback.isConnecting()) {
            Log.d(TAG, "canConnect : already connected or connecting");
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
