package com.samsung.remotespen.core.penaction.serialize;

import android.util.Log;

import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.KeyInjectionActionData;
import com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.util.debug.Assert;
import org.json.JSONObject;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: PenActionDataSerializer.java */
/* loaded from: classes.dex */
public class PenActionDataFactory {
    private static final String TAG = "PenActionDataFactory";

    public PenActionData createFromLegacyData(String str, String str2, String str3, PenActionClassTypeArray penActionClassTypeArray) {
        Class<?> classByType = penActionClassTypeArray.getClassByType(str);
        if (classByType == null) {
            String str4 = TAG;
            Log.e(str4, "createFromLegacyData : Failed to find class. type=" + str);
            Assert.fail("Failed to find class. type=" + str);
            return null;
        } else if (AppDefinedActionData.class == classByType) {
            return AppDefinedActionData.createFromLegacyData(str2, str3);
        } else {
            if (KeyInjectionActionData.class == classByType) {
                return KeyInjectionActionData.createFromLegacyData(str2, str3);
            }
            if (CameraControlActionData.class == classByType) {
                return CameraControlActionData.createFromLegacyData(str2, str3);
            }
            if (MediaControlActionData.class == classByType) {
                return MediaControlActionData.createFromLegacyData(str2, str3);
            }
            if (AppLaunchActionData.class == classByType) {
                return AppLaunchActionData.createFromLegacyData(str2, str3);
            }
            if (DoNothingActionData.class == classByType) {
                return DoNothingActionData.createFromLegacyData(str2, str3);
            }
            Assert.fail("Unknown class type : " + classByType.getName());
            return null;
        }
    }

    public PenActionData create(String str, JSONObject jSONObject, PenActionClassTypeArray penActionClassTypeArray) {
        Class<?> classByType = penActionClassTypeArray.getClassByType(str);
        if (classByType == null) {
            String str2 = TAG;
            Log.e(str2, "create : Failed to find class. type=" + str);
            Assert.fail("Failed to find class. type=" + str);
            return null;
        } else if (AppDefinedActionData.class == classByType) {
            return AppDefinedActionData.create(jSONObject);
        } else {
            if (KeyInjectionActionData.class == classByType) {
                return KeyInjectionActionData.create(jSONObject);
            }
            if (CameraControlActionData.class == classByType) {
                return CameraControlActionData.create(jSONObject);
            }
            if (MediaControlActionData.class == classByType) {
                return MediaControlActionData.create(jSONObject);
            }
            if (AppLaunchActionData.class == classByType) {
                return AppLaunchActionData.create(jSONObject);
            }
            if (DoNothingActionData.class == classByType) {
                return DoNothingActionData.create(jSONObject);
            }
            Assert.fail("Unknown class type : " + classByType.getName());
            return null;
        }
    }
}
