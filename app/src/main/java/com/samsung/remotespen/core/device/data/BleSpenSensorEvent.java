package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public abstract class BleSpenSensorEvent {
    private long mEventTime;
    private boolean mIsApproached;
    private BleSpenSensorId mSensorId;
    private BleSpenSensorType mSensorType;
    private String mSpenId;

    public BleSpenSensorEvent(long j, BleSpenSensorType bleSpenSensorType, BleSpenSensorId bleSpenSensorId, String str) {
        this.mEventTime = j;
        this.mSensorId = bleSpenSensorId;
        this.mSensorType = bleSpenSensorType;
        this.mSpenId = str;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    public BleSpenSensorType getSensorType() {
        return this.mSensorType;
    }

    public BleSpenSensorId getSensorDeviceId() {
        return this.mSensorId;
    }

    public String getSpenId() {
        return this.mSpenId;
    }

    public void setEventTime(long j) {
        this.mEventTime = j;
    }

    public void markAsApproached(boolean z) {
        this.mIsApproached = z;
    }

    public boolean isApproached() {
        return this.mIsApproached;
    }
}
