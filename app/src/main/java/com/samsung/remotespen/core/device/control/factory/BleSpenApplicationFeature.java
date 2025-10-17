package com.samsung.remotespen.core.device.control.factory;

import android.content.Context;
import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.data.RegulatoryInformationData;

/* loaded from: classes.dex */
public class BleSpenApplicationFeature {
    private int mDisplayModelNameStrId;
    private boolean mIsSupportAdvancedCharge;
    private boolean mIsSupportAirAction;
    private boolean mIsSupportAirMouse;
    private boolean mIsSupportAppGestureAction;
    private boolean mIsSupportBlindCharge;
    private boolean mIsSupportFirmwareUpgrade;
    private boolean mIsSupportFmm;
    private boolean mIsSupportGlobalGestureAction;
    private int mModelNumberStrId;
    private RegulatoryInformationData mRegulatoryInformationData;

    public BleSpenApplicationFeature(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, RegulatoryInformationData regulatoryInformationData) {
        this.mModelNumberStrId = i;
        this.mDisplayModelNameStrId = i2;
        this.mIsSupportAirAction = z;
        this.mIsSupportAppGestureAction = z2;
        this.mIsSupportGlobalGestureAction = z3;
        this.mIsSupportBlindCharge = z4;
        this.mIsSupportFirmwareUpgrade = z5;
        this.mIsSupportAirMouse = z6;
        this.mIsSupportFmm = z7;
        this.mIsSupportAdvancedCharge = z8;
        this.mRegulatoryInformationData = regulatoryInformationData;
    }

    public boolean isSupportAirAction() {
        return this.mIsSupportAirAction;
    }

    public boolean isSupportAppGestureAction() {
        return this.mIsSupportAppGestureAction;
    }

    public boolean isSupportGlobalGestureAction() {
        return this.mIsSupportGlobalGestureAction;
    }

    public boolean isSupportBlindCharge() {
        return this.mIsSupportBlindCharge;
    }

    public boolean isSupportFirmwareUpgrade() {
        return this.mIsSupportFirmwareUpgrade;
    }

    public boolean isSupportAirMouse() {
        return this.mIsSupportAirMouse;
    }

    public boolean isSupportFmm() {
        return this.mIsSupportFmm;
    }

    public String getDisplayModelName(Context context) {
        return context.getString(this.mDisplayModelNameStrId);
    }

    public boolean isSupportAdvancedCharge() {
        return this.mIsSupportAdvancedCharge;
    }

    public String getModelNumber(Context context) {
        int i = this.mModelNumberStrId;
        return i == 0 ? Constants.packageName.NONE : context.getString(i);
    }

    public RegulatoryInformationData getRegulatoryInformationData() {
        return this.mRegulatoryInformationData;
    }
}
