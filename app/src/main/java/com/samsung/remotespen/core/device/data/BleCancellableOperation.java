package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public interface BleCancellableOperation {

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    void cancelOperation(FinishListener finishListener);
}
