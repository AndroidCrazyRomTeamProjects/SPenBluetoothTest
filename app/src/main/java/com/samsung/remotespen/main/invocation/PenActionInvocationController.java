package com.samsung.remotespen.main.invocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.manager.PenActionDataType;
import com.samsung.remotespen.core.penaction.manager.PenActionHelper;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.remotespen.core.penaction.manager.PenActionRuleManager;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerCategory;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.main.invocation.PenActionInvoker;
import com.samsung.remotespen.util.MediaControlStateMonitor;
import com.samsung.util.ActivitySwitchDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.ViewHelper;
import com.samsung.util.constants.SpenComponents;
import com.samsung.util.usage.SAUtils;
import com.samsung.android.widget.SemLockPatternUtils;
import java.util.ArrayList;
import java.util.Locale;

/* loaded from: classes.dex */
public class PenActionInvocationController {
    private static final String TAG = "PenActionInvocationController";
    private Context mContext;
    private PenActionInvocationState mCurPenActionInvocationState;
    private InvocationDispatchListener mInvocationDispatchListener;
    private String mTopActivityName;
    private String mTopPackageName;
    private String[] mBlockedFeatureListInDex = {SpenComponents.SMART_SELECT.getClassName(), SpenComponents.SCREEN_WRITE.getClassName(), SpenComponents.LIVEDRAWING.getClassName(), SpenComponents.MAGNIFY.getClassName(), SpenComponents.PENTASTIC.getClassName()};
    private PenActionInvoker mPenActionInvoker = new PenActionInvoker();
    private PenActionRuleManager mPenActionRuleManager = PenActionRuleManager.getInstance();

    /* loaded from: classes.dex */
    public interface InvocationDispatchListener {
        boolean onDispatchInvocation(PenActionData penActionData);
    }

    /* loaded from: classes.dex */
    public interface InvocationFinishListener {
        void onFinishInvocation(InvocationResult invocationResult);
    }

    public PenActionInvocationController(Context context, InvocationDispatchListener invocationDispatchListener) {
        this.mContext = context;
        this.mInvocationDispatchListener = invocationDispatchListener;
    }

    public void setTopmostActivity(ComponentName componentName) {
        this.mTopPackageName = componentName.getPackageName();
        this.mTopActivityName = componentName.getClassName();
    }

    public void invokePenAction(PenActionTrigger penActionTrigger, InvocationFinishListener invocationFinishListener) {
        startPenActionInvocation(penActionTrigger, invocationFinishListener);
        finishPenActionInvocation();
    }

    public void startPenActionInvocation(PenActionTrigger penActionTrigger, InvocationFinishListener invocationFinishListener) {
        if (penActionTrigger == null) {
            Log.e(TAG, "startPenActionInvocation : PenActionTrigger is null!");
            return;
        }
        PenActionTriggerType primaryTrigger = penActionTrigger.getPrimaryTrigger();
        resetScreenOffTimer();
        InvocationResult invokeAction = invokeAction(penActionTrigger);
        setCurPenActionInvocationState(primaryTrigger, invokeAction);
        if (invocationFinishListener != null) {
            invocationFinishListener.onFinishInvocation(invokeAction);
        }
    }

    public void finishPenActionInvocation() {
        invokePenActionFinishListener();
    }

