package com.samsung.remotespen.external;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.external.CommandDispatcher;
import com.samsung.util.settings.SettingsValueManager;

/* loaded from: classes.dex */
public class CommonCmdDispatcher extends CommandDispatcher {
    private static final int BLE_SPEN_INTERFACE_VER = 1;
    private static final String CMD_GET_BLE_SPEN_INTERFACE_VER = "getBleSpenInterfaceVersion";
    private static final String CMD_SET_ALLOW_MULTIPLE_SPEN = "setAllowMultipleSpen";
    private static final String TAG = "CommonCmdDispatcher";
    private BleSpenManager mBleSpenMgr;
    private Context mContext;

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void init() {
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void release() {
    }

    public CommonCmdDispatcher(Context context, BleSpenManager bleSpenManager, CommandDispatcher.IDispatchEnvironment iDispatchEnvironment) {
        super(context, iDispatchEnvironment);
        this.mContext = context;
        this.mBleSpenMgr = bleSpenManager;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public boolean dispatchCommand(Transaction transaction) {
        String str = (String) transaction.mCommand;
        str.hashCode();
        if (str.equals(CMD_GET_BLE_SPEN_INTERFACE_VER)) {
            onRequestInterfaceVer(transaction);
            return true;
        } else if (str.equals(CMD_SET_ALLOW_MULTIPLE_SPEN)) {
            onRequestAllowMultipleSpen(transaction);
            return true;
        } else {
            return false;
        }
    }

    private void onRequestAllowMultipleSpen(Transaction transaction) {
        Boolean bool = (Boolean) transaction.getParameterFromBundle(BindingApiConstants.BUNDLE_KEY_IS_ENABLE);
        if (bool == null) {
            String str = TAG;
            Log.e(str, "onRequestAllowMultipleSpen : Invalid parameter : isEnable is missing");
            sendErrorResponse(transaction, "Invalid parameter : isEnable is missing");
            return;
        }
        String str2 = TAG;
        Log.i(str2, "onRequestAllowMultipleSpen : cmd=" + bool);
        SettingsValueManager.getInstance(this.mContext).setAllowMultiplePensEnabled(bool.booleanValue());
        sendSuccessResponse(transaction, null);
    }

    private void onRequestInterfaceVer(Transaction transaction) {
        sendSuccessResponse(transaction, 1);
    }
}
