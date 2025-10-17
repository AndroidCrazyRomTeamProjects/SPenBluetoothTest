package com.samsung.remotespen.core.device.data;

import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class BleSpenButtonEvent extends BleSpenSensorEvent {
    private Action mAction;
    private BleSpenButtonId mButtonId;

    /* loaded from: classes.dex */
    public enum Action {
        SINGLE_CLICKED,
        LONG_CLICK_STARTED,
        LONG_CLICK_FINISHED,
        DOUBLE_CLICKED,
        BUTTON_DOWN,
        BUTTON_UP,
        DOUBLE_CLICK_HOLD_STARTED,
        DOUBLE_CLICK_HOLD_FINISHED
    }

    public BleSpenButtonEvent(BleSpenButtonEvent bleSpenButtonEvent) {
        this(bleSpenButtonEvent.mAction, bleSpenButtonEvent.getEventTime(), bleSpenButtonEvent.getSensorType(), bleSpenButtonEvent.getSensorDeviceId(), bleSpenButtonEvent.getSpenId());
    }

    public BleSpenButtonEvent(Action action, long j, BleSpenSensorType bleSpenSensorType, BleSpenSensorId bleSpenSensorId, String str) {
        super(j, bleSpenSensorType, bleSpenSensorId, str);
        Assert.notNull(action);
        if (bleSpenSensorId == BleSpenSensorId.PRIMARY_BUTTON) {
            this.mButtonId = BleSpenButtonId.PRIMARY;
        } else if (bleSpenSensorId == BleSpenSensorId.SECONDARY_BUTTON) {
            this.mButtonId = BleSpenButtonId.SECONDARY;
        }
        this.mAction = action;
    }

    public BleSpenButtonId getButtonId() {
        return this.mButtonId;
    }

    public Action getAction() {
        return this.mAction;
    }

    public void setAction(Action action) {
        this.mAction = action;
    }
}
