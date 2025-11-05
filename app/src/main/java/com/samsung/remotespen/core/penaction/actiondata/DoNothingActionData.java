package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;

import com.crazyromteam.spenbletest.R;

import org.json.JSONObject;

/* loaded from: classes.dex */
public class DoNothingActionData extends PenActionData {
    public static final String ACTION_FOR_LOGGING = "donothing";

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getActionForLogging(Context context) {
        return ACTION_FOR_LOGGING;
    }

    public static DoNothingActionData createFromLegacyData(String str, String str2) {
        return new DoNothingActionData();
    }

    public static DoNothingActionData create(JSONObject jSONObject) {
        return new DoNothingActionData();
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        return context.getString(R.string.remotespen_do_nothing);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public JSONObject encodeToJsonObject() {
        return new JSONObject();
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }
}
