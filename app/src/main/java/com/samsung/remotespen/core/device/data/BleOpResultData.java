package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleOpResultData {
    private byte[] mByteData;
    private FmmConfig mFmmConfig;
    private Integer mGattStatusCode;
    private BleSpenLedState mLedState;
    private String mMessage;
    private Integer mMtu;
    private Integer mRawBatteryLevel;
    private BleOpResultCode mResultCode;
    private Integer mRssi;

    public BleOpResultData() {
    }

    public BleOpResultData(BleOpResultCode bleOpResultCode) {
        setBleResultCode(bleOpResultCode);
    }

    public boolean isSuccess() {
        BleOpResultCode bleOpResultCode = this.mResultCode;
        return bleOpResultCode != null && bleOpResultCode == BleOpResultCode.SUCCESS;
    }

    public void setByteData(byte[] bArr) {
        this.mByteData = bArr;
    }

    public byte[] getByteData() {
        return this.mByteData;
    }

    public void setBleResultCode(BleOpResultCode bleOpResultCode) {
        this.mResultCode = bleOpResultCode;
    }

    public void setGattStatusCode(int i) {
        this.mGattStatusCode = Integer.valueOf(i);
    }

    public void setRssi(int i) {
        this.mRssi = Integer.valueOf(i);
    }

    public void setRawBatteryLevel(Integer num) {
        this.mRawBatteryLevel = num;
    }

    public Integer getRawBatteryLevel() {
        return this.mRawBatteryLevel;
    }

    public void setMessage(String str) {
        this.mMessage = str;
    }

    public BleOpResultCode getResultCode() {
        return this.mResultCode;
    }

    public Integer getGattStatusCode(Integer num) {
        Integer num2 = this.mGattStatusCode;
        return num2 == null ? num : num2;
    }

    public Integer getRssi(Integer num) {
        Integer num2 = this.mRssi;
        return num2 == null ? num : num2;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public FmmConfig getFmmConfig() {
        return this.mFmmConfig;
    }

    public void setFmmConfig(FmmConfig fmmConfig) {
        this.mFmmConfig = fmmConfig;
    }

    public void setMtu(int i) {
        this.mMtu = Integer.valueOf(i);
    }

    public int getMtu() {
        return this.mMtu.intValue();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mResultCode);
        if (this.mGattStatusCode != null) {
            sb.append(", Gatt:" + this.mGattStatusCode);
        }
        if (this.mMessage != null) {
            sb.append(", msg:" + this.mMessage);
        }
        if (this.mRssi != null) {
            sb.append(", rssi:" + this.mRssi);
        }
        return sb.toString();
    }

    public BleSpenLedState getLedState() {
        return this.mLedState;
    }

    public void setLedState(BleSpenLedState bleSpenLedState) {
        this.mLedState = bleSpenLedState;
    }
}
