package com.samsung.remotespen.core.penaction.serialize;

import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.penaction.condition.CameraControlCondition;
import com.samsung.remotespen.core.penaction.condition.FixedMatchCondition;
import com.samsung.remotespen.core.penaction.condition.ForegroundActivityCondition;
import com.samsung.remotespen.core.penaction.condition.MediaSessionCondition;
import com.samsung.remotespen.core.penaction.condition.PenActionCondition;
import com.samsung.util.debug.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class PenActionConditionSerializer {
    private static final String KEY_DATA = "data";
    private static final String KEY_TYPE = "type";
    private static final String TAG = "PenActionConditionSerializer";
    private static PenActionClassTypeArray mClassTypeArray = new PenActionClassTypeArray();
    private static PenActionConditionFactory mFactory = new PenActionConditionFactory();

    static {
        mClassTypeArray.add("FG_ACTIVITY", ForegroundActivityCondition.class);
        mClassTypeArray.add("CAMERA_CONTROL", CameraControlCondition.class);
        mClassTypeArray.add("MEDIA_SESSION", MediaSessionCondition.class);
        mClassTypeArray.add("FIXED_MATCH", FixedMatchCondition.class);
    }

    public JSONObject serialize(PenActionCondition penActionCondition) {
        Assert.notNull(penActionCondition);
        String typeByClass = mClassTypeArray.getTypeByClass(penActionCondition.getClass());
        Assert.notNull(typeByClass);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_TYPE, typeByClass);
            jSONObject.put(KEY_DATA, penActionCondition.encodeToJsonObject());
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "serialize: e = " + e);
        }
        return jSONObject;
    }

    public PenActionCondition createFromLegacyData(String str, String str2) {
        String firstField = getFirstField(str, str2);
        Assert.notNull(firstField);
        int length = firstField.length() + str2.length();
        return mFactory.createFromLegacyData(firstField, str.length() > length ? str.substring(length) : Constants.packageName.NONE, str2, mClassTypeArray);
    }

    public PenActionCondition create(JSONObject jSONObject) {
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
