package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleStateChangeInfo {
    public BleConnReqData mConnReqData;
    public BleDisconnReqData mDisconnReqData;
    public Integer mGattStatusCode;
    public BleOpResultData mOpResult;

    public BleStateChangeInfo() {
    }

    public BleStateChangeInfo(int i) {
        this.mGattStatusCode = Integer.valueOf(i);
    }

    public BleStateChangeInfo(BleOpResultData bleOpResultData) {
        this.mOpResult = bleOpResultData;
    }

    public BleOpResultData getBleOpResultData() {
        return this.mOpResult;
    }

    public Integer getGattStatusCode() {
        return this.mGattStatusCode;
    }

    public BleConnReqData getConnReqData() {
        return this.mConnReqData;
    }

    public BleConnTriggerCode getConnectionTriggerCode() {
        BleConnReqData bleConnReqData = this.mConnReqData;
        if (bleConnReqData == null) {
            return null;
        }
        return bleConnReqData.getTriggerCode();
    }

    public void setConnReqData(BleConnReqData bleConnReqData) {
        this.mConnReqData = bleConnReqData;
    }

    public void setDisconnReqData(BleDisconnReqData bleDisconnReqData) {
        this.mDisconnReqData = bleDisconnReqData;
    }

    public BleDisconnTriggerCode getDisconnectionTriggerCode() {
        BleDisconnReqData bleDisconnReqData = this.mDisconnReqData;
        if (bleDisconnReqData == null) {
            return null;
        }
        return bleDisconnReqData.getTriggerCode();
    }

    public BleDisconnReqData getDisconnReqData() {
        return this.mDisconnReqData;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.mOpResult != null) {
            sb.append("[RC:" + this.mOpResult.toString() + "] ");
        }
        if (this.mGattStatusCode != null) {
            sb.append("[Gatt:" + this.mGattStatusCode + "]");
        }
        return sb.toString();
    }
}
