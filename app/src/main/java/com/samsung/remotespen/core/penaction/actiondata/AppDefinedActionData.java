package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import com.samsung.util.ViewHelper;
import com.samsung.util.debug.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class AppDefinedActionData extends PenActionData {
    private static final String KEY_ACTION_ID = "action_id";
    private static final String KEY_ACTIVITY_NAME = "activity_name";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String TAG = AppDefinedActionData.class.getSimpleName();
    private String mActionId;
    private String mActivityName;
    private String mPackageName;

    public static AppDefinedActionData createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 3;
        Assert.e(z, "Incorrect field count. count=" + split.length + " data=" + str);
        return new AppDefinedActionData(split[0], split[1], split[2]);
    }

    public static AppDefinedActionData create(JSONObject jSONObject) {
        String str;
        String str2;
        String str3 = null;
        try {
            str = jSONObject.getString("package_name");
            try {
                str2 = jSONObject.getString(KEY_ACTIVITY_NAME);
            } catch (JSONException e) {
                e = e;
                str2 = null;
            }
        } catch (JSONException e2) {
            e = e2;
            str = null;
            str2 = null;
        }
        try {
            str3 = jSONObject.getString(KEY_ACTION_ID);
        } catch (JSONException e3) {
            e = e3;
            Log.e(TAG, "create: e = " + e);
            return new AppDefinedActionData(str, str2, str3);
        }
        return new AppDefinedActionData(str, str2, str3);
    }

    public AppDefinedActionData(String str, String str2, String str3) {
        this.mPackageName = str;
        this.mActivityName = str2;
        this.mActionId = str3;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        RemoteAction remoteAction = RemoteActionManager.getInstance(context).getRemoteAction(this.mPackageName, this.mActivityName);
        if (remoteAction != null) {
            return remoteAction.getActionLabelFromResource(context, this.mActionId);
        }
        return null;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public JSONObject encodeToJsonObject() {
        String str = this.mActionId;
        if (str == null) {
            str = Constants.packageName.NONE;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("package_name", this.mPackageName);
            jSONObject.put(KEY_ACTIVITY_NAME, this.mActivityName);
            jSONObject.put(KEY_ACTION_ID, str);
        } catch (JSONException e) {
            String str2 = TAG;
            Log.e(str2, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getActionForLogging(Context context) {
        return this.mActionId.replace(ViewHelper.QUALIFIER_DELIMITER, Constants.packageName.NONE);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getActivityName() {
        return this.mActivityName;
    }

    public String getActionId() {
        return this.mActionId;
    }

    public Action getAction(RemoteActionManager remoteActionManager) {
        String str = this.mActionId;
        if (str != null) {
            return remoteActionManager.getAction(this.mPackageName, this.mActivityName, str);
        }
        return null;
    }
}
