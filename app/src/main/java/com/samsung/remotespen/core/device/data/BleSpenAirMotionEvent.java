package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleSpenAirMotionEvent extends BleSpenSensorEvent {
    private Action mAction;
    private boolean mImpurity;
    private int mX;
    private int mY;

    /* loaded from: classes.dex */
    public enum Action {
        MOVE,
        START,
        FINISH
    }

    public BleSpenAirMotionEvent(Action action, int i, int i2, long j, BleSpenSensorType bleSpenSensorType, BleSpenSensorId bleSpenSensorId, String str) {
        super(j, bleSpenSensorType, bleSpenSensorId, str);
        this.mAction = action;
        this.mX = i;
        this.mY = i2;
    }

    public Action getAction() {
        return this.mAction;
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public void markAsImpurity(boolean z) {
        this.mImpurity = z;
    }

    public boolean isImpurity() {
        return this.mImpurity;
    }
}
