package com.samsung.remotespen.core.device.data;

import com.samsung.aboutpage.Constants;

/* loaded from: classes.dex */
public class FmmConfig {
    public static final int NOT_SET = -1;
    private String mE2e;
    private String mFindingSupport;
    private String mFmmToken;
    private String mIv;
    private int mMaxN;
    private FmmConfigPolicy mPolicy;
    private int mRegion;
    private String mSecretKey;

    public FmmConfig() {
        this.mFindingSupport = Constants.packageName.NONE;
        this.mFmmToken = Constants.packageName.NONE;
        this.mSecretKey = Constants.packageName.NONE;
        this.mMaxN = -1;
        this.mRegion = -1;
        this.mE2e = Constants.packageName.NONE;
        this.mIv = Constants.packageName.NONE;
        this.mPolicy = null;
    }

    public FmmConfig(String str, String str2, String str3, int i, int i2, String str4, String str5) {
        this.mFindingSupport = str;
        this.mFmmToken = str2;
        this.mSecretKey = str3;
        this.mMaxN = i;
        this.mRegion = i2;
        this.mE2e = str4;
        this.mIv = str5;
    }

    public FmmConfig(String str, String str2, String str3, int i, int i2, String str4, String str5, FmmConfigPolicy fmmConfigPolicy) {
        this.mFindingSupport = str;
        this.mFmmToken = str2;
        this.mSecretKey = str3;
        this.mMaxN = i;
        this.mRegion = i2;
        this.mE2e = str4;
        this.mIv = str5;
        this.mPolicy = fmmConfigPolicy;
    }

    public FmmConfigPolicy getPolicy() {
        return this.mPolicy;
    }

    public void setPolicy(FmmConfigPolicy fmmConfigPolicy) {
        this.mPolicy = fmmConfigPolicy;
    }

    public String getFmmToken() {
        return this.mFmmToken;
    }

    public String getSecretKey() {
        return this.mSecretKey;
    }

    public int getMaxN() {
        return this.mMaxN;
    }

    public int getRegion() {
        return this.mRegion;
    }

    public String getFindingSupport() {
        return this.mFindingSupport;
    }

    public String getE2e() {
        return this.mE2e;
    }

    public String getIv() {
        return this.mIv;
    }

    public void setFmmToken(String str) {
        this.mFmmToken = str;
    }

    public void setSecretKey(String str) {
        this.mSecretKey = str;
    }

    public void setMaxN(int i) {
        this.mMaxN = i;
    }

    public void setRegion(int i) {
        this.mRegion = i;
    }

    public void setFindingSupport(String str) {
        this.mFindingSupport = str;
    }

    public void setE2e(String str) {
        this.mE2e = str;
    }

    public void setIv(String str) {
        this.mIv = str;
    }

    public String toString() {
        String str = "FmmConfig{, mFmmToken='" + this.mFmmToken + "', mSecretKey='" + this.mSecretKey + "', mMaxN=" + this.mMaxN + ", mRegion=" + this.mRegion + ", mFindingSupport='" + this.mFindingSupport + "', mE2e='" + this.mE2e + "', mIv='" + this.mIv + "', ='" + this.mIv + "'}";
        FmmConfigPolicy fmmConfigPolicy = this.mPolicy;
        return fmmConfigPolicy != null ? str.concat(fmmConfigPolicy.toString()) : str;
    }
}
