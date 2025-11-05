package com.samsung.remotespen.core.penaction.condition;

import android.content.Context;
import com.samsung.remotespen.util.MediaControlStateMonitor;
import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MediaSessionCondition extends PenActionCondition {
    private static final String TAG = MediaSessionCondition.class.getSimpleName();

    public static MediaSessionCondition createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 1;
        Assert.e(z, "Incorrect field count. count=" + split.length);
        return new MediaSessionCondition();
    }

    public static MediaSessionCondition create(JSONObject jSONObject) {
        return new MediaSessionCondition();
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public boolean isMatched(Context context, ConditionMatchState conditionMatchState) {
        return MediaControlStateMonitor.getInstance(context).isMediaControlAvailable(context, conditionMatchState.mForegroundPackageName);
    }

    @Override // com.samsung.remotespen.core.penaction.condition.PenActionCondition
    public JSONObject encodeToJsonObject() {
        return new JSONObject();
    }
}
