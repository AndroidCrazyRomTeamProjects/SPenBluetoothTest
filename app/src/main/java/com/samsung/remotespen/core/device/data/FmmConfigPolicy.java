package com.samsung.remotespen.core.device.data;

import java.util.ArrayList;

/* loaded from: classes.dex */
public class FmmConfigPolicy {
    private int mAdvertiseInterval;
    private int mLostModelInterval;
    private int mRound;
    private int mShuffleInterval;
    private ArrayList<String> mTable;
    private int mVersion;

    public FmmConfigPolicy() {
        this.mVersion = -1;
        this.mLostModelInterval = -1;
        this.mAdvertiseInterval = -1;
        this.mShuffleInterval = -1;
        this.mRound = -1;
        this.mTable = null;
    }

    public FmmConfigPolicy(int i, int i2, int i3, int i4, int i5, ArrayList<String> arrayList) {
        this.mVersion = i;
        this.mLostModelInterval = i2;
        this.mAdvertiseInterval = i3;
        this.mShuffleInterval = i4;
        this.mRound = i5;
        this.mTable = arrayList;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public int getLostModelInterval() {
        return this.mLostModelInterval;
    }

    public int getAdvertiseInterval() {
        return this.mAdvertiseInterval;
    }

    public int getShuffleInterval() {
        return this.mShuffleInterval;
    }

    public int getRound() {
        return this.mRound;
    }

    public ArrayList<String> getTable() {
        return this.mTable;
    }

    public void setVersion(int i) {
        this.mVersion = i;
    }

    public void setLostModeInterval(int i) {
        this.mLostModelInterval = i;
    }

    public void setAdvertiseInterval(int i) {
        this.mAdvertiseInterval = i;
    }

    public void setShuffleInterval(int i) {
        this.mShuffleInterval = i;
    }

    public void setRound(int i) {
        this.mRound = i;
    }

    public void setTable(ArrayList<String> arrayList) {
        this.mTable = arrayList;
    }

    public String toString() {
        return "FmmConfigPolicy{ mVersion='" + this.mVersion + "', mLostModelInterval='" + this.mLostModelInterval + "', mAdvertiseInterval='" + this.mAdvertiseInterval + "', mShuffleInterval=" + this.mShuffleInterval + ", mRound=" + this.mRound + ", mTable=[" + this.mTable.toString() + "]}";
    }
}
