package com.samsung.remotespen.core.device.control.behavior.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController;
import com.samsung.remotespen.core.device.control.connection.SpenAdvertiseMonitor;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleDisconnReqData;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.util.BatteryPolicy;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.constants.CommonIntent;

/* loaded from: classes.dex */
public abstract class AbsPenBehaviorPolicy {
    private static final String TAG = "AbsPenBehaviorPolicy";
    public Callback mCallback;
    public BleSpenDriver.ConnectionState mConnectionState;
    public Context mContext;
    public BleSpenInstanceId mInstanceId;
    public int mLastBatteryLevel;
    public BleSpenDriver.ConnectionState mPrevConnectionState;
    public ReconnectionController mReconnectionController;
    public SpenInsertionEventDetector mSpenInsertionEventDetector;
    public Handler mHandler = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = AbsPenBehaviorPolicy.TAG;
            Log.d(str, "onReceive : action = " + action);
            if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                AbsPenBehaviorPolicy.this.onAirplaneModeChanged(intent.getBooleanExtra("state", false));
            } else if ("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED".equals(action)) {
                if (!intent.hasExtra("android.bluetooth.adapter.extra.STATE")) {
                    Log.e(AbsPenBehaviorPolicy.TAG, "onReceive : EXTRA_STATE is not present!");
                }
                int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
                AbsPenBehaviorPolicy.this.onBleStateChanged(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10), intExtra);
            } else if (CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED.equals(action)) {
                AbsPenBehaviorPolicy.this.onEmergencyStateChanged(intent.getIntExtra(CommonIntent.INTENT_EXTRA_REASON, 0));
            }
        }
    };
    private BleSpenDriver.EventListener mEventListener = new BleSpenDriver.EventListener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.2
        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onChargeStateChanged(BleSpenChargeState bleSpenChargeState) {
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onLedStateChanged(BleSpenLedState bleSpenLedState) {
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onPenFrequencyChanged(BleSpenFrequency bleSpenFrequency) {
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onConnectionStateChanged(BleSpenDriver.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
            AbsPenBehaviorPolicy.this.onConnectionStateChanged(connectionState, bleStateChangeInfo);
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onBatteryLevelChanged(int i, int i2) {
            AbsPenBehaviorPolicy.this.mLastBatteryLevel = i;
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onNotifyBatteryState(BleSpenBatteryState bleSpenBatteryState) {
            AbsPenBehaviorPolicy.this.onBatteryStateChanged(bleSpenBatteryState);
        }
    };

    /* loaded from: classes.dex */
    public interface Callback {
        void cancelConnectionRetry();

        void closeBleSpenDriver(BleOpResultData bleOpResultData, BleSpenDriver.OperationFinishListener operationFinishListener);

        void disconnect(BleDisconnReqData bleDisconnReqData, BleSpenDriver.OperationFinishListener operationFinishListener);

        boolean isBleSpenAutoConnectionEnabled();

        boolean isConnected();

        boolean isConnecting();

        boolean isDisconnected();

        void reserveConnectionRetry();

        void setDriverEventListener(BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback);

        void setReconnectReason(boolean z);

        void setSpenOperationMode(BleSpenOperationMode bleSpenOperationMode, BleSpenDriver.OperationFinishListener operationFinishListener);

        boolean startPenConnectionTransaction(BleConnReqData bleConnReqData);
    }

    public abstract void doReserveReconnection(int i, ReconnectionController.ReconnectReason reconnectReason);

    public abstract boolean isSupportReconnection();

    public abstract void onBatteryStateChanged(BleSpenBatteryState bleSpenBatteryState);

    public AbsPenBehaviorPolicy(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, Callback callback) {
        this.mContext = context;
        this.mInstanceId = bleSpenInstanceId;
        this.mCallback = callback;
        this.mReconnectionController = new ReconnectionController(this.mContext, callback);
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(this.mContext);
    }

    public void start() {
        this.mCallback.setDriverEventListener(this.mEventListener, null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intentFilter.addAction(CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void stop() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBleStateChanged(int i, int i2) {
        String str = TAG;
        Log.i(str, "onBleStateChanged :" + BleUtils.convertBtStateToString(i2) + " -> " + BleUtils.convertBtStateToString(i));
        if (i == 10) {
            if (SettingsPreferenceManager.getInstance(this.mContext).getAirActionEnabled()) {
                if (this.mReconnectionController.canReserveReconnection()) {
                    this.mReconnectionController.reserveReconnection(this.mInstanceId.getSpenModelName(), SpenInstanceIdHelper.from(this.mContext, this.mInstanceId).getSpenAddress(), 1000, ReconnectionController.ReconnectReason.BLE_OFF);
                }
                this.mReconnectionController.setBleOffReason(null);
                return;
            }
            Log.e(str, "onBleStateChanged : Air action is disabled");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAirplaneModeChanged(boolean z) {
        String str = TAG;
        Log.i(str, "onAirplaneModeChanged : isAirplaneMode=" + z);
        if (z) {
            this.mReconnectionController.setBleOffReason(ReconnectionController.BleOffReason.AIRPLANE);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onEmergencyStateChanged(int i) {
        String str = TAG;
        Log.i(str, "onEmergencyStateChanged : state = " + i);
        if (i == 2) {
            Log.d(str, "onEmergencyStateChanged : Enabling the emergency mode..");
            this.mReconnectionController.setBleOffReason(ReconnectionController.BleOffReason.UPSM);
        }
    }

    public void onConnectionStateChanged(BleSpenDriver.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
        this.mPrevConnectionState = this.mConnectionState;
        this.mConnectionState = connectionState;
        if (!isSupportReconnection()) {
            Log.d(TAG, "onConnectionStateChanged : not support auto reconnection");
        } else if (!SpenAdvertiseMonitor.canSpenAdvertise(this.mContext, this.mInstanceId.getSpenModelName())) {
            Log.d(TAG, "onConnectionStateChanged : Can't advertise");
        } else {
            ReconnectionController.ReconnectReason convertStatus = bleStateChangeInfo != null ? ReconnectionController.convertStatus(bleStateChangeInfo.getGattStatusCode()) : null;
            if (!isLinkLossState(convertStatus)) {
                Log.d(TAG, "onConnectionStateChanged : Not Link Loss state");
            } else if (!isConnectableBatteryState()) {
                Log.d(TAG, "onConnectionStateChanged : Not workable battery state");
            } else {
                Log.i(TAG, "onConnectionStateChanged : Disconnected ");
                reserveReconnect(convertStatus);
            }
        }
    }

    private void reserveReconnect(final ReconnectionController.ReconnectReason reconnectReason) {
        final int resetWacomDsp = isNeedResetWacomDsp() ? resetWacomDsp() : 1500;
        String str = TAG;
        Log.d(str, "reserveReconnect : Reconnection will be started after " + resetWacomDsp + "ms");
        if (this.mConnectionState != BleSpenDriver.ConnectionState.DISCONNECTED) {
            Log.e(str, "reserveReconnect : Not disconnected state. curState = " + this.mConnectionState);
        }
        BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
        bleOpResultData.setMessage("Force connection close due to application request");
        this.mCallback.closeBleSpenDriver(bleOpResultData, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.3
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData2, long j) {
                AbsPenBehaviorPolicy.this.doReserveReconnection(resetWacomDsp, reconnectReason);
            }
        });
    }

    private boolean isLinkLossState(ReconnectionController.ReconnectReason reconnectReason) {
        if (this.mPrevConnectionState != BleSpenDriver.ConnectionState.CONNECTED) {
            String str = TAG;
            Log.d(str, "isLinkLossState : prevState = " + this.mPrevConnectionState);
            return false;
        } else if (this.mConnectionState != BleSpenDriver.ConnectionState.DISCONNECTED) {
            String str2 = TAG;
            Log.d(str2, "isLinkLossState : newState = " + this.mConnectionState);
            return false;
        } else if (ReconnectionController.ReconnectReason.LINK_LOSS == reconnectReason || ReconnectionController.ReconnectReason.LMP_RESPONSE_TIMEOUT == reconnectReason) {
            String str3 = TAG;
            Log.d(str3, "isLinkLossState : reason = " + reconnectReason);
            return true;
        } else {
            return false;
        }
    }

    private boolean isConnectableBatteryState() {
        int deviceBatteryTemperature = CommonUtils.getDeviceBatteryTemperature(this.mContext);
        if (BatteryPolicy.isWorkableTemperature(deviceBatteryTemperature) || this.mLastBatteryLevel > 1) {
            return true;
        }
        String str = TAG;
        Log.d(str, "isConnectableBatteryState : reconnection is blocked due to temperature and pen battery level.temperature = " + deviceBatteryTemperature);
        return false;
    }

    private boolean isNeedResetWacomDsp() {
        BleSpenDeviceFactory bleSpenDeviceFactory = BleSpenDeviceFactory.getInstance(this.mInstanceId.getSpenModelName());
        if (this.mLastBatteryLevel > 1 || !bleSpenDeviceFactory.getDeviceFeature().shouldPerformWacomDspResetOnLinkLossAndLowBattery()) {
            return false;
        }
        Log.d(TAG, "isNeedResetWacomDsp : Low battery state. reset the WACOM DSP");
        return true;
    }

    private int resetWacomDsp() {
        WacomChargingDriver wacomChargingDriver = BleSpenDeviceFactory.getInstance(this.mInstanceId.getSpenModelName()).getWacomChargingDriver(this.mContext);
        if (wacomChargingDriver != null) {
            return Math.max(1500, wacomChargingDriver.resetWacomDsp(false));
        }
        return 1500;
    }
}
