package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleSpenGestureEvent {
    private long mGestureStartTime;
    private BleSpenGestureType mGestureType = BleSpenGestureType.UNKNOWN;
    private Action mAction = Action.NO_REPEAT;
    private BleSpenGestureQuality mGestureQuality = new BleSpenGestureQuality();

    /* loaded from: classes.dex */
    public enum Action {
        NO_REPEAT,
        START_REPEAT,
        FINISH_REPEAT
    }

    public void setGestureType(BleSpenGestureType bleSpenGestureType) {
        this.mGestureType = bleSpenGestureType;
    }

    public void setGestureStartTime(long j) {
        this.mGestureStartTime = j;
    }

    public long getGestureStartTime() {
        return this.mGestureStartTime;
    }

    public void setAction(Action action) {
        this.mAction = action;
    }

    public void setGestureQuality(BleSpenGestureQuality bleSpenGestureQuality) {
        this.mGestureQuality = bleSpenGestureQuality;
    }

    public BleSpenGestureType getGestureType() {
        return this.mGestureType;
    }

    public Action getAction() {
        return this.mAction;
    }

    public BleSpenGestureQuality getGestureQuality() {
        return this.mGestureQuality;
    }

    /* renamed from: clone */
    public BleSpenGestureEvent m19clone() {
        BleSpenGestureEvent bleSpenGestureEvent = new BleSpenGestureEvent();
        bleSpenGestureEvent.setGestureType(this.mGestureType);
        bleSpenGestureEvent.setAction(this.mAction);
        bleSpenGestureEvent.setGestureQuality(this.mGestureQuality);
        bleSpenGestureEvent.setGestureStartTime(this.mGestureStartTime);
        return bleSpenGestureEvent;
    }

    public String toString() {
        return this.mGestureType + ", " + this.mAction + ", " + this.mGestureQuality;
    }
}
