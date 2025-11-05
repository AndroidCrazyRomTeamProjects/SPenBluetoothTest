package com.samsung.remotespen.core.penaction.condition;

import android.content.Context;
import com.samsung.util.debug.Assert;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class CameraControlCondition extends PenActionCondition {
    private static final String TAG = CameraControlCondition.class.getSimpleName();

    public static CameraControlCondition createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 1;
        Assert.e(z, "Incorrect field count. count=" + split.length);
        return new CameraControlCondition();
    }

    public static CameraControlCondition create(JSONObject jSONObject) {
        return new CameraControlCondition();
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public boolean isMatched(Context context, ConditionMatchState conditionMatchState) {
        return !conditionMatchState.mHasAppDefinedRule && conditionMatchState.mIsCameraPreviewOn;
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public JSONObject encodeToJsonObject() {
        return new JSONObject();
    }
}
