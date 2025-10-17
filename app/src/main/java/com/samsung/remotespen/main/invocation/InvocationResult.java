package com.samsung.remotespen.main.invocation;

import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;

/* loaded from: classes.dex */
public class InvocationResult {
    public PenActionRule mActionRule;
    public PenActionFinishListener mFinishListener;
    public PenActionData mInvokedPenAction;
    private boolean mIsConsumed;

    public InvocationResult() {
        this.mIsConsumed = false;
        this.mInvokedPenAction = null;
    }

    public InvocationResult(boolean z, PenActionData penActionData) {
        this.mIsConsumed = z;
        this.mInvokedPenAction = penActionData;
    }

    public InvocationResult setConsumed(boolean z) {
        this.mIsConsumed = z;
        return this;
    }

    public boolean isConsumed() {
        return this.mIsConsumed;
    }

    public InvocationResult setConsumedAtKeyguard() {
        this.mIsConsumed = true;
        return this;
    }

    public InvocationResult setConsumedAtScreenOffState() {
        this.mIsConsumed = true;
        return this;
    }

    public String toString() {
        if (isConsumed()) {
            String str = "invocation consumed. ";
            if (this.mInvokedPenAction != null) {
                str = "invocation consumed. invoked action=" + this.mInvokedPenAction.getClass().getSimpleName();
            }
            if (this.mFinishListener != null) {
                return str + ", finish listener exists";
            }
            return str;
        }
        return "invocation not consumed";
    }
}
