package com.samsung.remotespen.core.penaction.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.launcher.util.SettingDBHelper;
import com.samsung.remotespen.core.device.control.SpenFunctionalityManager;
import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.condition.CameraControlCondition;
import com.samsung.remotespen.core.penaction.condition.ConditionMatchState;
import com.samsung.remotespen.core.penaction.condition.FixedMatchCondition;
import com.samsung.remotespen.core.penaction.condition.ForegroundActivityCondition;
import com.samsung.remotespen.core.penaction.condition.MediaSessionCondition;
import com.samsung.remotespen.core.penaction.condition.PenActionCondition;
import com.samsung.remotespen.core.penaction.manager.PenActionDataSet;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.remotespen.core.penaction.serialize.PenActionConditionSerializer;
import com.samsung.remotespen.core.penaction.serialize.PenActionDataSerializer;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import com.samsung.remotespen.util.CameraAvailabilityMonitor;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.top.AirCommandApplication;
import com.samsung.util.ActivitySwitchDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.ViewHelper;
import com.samsung.util.debug.Assert;
import com.samsung.util.settings.SettingsValueManager;
import com.samsung.util.usage.SAUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class PenActionRuleManager {
    private static final String END_MARK_ACTION = "#";
    private static final String END_MARK_ACTION_SET = "^";
    private static final String END_MARK_CONDITION = "|";
    private static final String END_MARK_RULE_ITEM = "&";
    public static final String PREF_KEY_APP_DEFINED_RULE = "app_defined_rule";
    public static final String PREF_KEY_BASE_RULE = "base_rule";
    public static final String PREF_KEY_CAMERA_CONTROL_RULE = "camera_control_rule";
    public static final String PREF_KEY_MEDIA_CONTROL_RULE = "media_control_rule";
    public static final String PREF_KEY_RULE_VERSION = "rule_version";
    private static final String PREF_NAME = "actionrule";
    private static final int RULE_VERSION = 4;
    private static final String SEPARATOR_LEAF_FIELD = ",";
    private static final String TAG = "PenActionRuleManager";
    private static PenActionRuleManager sInstance;
    private PenActionRule mBaseRule;
    private PenActionRule mCameraControlRule;
    private Context mContext;
    private DefaultRuleEnableStateManager mDefaultRuleEnableStateManager;
    private PenActionRule mMediaControlRule;
    private int mLastRuleItemId = 0;
    private ArrayList<PenActionRule> mAppDefinedRuleSet = new ArrayList<>();
    private ArrayList<RuleChangeListener> mRuleChangeListener = new ArrayList<>();
    private SettingDBHelper mSettingDBHelper = null;

    /* loaded from: classes.dex */
    public interface RuleChangeListener {
        void onRuleChanged();
    }

    public static String getPreferenceName() {
        return PREF_NAME;
    }

    public static int getVersion() {
        return 4;
    }

    public static PenActionRuleManager getInstance() {
        if (sInstance == null) {
            sInstance = new PenActionRuleManager();
        }
        return sInstance;
    }

    private PenActionRuleManager() {
        initialize(AirCommandApplication.getStaticApplicationContext());
    }

    private void initialize(Context context) {
        String str = TAG;
        Log.v(str, "initialize");
        if (context == null) {
            Log.e(str, "initialize : context is null!");
        } else if (isInitialized()) {
            Log.e(str, "initialize : Already initalized", new Exception());
        } else {
            final RemoteActionManager remoteActionManager = RemoteActionManager.getInstance(context);
            if (!remoteActionManager.isCollected()) {
                Log.e(str, "initialize : remote action manager is not ready");
                Assert.fail("remote action manager is not ready");
                return;
            }
            remoteActionManager.registerRemoteActionChangeListener(new RemoteActionManager.RemoteActionChangeListener() { // from class: com.samsung.remotespen.core.penaction.manager.PenActionRuleManager.1
                @Override // com.samsung.remotespen.core.remoteaction.RemoteActionManager.RemoteActionChangeListener
                public void onRemoteActionChanged(String str2, boolean z) {
                    PenActionRuleManager.this.onRemoteActionChanged(remoteActionManager);
                    if ("com.samsung.android.app.notes".equals(str2)) {
                        if (z) {
                            PenActionRuleManager.this.exceptNotePackageFromShortcutList();
                            return;
                        }
                        SettingsValueManager.getInstance(PenActionRuleManager.this.mContext).setScreenOffMemoEnabled(true);
                        SettingsValueManager.getInstance(PenActionRuleManager.this.mContext).setCreateNoteWithPenButtonEnabled(true);
                    }
                }

                @Override // com.samsung.remotespen.core.remoteaction.RemoteActionManager.RemoteActionChangeListener
                public void onRemoteActionReloaded() {
                    PenActionRuleManager.this.onRemoteActionChanged(remoteActionManager);
                }
            });
            this.mDefaultRuleEnableStateManager = new DefaultRuleEnableStateManager(context);
            this.mContext = context.getApplicationContext();
            readFromStorage();
            validateAppDefinedRules(context, remoteActionManager);
            validateBaseRule();
            sendSpenActionStatusLog();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exceptNotePackageFromShortcutList() {
        if (this.mSettingDBHelper == null) {
            this.mSettingDBHelper = new SettingDBHelper(this.mContext);
        }
        ArrayList<String> shortcutList = this.mSettingDBHelper.getShortcutList();
        shortcutList.removeIf(PenActionRuleManager$$ExternalSyntheticLambda0.INSTANCE);
        this.mSettingDBHelper.putShortcutList(shortcutList);
    }

    public void resetSettings() {
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREF_NAME, 0).edit();
        edit.clear();
        edit.apply();
        this.mAppDefinedRuleSet.clear();
        this.mCameraControlRule = null;
        this.mMediaControlRule = null;
        this.mBaseRule = null;
        RemoteActionManager remoteActionManager = RemoteActionManager.getInstance(this.mContext);
        readFromStorage();
        validateAppDefinedRules(this.mContext, remoteActionManager);
        validateBaseRule();
        invokeRuleChangedListener();
    }

    public void restoreFromStorage() {
        if (isInitialized()) {
            readFromStorage();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRemoteActionChanged(RemoteActionManager remoteActionManager) {
        Log.d(TAG, "onRemoteActionChanged");
        validateAppDefinedRules(this.mContext, remoteActionManager);
        validateBaseRule();
        invokeRuleChangedListener();
    }

    private void invokeRuleChangedListener() {
        Iterator<RuleChangeListener> it = this.mRuleChangeListener.iterator();
        while (it.hasNext()) {
            it.next().onRuleChanged();
        }
    }

    public ArrayList<PenActionRule> getRule(ConditionMatchState conditionMatchState) {
        return getRule(conditionMatchState, false);
    }

    public ArrayList<PenActionRule> getRule(ConditionMatchState conditionMatchState, boolean z) {
        ensureInitialized();
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            PenActionRule next = it.next();
            if (next.isMatched(this.mContext, conditionMatchState, z)) {
                arrayList.add(next.m24clone());
            }
        }
        if (this.mCameraControlRule.isMatched(this.mContext, conditionMatchState, z)) {
            arrayList.add(this.mCameraControlRule.m24clone());
        }
        if (this.mMediaControlRule.isMatched(this.mContext, conditionMatchState, z)) {
            arrayList.add(this.mMediaControlRule.m24clone());
        }
        if (this.mBaseRule.isMatched(this.mContext, conditionMatchState, z)) {
            arrayList.add(this.mBaseRule.m24clone());
        }
        return arrayList;
    }

    public PenActionRule getRule(int i) {
        ensureInitialized();
        Object[] objArr = {this.mAppDefinedRuleSet, this.mCameraControlRule, this.mMediaControlRule, this.mBaseRule};
        for (int i2 = 0; i2 < 4; i2++) {
            Object obj = objArr[i2];
            if (obj instanceof ArrayList) {
                Iterator it = ((ArrayList) obj).iterator();
                while (it.hasNext()) {
                    PenActionRule penActionRule = (PenActionRule) it.next();
                    if (penActionRule.getId() == i) {
                        return penActionRule.m24clone();
                    }
                }
                continue;
            } else if (obj instanceof PenActionRule) {
                PenActionRule penActionRule2 = (PenActionRule) obj;
                if (penActionRule2.getId() == i) {
                    return penActionRule2.m24clone();
                }
            } else {
                Assert.fail("Unexpected obj : " + obj);
            }
        }
        return null;
    }

    public PenActionRule getCameraControlRule() {
        return this.mCameraControlRule.m24clone();
    }

    public PenActionRule getMediaControlRule() {
        return this.mMediaControlRule.m24clone();
    }

    public PenActionRule getBaseRule() {
        return this.mBaseRule.m24clone();
    }

    public PenActionRule getAppDefinedRule(String str, String str2) {
        PenActionRule rawAppDefinedRule = getRawAppDefinedRule(str, str2);
        if (rawAppDefinedRule != null) {
            return rawAppDefinedRule.m24clone();
        }
        return null;
    }

    public ArrayList<PenActionRule> getAppDefinedRuleSet() {
        ensureInitialized();
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().m24clone());
        }
        return arrayList;
    }

    public int getCameraControlRuleId() {
        return this.mCameraControlRule.getId();
    }

    public int getMediaControlRuleId() {
        return this.mMediaControlRule.getId();
    }

    public int getBaseRuleId() {
        return this.mBaseRule.getId();
    }

    public void setActionData(int i, PenActionTriggerType penActionTriggerType, PenActionData penActionData) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str = TAG;
            Log.e(str, "setActionData : Not valid rule item. id=" + i, new Exception());
            return;
        }
        PenActionDataSet actionSet = actionRuleById.getActionSet();
        ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
        if (actionItem != null) {
            actionItem.setPenActionData(penActionData);
        } else {
            actionItem = new ActionItem(penActionData);
        }
        actionSet.setActionItem(penActionTriggerType, actionItem);
        writeToStorage();
        invokeRuleChangedListener();
    }

    public void setActionData(String str, PenActionTriggerType penActionTriggerType, PenActionData penActionData) {
        ensureInitialized();
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            PenActionRule next = it.next();
            if (((ForegroundActivityCondition) next.getCondition()).getPackageName().equals(str)) {
                PenActionDataSet actionSet = next.getActionSet();
                ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
                if (actionItem != null) {
                    actionItem.setPenActionData(penActionData);
                } else {
                    actionItem = new ActionItem(penActionData);
                }
                actionSet.setActionItem(penActionTriggerType, actionItem);
            }
        }
        writeToStorage();
        invokeRuleChangedListener();
    }

    public void setGuideUiShownTime(int i, long j) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str = TAG;
            Log.e(str, "setGuideUiShownTime : Not valid rule item. id=" + i, new Exception());
            return;
        }
        actionRuleById.setGuideUiShownTime(j);
        writeToStorage();
    }

    public boolean isActionEnabled(int i, PenActionTriggerType penActionTriggerType) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str = TAG;
            Log.e(str, "isActionEnabled : Not valid rule item. id=" + i, new Exception());
            return false;
        }
        ActionItem actionItem = actionRuleById.getActionSet().getActionItem(penActionTriggerType);
        if (actionItem == null) {
            String str2 = TAG;
            Log.d(str2, "isActionEnabled : ActionItem is null. id=" + i);
            return true;
        }
        return actionItem.isEnabled();
    }

    public void setEnabled(int i, boolean z) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str = TAG;
            Log.e(str, "setEnabled : Not valid rule item. id=" + i, new Exception());
            return;
        }
        actionRuleById.setEnabled(z);
        writeToStorage();
        invokeRuleChangedListener();
    }

    public void setEnabled(String str, boolean z) {
        ensureInitialized();
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            PenActionRule next = it.next();
            if (((ForegroundActivityCondition) next.getCondition()).getPackageName().equals(str)) {
                next.setEnabled(z);
            }
        }
        writeToStorage();
        invokeRuleChangedListener();
    }

    public void setActionEnabled(int i, PenActionTriggerType penActionTriggerType, boolean z) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str = TAG;
            Log.e(str, "setActionEnabled : Not valid rule item. id=" + i, new Exception());
            return;
        }
        PenActionDataSet actionSet = actionRuleById.getActionSet();
        ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
        if (actionItem == null) {
            actionItem = new ActionItem(null, z);
            actionSet.setActionItem(penActionTriggerType, actionItem);
        }
        actionItem.setEnabled(z);
        writeToStorage();
        invokeRuleChangedListener();
    }

    public boolean addGuideUiShownPackageToList(int i, String str) {
        ensureInitialized();
        PenActionRule actionRuleById = getActionRuleById(i);
        if (actionRuleById == null) {
            String str2 = TAG;
            Log.e(str2, "addGuideUiShownPackageToList : Not valid rule item. id=" + i, new Exception());
            return false;
        } else if (actionRuleById.addGuideUiShownPackageToList(str)) {
            writeToStorage();
            return true;
        } else {
            return false;
        }
    }

    public boolean isInitialized() {
        return this.mContext != null;
    }

    public ConditionMatchState getCurrentMatchState() {
        ConditionMatchState conditionMatchState = new ConditionMatchState();
        ComponentName topMostActivity = ActivitySwitchDetector.getInstance(this.mContext).getTopMostActivity();
        if (topMostActivity != null) {
            conditionMatchState.mForegroundPackageName = topMostActivity.getPackageName();
            conditionMatchState.mForegorundActivityName = topMostActivity.getClassName();
        }
        conditionMatchState.mIsCameraPreviewOn = CameraAvailabilityMonitor.getInstance(this.mContext).isCameraPreviewOn();
        conditionMatchState.mHasAppDefinedRule = getAppDefinedRule(conditionMatchState.mForegroundPackageName, conditionMatchState.mForegorundActivityName) != null;
        return conditionMatchState;
    }

    public void registerRuleChangeListener(RuleChangeListener ruleChangeListener) {
        this.mRuleChangeListener.add(ruleChangeListener);
    }

    public void unregisterRuleChangeListener(RuleChangeListener ruleChangeListener) {
        this.mRuleChangeListener.remove(ruleChangeListener);
    }

    private PenActionRule getRawAppDefinedRule(String str, String str2) {
        ensureInitialized();
        ConditionMatchState conditionMatchState = new ConditionMatchState();
        conditionMatchState.mForegroundPackageName = str;
        conditionMatchState.mForegorundActivityName = str2;
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            PenActionRule next = it.next();
            PenActionCondition condition = next.getCondition();
            if (condition != null && condition.isMatched(this.mContext, conditionMatchState)) {
                return next;
            }
        }
        return null;
    }

    private void validateAppDefinedRules(Context context, RemoteActionManager remoteActionManager) {
        boolean z;
        if (!remoteActionManager.isCollected()) {
            Log.e(TAG, "validateAppDefinedRules : Not collected yet");
            return;
        }
        List<String> packageList = remoteActionManager.getPackageList();
        if (packageList == null) {
            Log.e(TAG, "validateAppDefinedRules : No custom activity actions");
            this.mAppDefinedRuleSet.clear();
            writeToStorage();
            return;
        }
        boolean z2 = false;
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        for (String str : packageList) {
            List<String> activityList = remoteActionManager.getActivityList(str);
            if (activityList == null) {
                Log.e(TAG, "validateAppDefinedRules : activityList is null");
            } else {
                for (String str2 : activityList) {
                    RemoteAction remoteAction = remoteActionManager.getRemoteAction(str, str2);
                    if (remoteAction == null) {
                        String str3 = TAG;
                        Log.e(str3, "validateAppDefinedRules : no remote action! pkg=" + str + " act=" + str2);
                    } else if (!SpenFunctionalityManager.getInstance(context).isAnyPenSupportAppGesture() && remoteAction.isOnlyConsistedWithMotionOnlyActions()) {
                        String str4 = TAG;
                        Log.d(str4, "validateAppDefinedRules : " + str + "has only motion_only actions");
                    } else {
                        PenActionRule rawAppDefinedRule = getRawAppDefinedRule(str, str2);
                        if (rawAppDefinedRule == null) {
                            boolean shouldEnableRuleByDefault = this.mDefaultRuleEnableStateManager.shouldEnableRuleByDefault(str);
                            if (!shouldEnableRuleByDefault) {
                                String enableKey = remoteAction.getEnableKey();
                                if (!TextUtils.isEmpty(enableKey)) {
                                    if (enableKey.equalsIgnoreCase(RuleEnableManager.getKey(str))) {
                                        String str5 = TAG;
                                        Log.d(str5, "validateAppDefinedRules : rule enabled by key - " + str);
                                        z = true;
                                        arrayList.add(createRule(PenActionRule.Type.APP_DEFINED, new ForegroundActivityCondition(str, str2), new PenActionDataSet(new PenActionDataSet.ActionMap()), z, remoteAction.isSwiftActionDeclared()));
                                        String str6 = TAG;
                                        Log.d(str6, "validateAppDefinedRules : Adds new activity : " + str2);
                                        z2 = true;
                                    } else {
                                        String str7 = TAG;
                                        Log.e(str7, "validateAppDefinedRules : incorrect enable key - " + str + ", " + enableKey);
                                    }
                                }
                            }
                            z = shouldEnableRuleByDefault;
                            arrayList.add(createRule(PenActionRule.Type.APP_DEFINED, new ForegroundActivityCondition(str, str2), new PenActionDataSet(new PenActionDataSet.ActionMap()), z, remoteAction.isSwiftActionDeclared()));
                            String str62 = TAG;
                            Log.d(str62, "validateAppDefinedRules : Adds new activity : " + str2);
                            z2 = true;
                        } else {
                            if (remoteAction.isSwiftActionDeclared() != rawAppDefinedRule.isSwiftActionDeclared()) {
                                rawAppDefinedRule.setSwiftActionDeclared(remoteAction.isSwiftActionDeclared());
                                z2 = true;
                            }
                            PenActionDataSet actionSet = rawAppDefinedRule.getActionSet();
                            for (PenActionTriggerType penActionTriggerType : actionSet.getPenActionTriggerTypeList()) {
                                ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
                                PenActionData penActionData = actionItem != null ? actionItem.getPenActionData() : null;
                                if (penActionData != null) {
                                    if (penActionData instanceof AppDefinedActionData) {
                                        String actionId = ((AppDefinedActionData) penActionData).getActionId();
                                        if (remoteAction.getAction(actionId) == null) {
                                            String str8 = TAG;
                                            Log.i(str8, "validateAppDefinedRules : not valid action ID! pkg=" + str + ", act=" + str2 + " actionId=" + actionId);
                                            actionSet.setActionItem(penActionTriggerType, null);
                                            z2 = true;
                                        }
                                    } else {
                                        String str9 = TAG;
                                        Log.i(str9, "validateAppDefinedRules : Skipping the action data : " + penActionData.getClass().getSimpleName());
                                    }
                                }
                            }
                            arrayList.add(rawAppDefinedRule);
                            this.mAppDefinedRuleSet.remove(rawAppDefinedRule);
                        }
                    }
                }
            }
        }
        if (this.mAppDefinedRuleSet.size() > 0) {
            String str10 = TAG;
            Log.d(str10, "validateAppDefinedRules : Some activity has removed. count = " + this.mAppDefinedRuleSet.size());
            z2 = true;
        }
        setAppDefinedRuleSet(arrayList);
        if (z2) {
            writeToStorage();
        }
    }

    private void validateBaseRule() {
        boolean isAnyPenSupportGlobalGesture = SpenFunctionalityManager.getInstance(this.mContext).isAnyPenSupportGlobalGesture();
        PenActionDataSet actionSet = this.mBaseRule.getActionSet();
        boolean z = false;
        for (PenActionTriggerType penActionTriggerType : actionSet.getPenActionTriggerTypeList()) {
            ActionItem actionItem = actionSet.getActionItem(penActionTriggerType);
            PenActionData penActionData = actionItem != null ? actionItem.getPenActionData() : null;
            if (penActionData != null && hasChanges(isAnyPenSupportGlobalGesture, actionSet, penActionTriggerType, penActionData)) {
                z = true;
            }
        }
        if (z) {
            writeToStorage();
        }
    }

    private boolean hasChanges(boolean z, PenActionDataSet penActionDataSet, PenActionTriggerType penActionTriggerType, PenActionData penActionData) {
        boolean z2;
        AppLaunchActionData appLaunchActionData;
        if (!penActionTriggerType.isGlobalGestureActionType() || z) {
            z2 = false;
        } else {
            Log.i(TAG, "hasChanges : not supports global gesture! trigger=" + penActionTriggerType);
            penActionDataSet.setActionItem(penActionTriggerType, null);
            z2 = true;
        }
        if (penActionData instanceof AppLaunchActionData) {
            if (!CommonUtils.isPackageInstalled(this.mContext, ((AppLaunchActionData) penActionData).getPackageName())) {
                Log.i(TAG, "hasChanges : not installed package : " + appLaunchActionData.getPackageName() + " for global gesture! trigger = " + penActionTriggerType);
                setDefaultActionSet(penActionDataSet, penActionTriggerType);
                setActionEnabled(this.mBaseRule.getId(), penActionTriggerType, false);
                return true;
            }
        }
        return z2;
    }

    private void setDefaultActionSet(PenActionDataSet penActionDataSet, PenActionTriggerType penActionTriggerType) {
        if (penActionTriggerType == PenActionTriggerType.LONG_CLICK) {
            penActionDataSet.setActionItem(penActionTriggerType, new ActionItem(getDefaultQuickLaunchActionData()));
        } else {
            penActionDataSet.setActionItem(penActionTriggerType, null);
        }
    }

    private PenActionRule getActionRuleById(int i) {
        Iterator<PenActionRule> it = this.mAppDefinedRuleSet.iterator();
        while (it.hasNext()) {
            PenActionRule next = it.next();
            if (next.getId() == i) {
                return next;
            }
        }
        PenActionRule penActionRule = this.mCameraControlRule;
        if (penActionRule != null && penActionRule.getId() == i) {
            return this.mCameraControlRule;
        }
        PenActionRule penActionRule2 = this.mMediaControlRule;
        if (penActionRule2 != null && penActionRule2.getId() == i) {
            return this.mMediaControlRule;
        }
        PenActionRule penActionRule3 = this.mBaseRule;
        if (penActionRule3 == null || penActionRule3.getId() != i) {
            return null;
        }
        return this.mBaseRule;
    }

    private void writeToStorage() {
        writeRuleSetToStorage(PREF_KEY_APP_DEFINED_RULE, this.mAppDefinedRuleSet);
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        arrayList.add(this.mCameraControlRule);
        writeRuleSetToStorage(PREF_KEY_CAMERA_CONTROL_RULE, arrayList);
        ArrayList<PenActionRule> arrayList2 = new ArrayList<>();
        arrayList2.add(this.mMediaControlRule);
        writeRuleSetToStorage(PREF_KEY_MEDIA_CONTROL_RULE, arrayList2);
        ArrayList<PenActionRule> arrayList3 = new ArrayList<>();
        arrayList3.add(this.mBaseRule);
        writeRuleSetToStorage(PREF_KEY_BASE_RULE, arrayList3);
        sendSpenActionStatusLog();
    }

    private void sendSpenActionStatusLog() {
        Log.v(TAG, "sendSpenActionStatusLog");
        PenActionRule penActionRule = this.mMediaControlRule;
        int i = SAUtils.AirCommandSpenRemote.STATUS_ID_ACTION_BASE;
        if (penActionRule != null) {
            SAUtils.insertSpenActionStatusLog(String.valueOf((int) SAUtils.AirCommandSpenRemote.STATUS_ID_ACTION_BASE), getDetailForStatusLog("media", this.mMediaControlRule));
            i = 9502;
        }
        if (this.mCameraControlRule != null) {
            SAUtils.insertSpenActionStatusLog(String.valueOf(i), getDetailForStatusLog("shutter", this.mCameraControlRule));
            i++;
        }
        if (this.mAppDefinedRuleSet != null) {
            HashMap hashMap = new HashMap();
            for (int i2 = 0; i2 < this.mAppDefinedRuleSet.size(); i2++) {
                PenActionRule penActionRule2 = this.mAppDefinedRuleSet.get(i2);
                if (penActionRule2 != null) {
                    String packageName = ((ForegroundActivityCondition) penActionRule2.getCondition()).getPackageName();
                    if (hashMap.containsKey(packageName)) {
                        SAUtils.insertSpenActionStatusLog(String.valueOf(hashMap.get(packageName)), getDetailForStatusLog(packageName, penActionRule2));
                    } else {
                        SAUtils.insertSpenActionStatusLog(String.valueOf(i), getDetailForStatusLog(packageName, penActionRule2));
                        hashMap.put(packageName, Integer.valueOf(i));
                        i++;
                        if (i > 9599) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private String getDetailForStatusLog(String str, PenActionRule penActionRule) {
        if (penActionRule != null) {
            String str2 = str + ViewHelper.QUALIFIER_DELIMITER;
            ActionItem actionItem = penActionRule.getActionSet().getActionItem(PenActionTriggerType.SINGLE_CLICK);
            PenActionData penActionData = actionItem != null ? actionItem.getPenActionData() : null;
            ActionItem actionItem2 = penActionRule.getActionSet().getActionItem(PenActionTriggerType.DOUBLE_CLICK);
            PenActionData penActionData2 = actionItem2 != null ? actionItem2.getPenActionData() : null;
            StringBuilder sb = new StringBuilder();
            sb.append(str2);
            String str3 = DoNothingActionData.ACTION_FOR_LOGGING;
            sb.append(penActionData == null ? DoNothingActionData.ACTION_FOR_LOGGING : penActionData.getActionForLogging(this.mContext));
            String str4 = sb.toString() + ViewHelper.QUALIFIER_DELIMITER;
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str4);
            if (penActionData2 != null) {
                str3 = penActionData2.getActionForLogging(this.mContext);
            }
            sb2.append(str3);
            String sb3 = sb2.toString();
            StringBuilder sb4 = new StringBuilder();
            sb4.append(sb3);
            sb4.append(penActionRule.getEnabled() ? "_on" : "_off");
            return sb4.toString();
        }
        return Constants.packageName.NONE;
    }

    private void writeRuleSetToStorage(String str, ArrayList<PenActionRule> arrayList) {
        JSONArray jSONArray = new JSONArray();
        Iterator<PenActionRule> it = arrayList.iterator();
        while (it.hasNext()) {
            jSONArray.put(it.next().encodeToJsonObject());
        }
        String jSONArray2 = jSONArray.toString();
        String str2 = TAG;
        Log.d(str2, "writeRuleSetToStorage : " + jSONArray2);
        SharedPreferences.Editor edit = this.mContext.getSharedPreferences(PREF_NAME, 0).edit();
        edit.putInt(PREF_KEY_RULE_VERSION, 4);
        edit.putString(str, jSONArray2);
        edit.apply();
    }

    private void readFromStorage() {
        setAppDefinedRuleSet(readRuleSetFromStorage(PREF_KEY_APP_DEFINED_RULE));
        ArrayList<PenActionRule> readRuleSetFromStorage = readRuleSetFromStorage(PREF_KEY_CAMERA_CONTROL_RULE);
        if (readRuleSetFromStorage.size() > 0) {
            this.mCameraControlRule = readRuleSetFromStorage.get(0);
        } else {
            setCameraControlRule(createRule(PenActionRule.Type.CAMERA_CONTROL, new CameraControlCondition(), new PenActionDataSet(), true, true));
        }
        ArrayList<PenActionRule> readRuleSetFromStorage2 = readRuleSetFromStorage(PREF_KEY_MEDIA_CONTROL_RULE);
        if (readRuleSetFromStorage2.size() > 0) {
            this.mMediaControlRule = readRuleSetFromStorage2.get(0);
        } else {
            setMediaControlRule(createRule(PenActionRule.Type.MEDIA_CONTROL, new MediaSessionCondition(), new PenActionDataSet(), true, false));
        }
        ArrayList<PenActionRule> readRuleSetFromStorage3 = readRuleSetFromStorage(PREF_KEY_BASE_RULE);
        if (readRuleSetFromStorage3.size() > 0) {
            this.mBaseRule = readRuleSetFromStorage3.get(0);
            int i = this.mContext.getSharedPreferences(PREF_NAME, 0).getInt(PREF_KEY_RULE_VERSION, -1);
            String str = TAG;
            Log.d(str, "readFromStorage : rule_version = " + i);
            if (i == 3) {
                boolean globalActionEnabled = SettingsPreferenceManager.getInstance(this.mContext).getGlobalActionEnabled();
                Log.d(str, "readFromStorage : globalActionEnabed = " + globalActionEnabled);
                setActionEnabled(this.mBaseRule.getId(), PenActionTriggerType.LONG_CLICK, globalActionEnabled);
                return;
            }
            return;
        }
        FixedMatchCondition fixedMatchCondition = new FixedMatchCondition(true);
        PenActionDataSet penActionDataSet = new PenActionDataSet();
        penActionDataSet.setActionItem(PenActionTriggerType.LONG_CLICK, new ActionItem(getDefaultQuickLaunchActionData()));
        setBaseRule(createRule(PenActionRule.Type.BASE, fixedMatchCondition, penActionDataSet, true, false));
    }

    private ArrayList<PenActionRule> readRuleSetFromStorage(String str) {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(PREF_NAME, 0);
        String string = sharedPreferences.getString(str, null);
        if (string == null) {
            string = Constants.packageName.NONE;
        }
        String str2 = TAG;
        Log.d(str2, "readRuleSetFromStorage : key=" + str + ", data = " + string);
        int i = sharedPreferences.getInt(PREF_KEY_RULE_VERSION, -1);
        if (i != -1 && i < 4) {
            Log.i(str2, "readRuleSetFromStorage : Rule is old version. storage=" + i + ", cur=4, prefKey=" + str);
        }
        if (i == 0 || i == 1) {
            return readRuleSetFromStorageFromLegacyData(string);
        }
        if (i == 2) {
            return readRuleSetFromStorageFromJsonObject(Constants.packageName.NONE);
        }
        return readRuleSetFromStorageFromJsonObject(string);
    }

    private ArrayList<PenActionRule> readRuleSetFromStorageFromJsonObject(String str) {
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        try {
            JSONArray jSONArray = TextUtils.isEmpty(str) ? new JSONArray() : new JSONArray(str);
            int length = jSONArray.length();
            for (int i = 0; i < length; i++) {
                arrayList.add(createRule(jSONArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            String str2 = TAG;
            Log.e(str2, "readRuleSetFromStorageFromJsonObject : e=" + e, e);
        }
        String str3 = TAG;
        Log.v(str3, "readRuleSetFromStorageFromJsonObject : created rule item = " + arrayList.size() + " inputDataLen=" + str.length());
        return arrayList;
    }

    private ArrayList<PenActionRule> readRuleSetFromStorageFromLegacyData(String str) {
        ArrayList<PenActionRule> arrayList = new ArrayList<>();
        int i = 0;
        String[] split = TextUtils.isEmpty(str) ? new String[0] : str.split(END_MARK_RULE_ITEM);
        PenActionConditionSerializer penActionConditionSerializer = new PenActionConditionSerializer();
        PenActionDataSerializer penActionDataSerializer = new PenActionDataSerializer();
        int i2 = 3;
        PenActionTriggerType[] penActionTriggerTypeArr = {PenActionTriggerType.SINGLE_CLICK, PenActionTriggerType.DOUBLE_CLICK, PenActionTriggerType.LONG_CLICK};
        int i3 = 0;
        while (i3 < split.length) {
            String str2 = split[i3];
            int indexOf = str2.indexOf(END_MARK_CONDITION);
            int indexOf2 = str2.indexOf(END_MARK_ACTION_SET);
            String substring = str2.substring(i, indexOf);
            String substring2 = str2.substring(indexOf + 1, indexOf2);
            String substring3 = str2.substring(indexOf2 + 1);
            String[] split2 = substring2.split(END_MARK_ACTION);
            PenActionDataSet.ActionMap actionMap = new PenActionDataSet.ActionMap();
            int min = Math.min(split2.length, i2);
            for (int i4 = i; i4 < min; i4++) {
                String str3 = split2[i4];
                if (!TextUtils.isEmpty(str3)) {
                    actionMap.put(penActionTriggerTypeArr[i4], new ActionItem(penActionDataSerializer.createFromLegacyData(str3, SEPARATOR_LEAF_FIELD)));
                }
            }
            arrayList.add(createRule(substring3, SEPARATOR_LEAF_FIELD, penActionConditionSerializer.createFromLegacyData(substring, SEPARATOR_LEAF_FIELD), new PenActionDataSet(actionMap)));
            i3++;
            i = 0;
            i2 = 3;
        }
        Log.d(TAG, "readRuleSetFromStorageFromLegacyData : created rule item = " + arrayList.size());
        return arrayList;
    }

    private void ensureInitialized() {
        if (isInitialized()) {
            return;
        }
        Assert.fail("class is not initialized");
    }

    private int allocateRuleItemId() {
        int i = this.mLastRuleItemId + 1;
        this.mLastRuleItemId = i;
        return i;
    }

    private void setAppDefinedRuleSet(ArrayList<PenActionRule> arrayList) {
        Iterator<PenActionRule> it = arrayList.iterator();
        while (it.hasNext()) {
            this.mLastRuleItemId = Math.max(this.mLastRuleItemId, it.next().getId());
        }
        this.mAppDefinedRuleSet = arrayList;
    }

    private void setCameraControlRule(PenActionRule penActionRule) {
        this.mLastRuleItemId = Math.max(this.mLastRuleItemId, penActionRule.getId());
        this.mCameraControlRule = penActionRule;
    }

    private void setMediaControlRule(PenActionRule penActionRule) {
        this.mLastRuleItemId = Math.max(this.mLastRuleItemId, penActionRule.getId());
        this.mMediaControlRule = penActionRule;
    }

    private void setBaseRule(PenActionRule penActionRule) {
        this.mLastRuleItemId = Math.max(this.mLastRuleItemId, penActionRule.getId());
        this.mBaseRule = penActionRule;
    }

    private PenActionRule createRule(PenActionRule.Type type, PenActionCondition penActionCondition, PenActionDataSet penActionDataSet, boolean z, boolean z2) {
        return new PenActionRule(allocateRuleItemId(), type, penActionCondition, penActionDataSet, z, z2);
    }

    private PenActionRule createRule(String str, String str2, PenActionCondition penActionCondition, PenActionDataSet penActionDataSet) {
        PenActionRule penActionRule = new PenActionRule(allocateRuleItemId(), str, str2, penActionCondition, penActionDataSet);
        this.mLastRuleItemId = Math.max(this.mLastRuleItemId, penActionRule.getId());
        return penActionRule;
    }

    private PenActionRule createRule(JSONObject jSONObject) {
        PenActionRule penActionRule = new PenActionRule(allocateRuleItemId(), jSONObject);
        this.mLastRuleItemId = Math.max(this.mLastRuleItemId, penActionRule.getId());
        return penActionRule;
    }

    private AppLaunchActionData getDefaultQuickLaunchActionData() {
        return new AppLaunchActionData(AppLaunchActionData.LaunchType.ACTIVITY, com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_PACKAGE_NAME, com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_ACTIVITY_NAME, null);
    }
}
