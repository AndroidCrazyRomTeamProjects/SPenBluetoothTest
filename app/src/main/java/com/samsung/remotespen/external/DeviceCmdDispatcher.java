package com.samsung.remotespen.external;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.external.CommandDispatcher;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class DeviceCmdDispatcher extends CommandDispatcher {
    private static final int BUNDLE_DEVICE_ID = 1;
    private static final int BUNDLE_DEVICE_TYPE = 1;
    private static final String CMD_GET_ADDRESS = "getBleSpenAddress";
    private static final String CMD_GET_DEVICE_ID = "getBleSpenDeviceId";
    private static final String CMD_GET_DEVICE_INFO = "getBleSpenDeviceInfo";
    private static final String CMD_GET_DEVICE_INFO_LIST = "getBleSpenDeviceInfoList";
    private static final String CMD_GET_DEVICE_TYPE = "getBleSpenDeviceType";
    private static final String CMD_GET_DEVICE_UID_LIST = "getBleSpenDeviceUidList";
    private static final String CMD_GET_FIRMWARE_VER = "getBleSpenFirmwareVersion";
    private static final String CMD_GET_MODEL_NAME = "getBleSpenModelName";
    private static final String CMD_GET_MODEL_NUMBER = "getBleSpenModelNumber";
    private static final String CMD_GET_NICK_NAME = "getBleSpenNickName";
    private static final String CMD_GET_RSSI = "getBleSpenRssi";
    private static final String CMD_SET_NICK_NAME = "setBleSpenNickName";
    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";
    private static final String ERR_MSG_FAILED_TO_READ_RSSI = "Failed to read RSSI";
    private static final String ERR_MSG_FAIL_TO_CREATE_BUNDLE = "FAIL_TO_CREATE_BUNDLE";
    private static final String ERR_MSG_FAIL_TO_GET_NICKNAME = "FAIL_TO_GET_NICKNAME";
    private static final String ERR_MSG_FAIL_TO_SET_NICKNAME = "FAIL_TO_SET_NICKNAME";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_BATTERY_LEVEL = "batteryLevel";
    private static final String KEY_CHARGING_STATE = "chargingState";
    private static final String KEY_CONNECTION_STATE = "connectionState";
    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_DEVICE_TYPE = "deviceType";
    private static final String KEY_MODEL_NAME = "modelName";
    private static final String KEY_MODEL_NUMBER = "modelNumber";
    private static final String KEY_NICK_NAME = "nickName";
    private static final String KEY_UID = "uid";
    private static final String TAG = "DeviceCmdDispatcher";
    private static final int UNBUNDLE_DEVICE_ID = 16897;
    private static final int UNBUNDLE_DEVICE_TYPE = 16;
    private BleSpenManager mBleSpenMgr;
    private Context mContext;

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void init() {
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void release() {
    }

    public DeviceCmdDispatcher(Context context, BleSpenManager bleSpenManager, CommandDispatcher.IDispatchEnvironment iDispatchEnvironment) {
        super(context, iDispatchEnvironment);
        this.mContext = context;
        this.mBleSpenMgr = bleSpenManager;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public boolean dispatchCommand(Transaction transaction) {
        String str = (String) transaction.mCommand;
        Log.d(TAG, "requestCommand : command : " + str);
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1518051667:
                if (str.equals(CMD_SET_NICK_NAME)) {
                    c = 0;
                    break;
                }
                break;
            case -1398631748:
                if (str.equals(CMD_GET_DEVICE_ID)) {
                    c = 1;
                    break;
                }
                break;
            case -705685491:
                if (str.equals(CMD_GET_DEVICE_UID_LIST)) {
                    c = 2;
                    break;
                }
                break;
            case -324516222:
                if (str.equals(CMD_GET_RSSI)) {
                    c = 3;
                    break;
                }
                break;
            case -38308974:
                if (str.equals(CMD_GET_FIRMWARE_VER)) {
                    c = 4;
                    break;
                }
                break;
            case 239666703:
                if (str.equals(CMD_GET_DEVICE_INFO)) {
                    c = 5;
                    break;
                }
                break;
            case 240005275:
                if (str.equals(CMD_GET_DEVICE_TYPE)) {
                    c = 6;
                    break;
                }
                break;
            case 390957901:
                if (str.equals(CMD_GET_DEVICE_INFO_LIST)) {
                    c = 7;
                    break;
                }
                break;
            case 749736775:
                if (str.equals(CMD_GET_MODEL_NUMBER)) {
                    c = '\b';
                    break;
                }
                break;
            case 1851038217:
                if (str.equals(CMD_GET_MODEL_NAME)) {
                    c = '\t';
                    break;
                }
                break;
            case 1856881977:
                if (str.equals(CMD_GET_NICK_NAME)) {
                    c = '\n';
                    break;
                }
                break;
            case 1957999081:
                if (str.equals(CMD_GET_ADDRESS)) {
                    c = 11;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                onRequestBleSpenSetNickName(transaction);
                return true;
            case 1:
                onRequestBleSpenDeviceId(transaction);
                return true;
            case 2:
                onRequestDeviceUidList(transaction);
                return true;
            case 3:
                onRequestBleSpenRssi(transaction);
                return true;
            case 4:
                onRequestBleSpenFirmwareVersion(transaction);
                return true;
            case 5:
                onRequestBleSpenDeviceInfo(transaction);
                return true;
            case 6:
                onRequestBleSpenDeviceType(transaction);
                return true;
            case 7:
                onRequestBleSpenDeviceInfoList(transaction);
                return true;
            case '\b':
                onRequestBleSpenModelNumber(transaction);
                return true;
            case '\t':
                onRequestBleSpenModelName(transaction);
                return true;
            case '\n':
                onRequestBleSpenGetNickName(transaction);
                return true;
            case 11:
                onRequestBleSpenAddress(transaction);
                return true;
            default:
                return false;
        }
    }

    private void onRequestBleSpenDeviceType(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceId)) {
            sendNotConnectedResponse(transaction);
        } else {
            sendSuccessResponse(transaction, Integer.valueOf(getBleSpenDeviceType(targetSpenInstanceId)));
        }
    }

    private void onRequestBleSpenDeviceId(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceId)) {
            sendNotConnectedResponse(transaction);
        } else {
            sendSuccessResponse(transaction, Integer.valueOf(getBleSpenDeviceId(targetSpenInstanceId)));
        }
    }

    private int getBleSpenDeviceType(BleSpenInstanceId bleSpenInstanceId) {
        return bleSpenInstanceId.isBundledSpen() ? 1 : 16;
    }

    private int getBleSpenDeviceId(BleSpenInstanceId bleSpenInstanceId) {
        if (bleSpenInstanceId.isBundledSpen()) {
            return 1;
        }
        return UNBUNDLE_DEVICE_ID;
    }

    private void onRequestBleSpenAddress(Transaction transaction) {
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
            sendSuccessResponse(transaction, getSpenAddress(targetSpenInstanceIdWithDefault));
        }
    }

    private void onRequestBleSpenFirmwareVersion(Transaction transaction) {
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
                sendSuccessResponse(transaction, bleSpenManager.getFirmwareVersion(targetSpenInstanceIdWithDefault));
            }
        }
    }

    private void onRequestBleSpenRssi(final Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else {
            this.mBleSpenMgr.requestRemoteRssi(this.mPairedSpenManager.getBundledSpenInstanceId(), new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.DeviceCmdDispatcher.1
                @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                public void onFinish(BleSpenManager.OpResultData opResultData) {
                    String str = DeviceCmdDispatcher.TAG;
                    Log.d(str, "onRequestBleSpenRssi : success=" + opResultData.isSuccess());
                    if (opResultData.isSuccess()) {
                        DeviceCmdDispatcher.this.sendSuccessResponse(transaction, opResultData.getRssi());
                    } else {
                        DeviceCmdDispatcher.this.sendSuccessResponse(transaction, DeviceCmdDispatcher.ERR_MSG_FAILED_TO_READ_RSSI);
                    }
                }
            });
        }
    }

    private void onRequestBleSpenDeviceInfoList(Transaction transaction) {
        ArrayList arrayList = new ArrayList();
        Iterator<BleSpenInstanceId> it = this.mPairedSpenManager.getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            Bundle createDeviceInfoBundle = createDeviceInfoBundle(it.next());
            if (createDeviceInfoBundle == null) {
                sendErrorResponse(transaction, ERR_MSG_FAIL_TO_CREATE_BUNDLE);
            }
            arrayList.add(createDeviceInfoBundle);
        }
        sendSuccessResponse(transaction, arrayList);
    }

    private void onRequestBleSpenDeviceInfo(Transaction transaction) {
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else {
            sendSuccessResponse(transaction, createDeviceInfoBundle(targetSpenInstanceId));
        }
    }

    private Bundle createDeviceInfoBundle(BleSpenInstanceId bleSpenInstanceId) {
        if (bleSpenInstanceId == null) {
            Log.e(TAG, "createDeviceInfoBundle : targetId is null");
            return null;
        } else if (!this.mPairedSpenManager.isPairedSpen(bleSpenInstanceId)) {
            String str = TAG;
            Log.e(str, "createDeviceInfoBundle : targetInstanceId is not paired  : " + bleSpenInstanceId);
            return null;
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("uid", getSpenInstanceUidString(bleSpenInstanceId));
            bundle.putInt("deviceType", getBleSpenDeviceType(bleSpenInstanceId));
            bundle.putInt(KEY_DEVICE_ID, getBleSpenDeviceId(bleSpenInstanceId));
            bundle.putString(KEY_MODEL_NAME, getBleSpenModelName(bleSpenInstanceId));
            bundle.putString(KEY_MODEL_NUMBER, getBleSpenModelNumber(bleSpenInstanceId));
            bundle.putString("nickName", getBleSpenNickName(bleSpenInstanceId));
            bundle.putString(KEY_ADDRESS, getSpenAddress(bleSpenInstanceId));
            bundle.putString(KEY_CONNECTION_STATE, getBleSpenConnectionState(bleSpenInstanceId));
            bundle.putString(KEY_CHARGING_STATE, getBleSpenChargingState(bleSpenInstanceId));
            bundle.putInt(KEY_BATTERY_LEVEL, getBleSpenBatteryLevel(bleSpenInstanceId));
            return bundle;
        }
    }

    private void onRequestDeviceUidList(Transaction transaction) {
        ArrayList<BleSpenInstanceId> availableSpenInstanceIds = this.mPairedSpenManager.getAvailableSpenInstanceIds();
        ArrayList arrayList = new ArrayList();
        Iterator<BleSpenInstanceId> it = availableSpenInstanceIds.iterator();
        while (it.hasNext()) {
            arrayList.add(getSpenInstanceUidString(it.next()));
        }
        sendSuccessResponse(transaction, arrayList);
    }

    private String getBleSpenModelName(BleSpenInstanceId bleSpenInstanceId) {
        return BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName()).getApplicationFeature().getDisplayModelName(this.mContext);
    }

    private void onRequestBleSpenModelName(Transaction transaction) {
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else {
            sendSuccessResponse(transaction, getBleSpenModelName(targetSpenInstanceId));
        }
    }

    private String getBleSpenModelNumber(BleSpenInstanceId bleSpenInstanceId) {
        return BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName()).getApplicationFeature().getModelNumber(this.mContext);
    }

    private void onRequestBleSpenModelNumber(Transaction transaction) {
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else {
            sendSuccessResponse(transaction, getBleSpenModelNumber(targetSpenInstanceId));
        }
    }

    private String getBleSpenNickName(BleSpenInstanceId bleSpenInstanceId) {
        return this.mPairedSpenManager.getSpenNickNameForExternal(bleSpenInstanceId);
    }

    private void onRequestBleSpenGetNickName(Transaction transaction) {
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
            return;
        }
        String bleSpenNickName = getBleSpenNickName(targetSpenInstanceId);
        if (bleSpenNickName != null) {
            sendSuccessResponse(transaction, bleSpenNickName);
        } else {
            sendErrorResponse(transaction, ERR_MSG_FAIL_TO_GET_NICKNAME);
        }
    }

    private void onRequestBleSpenSetNickName(Transaction transaction) {
        String str = (String) transaction.getParameterFromBundle("nickName");
        if (str == null) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_PARAMETER);
        }
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        if (targetSpenInstanceId == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceId)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (this.mPairedSpenManager.setSpenNickName(targetSpenInstanceId, str)) {
            sendSuccessResponse(transaction, getBleSpenNickName(targetSpenInstanceId));
        } else {
            sendErrorResponse(transaction, ERR_MSG_FAIL_TO_SET_NICKNAME);
        }
    }

    private String getBleSpenConnectionState(BleSpenInstanceId bleSpenInstanceId) {
        return this.mBleSpenMgr.getConnectionState(bleSpenInstanceId).equals(BleSpenManager.ConnectionState.CONNECTED) ? CONNECTED : "disconnected";
    }

    private int getBleSpenBatteryLevel(BleSpenInstanceId bleSpenInstanceId) {
        return this.mBleSpenMgr.getBatteryLevel(bleSpenInstanceId);
    }

    private String getBleSpenChargingState(BleSpenInstanceId bleSpenInstanceId) {
        return this.mBleSpenMgr.getChargeState(bleSpenInstanceId).toString();
    }
}
