package com.samsung.remotespen.main.invocation;

import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;

/* loaded from: classes.dex */
public class PenActionTrigger {
    private PenActionTriggerType mAlternativeTrigger;
    private PenActionTriggerType mPrimaryTrigger;

    public PenActionTrigger(PenActionTriggerType penActionTriggerType) {
        this.mPrimaryTrigger = penActionTriggerType;
    }

    public PenActionTriggerType getPrimaryTrigger() {
        return this.mPrimaryTrigger;
    }

    public PenActionTriggerType getAlternativeTrigger() {
        return this.mAlternativeTrigger;
    }

    public void setAlternativeTrigger(PenActionTriggerType penActionTriggerType) {
        this.mAlternativeTrigger = penActionTriggerType;
    }
}
