package com.samsung.remotespen.core.penaction.manager;

import android.util.Log;

import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.serialize.PenActionDataSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class ActionItem implements Cloneable {
    private static final String KEY_ACTION_DATA = "action_data";
    private static final String KEY_ENABLED = "enabled";
    private static final String TAG = ActionItem.class.getSimpleName();
    private boolean mIsEnabled;
    private PenActionData mPenActionData;

    public ActionItem(PenActionData penActionData) {
        this(penActionData, true);
    }

    public ActionItem(PenActionData penActionData, boolean z) {
        this.mPenActionData = penActionData;
        this.mIsEnabled = z;
    }

    public static ActionItem create(JSONObject jSONObject) {
        JSONObject jSONObject2;
        try {
            boolean z = jSONObject.getBoolean(KEY_ENABLED);
            try {
                jSONObject2 = jSONObject.getJSONObject(KEY_ACTION_DATA);
            } catch (JSONException e) {
                Log.i(TAG, "create : + ", e);
                jSONObject2 = null;
            }
            return new ActionItem(jSONObject2 != null ? new PenActionDataSerializer().create(jSONObject2) : null, z);
        } catch (JSONException e2) {
            Log.i(TAG, "create : Not ActionItem json data", e2);
            return null;
        }
    }

    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_ENABLED, this.mIsEnabled);
            if (this.mPenActionData != null) {
                jSONObject.put(KEY_ACTION_DATA, new PenActionDataSerializer().serialize(this.mPenActionData));
            }
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    public void setPenActionData(PenActionData penActionData) {
        this.mPenActionData = penActionData;
    }

    public PenActionData getPenActionData() {
        return this.mPenActionData;
    }

    public void setEnabled(boolean z) {
        this.mIsEnabled = z;
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    /* renamed from: clone */
    public ActionItem m22clone() {
        ActionItem actionItem;
        try {
            actionItem = (ActionItem) super.clone();
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "clone : e=" + e);
            actionItem = new ActionItem(null);
        }
        actionItem.setEnabled(this.mIsEnabled);
        PenActionData penActionData = this.mPenActionData;
        if (penActionData == null) {
            actionItem.setPenActionData(null);
        } else {
            actionItem.setPenActionData(penActionData.m20clone());
        }
        return actionItem;
    }
}
