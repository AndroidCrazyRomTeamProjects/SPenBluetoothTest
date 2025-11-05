package com.samsung.remotespen.core.penaction.serialize;

import android.util.Log;

import com.samsung.remotespen.core.penaction.condition.CameraControlCondition;
import com.samsung.remotespen.core.penaction.condition.FixedMatchCondition;
import com.samsung.remotespen.core.penaction.condition.ForegroundActivityCondition;
import com.samsung.remotespen.core.penaction.condition.MediaSessionCondition;
import com.samsung.remotespen.core.penaction.condition.PenActionCondition;
import com.samsung.util.debug.Assert;
import org.json.JSONObject;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: PenActionConditionSerializer.java */
/* loaded from: classes.dex */
public class PenActionConditionFactory {
    private static final String TAG = "PenActionConditionFactory";

    public PenActionCondition createFromLegacyData(String str, String str2, String str3, PenActionClassTypeArray penActionClassTypeArray) {
        Class<?> classByType = penActionClassTypeArray.getClassByType(str);
        if (classByType == null) {
            String str4 = TAG;
            Log.e(str4, "createFromLegacyData : Failed to find class. type=" + str);
            Assert.fail("Failed to find class. type=" + str);
            return null;
        } else if (ForegroundActivityCondition.class == classByType) {
            return ForegroundActivityCondition.createFromLegacyData(str2, str3);
        } else {
            if (CameraControlCondition.class == classByType) {
                return CameraControlCondition.createFromLegacyData(str2, str3);
            }
            if (MediaSessionCondition.class == classByType) {
                return MediaSessionCondition.createFromLegacyData(str2, str3);
            }
            if (FixedMatchCondition.class == classByType) {
                return FixedMatchCondition.createFromLegacyData(str2, str3);
            }
            Assert.fail("Unknown class type : " + classByType.getName());
            return null;
        }
    }

    public PenActionCondition create(String str, JSONObject jSONObject, PenActionClassTypeArray penActionClassTypeArray) {
        Class<?> classByType = penActionClassTypeArray.getClassByType(str);
        if (classByType == null) {
            String str2 = TAG;
            Log.e(str2, "create : Failed to find class. type=" + str);
            Assert.fail("Failed to find class. type=" + str);
            return null;
        } else if (ForegroundActivityCondition.class == classByType) {
            return ForegroundActivityCondition.create(jSONObject);
        } else {
            if (CameraControlCondition.class == classByType) {
                return CameraControlCondition.create(jSONObject);
            }
            if (MediaSessionCondition.class == classByType) {
                return MediaSessionCondition.create(jSONObject);
            }
            if (FixedMatchCondition.class == classByType) {
                return FixedMatchCondition.create(jSONObject);
            }
            Assert.fail("Unknown class type : " + classByType.getName());
            return null;
        }
    }
}
