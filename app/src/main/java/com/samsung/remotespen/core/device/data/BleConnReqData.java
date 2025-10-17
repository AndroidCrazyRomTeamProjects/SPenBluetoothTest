package com.samsung.remotespen.core.device.data;

import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleConnReqData {
    private static final String TAG = "BleConnReqData";
    private int mBatteryLevel;
    private boolean mIsSpenHardResetStepEnabled;
    private String mTargetAddress;
    private SpenModelName mTargetSpenModelName;
    private BleConnTriggerCode mTriggerCode;

    public BleConnReqData(SpenModelName spenModelName, BleConnTriggerCode bleConnTriggerCode) {
        this.mBatteryLevel = 0;
        this.mIsSpenHardResetStepEnabled = false;
        this.mTargetSpenModelName = spenModelName;
        this.mTriggerCode = bleConnTriggerCode;
    }

    public BleConnReqData(SpenModelName spenModelName, BleConnTriggerCode bleConnTriggerCode, String str) {
        this(spenModelName, bleConnTriggerCode);
        this.mTargetAddress = str;
    }

    public BleConnReqData(SpenModelName spenModelName, BleConnTriggerCode bleConnTriggerCode, int i) {
        this(spenModelName, bleConnTriggerCode);
        this.mBatteryLevel = i;
    }

    public void enableSpenHardResetStep(boolean z) {
        this.mIsSpenHardResetStepEnabled = z;
    }

    public BleConnTriggerCode getTriggerCode() {
        return this.mTriggerCode;
    }

    public String getTargetAddress() {
        return this.mTargetAddress;
    }

    public SpenModelName getTargetSpenModelName() {
        return this.mTargetSpenModelName;
    }

    public boolean isSpenHardResetStepEnabled() {
        return this.mIsSpenHardResetStepEnabled;
    }

    public int getBatteryLevel() {
        return this.mBatteryLevel;
    }
}
