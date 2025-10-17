package com.samsung.remotespen.external;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.control.connection.advertisement.AdvertisementData;
import com.samsung.remotespen.core.device.control.connection.advertisement.AdvertisementDataParser;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleConnTriggerCode;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.external.CommandDispatcher;
import com.samsung.remotespen.ui.settings.ManualPairingActivity;
import com.samsung.util.CommonUtils;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenGarageTipType;
import com.samsung.util.usage.SAUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public class ConnectionCmdDispatcher extends CommandDispatcher {
    private static final String BUNDLE_KEY_PARAM_ADV_DATA = "adData";
    private static final String BUNDLE_KEY_PARAM_BD_ADDRESS = "bdAddress";
    private static final String BUNDLE_KEY_PARAM_STATE = "state";
    private static final String BUNDLE_KEY_PARAM_TRIGGER = "trigger";
    private static final String BUNDLE_KEY_RESULT_VALUE_ACCOUNT_MATCHED = "accountMatched";
    private static final String BUNDLE_KEY_RESULT_VALUE_ACTION = "action";
    private static final String BUNDLE_KEY_RESULT_VALUE_BATTERY_LEVEL = "batteryLevel";
    private static final String BUNDLE_KEY_RESULT_VALUE_DESCRIPTION = "description";
    private static final String BUNDLE_KEY_RESULT_VALUE_DEVICE_MATCHED = "deviceMatched";
    private static final String BUNDLE_KEY_RESULT_VALUE_DISPLAY_NAME = "displayName";
    private static final String BUNDLE_KEY_RESULT_VALUE_IS_NEW_SPEN = "isNewSpen";
    private static final String BUNDLE_KEY_RESULT_VALUE_NEED_APP_UPDATE = "needUpdate";
    private static final String BUNDLE_VALUE_ACTION_DIRECT_CONNECTION = "direct_connection";
    private static final String BUNDLE_VALUE_ACTION_NEED_USER_CONFIRM = "need_user_confirm";
    private static final String BUNDLE_VALUE_ACTION_NO_ACTION = "no_action";
    private static final String BUNDLE_VALUE_TRIGGER_DIRECT = "direct";
    private static final String BUNDLE_VALUE_TRIGGER_USER_CONFIRM = "user_confirm";
    private static final String CMD_CHECK_SPEN_ADVERTISE_PACKET = "checkSpenAdvertisePacket";
    private static final String CMD_CONNECT_SPEN = "connectSpen";
    private static final String CMD_DISCONNECT_SPEN = "disconnectSpen";
    private static final String CMD_GET_CONNECTION_STATE = "getBleSpenConnectionState";
    private static final String CMD_LAUNCH_CONNECTION_UI = "launchConnectionUi";
    private static final String CMD_REMOVE_NOTIFY_PAIRED_SPEN_CALLBACK = "removePairedSpenNotifyCallback";
    private static final String CMD_REMOVE_SPEN = "removeSpen";
    private static final String CMD_REMOVE_STATE_CHANGE_CALLBACK = "removeBleSpenStateChangeCallback";
    private static final String CMD_SET_NOTIFY_PAIRED_SPEN_CALLBACK = "setPairedSpenNotifyCallback";
    private static final String CMD_SET_STATE_CHANGE_CALLBACK = "setBleSpenStateChangeCallback";
    private static final String CONNECTION_STATE_PAIRED = "paired";
    private static final String CONNECTION_STATE_UNPAIRED = "unpaired";
    private static final long DETERMINE_ADV_ACTION_AND_SEND_RESPONSE_DELAY = 2000;
    private static final long DIRECT_CONNECTION_TIMEOUT = 15000;
    private static final String ERR_MSG_CONNECTION_BLOCKED = "CONNECTION_BLOCKED";
    private static final String ERR_MSG_INVALID_INSTANCE_ID = "INVALID_INSTANCE_ID";
    private static final String ERR_MSG_INVALID_PARAMETER = "INVALID_PARAM";
    private static final String ERR_MSG_UNABLE_TO_CONNECT = "UNABLE_TO_CONNECT";
    private static final String ERR_MSG_UNABLE_TO_DISCONNECT = "UNABLE_TO_DISCONNECT";
    private static final String ERR_MSG_UNSUPPORTED_DEVICE = "UNSUPPORTED_DEVICE";
    private static final long RESET_LAST_RESPONSE_ACTION_TIME = 5000;
    private static final String TAG = "ConnectionCmdDispatcher";
    private static final long USER_CONFIRM_CONNECTION_TIMEOUT = 7000;
    private BleSpenManager mBleSpenMgr;
    private final BleSpenPairedSpenManager.ChangeListener mBleSpenPairedStatusChangeListener;
    private ArrayList<Transaction> mConnectionStateChangedCallbackList;
    private Handler mHandler;
    private String mLastAdvResponseAction;
    private long mLastAdvResponseTime;
    private ArrayList<Transaction> mPairedStateNotifyCallbackList;
    private final BleSpenManager.StateListener mStateListener;

    public ConnectionCmdDispatcher(Context context, BleSpenManager bleSpenManager, CommandDispatcher.IDispatchEnvironment iDispatchEnvironment) {
        super(context, iDispatchEnvironment);
        this.mBleSpenPairedStatusChangeListener = new BleSpenPairedSpenManager.ChangeListener() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.1
            @Override // com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager.ChangeListener
            public void onAdded(BleSpenInstanceId bleSpenInstanceId) {
                Iterator it = ConnectionCmdDispatcher.this.mPairedStateNotifyCallbackList.iterator();
                while (it.hasNext()) {
                    Bundle makeConnectionStateBundle = ConnectionCmdDispatcher.this.makeConnectionStateBundle(ConnectionCmdDispatcher.this.getSpenInstanceUidString(bleSpenInstanceId), ConnectionCmdDispatcher.CONNECTION_STATE_PAIRED);
                    ConnectionCmdDispatcher.this.sendSuccessResponse((Transaction) it.next(), makeConnectionStateBundle);
                    String str = ConnectionCmdDispatcher.TAG;
                    Log.d(str, "onAdded : new PAIRED - " + bleSpenInstanceId);
                }
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager.ChangeListener
            public void onRemoved(BleSpenInstanceId bleSpenInstanceId, boolean z) {
                Iterator it = ConnectionCmdDispatcher.this.mConnectionStateChangedCallbackList.iterator();
                while (it.hasNext()) {
                    Transaction transaction = (Transaction) it.next();
                    BleSpenInstanceId targetSpenInstanceId = ConnectionCmdDispatcher.this.getTargetSpenInstanceId(transaction);
                    if (targetSpenInstanceId != null && targetSpenInstanceId.equals(bleSpenInstanceId)) {
                        ConnectionCmdDispatcher.this.sendSuccessResponse(transaction, ConnectionCmdDispatcher.this.makeConnectionStateBundle(ConnectionCmdDispatcher.this.getSpenInstanceUidString(bleSpenInstanceId), ConnectionCmdDispatcher.CONNECTION_STATE_UNPAIRED));
                        String str = ConnectionCmdDispatcher.TAG;
                        Log.d(str, "ConnectionStateChangedCallback : UNPAIRED - " + targetSpenInstanceId);
                    }
                }
                Iterator it2 = ConnectionCmdDispatcher.this.mPairedStateNotifyCallbackList.iterator();
                while (it2.hasNext()) {
                    Bundle makeConnectionStateBundle = ConnectionCmdDispatcher.this.makeConnectionStateBundle(ConnectionCmdDispatcher.this.getSpenInstanceUidString(bleSpenInstanceId), ConnectionCmdDispatcher.CONNECTION_STATE_UNPAIRED);
                    ConnectionCmdDispatcher.this.sendSuccessResponse((Transaction) it2.next(), makeConnectionStateBundle);
                    String str2 = ConnectionCmdDispatcher.TAG;
                    Log.d(str2, "PairedStateNotifyCallback : UNPAIRED - " + bleSpenInstanceId);
                }
            }
        };
        this.mStateListener = new BleSpenManager.StateListener() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.2
            @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
            public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
            public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenManager.ConnectionState connectionState, BleSpenManager.ConnectionState connectionState2, BleSpenManager.StateChangeInfo stateChangeInfo) {
                Iterator it = ConnectionCmdDispatcher.this.mConnectionStateChangedCallbackList.iterator();
                while (it.hasNext()) {
                    Transaction transaction = (Transaction) it.next();
                    BleSpenInstanceId targetSpenInstanceId = ConnectionCmdDispatcher.this.getTargetSpenInstanceId(transaction);
                    if (targetSpenInstanceId != null && ConnectionCmdDispatcher.this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId) && bleSpenInstanceId.equals(targetSpenInstanceId)) {
                        ConnectionCmdDispatcher.this.sendSuccessResponse(transaction, ConnectionCmdDispatcher.this.makeConnectionStateBundle(ConnectionCmdDispatcher.this.getSpenInstanceUidString(targetSpenInstanceId), connectionState.toString()));
                        String str = ConnectionCmdDispatcher.TAG;
                        Log.d(str, "onConnectionStateChanged : newState " + connectionState.toString());
                    }
                }
            }
        };
        this.mBleSpenMgr = bleSpenManager;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void init() {
        this.mBleSpenMgr.registerListener(this.mStateListener, null);
        this.mPairedSpenManager.registerChangeListener(this.mBleSpenPairedStatusChangeListener);
        this.mConnectionStateChangedCallbackList = new ArrayList<>();
        this.mPairedStateNotifyCallbackList = new ArrayList<>();
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void release() {
        this.mBleSpenMgr.unregisterListener(this.mStateListener, null);
        this.mPairedSpenManager.unregisterChangeListener(this.mBleSpenPairedStatusChangeListener);
        this.mConnectionStateChangedCallbackList = null;
        this.mPairedStateNotifyCallbackList = null;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public boolean dispatchCommand(Transaction transaction) {
        String str = (String) transaction.mCommand;
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1912291473:
                if (str.equals(CMD_SET_NOTIFY_PAIRED_SPEN_CALLBACK)) {
                    c = 0;
                    break;
                }
                break;
            case -1273276223:
                if (str.equals(CMD_CHECK_SPEN_ADVERTISE_PACKET)) {
                    c = 1;
                    break;
                }
                break;
            case -370207102:
                if (str.equals(CMD_DISCONNECT_SPEN)) {
                    c = 2;
                    break;
                }
                break;
            case -330969755:
                if (str.equals(CMD_LAUNCH_CONNECTION_UI)) {
                    c = 3;
                    break;
                }
                break;
            case -106151608:
                if (str.equals(CMD_GET_CONNECTION_STATE)) {
                    c = 4;
                    break;
                }
                break;
            case 734279057:
                if (str.equals(CMD_REMOVE_NOTIFY_PAIRED_SPEN_CALLBACK)) {
                    c = 5;
                    break;
                }
                break;
            case 1089612679:
                if (str.equals(CMD_SET_STATE_CHANGE_CALLBACK)) {
                    c = 6;
                    break;
                }
                break;
            case 1098547818:
                if (str.equals(CMD_REMOVE_SPEN)) {
                    c = 7;
                    break;
                }
                break;
            case 1723949008:
                if (str.equals(CMD_CONNECT_SPEN)) {
                    c = '\b';
                    break;
                }
                break;
            case 1823252777:
                if (str.equals(CMD_REMOVE_STATE_CHANGE_CALLBACK)) {
                    c = '\t';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                onRequestRegisterPairedStateCallback(transaction);
                return true;
            case 1:
                onRequestCheckSpenAdvertisePacket(transaction);
                return true;
            case 2:
                onRequestDisconnectSpen(transaction);
                return true;
            case 3:
                onLaunchConnectionUi(transaction);
                return true;
            case 4:
                onRequestConnectionState(transaction);
                return true;
            case 5:
                onRequestUnregisterPairedStateCallback(transaction);
                return true;
            case 6:
                onRequestRegisterChangeStateCallback(transaction);
                return true;
            case 7:
                onRequestRemoveSpen(transaction);
                return true;
            case '\b':
                onRequestConnectSpen(transaction);
                return true;
            case '\t':
                onRequestUnregisterChangeStateCallback(transaction);
                return true;
            default:
                return false;
        }
    }

    private void onRequestRemoveSpen(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else {
            BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
            if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
            } else if (this.mPairedSpenManager.removeSpen(targetSpenInstanceId, true)) {
                sendSuccessResponse(transaction, null);
            } else {
                sendErrorResponse(transaction, ERR_MSG_INVALID_INSTANCE_ID);
            }
        }
    }

    private void onRequestDisconnectSpen(final Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else {
            BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
            if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
                return;
            }
            BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
            BleSpenManager.ConnectionState connectionState = bleSpenManager.getConnectionState(targetSpenInstanceIdWithDefault);
            if (connectionState != BleSpenManager.ConnectionState.CONNECTED) {
                String str = TAG;
                Log.i(str, "onRequestDisconnectSpen : Spen not disconnected state. curState=" + connectionState);
                sendErrorResponse(transaction, ERR_MSG_UNABLE_TO_DISCONNECT);
                return;
            }
            bleSpenManager.disconnect(targetSpenInstanceIdWithDefault, null, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.3
                @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                public void onFinish(BleSpenManager.OpResultData opResultData) {
                    if (opResultData.isSuccess()) {
                        ConnectionCmdDispatcher.this.sendSuccessResponse(transaction, opResultData);
                    }
                }
            });
        }
    }

    private void onRequestRegisterChangeStateCallback(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (transaction.mReplyTo == null) {
            Log.e(TAG, "onRequestRegisterChangeStateCallback : messenger is null");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
        } else {
            Iterator<Transaction> it = this.mConnectionStateChangedCallbackList.iterator();
            while (it.hasNext()) {
                if (it.next().isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                    Log.e(TAG, "onRequestRegisterChangeStateCallback : already register callback : ");
                    sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_ALREADY_REGISTERED);
                    return;
                }
            }
            BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
            if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
                return;
            }
            sendSuccessResponse(transaction, makeConnectionStateBundle(getSpenInstanceUidString(targetSpenInstanceId), this.mBleSpenMgr.getConnectionState(targetSpenInstanceId).toString()));
            this.mConnectionStateChangedCallbackList.add(transaction);
            int size = this.mConnectionStateChangedCallbackList.size();
            String str = TAG;
            Log.i(str, "onRequestRegisterChangeStateCallback : size = " + size);
        }
    }

    private void onRequestUnregisterChangeStateCallback(Transaction transaction) {
        if (transaction.mReplyTo == null) {
            Log.e(TAG, "onRequestUnregisterChangeStateCallback : messenger is null");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
            return;
        }
        Iterator<Transaction> it = this.mConnectionStateChangedCallbackList.iterator();
        while (it.hasNext()) {
            Transaction next = it.next();
            if (next.isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                this.mConnectionStateChangedCallbackList.remove(next);
                sendSuccessResponse(transaction, null);
                int size = this.mConnectionStateChangedCallbackList.size();
                String str = TAG;
                Log.i(str, "onRequestUnregisterChangeStateCallback : size = " + size);
                return;
            }
        }
        Log.e(TAG, "onRequestUnregisterChangeStateCallback : can't found callback : ");
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
    }

    private void onRequestRegisterPairedStateCallback(Transaction transaction) {
        if (transaction.mReplyTo == null) {
            Log.e(TAG, "onRequestRegisterPairedStateCallback : messenger is null");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
            return;
        }
        Iterator<Transaction> it = this.mPairedStateNotifyCallbackList.iterator();
        while (it.hasNext()) {
            if (it.next().isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                Log.e(TAG, "onRequestRegisterPairedStateCallback : already register callback : ");
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_ALREADY_REGISTERED);
                return;
            }
        }
        this.mPairedStateNotifyCallbackList.add(transaction);
        int size = this.mPairedStateNotifyCallbackList.size();
        String str = TAG;
        Log.i(str, "onRequestRegisterPairedStateCallback : size = " + size);
    }

    private void onRequestUnregisterPairedStateCallback(Transaction transaction) {
        if (transaction.mReplyTo == null) {
            Log.e(TAG, "onRequestUnregisterPairedStateCallback : messenger is null");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
            return;
        }
        Iterator<Transaction> it = this.mPairedStateNotifyCallbackList.iterator();
        while (it.hasNext()) {
            Transaction next = it.next();
            if (next.isTransactionMatched(transaction.mCallerUid, transaction.mReplyTo)) {
                this.mPairedStateNotifyCallbackList.remove(next);
                sendSuccessResponse(transaction, null);
                int size = this.mPairedStateNotifyCallbackList.size();
                String str = TAG;
                Log.i(str, "onRequestUnregisterPairedStateCallback : size = " + size);
                return;
            }
        }
        Log.e(TAG, "onRequestUnregisterPairedStateCallback : can't found callback : ");
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
    }

    private void onRequestConnectionState(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else {
            BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
            if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
                return;
            }
            BleSpenManager.ConnectionState connectionState = this.mBleSpenMgr.getConnectionState(targetSpenInstanceIdWithDefault);
            String lowerCase = connectionState != null ? connectionState.name().toLowerCase(Locale.ENGLISH) : null;
            if (lowerCase != null) {
                sendSuccessResponse(transaction, lowerCase);
            } else {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_UNKNOWN_CONNECTION_STATE);
            }
        }
    }

    private void onRequestCheckSpenAdvertisePacket(final Transaction transaction) {
        if (!isSupportBleSpen()) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (BleSpenManager.getInstance(this.mContext).isManualPairingInProgress()) {
            Log.d(TAG, "onRequestCheckSpenAdvertisePacket : manual pairing is in progress.");
            sendErrorResponse(transaction, ERR_MSG_CONNECTION_BLOCKED);
        } else {
            byte[] bArr = (byte[]) transaction.getParameterFromBundle(BUNDLE_KEY_PARAM_ADV_DATA);
            if (bArr == null) {
                sendErrorResponse(transaction, "INVALID_PARAM : advData is null");
                return;
            }
            String str = TAG;
            Log.i(str, "onRequestCheckSpenAdvertisePacket : adData = " + BleUtils.getRawDataHexDumpStr(bArr));
            final AdvertisementData parseSpenAdvertisementData = new AdvertisementDataParser().parseSpenAdvertisementData(this.mContext, bArr);
            if (parseSpenAdvertisementData == null || parseSpenAdvertisementData.spenModelName == null) {
                sendErrorResponse(transaction, ERR_MSG_UNSUPPORTED_DEVICE);
            } else if (isRemoteSpenServiceRunning()) {
                determineAdvActionAndSendResponse(transaction, parseSpenAdvertisementData);
            } else if (startRemoteSpenService(false)) {
                this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.4
                    @Override // java.lang.Runnable
                    public void run() {
                        ConnectionCmdDispatcher.this.determineAdvActionAndSendResponse(transaction, parseSpenAdvertisementData);
                    }
                }, 2000L);
            } else {
                sendServiceNotRunningResponse(transaction);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0059  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x0126  */
    /* JADX WARN: Removed duplicated region for block: B:43:0x0134  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x0071 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:47:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void determineAdvActionAndSendResponse(com.samsung.remotespen.external.Transaction r13, com.samsung.remotespen.core.device.control.connection.advertisement.AdvertisementData r14) {
        /*
            Method dump skipped, instructions count: 327
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.external.ConnectionCmdDispatcher.determineAdvActionAndSendResponse(com.samsung.remotespen.external.Transaction, com.samsung.remotespen.core.device.control.connection.advertisement.AdvertisementData):void");
    }

    private String getUserConfirmDescription(BleSpenFrequency bleSpenFrequency) {
        SpenGarageTipType spenGarageTipType = ModelFeatures.getSpenGarageTipType(this.mContext);
        if (spenGarageTipType == SpenGarageTipType.SOFT && bleSpenFrequency != BleSpenFrequency.FOLD) {
            return this.mContext.getString(R.string.switch_z_fold_this_phone);
        }
        if (spenGarageTipType != SpenGarageTipType.NORMAL || bleSpenFrequency == BleSpenFrequency.DEFAULT) {
            return null;
        }
        if (CommonUtils.isTablet()) {
            return this.mContext.getString(R.string.switch_s_pen_this_tablet);
        }
        return this.mContext.getString(R.string.switch_s_pen_this_phone);
    }

    private void onRequestConnectSpen(Transaction transaction) {
        if (((String) transaction.getParameterFromBundle("uid")) != null) {
            onRequestConnectSpenForApplication(transaction);
        } else {
            onRequestConnectSpenForEasyConnection(transaction);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bundle makeConnectionStateBundle(String str, String str2) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", str);
        bundle.putString(BUNDLE_KEY_PARAM_STATE, str2.toLowerCase());
        return bundle;
    }

    private void onRequestConnectSpenForEasyConnection(final Transaction transaction) {
        if (!isSupportBleSpen()) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else {
            final String str = (String) transaction.getParameterFromBundle(BUNDLE_KEY_PARAM_BD_ADDRESS);
            if (TextUtils.isEmpty(str)) {
                sendErrorResponse(transaction, "INVALID_PARAM : no address");
                return;
            }
            byte[] bArr = (byte[]) transaction.getParameterFromBundle(BUNDLE_KEY_PARAM_ADV_DATA);
            if (bArr == null) {
                sendErrorResponse(transaction, "INVALID_PARAM : advData is empty");
                return;
            }
            AdvertisementData parseSpenAdvertisementData = new AdvertisementDataParser().parseSpenAdvertisementData(this.mContext, bArr);
            if (parseSpenAdvertisementData == null || parseSpenAdvertisementData.spenModelName == null) {
                sendErrorResponse(transaction, ERR_MSG_UNSUPPORTED_DEVICE);
                return;
            }
            final BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
            final BleSpenInstanceId createInstanceId = SpenInstanceIdHelper.createInstanceId(this.mContext, parseSpenAdvertisementData.spenModelName, str);
            if (this.mBleSpenMgr.getConnectionState(createInstanceId) == BleSpenManager.ConnectionState.CONNECTED) {
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_KEY_RESULT_VALUE_BATTERY_LEVEL, bleSpenManager.getBatteryLevel(createInstanceId));
                sendSuccessResponse(transaction, bundle);
                return;
            }
            Iterator<BleSpenInstanceId> it = this.mPairedSpenManager.getAvailableUnbundledSpenInstanceIds().iterator();
            while (it.hasNext()) {
                if (this.mBleSpenMgr.getConnectionState(it.next()) == BleSpenManager.ConnectionState.CONNECTING) {
                    Log.i(TAG, "onRequestConnectSpenForEasyConnection : Another unbundled pen is connecting");
                    sendErrorResponse(transaction, ERR_MSG_UNABLE_TO_CONNECT);
                    return;
                }
            }
            String str2 = TAG;
            Log.i(str2, "onRequestConnectSpenForEasyConnection : " + parseSpenAdvertisementData.spenModelName + " / " + str + " / advData = " + BleUtils.getRawDataHexDumpStr(bArr));
            SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_UNBUNDLED_SPEN_CONNECTION, BleSpenPairedSpenManager.getInstance(this.mContext).isPairedSpen(createInstanceId) ? SAUtils.AirCommandSpenRemote.UNBUNDLED_SPEN_CONNECTION_PAIRED_SPEN : SAUtils.AirCommandSpenRemote.UNBUNDLED_SPEN_CONNECTION_NEW_SPEN);
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.5
                @Override // java.lang.Runnable
                public void run() {
                    BleSpenManager.ConnectionState connectionState = bleSpenManager.getConnectionState(createInstanceId);
                    if (connectionState == BleSpenManager.ConnectionState.CONNECTING) {
                        Log.e(ConnectionCmdDispatcher.TAG, "onRequestConnectSpenForEasyConnection : Connection timeout. cancels current connecting");
                        bleSpenManager.disconnect(createInstanceId, null, null);
                        return;
                    }
                    String str3 = ConnectionCmdDispatcher.TAG;
                    Log.d(str3, "onRequestConnectSpenForEasyConnection : Connection timeout. connState=" + connectionState);
                }
            };
            bleSpenManager.registerListener(new BleSpenManager.StateListener() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.6
                @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
                public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
                }

                @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
                public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenManager.ConnectionState connectionState, BleSpenManager.ConnectionState connectionState2, BleSpenManager.StateChangeInfo stateChangeInfo) {
                    String spenAddress = ConnectionCmdDispatcher.this.getSpenAddress(bleSpenInstanceId);
                    if (!str.equals(spenAddress)) {
                        String str3 = ConnectionCmdDispatcher.TAG;
                        Log.d(str3, "onConnectionStateChanged : spen address is different. bdAddress = " + str + ", targetAddress = " + spenAddress);
                        return;
                    }
                    String str4 = ConnectionCmdDispatcher.TAG;
                    Log.d(str4, "onConnectionStateChanged : newState=" + connectionState + ", prevState=" + connectionState2);
                    BleSpenManager.ConnectionState connectionState3 = BleSpenManager.ConnectionState.DISCONNECTED;
                    if (connectionState == connectionState3 || connectionState == BleSpenManager.ConnectionState.CONNECTED) {
                        handler.removeCallbacks(runnable);
                        bleSpenManager.unregisterListener(this, null);
                        if (connectionState == connectionState3) {
                            bleSpenManager.closeConnection(createInstanceId, null, null);
                            ConnectionCmdDispatcher.this.sendNotConnectedResponse(transaction);
                        } else if (connectionState2 == BleSpenManager.ConnectionState.CONNECTING) {
                            if (bleSpenManager.getConnectionState(createInstanceId) == connectionState3) {
                                ConnectionCmdDispatcher.this.sendNotConnectedResponse(transaction);
                                return;
                            }
                            Bundle bundle2 = new Bundle();
                            bundle2.putInt(ConnectionCmdDispatcher.BUNDLE_KEY_RESULT_VALUE_BATTERY_LEVEL, bleSpenManager.getBatteryLevel(createInstanceId));
                            ConnectionCmdDispatcher.this.sendSuccessResponse(transaction, bundle2);
                        }
                    }
                }
            }, null);
            String str3 = (String) transaction.getParameterFromBundle(BUNDLE_KEY_PARAM_TRIGGER);
            BleConnTriggerCode determineConnTriggerCode = determineConnTriggerCode(str3);
            Log.d(str2, "onRequestConnectSpenForEasyConnection : trigger = " + str3);
            long j = DIRECT_CONNECTION_TIMEOUT;
            if (BleConnTriggerCode.EASY_CONNECT_USER_CONFIRM.equals(determineConnTriggerCode)) {
                j = USER_CONFIRM_CONNECTION_TIMEOUT;
                SAUtils.insertEventLog(SAUtils.EasyConnection.SCREEN_ID, SAUtils.EasyConnection.EVENT_ID_EASY_CONNECTION_POPUP_CONNECT, null);
            }
            handler.postDelayed(runnable, j);
            bleSpenManager.connectToSpen(new BleConnReqData(parseSpenAdvertisementData.spenModelName, determineConnTriggerCode, str));
        }
    }

    private BleConnTriggerCode determineConnTriggerCode(String str) {
        if (str != null) {
            return BUNDLE_VALUE_TRIGGER_USER_CONFIRM.equals(str) ? BleConnTriggerCode.EASY_CONNECT_USER_CONFIRM : BleConnTriggerCode.EASY_CONNECT_DIRECT;
        }
        if (SystemClock.elapsedRealtime() - this.mLastAdvResponseTime > RESET_LAST_RESPONSE_ACTION_TIME) {
            this.mLastAdvResponseAction = null;
        }
        if (BUNDLE_VALUE_ACTION_DIRECT_CONNECTION.equals(this.mLastAdvResponseAction)) {
            return BleConnTriggerCode.EASY_CONNECT_DIRECT;
        }
        return BleConnTriggerCode.EASY_CONNECT_USER_CONFIRM;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRequestConnectSpenForApplication(final Transaction transaction) {
        if (!isSupportBleSpen()) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (!isRemoteSpenServiceRunning()) {
            if (startRemoteSpenService(false)) {
                this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.7
                    @Override // java.lang.Runnable
                    public void run() {
                        ConnectionCmdDispatcher.this.onRequestConnectSpenForApplication(transaction);
                    }
                }, 2000L);
                Log.i(TAG, "onRequestConnectSpenForApplication : run postDelayed");
                return;
            }
            sendServiceNotRunningResponse(transaction);
        } else {
            final BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
            final BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
            if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
                return;
            }
            BleSpenManager.ConnectionState connectionState = bleSpenManager.getConnectionState(targetSpenInstanceId);
            if (connectionState != BleSpenManager.ConnectionState.DISCONNECTED) {
                String str = TAG;
                Log.i(str, "onRequestConnectSpenForApplication : Spen not disconnected state. curState=" + connectionState);
                sendErrorResponse(transaction, ERR_MSG_UNABLE_TO_CONNECT);
                return;
            }
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.8
                @Override // java.lang.Runnable
                public void run() {
                    BleSpenManager.ConnectionState connectionState2 = bleSpenManager.getConnectionState(targetSpenInstanceId);
                    if (connectionState2 == BleSpenManager.ConnectionState.CONNECTING) {
                        Log.e(ConnectionCmdDispatcher.TAG, "onRequestConnectSpenForApplication : Connection timeout. cancels current connecting");
                        bleSpenManager.disconnect(targetSpenInstanceId, null, null);
                        return;
                    }
                    String str2 = ConnectionCmdDispatcher.TAG;
                    Log.d(str2, "onRequestConnectSpenForApplication : Connection timeout. connState=" + connectionState2);
                }
            };
            bleSpenManager.registerListener(new BleSpenManager.StateListener() { // from class: com.samsung.remotespen.external.ConnectionCmdDispatcher.9
                @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
                public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
                }

                @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
                public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenManager.ConnectionState connectionState2, BleSpenManager.ConnectionState connectionState3, BleSpenManager.StateChangeInfo stateChangeInfo) {
                    if (!bleSpenInstanceId.equals(targetSpenInstanceId)) {
                        String str2 = ConnectionCmdDispatcher.TAG;
                        Log.d(str2, "onConnectionStateChanged : instanceId is different. spenInstanceId = " + bleSpenInstanceId + ", targetInstanceId = " + targetSpenInstanceId);
                        return;
                    }
                    String str3 = ConnectionCmdDispatcher.TAG;
                    Log.d(str3, "onConnectionStateChanged : newState=" + connectionState2 + ", prevState=" + connectionState3);
                    BleSpenManager.ConnectionState connectionState4 = BleSpenManager.ConnectionState.DISCONNECTED;
                    if (connectionState2 == connectionState4 || connectionState2 == BleSpenManager.ConnectionState.CONNECTED) {
                        handler.removeCallbacks(runnable);
                        bleSpenManager.unregisterListener(this, null);
                        if (connectionState2 == connectionState4) {
                            bleSpenManager.closeConnection(bleSpenInstanceId, null, null);
                            ConnectionCmdDispatcher.this.sendNotConnectedResponse(transaction);
                        } else if (connectionState3 == BleSpenManager.ConnectionState.CONNECTING) {
                            if (bleSpenManager.getConnectionState(bleSpenInstanceId) == connectionState4) {
                                ConnectionCmdDispatcher.this.sendNotConnectedResponse(transaction);
                                return;
                            }
                            Bundle bundle = new Bundle();
                            bundle.putInt(ConnectionCmdDispatcher.BUNDLE_KEY_RESULT_VALUE_BATTERY_LEVEL, bleSpenManager.getBatteryLevel(bleSpenInstanceId));
                            ConnectionCmdDispatcher.this.sendSuccessResponse(transaction, bundle);
                        }
                    }
                }
            }, null);
            handler.postDelayed(runnable, DIRECT_CONNECTION_TIMEOUT);
            bleSpenManager.connectToSpen(new BleConnReqData(targetSpenInstanceId.getSpenModelName(), BleConnTriggerCode.APP_CONNECT, SpenInstanceIdHelper.from(this.mContext, targetSpenInstanceId).getSpenAddress()));
        }
    }

    private void onLaunchConnectionUi(Transaction transaction) {
        if (!isSupportBleSpen()) {
            sendBleSpenNotSupportedResponse(transaction);
        } else if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
        } else {
            BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
            BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
            if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
                sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
            } else if (targetSpenInstanceId.isBundledSpen()) {
                Log.e(TAG, "onLaunchConnectionUi : Not supports bundled SPen instance ID");
                sendErrorResponse(transaction, ERR_MSG_INVALID_PARAMETER);
            } else {
                BleSpenManager.ConnectionState connectionState = bleSpenManager.getConnectionState(targetSpenInstanceId);
                if (connectionState != BleSpenManager.ConnectionState.DISCONNECTED) {
                    String str = TAG;
                    Log.i(str, "onRequestConnectSpenForApplication : Spen not disconnected state. curState=" + connectionState);
                    sendErrorResponse(transaction, ERR_MSG_UNABLE_TO_CONNECT);
                    return;
                }
                String uidString = SpenInstanceIdHelper.from(this.mContext, targetSpenInstanceId).getUidString();
                String str2 = TAG;
                Log.d(str2, "onLaunchConnectionUi : instanceUidString = " + uidString);
                this.mContext.startActivity(ManualPairingActivity.getStartIntent(this.mContext, uidString, false));
            }
        }
    }
}
