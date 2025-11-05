package com.samsung.remotespen.core.penaction.condition;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class ForegroundActivityCondition extends PenActionCondition {
    private static final String KEY_ACTIVITY_NAME = "activity_name";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String TAG = ForegroundActivityCondition.class.getSimpleName();
    private String mActivityName;
    private String mPackageName;

    public static ForegroundActivityCondition createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 2;
        Assert.e(z, "Incorrect field count. count=" + split.length);
        return new ForegroundActivityCondition(split[0], split[1]);
    }

    public static ForegroundActivityCondition create(JSONObject jSONObject) {
        String str;
        String str2 = null;
        try {
            str = jSONObject.getString("package_name");
        } catch (JSONException e) {
            e = e;
            str = null;
        }
        try {
            str2 = jSONObject.getString(KEY_ACTIVITY_NAME);
        } catch (JSONException e2) {
            e = e2;
            Log.e(TAG, "create: e = " + e);
            return new ForegroundActivityCondition(str, str2);
        }
        return new ForegroundActivityCondition(str, str2);
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("package_name", this.mPackageName);
            jSONObject.put(KEY_ACTIVITY_NAME, this.mActivityName);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    public ForegroundActivityCondition(String str, String str2) {
        Assert.notNull(str);
        Assert.notNull(str2);
        this.mPackageName = str;
        this.mActivityName = str2;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getActivityName() {
        return this.mActivityName;
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public boolean isMatched(Context context, ConditionMatchState conditionMatchState) {
        String str = conditionMatchState.mForegroundPackageName;
        boolean z = str != null && str.equals(this.mPackageName);
        String str2 = conditionMatchState.mForegorundActivityName;
        return z && (str2 != null && str2.equals(this.mActivityName));
    }
}
