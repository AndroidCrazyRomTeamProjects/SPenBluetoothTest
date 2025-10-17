package com.samsung.remotespen.external;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.external.CommandDispatcher;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BatteryCmdDispatcher extends CommandDispatcher {
    private static final String BUNDLE_KEY_BATTERY_LEVEL = "batteryLevel";
    private static final String BUNDLE_KEY_CHARGING_STATE = "state";
    private static final String CMD_GET_BATTERY_LEVEL = "getBleSpenBatteryLevel";
    private static final String CMD_GET_CHARGE_STATE = "getBleSpenChargeState";
    private static final String CMD_REMOVE_BATTERY_LEVEL_CHANGE_CALLBACK = "removeBleSpenBatteryLevelChangeCallback";
    private static final String CMD_SEND_FAST_DISCHARGE_CMD = "sendFastDischargeCmd";
    private static final String CMD_SET_BATTERY_LEVEL_CHANGE_CALLBACK = "setBleSpenBatteryLevelChangeCallback";
    private static final String TAG = "BatteryCmdDispatcher";
    private ArrayList<Transaction> mBatteryLevelMonitoringTransactionList;
    private BleSpenManager mBleSpenMgr;
    private BleSpenManager.ChargeStateChangeListener mChargingStateListener;
    private BleSpenManager.StateListener mStateListener;

    public BatteryCmdDispatcher(Context context, BleSpenManager bleSpenManager, CommandDispatcher.IDispatchEnvironment iDispatchEnvironment) {
        super(context, iDispatchEnvironment);
        this.mStateListener = new BleSpenManager.StateListener() { // from class: com.samsung.remotespen.external.BatteryCmdDispatcher.1
            @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
            public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenManager.ConnectionState connectionState, BleSpenManager.ConnectionState connectionState2, BleSpenManager.StateChangeInfo stateChangeInfo) {
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
            public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
                Iterator it = BatteryCmdDispatcher.this.mBatteryLevelMonitoringTransactionList.iterator();
                while (it.hasNext()) {
                    Transaction transaction = (Transaction) it.next();
                    BleSpenInstanceId targetSpenInstanceId = BatteryCmdDispatcher.this.getTargetSpenInstanceId(transaction);
                    if (targetSpenInstanceId != null && BatteryCmdDispatcher.this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId) && bleSpenInstanceId.equals(targetSpenInstanceId)) {
                        BatteryCmdDispatcher.this.sendSuccessResponse(transaction, BatteryCmdDispatcher.this.makeBatteryLevelBundle(BatteryCmdDispatcher.this.getSpenInstanceUidString(targetSpenInstanceId), BatteryCmdDispatcher.this.mBleSpenMgr.getChargeState(targetSpenInstanceId), i, targetSpenInstanceId.isBundledSpen()));
                        String str = BatteryCmdDispatcher.TAG;
                        Log.d(str, "onBatteryLevelChanged : name :" + bleSpenInstanceId.getSpenModelName() + "/ newLevel " + i);
                    }
                }
            }
        };
        this.mChargingStateListener = new BleSpenManager.ChargeStateChangeListener() { // from class: com.samsung.remotespen.external.BatteryCmdDispatcher.2
            @Override // com.samsung.remotespen.core.device.BleSpenManager.ChargeStateChangeListener
            public void onChargeStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenChargeState bleSpenChargeState) {
                Iterator it = BatteryCmdDispatcher.this.mBatteryLevelMonitoringTransactionList.iterator();
                while (it.hasNext()) {
                    Transaction transaction = (Transaction) it.next();
                    BleSpenInstanceId targetSpenInstanceId = BatteryCmdDispatcher.this.getTargetSpenInstanceId(transaction);
                    if (targetSpenInstanceId != null && BatteryCmdDispatcher.this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId) && bleSpenInstanceId.equals(targetSpenInstanceId)) {
                        BatteryCmdDispatcher.this.sendSuccessResponse(transaction, BatteryCmdDispatcher.this.makeBatteryLevelBundle(BatteryCmdDispatcher.this.getSpenInstanceUidString(targetSpenInstanceId), bleSpenChargeState, BatteryCmdDispatcher.this.mBleSpenMgr.getBatteryLevel(targetSpenInstanceId), targetSpenInstanceId.isBundledSpen()));
                        String str = BatteryCmdDispatcher.TAG;
                        Log.d(str, "onChargeStateChanged : name :" + bleSpenInstanceId.getSpenModelName() + "/ charging state " + bleSpenChargeState.toString());
                    }
                }
            }
        };
        this.mContext = context;
        this.mBleSpenMgr = bleSpenManager;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void init() {
        this.mBleSpenMgr.registerListener(this.mStateListener, null);
        this.mBleSpenMgr.registerChargeStateChangeListener(this.mChargingStateListener);
        this.mBatteryLevelMonitoringTransactionList = new ArrayList<>();
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void release() {
        this.mBleSpenMgr.unregisterListener(this.mStateListener, null);
        this.mBleSpenMgr.unregisterChargeStateChangeListener(this.mChargingStateListener);
        this.mBatteryLevelMonitoringTransactionList = null;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public boolean dispatchCommand(Transaction transaction) {
        String str = (String) transaction.mCommand;
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -2096883550:
                if (str.equals(CMD_GET_BATTERY_LEVEL)) {
                    c = 0;
                    break;
                }
                break;
            case -1501054148:
                if (str.equals(CMD_SEND_FAST_DISCHARGE_CMD)) {
                    c = 1;
                    break;
                }
                break;
            case 734920169:
                if (str.equals(CMD_REMOVE_BATTERY_LEVEL_CHANGE_CALLBACK)) {
                    c = 2;
                    break;
                }
                break;
            case 1413830066:
                if (str.equals(CMD_GET_CHARGE_STATE)) {
                    c = 3;
                    break;
                }
                break;
            case 1992797643:
                if (str.equals(CMD_SET_BATTERY_LEVEL_CHANGE_CALLBACK)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                onRequestBatteryLevel(transaction);
                return true;
            case 1:
                onRequestFastDischargeCmd(transaction);
                return true;
            case 2:
                onRequestUnregisterBatteryLevelChangeCallback(transaction);
                return true;
            case 3:
                onRequestChargeState(transaction);
                return true;
            case 4:
                onRequestRegisterBatteryLevelChangeCallback(transaction);
                return true;
            default:
                return false;
        }
    }

    private void onRequestUnregisterBatteryLevelChangeCallback(Transaction transaction) {
        if (transaction.mReplyTo == null) {
            Log.i(TAG, "onRequestUnregisterBatteryLevelChangeCallback : ERR_MSG_INVALID_PARAMETER");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
            return;
        }
        Iterator<Transaction> it = this.mBatteryLevelMonitoringTransactionList.iterator();
        while (it.hasNext()) {
            Transaction next = it.next();
            if (next.isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                this.mBatteryLevelMonitoringTransactionList.remove(next);
                sendSuccessResponse(transaction, null);
                int size = this.mBatteryLevelMonitoringTransactionList.size();
                String str = TAG;
                Log.i(str, "onRequestUnregisterBatteryLevelChangeCallback : size = " + size);
                return;
            }
        }
        Log.e(TAG, "onRequestUnregisterBatteryLevelChangeCallback : can't found callback");
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
    }

    private void onRequestRegisterBatteryLevelChangeCallback(Transaction transaction) {
        clearInvalidCallbackList(this.mBatteryLevelMonitoringTransactionList);
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (!isConnectedToBleSpen(targetSpenInstanceId)) {
            sendNotConnectedResponse(transaction);
        } else if (transaction.mReplyTo == null) {
            Log.e(TAG, "onRequestRegisterBatteryLevelChangeCallback : messenger is null");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
        } else {
            Iterator<Transaction> it = this.mBatteryLevelMonitoringTransactionList.iterator();
            while (it.hasNext()) {
                if (it.next().isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                    Log.e(TAG, "onRequestRegisterBatteryLevelChangeCallback : already registered callback");
                    sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_ALREADY_REGISTERED);
                    return;
                }
            }
            int batteryLevel = this.mBleSpenMgr.getBatteryLevel(targetSpenInstanceId);
            this.mBatteryLevelMonitoringTransactionList.add(transaction);
            int size = this.mBatteryLevelMonitoringTransactionList.size();
            sendSuccessResponse(transaction, makeBatteryLevelBundle(getSpenInstanceUidString(targetSpenInstanceId), this.mBleSpenMgr.getChargeState(targetSpenInstanceId), batteryLevel, targetSpenInstanceId.isBundledSpen()));
            String str = TAG;
            Log.i(str, "onRequestRegisterBatteryLevelChangeCallback : battery : + " + batteryLevel + " / size :  " + size);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bundle makeBatteryLevelBundle(String str, BleSpenChargeState bleSpenChargeState, int i, boolean z) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", str);
        bundle.putInt(BUNDLE_KEY_BATTERY_LEVEL, i);
        if (z && i == 100) {
            bleSpenChargeState = BleSpenChargeState.DISCHARGING;
        }
        bundle.putString(BUNDLE_KEY_CHARGING_STATE, bleSpenChargeState.toString());
        return bundle;
    }

    private void onRequestBatteryLevel(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else {
            BleSpenManager bleSpenManager = this.mBleSpenMgr;
            if (bleSpenManager == null) {
                sendBleSpenNotSupportedResponse(transaction);
            } else {
                sendSuccessResponse(transaction, Integer.valueOf(bleSpenManager.getBatteryLevel(targetSpenInstanceIdWithDefault)));
            }
        }
    }

    private void onRequestChargeState(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else {
            BleSpenManager bleSpenManager = this.mBleSpenMgr;
            if (bleSpenManager == null) {
                sendBleSpenNotSupportedResponse(transaction);
                return;
            }
            BleSpenChargeState chargeState = bleSpenManager.getChargeState(targetSpenInstanceIdWithDefault);
            String str = TAG;
            Log.d(str, "onRequest Charge State=" + chargeState);
            sendSuccessResponse(transaction, chargeState);
        }
    }

    private void onRequestFastDischargeCmd(final Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else {
            Integer num = (Integer) transaction.getParameterFromBundle(BindingApiConstants.BUNDLE_KEY_DISCHARGE_CMD);
            if (num == null) {
                String str = TAG;
                Log.e(str, "onRequestBleSpenFastDischargeCmd : Invalid parameter : dischargeCmd is missing");
                sendErrorResponse(transaction, "Invalid parameter : dischargeCmd is missing");
                return;
            }
            String str2 = TAG;
            Log.v(str2, "onRequestBleSpenFastDischargeCmd : cmd=" + num);
            if (this.mBleSpenMgr == null) {
                sendBleSpenNotSupportedResponse(transaction);
                return;
            }
            this.mBleSpenMgr.sendFastDischargeCommand(this.mPairedSpenManager.getBundledSpenInstanceId(), num.intValue(), new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.BatteryCmdDispatcher.3
                @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                public void onFinish(BleSpenManager.OpResultData opResultData) {
                    String str3 = BatteryCmdDispatcher.TAG;
                    Log.d(str3, "onRequestBleSpenFastDischargeCmd : success=" + opResultData.isSuccess());
                    if (opResultData.isSuccess()) {
                        BatteryCmdDispatcher.this.sendSuccessResponse(transaction, null);
                    } else {
                        BatteryCmdDispatcher.this.sendErrorResponse(transaction, opResultData.getResultCode().toString());
                    }
                }
            });
        }
    }
}
