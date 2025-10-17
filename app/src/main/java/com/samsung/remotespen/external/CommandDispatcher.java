package com.samsung.remotespen.external;

import android.content.Context;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.util.debug.Assert;
import java.util.ArrayList;

/* loaded from: classes.dex */
public abstract class CommandDispatcher {
    public Context mContext;
    public BleSpenPairedSpenManager mPairedSpenManager;
    public IDispatchEnvironment mResponseSender;

    /* loaded from: classes.dex */
    public interface IDispatchEnvironment {
        boolean isConnectedToBleSpen(BleSpenInstanceId bleSpenInstanceId);

        boolean isRemoteSpenServiceRunning();

        boolean isSupportBleSpen();

        void sendBleSpenNotSupportedResponse(Transaction transaction);

        void sendErrorResponse(Transaction transaction, String str);

        void sendNotConnectedResponse(Transaction transaction);

        void sendServiceNotRunningResponse(Transaction transaction);

        void sendSuccessResponse(Transaction transaction, Object obj);

        boolean startRemoteSpenService(boolean z);
    }

    public abstract boolean dispatchCommand(Transaction transaction);

    public abstract void init();

    public abstract void release();

    public CommandDispatcher(Context context, IDispatchEnvironment iDispatchEnvironment) {
        Assert.notNull(iDispatchEnvironment);
        this.mContext = context.getApplicationContext();
        this.mResponseSender = iDispatchEnvironment;
        this.mPairedSpenManager = BleSpenPairedSpenManager.getInstance(context);
    }

    public void sendSuccessResponse(Transaction transaction, Object obj) {
        this.mResponseSender.sendSuccessResponse(transaction, obj);
    }

    public void sendServiceNotRunningResponse(Transaction transaction) {
        this.mResponseSender.sendServiceNotRunningResponse(transaction);
    }

    public void sendNotConnectedResponse(Transaction transaction) {
        this.mResponseSender.sendNotConnectedResponse(transaction);
    }

    public void sendBleSpenNotSupportedResponse(Transaction transaction) {
        this.mResponseSender.sendBleSpenNotSupportedResponse(transaction);
    }

    public void sendErrorResponse(Transaction transaction, String str) {
        this.mResponseSender.sendErrorResponse(transaction, str);
    }

    public boolean isRemoteSpenServiceRunning() {
        return this.mResponseSender.isRemoteSpenServiceRunning();
    }

    public boolean startRemoteSpenService(boolean z) {
        return this.mResponseSender.startRemoteSpenService(z);
    }

    public boolean isConnectedToBleSpen(BleSpenInstanceId bleSpenInstanceId) {
        return this.mResponseSender.isConnectedToBleSpen(bleSpenInstanceId);
    }

    public boolean isSupportBleSpen() {
        return this.mResponseSender.isSupportBleSpen();
    }

    public String getSpenInstanceUidString(BleSpenInstanceId bleSpenInstanceId) {
        return SpenInstanceIdHelper.from(this.mContext, bleSpenInstanceId).getUidString();
    }

    public String getSpenAddress(BleSpenInstanceId bleSpenInstanceId) {
        return SpenInstanceIdHelper.from(this.mContext, bleSpenInstanceId).getSpenAddress();
    }

    public BleSpenInstanceId getTargetSpenInstanceIdWithDefault(Transaction transaction) {
        BleSpenInstanceId targetSpenInstanceId = getTargetSpenInstanceId(transaction);
        return targetSpenInstanceId == null ? this.mPairedSpenManager.getBundledSpenInstanceId() : targetSpenInstanceId;
    }

    public BleSpenInstanceId getTargetSpenInstanceId(Transaction transaction) {
        String str = (String) transaction.getParameterFromBundle("uid");
        if (str == null) {
            return null;
        }
        return SpenInstanceIdHelper.createInstanceId(this.mContext, str);
    }

    public void clearInvalidCallbackList(ArrayList<Transaction> arrayList) {
        if (arrayList != null) {
            for (int size = arrayList.size() - 1; size >= 0; size--) {
                if (!arrayList.get(size).mReplyTo.getBinder().pingBinder()) {
                    arrayList.remove(size);
                }
            }
        }
    }
}