    private InvocationResult invokeAction(PenActionTrigger penActionTrigger) {
        String str;
        PenActionRule penActionRule;
        PenActionData action;
        if (!CommonUtils.isSetupWizardCompleted(this.mContext)) {
            Log.d(TAG, "invokeAction : Setup wizard is not finished yet");
            return new InvocationResult().setConsumed(false);
        }
        if (this.mTopPackageName == null || this.mTopActivityName == null) {
            ComponentName topMostActivity = ActivitySwitchDetector.getInstance(this.mContext).getTopMostActivity();
            str = topMostActivity.getPackageName() + ViewHelper.QUALIFIER_DELIMITER + topMostActivity.getClassName();
        } else {
            str = this.mTopPackageName + ViewHelper.QUALIFIER_DELIMITER + this.mTopActivityName;
        }
        PenActionTriggerType primaryTrigger = penActionTrigger.getPrimaryTrigger();
        ArrayList<PenActionRule> rule = this.mPenActionRuleManager.getRule(this.mPenActionRuleManager.getCurrentMatchState());
        int size = rule.size();
        PenActionData penActionData = null;
        if (size == 0) {
            Log.d(TAG, "invokeAction : no rule present");
            penActionRule = null;
        } else {
            if (isGlobalPenActionDataTriggerType(primaryTrigger)) {
                for (int i = 0; i < size; i++) {
                    penActionRule = rule.get(i);
                    if (penActionRule != null && (action = penActionRule.getAction(this.mContext, primaryTrigger)) != null && PenActionHelper.getActioDataType(action) != PenActionDataType.DO_NOTHING && (penActionRule.getActionType() == PenActionRule.Type.BASE || primaryTrigger == PenActionTriggerType.LONG_CLICK)) {
                        if (!this.mPenActionRuleManager.isActionEnabled(penActionRule.getId(), primaryTrigger)) {
                            InvocationResult invocationResult = new InvocationResult();
                            invocationResult.setConsumed(false);
                            invocationResult.mActionRule = penActionRule;
                            return invocationResult;
                        }
                        if (action != null && penActionRule != null && penActionRule.getActionType() == PenActionRule.Type.BASE && !PenActionRuleManager.getInstance().isActionEnabled(penActionRule.getId(), primaryTrigger)) {
                            Log.d(TAG, "invokeAction : " + primaryTrigger + " global action is disabled");
                            action = null;
                        }
                    }
                }
                penActionRule = null;
                action = null;
                if (action != null) {
                    Log.d(TAG, "invokeAction : " + primaryTrigger + " global action is disabled");
                    action = null;
                }
            } else {
                penActionRule = rule.get(0);
                if (penActionRule == null) {
                    Log.d(TAG, "invokeAction : rule is null");
                } else {
                    action = penActionRule.getAction(this.mContext, primaryTrigger);
                }
            }
            if (penActionRule == null) {
                Log.d(TAG, "invokeAction : rule is null");
            } else {
                PenActionTriggerCategory category = primaryTrigger.getCategory();
                PenActionTriggerCategory penActionTriggerCategory = PenActionTriggerCategory.GESTURE;
                boolean z = category == penActionTriggerCategory && primaryTrigger != PenActionTriggerType.GESTURE_UNKNOWN;
                if (PenActionHelper.getActioDataType(action) == PenActionDataType.DO_NOTHING && (primaryTrigger == PenActionTriggerType.SINGLE_CLICK || primaryTrigger == PenActionTriggerType.DOUBLE_CLICK || z)) {
                    return handleDoNothingAction(primaryTrigger, action, penActionRule, str);
                }
                if (action == null) {
                    if (primaryTrigger.getCategory() == PenActionTriggerCategory.BUTTON) {
                        SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_BUTTON_ACTION_EMPTY, str, null);
                    } else if (primaryTrigger.getCategory() == penActionTriggerCategory) {
                        SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_GESTURE_ACTION_EMPTY, str, null);
                    }
                } else {
                    sendEventLogForInvokeAction(primaryTrigger, str + getActionForStatusLog(penActionRule, primaryTrigger), action);
                }
            }
            penActionData = action;
        }
        InvocationResult invocationResult2 = new InvocationResult();
        if ((penActionData instanceof AppLaunchActionData) && isAppLaunchingBlocked()) {
            Log.i(TAG, "invokeAction : app launching is blocked");
            return invocationResult2.setConsumed(true);
        }
        invocationResult2.setConsumed(false);
        invocationResult2.mActionRule = penActionRule;
        if (penActionData != null) {
            InvocationResult handlePenActionTrigger = handlePenActionTrigger(penActionData, penActionRule);
            return handlePenActionTrigger != null ? handlePenActionTrigger : invocationResult2;
        }
        Log.d(TAG, "invokeAction : no action present");
        return invocationResult2;
    }

    private boolean isAppLaunchingBlocked() {
        SemLockPatternUtils semLockPatternUtils = new SemLockPatternUtils(this.mContext);
        int focusedUserId = CommonUtils.getFocusedUserId(this.mContext);
        boolean isFmmLockEnabled = semLockPatternUtils.isFmmLockEnabled(focusedUserId);
        boolean isRemoteMobileManagerLockEnabled = semLockPatternUtils.isRemoteMobileManagerLockEnabled(focusedUserId);
        boolean isCarrierLockEnabled = semLockPatternUtils.isCarrierLockEnabled(focusedUserId);
        boolean isAppSwitchingBlocked = CommonUtils.isAppSwitchingBlocked(this.mContext);
        boolean z = !CommonUtils.isSetupWizardCompleted(this.mContext);
        boolean isPinningMode = CommonUtils.isPinningMode(this.mContext);
        if (isFmmLockEnabled || isRemoteMobileManagerLockEnabled || isCarrierLockEnabled || isAppSwitchingBlocked || z || isPinningMode) {
            Log.i(TAG, "isAppLaunchingBlocked : fmm=" + isFmmLockEnabled + ", rmm=" + isRemoteMobileManagerLockEnabled + ", carrier=" + isCarrierLockEnabled + ", recentKeyLocked=" + isAppSwitchingBlocked + ", setupWizard=" + z + ", pinningMode=" + isPinningMode);
            return true;
        }
        return false;
    }

    private boolean isGlobalPenActionDataTriggerType(PenActionTriggerType penActionTriggerType) {
        return penActionTriggerType.isGlobalActionType();
    }

    private void invokePenActionFinishListener() {
        PenActionInvocationState penActionInvocationState = this.mCurPenActionInvocationState;
        if (penActionInvocationState != null && penActionInvocationState.mPenActionFinishListener != null) {
            Log.v(TAG, "invokePenActionFinishListener : invoking the finish listener");
            this.mCurPenActionInvocationState.mPenActionFinishListener.onFinishPenActionTrigger();
            this.mCurPenActionInvocationState = null;
            return;
        }
        Log.d(TAG, "invokePenActionFinishListener : nothing to invoke");
    }

    private void setCurPenActionInvocationState(PenActionTriggerType penActionTriggerType, InvocationResult invocationResult) {
        if (invocationResult != null && invocationResult.isConsumed()) {
            PenActionFinishListener penActionFinishListener = invocationResult.mFinishListener;
            if (penActionFinishListener != null) {
                Log.d(TAG, "setCurPenActionInvocationState : finish listener exists");
                this.mCurPenActionInvocationState = new PenActionInvocationState(penActionFinishListener);
                return;
            }
            return;
        }
        Log.d(TAG, "setCurPenActionInvocationState : invoked action does not consumed");
    }

    private InvocationResult handleDoNothingAction(PenActionTriggerType penActionTriggerType, PenActionData penActionData, PenActionRule penActionRule, String str) {
        if (penActionRule.isSwiftActionDeclared() && penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
            return new InvocationResult(true, penActionData);
        }
        InvocationResult invocationResult = new InvocationResult(true, penActionData);
        invocationResult.mActionRule = penActionRule;
        return invocationResult;
    }

    private InvocationResult handlePenActionTrigger(final PenActionData penActionData, PenActionRule penActionRule) {
        InvocationDispatchListener invocationDispatchListener = this.mInvocationDispatchListener;
        boolean onDispatchInvocation = invocationDispatchListener != null ? invocationDispatchListener.onDispatchInvocation(penActionData) : false;
        if (isNeedToShowGuidePanel(penActionRule)) {
            Log.d(TAG, "handlePenActionTrigger : Need to show Guide UI");
            return new InvocationResult(true, penActionData);
        }
        int focusedDisplayId = CommonUtils.getFocusedDisplayId();
        if (focusedDisplayId != -1 && focusedDisplayId == CommonUtils.getDesktopDisplayId(this.mContext) && isBlockedActionInDex(penActionData)) {
            Context context = this.mContext;
            Toast.makeText(CommonUtils.getDesktopDisplayContext(this.mContext), context.getString(R.string.toast_isnt_available_in_samsung_dex, penActionData.getLabel(context)), 0).show();
            Log.d(TAG, "handlePenActionTrigger : can't invoke. Focused display isn't device.");
            return new InvocationResult(true, penActionData);
        }
        boolean invokeAction = !onDispatchInvocation ? this.mPenActionInvoker.invokeAction(this.mContext, penActionData, PenActionInvoker.InvocationType.START) : true;
        if (invokeAction) {
            InvocationResult invocationResult = new InvocationResult(true, penActionData);
            invocationResult.mActionRule = penActionRule;
            invocationResult.mFinishListener = new PenActionFinishListener() { // from class: com.samsung.remotespen.main.invocation.PenActionInvocationController.1
                @Override // com.samsung.remotespen.main.invocation.PenActionFinishListener
                public void onFinishPenActionTrigger() {
                    PenActionInvocationController.this.mPenActionInvoker.invokeAction(PenActionInvocationController.this.mContext, penActionData, PenActionInvoker.InvocationType.FINISH);
                }
            };
            return invocationResult;
        }
        String str = TAG;
        Log.d(str, "handlePenActionTrigger : isSuccess = " + invokeAction);
        return null;
    }

    private boolean isNeedToShowGuidePanel(PenActionRule penActionRule) {
        String str = this.mPenActionRuleManager.getCurrentMatchState().mForegroundPackageName;
        if (QualifiedCameraAppListManager.getInstance(this.mContext).isUnqualifiedCameraAppCase(penActionRule, str)) {
            Log.d(TAG, "isNeedToShowGuidePanel : Not qualified camera app.");
            return false;
        }
        if (penActionRule.getActionType() == PenActionRule.Type.MEDIA_CONTROL) {
            str = MediaControlStateMonitor.getInstance(this.mContext).getActiveNotificationPackageName();
        }
        if (penActionRule.getId() == this.mPenActionRuleManager.getBaseRuleId() || penActionRule.isGuideUiShownPackage(str)) {
            return false;
        }
        Log.d(TAG, "isNeedToShowGuidePanel : Need to show Guide UI");
        return true;
    }

    private void resetScreenOffTimer() {
        try {
            ReflectionUtils.invokeMethod((PowerManager) this.mContext.getSystemService("power"), "userActivity", Long.valueOf(SystemClock.uptimeMillis()), ReflectionUtils.getStaticObjectField(PowerManager.class, "USER_ACTIVITY_EVENT_TOUCH"), 0);
        } catch (RuntimeException unused) {
            Log.e(TAG, "RuntimeException is occurred");
        } catch (Exception unused2) {
            Log.e(TAG, "userActivity invoke fail");
        }
    }

    private String getActionForStatusLog(PenActionRule penActionRule, PenActionTriggerType penActionTriggerType) {
        if (penActionRule != null) {
            PenActionData action = penActionRule.getAction(this.mContext, penActionTriggerType);
            StringBuilder sb = new StringBuilder();
            sb.append(ViewHelper.QUALIFIER_DELIMITER);
            sb.append(action == null ? DoNothingActionData.ACTION_FOR_LOGGING : action.getActionForLogging(this.mContext));
            return sb.toString();
        }
        return Constants.packageName.NONE;
    }

    private void sendEventLogForInvokeAction(PenActionTriggerType penActionTriggerType, String str, PenActionData penActionData) {
        String str2;
        if (penActionTriggerType == PenActionTriggerType.LONG_CLICK) {
            if (penActionData instanceof AppLaunchActionData) {
                String packageName = ((AppLaunchActionData) penActionData).getPackageName();
                Log.v(TAG, "sendEventLogForInvokeAction : target app = " + packageName);
                SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_AIR_ACTIONS_LONG_PRESS, packageName, null);
            } else {
                Log.v(TAG, "sendEventLogForInvokeAction : not app launch action data - " + penActionData);
            }
        }
        if (!penActionTriggerType.isGlobalActionType()) {
            SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_APP_ACTION, penActionTriggerType.getTriggerNameForSamsungAnalytics() + ViewHelper.QUALIFIER_DELIMITER + str, null);
        } else if (penActionData != null) {
            if (penActionData instanceof AppLaunchActionData) {
                AppLaunchActionData appLaunchActionData = (AppLaunchActionData) penActionData;
                str2 = penActionTriggerType.getTriggerNameForSamsungAnalytics() + ViewHelper.QUALIFIER_DELIMITER + appLaunchActionData.getPackageName() + ViewHelper.QUALIFIER_DELIMITER + appLaunchActionData.getComponentName();
            } else {
                Configuration configuration = this.mContext.getResources().getConfiguration();
                configuration.setLocale(Locale.ENGLISH);
                Context createConfigurationContext = this.mContext.createConfigurationContext(configuration);
                str2 = penActionTriggerType.getTriggerNameForSamsungAnalytics() + ViewHelper.QUALIFIER_DELIMITER + penActionData.getActionForLogging(createConfigurationContext);
            }
            SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_ANYWHERE_ACTIONS, str2, null);
        }
    }

    private boolean isBlockedActionInDex(PenActionData penActionData) {
        if (penActionData instanceof AppLaunchActionData) {
            String componentName = ((AppLaunchActionData) penActionData).getComponentName();
            for (String str : this.mBlockedFeatureListInDex) {
                if (str.equals(componentName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
}
