package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class RegulatoryInformationData {
    private int mFccCertificationStrId;
    private int mIcStrId;
    private int mJapanDataImageId;
    private int mKoreaDataStrId;

    public RegulatoryInformationData(int i, int i2, int i3, int i4) {
        this.mFccCertificationStrId = i;
        this.mIcStrId = i2;
        this.mKoreaDataStrId = i3;
        this.mJapanDataImageId = i4;
    }

    public int getFccCertificationStrId() {
        return this.mFccCertificationStrId;
    }

    public int getIcStrId() {
        return this.mIcStrId;
    }

    public int getKoreaDataStrId() {
        return this.mKoreaDataStrId;
    }

    public int getJapanDataImageId() {
        return this.mJapanDataImageId;
    }
}
