package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleSpenAccGyroEvent extends BleSpenSensorEvent {
    private int mAccX;
    private int mAccY;
    private int mAccZ;
    private int mGyroX;
    private int mGyroY;
    private int mGyroZ;

    public BleSpenAccGyroEvent(int i, int i2, int i3, int i4, int i5, int i6, long j, BleSpenSensorType bleSpenSensorType, BleSpenSensorId bleSpenSensorId, String str) {
        super(j, bleSpenSensorType, bleSpenSensorId, str);
        this.mAccX = i;
        this.mAccY = i2;
        this.mAccZ = i3;
        this.mGyroX = i4;
        this.mGyroY = i5;
        this.mGyroZ = i6;
    }

    public int getAccX() {
        return this.mAccX;
    }

    public int getAccY() {
        return this.mAccY;
    }

    public int getAccZ() {
        return this.mAccZ;
    }

    public int getGyroX() {
        return this.mGyroX;
    }

    public int getGyroY() {
        return this.mGyroY;
    }

    public int getGyroZ() {
        return this.mGyroZ;
    }
}
