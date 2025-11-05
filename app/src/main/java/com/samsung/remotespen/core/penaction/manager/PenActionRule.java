package com.samsung.remotespen.core.penaction.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.crazyromteam.spenbletest.R;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.condition.ConditionMatchState;
import com.samsung.remotespen.core.penaction.condition.ForegroundActivityCondition;
import com.samsung.remotespen.core.penaction.condition.PenActionCondition;
import com.samsung.remotespen.core.penaction.manager.PenActionDataSet;
import com.samsung.remotespen.core.penaction.serialize.PenActionConditionSerializer;
import com.samsung.remotespen.core.penaction.serialize.PenActionDataSerializer;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import com.samsung.util.CommonUtils;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class PenActionRule implements Cloneable {
    private static final String KEY_GUIDE_UI_SHOWN_PACKAGE_LIST = "guide_ui_shown_package_list";
    private static final String KEY_GUIDE_UI_SHOWN_TIME = "guide_ui_shown_time";
    private static final String KEY_IS_ENABLED = "is_enabled";
    private static final String KEY_IS_SWIFT_ACTION_DECLARED = "swift_action_declared";
    private static final String KEY_RULE_ACTION_SET = "action_set";
    private static final String KEY_RULE_CONDITION = "condition";
    private static final String KEY_TYPE = "type";
    private static final String TAG = PenActionRule.class.getSimpleName();
    private PenActionDataSet mActionSet;
    private PenActionCondition mCondition;
    private ArrayList<String> mGuideUiShownPackageList;
    private long mGuideUiShownTime;
    private int mId;
    private boolean mIsEnabled;
    private boolean mIsSwiftActionDeclared;
    private Type mType;

    /* loaded from: classes.dex */
    public enum Type {
        NONE,
        BASE,
        APP_DEFINED,
        CAMERA_CONTROL,
        MEDIA_CONTROL
    }

    public PenActionRule(int i, Type type, PenActionCondition penActionCondition, PenActionDataSet penActionDataSet, boolean z, boolean z2) {
        this.mId = 0;
        Type type2 = Type.NONE;
        this.mGuideUiShownTime = 0L;
        this.mId = i;
        this.mType = type;
        this.mIsEnabled = z;
        this.mGuideUiShownPackageList = new ArrayList<>();
        this.mCondition = penActionCondition;
        this.mActionSet = penActionDataSet;
        this.mIsSwiftActionDeclared = z2;
    }

    public PenActionRule(int i, String str, String str2, PenActionCondition penActionCondition, PenActionDataSet penActionDataSet) {
        this.mId = 0;
        this.mType = Type.NONE;
        this.mGuideUiShownTime = 0L;
        String[] split = str.split(str2);
        boolean z = split.length == 3 || split.length == 4 || split.length == 5;
        Assert.e(z, "Incorrect field count : " + split.length);
        this.mId = i;
        String str3 = split[0];
        if (!TextUtils.isEmpty(str3)) {
            Type valueOf = Type.valueOf(str3);
            this.mType = valueOf;
            if (valueOf == Type.CAMERA_CONTROL) {
                this.mIsSwiftActionDeclared = true;
            }
        }
        this.mGuideUiShownTime = Long.valueOf(split[1]).longValue();
        this.mIsEnabled = Boolean.valueOf(split[2]).booleanValue();
        if (split.length == 3) {
            Log.e(TAG, "PenActionRule : fields.length is still 3.");
            this.mGuideUiShownPackageList = new ArrayList<>();
        } else if (split.length == 4) {
            this.mGuideUiShownPackageList = createStringArrayFromStringData(split[3]);
        } else if (split.length >= 5) {
            this.mGuideUiShownPackageList = createStringArrayFromStringData(split[3]);
            this.mIsSwiftActionDeclared = Boolean.valueOf(split[4]).booleanValue();
        }
        this.mCondition = penActionCondition;
        this.mActionSet = penActionDataSet;
    }

    public PenActionRule(int i, JSONObject jSONObject) {
        this.mId = 0;
        this.mType = Type.NONE;
        this.mGuideUiShownTime = 0L;
        this.mId = i;
        try {
            String string = jSONObject.getString(KEY_TYPE);
            if (!TextUtils.isEmpty(string)) {
                Type valueOf = Type.valueOf(string);
                this.mType = valueOf;
                if (valueOf == Type.CAMERA_CONTROL) {
                    this.mIsSwiftActionDeclared = true;
                }
            }
            this.mGuideUiShownTime = jSONObject.getLong(KEY_GUIDE_UI_SHOWN_TIME);
            this.mIsEnabled = jSONObject.getBoolean(KEY_IS_ENABLED);
            this.mGuideUiShownPackageList = createStringArrayFromJsonArray(jSONObject.getJSONArray(KEY_GUIDE_UI_SHOWN_PACKAGE_LIST));
            this.mIsSwiftActionDeclared = jSONObject.getBoolean(KEY_IS_SWIFT_ACTION_DECLARED);
            this.mCondition = new PenActionConditionSerializer().create(jSONObject.getJSONObject(KEY_RULE_CONDITION));
            PenActionDataSerializer penActionDataSerializer = new PenActionDataSerializer();
            JSONObject jSONObject2 = jSONObject.getJSONObject(KEY_RULE_ACTION_SET);
            PenActionDataSet.ActionMap actionMap = new PenActionDataSet.ActionMap();
            Iterator<String> keys = jSONObject2.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                JSONObject jSONObject3 = jSONObject2.getJSONObject(next);
                if (jSONObject3.length() > 0) {
                    PenActionTriggerType valueOf2 = PenActionTriggerType.valueOf(next);
                    ActionItem create = ActionItem.create(jSONObject3);
                    create = create == null ? new ActionItem(penActionDataSerializer.create(jSONObject3), true) : create;
                    if (valueOf2 != null) {
                        actionMap.put(valueOf2, create);
                    }
                }
            }
            this.mActionSet = new PenActionDataSet(actionMap);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "PenActionRule : e=" + e, e);
        }
    }

    /* renamed from: clone */
    public PenActionRule m24clone() {
        try {
            PenActionRule penActionRule = (PenActionRule) super.clone();
            penActionRule.mCondition = this.mCondition.m21clone();
            penActionRule.mActionSet = this.mActionSet.m23clone();
            return penActionRule;
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "clone : e=" + e);
            return null;
        }
    }

    public int getId() {
        return this.mId;
    }

    public PenActionCondition getCondition() {
        return this.mCondition;
    }

    public PenActionDataSet getActionSet() {
        return this.mActionSet;
    }

    public PenActionData getAction(Context context, PenActionTriggerType penActionTriggerType) {
        return getAction(context, penActionTriggerType, true);
    }

    public PenActionData getEnabledAction(Context context, PenActionTriggerType penActionTriggerType) {
        ActionItem actionItem = this.mActionSet.getActionItem(penActionTriggerType);
        if (actionItem == null || actionItem.isEnabled()) {
            return getAction(context, penActionTriggerType, true);
        }
        return null;
    }

    private PenActionData getAction(Context context, PenActionTriggerType penActionTriggerType, boolean z) {
        PenActionCondition penActionCondition;
        if (this.mIsSwiftActionDeclared && penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
            String str = TAG;
            Log.d(str, "getAction : triggerType is " + penActionTriggerType + " and swift action is declared.");
            return PenActionHelper.createSimpleActionData(PenActionDataType.DO_NOTHING);
        }
        ActionItem actionItem = this.mActionSet.getActionItem(penActionTriggerType);
        PenActionData penActionData = actionItem != null ? actionItem.getPenActionData() : null;
        if (z && penActionData == null) {
            Log.d(TAG, "getAction : get default action data from remote action because pen action data is null.");
            Type type = this.mType;
            if (type == Type.APP_DEFINED && (penActionCondition = this.mCondition) != null) {
                ForegroundActivityCondition foregroundActivityCondition = (ForegroundActivityCondition) penActionCondition;
                return DefaultActionDetector.getDefaultAppDefinedActionData(context, foregroundActivityCondition.getPackageName(), foregroundActivityCondition.getActivityName(), penActionTriggerType);
            } else if (type == Type.MEDIA_CONTROL) {
                return DefaultActionDetector.getDefaultMediaActionData(penActionTriggerType);
            } else {
                if (type == Type.CAMERA_CONTROL) {
                    return DefaultActionDetector.getDefaultCameraActionData(penActionTriggerType);
                }
                return type == Type.BASE ? DefaultActionDetector.getDefaultBaseActionData(context, penActionTriggerType) : penActionData;
            }
        }
        return penActionData;
    }

    public boolean getEnabled() {
        return this.mIsEnabled;
    }

    public Type getActionType() {
        return this.mType;
    }

    public void setGuideUiShownTime(long j) {
        this.mGuideUiShownTime = j;
    }

    public void setEnabled(boolean z) {
        this.mIsEnabled = z;
    }

    public boolean addGuideUiShownPackageToList(String str) {
        if (this.mGuideUiShownPackageList == null) {
            this.mGuideUiShownPackageList = new ArrayList<>();
        }
        if (isGuideUiShownPackage(str)) {
            return false;
        }
        String str2 = TAG;
        Log.i(str2, "addGuideUiShownPackageToList : add=" + str);
        this.mGuideUiShownPackageList.add(str);
        return true;
    }

    public boolean isGuideUiShownPackage(String str) {
        ArrayList<String> arrayList = this.mGuideUiShownPackageList;
        if (arrayList == null) {
            return false;
        }
        return arrayList.contains(str);
    }

    public boolean isMatched(Context context, ConditionMatchState conditionMatchState, boolean z) {
        PenActionCondition penActionCondition = this.mCondition;
        if (penActionCondition != null) {
            if (z || this.mIsEnabled) {
                return penActionCondition.isMatched(context, conditionMatchState);
            }
            return false;
        }
        return false;
    }

    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_TYPE, this.mType.name());
            jSONObject.put(KEY_GUIDE_UI_SHOWN_TIME, this.mGuideUiShownTime);
            jSONObject.put(KEY_IS_ENABLED, this.mIsEnabled);
            jSONObject.put(KEY_GUIDE_UI_SHOWN_PACKAGE_LIST, encodeStringArrayToJasonArray(this.mGuideUiShownPackageList));
            jSONObject.put(KEY_IS_SWIFT_ACTION_DECLARED, this.mIsSwiftActionDeclared);
            jSONObject.put(KEY_RULE_CONDITION, new PenActionConditionSerializer().serialize(getCondition()));
            PenActionDataSet actionSet = getActionSet();
            JSONObject jSONObject2 = new JSONObject();
            for (PenActionTriggerType penActionTriggerType : actionSet.getPenActionTriggerTypeList()) {
                ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
                if (actionItem != null) {
                    jSONObject2.put(penActionTriggerType.name(), actionItem.encodeToJsonObject());
                }
            }
            jSONObject.put(KEY_RULE_ACTION_SET, jSONObject2);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject : e=" + e, e);
        }
        return jSONObject;
    }

    private ArrayList<String> createStringArrayFromStringData(String str) {
        if (!TextUtils.isEmpty(str)) {
            return new ArrayList<>(Arrays.asList(str.split("@")));
        }
        return new ArrayList<>();
    }

    private ArrayList<String> createStringArrayFromJsonArray(JSONArray jSONArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                arrayList.add(jSONArray.getString(i));
            } catch (JSONException e) {
                String str = TAG;
                Log.e(str, "createStringArrayFromJsonArray: e = " + e);
            }
        }
        return arrayList;
    }

    private JSONArray encodeStringArrayToJasonArray(ArrayList<String> arrayList) {
        JSONArray jSONArray = new JSONArray();
        if (arrayList != null) {
            Iterator<String> it = arrayList.iterator();
            while (it.hasNext()) {
                jSONArray.put(it.next());
            }
        }
        return jSONArray;
    }

    public boolean isSwiftActionDeclared() {
        return this.mIsSwiftActionDeclared;
    }

    public void setSwiftActionDeclared(boolean z) {
        this.mIsSwiftActionDeclared = z;
    }

    public String getDisplayName(Context context) {
        RemoteAction remoteAction;
        String actionSetLabel;
        int resourceId;
        if (this.mType != Type.APP_DEFINED) {
            return null;
        }
        PenActionCondition penActionCondition = this.mCondition;
        if (penActionCondition != null) {
            ForegroundActivityCondition foregroundActivityCondition = (ForegroundActivityCondition) penActionCondition;
            String packageName = foregroundActivityCondition.getPackageName();
            String activityName = foregroundActivityCondition.getActivityName();
            RemoteActionManager remoteActionManager = RemoteActionManager.getInstance(context);
            if (remoteActionManager != null && (remoteAction = remoteActionManager.getRemoteAction(packageName, activityName)) != null && (actionSetLabel = remoteAction.getActionSetLabel()) != null && (resourceId = CommonUtils.getResourceId(context, packageName, actionSetLabel)) > 0) {
                return CommonUtils.getStringFromPackage(context, packageName, resourceId);
            }
            PackageManager packageManager = context.getPackageManager();
            try {
                return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 128));
            } catch (PackageManager.NameNotFoundException unused) {
                return context.getString(R.string.remotespen_none);
            }
        }
        return context.getString(R.string.remotespen_none);
    }
}
