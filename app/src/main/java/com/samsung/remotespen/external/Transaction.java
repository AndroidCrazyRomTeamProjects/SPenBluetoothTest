package com.samsung.remotespen.external;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: RemoteSpenInternalBindingService.java */
/* loaded from: classes.dex */
public class Transaction {
    public Bundle mArrivedBundle;
    public int mCallerUid;
    public Object mCommand;
    public String mErrorMsg;
    public Boolean mIsSuccess;
    public Messenger mReplyTo;
    public Object mRequestId;
    public Object mResultValue;

    public Transaction(Message message) {
        this.mReplyTo = message.replyTo;
        Bundle data = message.getData();
        this.mArrivedBundle = data;
        if (data != null) {
            this.mRequestId = data.get(BindingApiConstants.BUNDLE_KEY_REQUEST_ID);
            this.mCommand = this.mArrivedBundle.get(BindingApiConstants.BUNDLE_KEY_CMD);
            this.mCallerUid = message.sendingUid;
        }
    }

    public void setSuccessResult(Object obj) {
        this.mIsSuccess = Boolean.TRUE;
        this.mResultValue = obj;
        this.mErrorMsg = null;
    }

    public void setErrorResult(String str) {
        this.mIsSuccess = Boolean.FALSE;
        this.mResultValue = null;
        this.mErrorMsg = str;
    }

    public boolean isTransactionMatched(int i, Object obj) {
        Bundle bundle = this.mArrivedBundle;
        if (bundle != null) {
            Object obj2 = bundle.get(BindingApiConstants.BUNDLE_KEY_REGISTRATION_REQUEST_ID);
            return this.mCallerUid == i && obj2 != null && obj2.equals(obj);
        }
        return false;
    }

    public boolean isTransactionMatched(int i, Messenger messenger) {
        return this.mCallerUid == i && this.mReplyTo.equals(messenger);
    }

    public Object getParameterFromBundle(String str) {
        Bundle bundle = this.mArrivedBundle;
        if (bundle == null) {
            return null;
        }
        return bundle.get(str);
    }
}
