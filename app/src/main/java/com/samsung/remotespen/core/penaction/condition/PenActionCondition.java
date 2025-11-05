package com.samsung.remotespen.core.penaction.condition;

import android.content.Context;
import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONObject;

/* loaded from: classes.dex */
public abstract class PenActionCondition implements Cloneable {
    public abstract JSONObject encodeToJsonObject();

    public abstract boolean isMatched(Context context, ConditionMatchState conditionMatchState);

    /* renamed from: clone */
    public PenActionCondition m21clone() {
        try {
            return (PenActionCondition) super.clone();
        } catch (Exception e) {
            Assert.fail("Failed to clone. e=" + e);
            return null;
        }
    }
}
