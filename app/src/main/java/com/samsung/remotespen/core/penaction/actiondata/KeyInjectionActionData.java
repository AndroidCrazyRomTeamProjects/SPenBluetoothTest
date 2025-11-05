package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.R;
import com.samsung.aboutpage.Constants;
import com.samsung.util.ViewHelper;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class KeyInjectionActionData extends PenActionData {
    private static final int KEYCODE_ENCODE_VALUE = 4312;
    private static final String KEY_DATA = "data";
    private static final String TAG = KeyInjectionActionData.class.getSimpleName();
    private ArrayList<Integer> mKeyCodeArray;

    public static KeyInjectionActionData createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        Assert.e(split.length >= 1, "Incorrect field count. count=" + split.length + " data=" + str);
        ArrayList arrayList = new ArrayList();
        for (String str3 : split) {
            try {
                arrayList.add(Integer.valueOf(Integer.valueOf(str3).intValue() / KEYCODE_ENCODE_VALUE));
            } catch (Exception e) {
                Log.e(TAG, "createFromLegacyData : e=" + e, e);
                arrayList.clear();
            }
        }
        return new KeyInjectionActionData(arrayList);
    }

    public static KeyInjectionActionData create(JSONObject jSONObject) {
        ArrayList arrayList = new ArrayList();
        try {
            JSONArray jSONArray = jSONObject.getJSONArray(KEY_DATA);
            for (int i = 0; i < jSONArray.length(); i++) {
                arrayList.add(Integer.valueOf(jSONArray.getInt(i) / KEYCODE_ENCODE_VALUE));
            }
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "create: e = " + e);
        }
        return new KeyInjectionActionData(arrayList);
    }

    public KeyInjectionActionData(int i) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        this.mKeyCodeArray = arrayList;
        arrayList.clear();
        this.mKeyCodeArray.add(Integer.valueOf(i));
    }

    public KeyInjectionActionData(List<Integer> list) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        this.mKeyCodeArray = arrayList;
        arrayList.clear();
        this.mKeyCodeArray.addAll(list);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        if (this.mKeyCodeArray.size() == 1) {
            int intValue = this.mKeyCodeArray.get(0).intValue();
            if (intValue == 3) {
                return context.getString(R.string.settings_spen_keycode_home);
            }
            if (intValue == 4) {
                return context.getString(R.string.settings_spen_keycode_back);
            }
            if (intValue == 66) {
                return context.getString(R.string.settings_spen_keycode_enter);
            }
            if (intValue == 93) {
                return context.getString(R.string.settings_spen_keycode_page_down);
            }
            if (intValue == 187) {
                return context.getString(R.string.settings_spen_keycode_recent);
            }
        }
        return context.getString(R.string.settings_spen_keycode_custom);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            Iterator<Integer> it = this.mKeyCodeArray.iterator();
            while (it.hasNext()) {
                jSONArray.put(it.next().intValue() * KEYCODE_ENCODE_VALUE);
            }
            jSONObject.put(KEY_DATA, jSONArray);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getActionForLogging(Context context) {
        String label = getLabel(context);
        return label == null ? Constants.packageName.NONE : label.replace(ViewHelper.QUALIFIER_DELIMITER, Constants.packageName.NONE).replaceAll(" ", Constants.packageName.NONE);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }

    public ArrayList<Integer> getKeyCodeArray() {
        return (ArrayList) this.mKeyCodeArray.clone();
    }
}
