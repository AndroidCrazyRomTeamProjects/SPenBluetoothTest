package com.samsung.remotespen.core.penaction.condition;

import android.content.Context;
import android.util.Log;

import com.samsung.util.debug.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class FixedMatchCondition extends PenActionCondition {
    private static final String KEY_IS_MATCHED = "is_matched";
    private static final String TAG = FixedMatchCondition.class.getSimpleName();
    private boolean mIsMatched;

    public static FixedMatchCondition createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 1;
        Assert.e(z, "Incorrect field count. count=" + split.length);
        return new FixedMatchCondition(Boolean.parseBoolean(split[0]));
    }

    public static FixedMatchCondition create(JSONObject jSONObject) {
        Boolean bool = Boolean.FALSE;
        try {
            bool = Boolean.valueOf(jSONObject.getBoolean(KEY_IS_MATCHED));
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "create: e = " + e);
        }
        return new FixedMatchCondition(bool.booleanValue());
    }

    public FixedMatchCondition(boolean z) {
        this.mIsMatched = z;
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public boolean isMatched(Context context, ConditionMatchState conditionMatchState) {
        return this.mIsMatched;
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_IS_MATCHED, this.mIsMatched);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }
}
