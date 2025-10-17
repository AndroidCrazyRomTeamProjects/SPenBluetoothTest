package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleDisconnReqData {
    private BleDisconnTriggerCode mTriggerCode;

    public BleDisconnReqData(BleDisconnTriggerCode bleDisconnTriggerCode) {
        this.mTriggerCode = bleDisconnTriggerCode;
    }

    public BleDisconnTriggerCode getTriggerCode() {
        return this.mTriggerCode;
    }
}
