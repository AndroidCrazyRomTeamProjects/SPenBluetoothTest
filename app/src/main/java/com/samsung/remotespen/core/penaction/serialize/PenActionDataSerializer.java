package com.samsung.remotespen.core.penaction.serialize;

import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.KeyInjectionActionData;
import com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class PenActionDataSerializer {
    private static final String KEY_DATA = "data";
    private static final String KEY_TYPE = "type";
    private static final String TAG = PenActionConditionSerializer.class.getSimpleName();
    private static PenActionClassTypeArray mClassTypeArray = new PenActionClassTypeArray();
    private static PenActionDataFactory mFactory = new PenActionDataFactory();

    static {
        mClassTypeArray.add("APP_DEFINED", AppDefinedActionData.class);
        mClassTypeArray.add("SND_KEY", KeyInjectionActionData.class);
        mClassTypeArray.add("CAMERA_CONTROL", CameraControlActionData.class);
        mClassTypeArray.add("MEDIA_CONTROL", MediaControlActionData.class);
        mClassTypeArray.add("APP_LAUNCH", AppLaunchActionData.class);
        mClassTypeArray.add("DO_NOTHING", DoNothingActionData.class);
    }

    public JSONObject serialize(PenActionData penActionData) {
        Assert.notNull(penActionData);
        String typeByClass = mClassTypeArray.getTypeByClass(penActionData.getClass());
        Assert.notNull(typeByClass, "Undefined action data type : " + penActionData.getClass().getSimpleName());
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_TYPE, typeByClass);
            jSONObject.put(KEY_DATA, penActionData.encodeToJsonObject());
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "serialize: e = " + e);
        }
        return jSONObject;
    }

    public PenActionData createFromLegacyData(String str, String str2) {
        String firstField = getFirstField(str, str2);
        Assert.notNull(firstField);
        int length = firstField.length() + str2.length();
        return mFactory.createFromLegacyData(firstField, str.length() > length ? str.substring(length) : Constants.packageName.NONE, str2, mClassTypeArray);
    }

    public PenActionData create(JSONObject jSONObject) {
        JSONObject jSONObject2 = new JSONObject();
        String str = null;
        try {
            str = jSONObject.getString(KEY_TYPE);
            jSONObject2 = jSONObject.getJSONObject(KEY_DATA);
        } catch (JSONException e) {
            String str2 = TAG;
            Log.e(str2, "create: e = " + e);
        }
        Assert.notNull(str);
        return mFactory.create(str, jSONObject2, mClassTypeArray);
    }

    private String getFirstField(String str, String str2) {
        int indexOf = str.indexOf(str2);
        if (indexOf < 0) {
            indexOf = str.length();
        }
        return str.substring(0, indexOf);
    }
}
