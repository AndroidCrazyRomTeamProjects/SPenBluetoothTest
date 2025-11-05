package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONObject;

/* loaded from: classes.dex */
public abstract class PenActionData implements Cloneable {
    public abstract Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor);

    public abstract JSONObject encodeToJsonObject();

    public abstract String getActionForLogging(Context context);

    public abstract String getLabel(Context context);

    /* renamed from: clone */
    public PenActionData m20clone() {
        try {
            return (PenActionData) super.clone();
        } catch (Exception e) {
            Assert.fail("Failed to clone. e=" + e);
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        return encodeToJsonObject().toString().equals(((PenActionData) obj).encodeToJsonObject().toString());
    }

    public int hashCode() {
        return super.hashCode();
    }
}
