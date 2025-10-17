package com.samsung.remotespen.main.invocation;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.launcher.Launcher;
import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.condition.ConditionMatchState;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.remotespen.core.penaction.manager.PenActionRuleManager;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.main.unlock.RemoteSpenTrustAgentService;
import com.samsung.remotespen.util.CameraAvailabilityMonitor;
import com.samsung.remotespen.util.KeyguardStateChangeDetector;
import com.samsung.remotespen.util.MediaControlStateMonitor;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.sep.SepWrapper;
import com.samsung.util.shortcut.AppShortcut;
import com.samsung.util.usage.SAUtils;
import com.samsung.android.widget.SemLockPatternUtils;

/* loaded from: classes.dex */
public class ScreenOffAndLockStateActionInvoker {
    private static final String TAG = "ScreenOffAndLockStateActionInvoker";
    public BleSpenManager mBleSpenManager;
    private CameraAvailabilityMonitor mCameraAvailabilityMonitor;
    private Context mContext;
    private InvocationDispatchListener mInvocationDispatchListener;
    private boolean mIsKeyguardLocked;
    private boolean mIsSatisfySpenUnlockCondition;
    private KeyguardManager mKeyguardManager;
    private KeyguardStateChangeDetector mKeyguardStateChangeDetector;
    private PowerManager mPowerManager;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    private PenActionInvoker mPenActionInvoker = new PenActionInvoker();
    private SpenInsertionEventDetector.Listener mSpenInsertionEventListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.1
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            if (z) {
                ScreenOffAndLockStateActionInvoker.this.setEnableSpenUnlockCondition(false);
            }
        }
    };
    public BleSpenManager.StateListener mSpenStateListener = new BleSpenManager.StateListener() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.2
        @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
        public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
        }

        @Override // com.samsung.remotespen.core.device.BleSpenManager.StateListener
        public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenManager.ConnectionState connectionState, BleSpenManager.ConnectionState connectionState2, BleSpenManager.StateChangeInfo stateChangeInfo) {
            if (ScreenOffAndLockStateActionInvoker.this.mBleSpenManager.isAnySpenConnected()) {
                return;
            }
            ScreenOffAndLockStateActionInvoker.this.setEnableSpenUnlockCondition(false);
        }
    };

    /* loaded from: classes.dex */
    public interface InvocationDispatchListener {
        boolean onDispatchInvocation(PenActionData penActionData);
    }

    public ScreenOffAndLockStateActionInvoker(Context context, InvocationDispatchListener invocationDispatchListener) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mBleSpenManager = BleSpenManager.getInstance(applicationContext);
        this.mInvocationDispatchListener = invocationDispatchListener;
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mKeyguardManager = keyguardManager;
        this.mIsKeyguardLocked = keyguardManager.isKeyguardLocked();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mCameraAvailabilityMonitor = CameraAvailabilityMonitor.getInstance(this.mContext);
        SpenInsertionEventDetector spenInsertionEventDetector = SpenInsertionEventDetector.getInstance(this.mContext);
        this.mSpenInsertionEventDetector = spenInsertionEventDetector;
        spenInsertionEventDetector.registerListener(this.mSpenInsertionEventListener);
        KeyguardStateChangeDetector keyguardStateChangeDetector = new KeyguardStateChangeDetector(this.mContext);
        this.mKeyguardStateChangeDetector = keyguardStateChangeDetector;
        keyguardStateChangeDetector.registerReceiver(new KeyguardStateChangeDetector.Listener() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.3
            @Override // com.samsung.remotespen.util.KeyguardStateChangeDetector.Listener
            public void onKeyguardStateUpdated(boolean z) {
                ScreenOffAndLockStateActionInvoker.this.setKeyguardLockState(z);
            }
        });
        this.mBleSpenManager.registerListener(this.mSpenStateListener, null);
    }

    public void release() {
        this.mSpenInsertionEventDetector.unregisterListener(this.mSpenInsertionEventListener);
        this.mKeyguardStateChangeDetector.unregisterReceiver();
        this.mBleSpenManager.unregisterListener(this.mSpenStateListener, null);
    }

    public InvocationResult invokeAction(PenActionTriggerType penActionTriggerType, boolean z) {
        if (CommonUtils.isScoverClosed(this.mContext)) {
            Log.d(TAG, "invokeAction : Scover is closed");
            InvocationResult consumed = new InvocationResult().setConsumed(true);
            if (!isMediaControlTriggerType(penActionTriggerType) || MediaControlStateMonitor.getInstance(this.mContext).getActiveNotificationPackageName() == null) {
                return consumed;
            }
            InvocationResult invokeMediaControlAction = invokeMediaControlAction(penActionTriggerType);
            invokeMediaControlAction.setConsumed(true);
            return invokeMediaControlAction;
        } else if (this.mPowerManager.isInteractive() && !this.mKeyguardManager.isKeyguardLocked()) {
            Log.d(TAG, "invokeAction : screen on and unlock state.");
            return new InvocationResult().setConsumed(false);
        } else if (penActionTriggerType == PenActionTriggerType.GESTURE_UNKNOWN) {
            Log.d(TAG, "invokeAction : unknown gesture case");
            return new InvocationResult().setConsumed(true);
        } else {
            String str = TAG;
            Log.d(str, "invokeAction : trigger = " + penActionTriggerType);
            if (penActionTriggerType == PenActionTriggerType.LONG_CLICK || penActionTriggerType.isGlobalGestureActionType()) {
                return handleGlobalAction(penActionTriggerType, z);
            }
            return handleSingleDoubleClick(penActionTriggerType, z);
        }
    }

    private boolean canUnlockScreen(boolean z, boolean z2) {
        boolean isKeyguardLocked = this.mKeyguardManager.isKeyguardLocked();
        if (z || isKeyguardLocked) {
            boolean isKeyguardSecure = this.mKeyguardManager.isKeyguardSecure();
            String str = TAG;
            Log.d(str, "canUnlockScreen: isKeyGuardSecure " + isKeyguardSecure + " isSpenUnlockSettingEnabled " + isSpenUnlockSettingEnabled() + " isBundledSpen " + z2);
            if (this.mKeyguardManager.isKeyguardSecure()) {
                if (isSpenUnlockSettingEnabled() && z2) {
                    return true;
                }
            } else if (this.mIsSatisfySpenUnlockCondition) {
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean canInvokeMediaAction() {
        return PenActionRuleManager.getInstance().getMediaControlRule().getEnabled() && MediaControlStateMonitor.getInstance(this.mContext).getActiveNotificationPackageName() != null;
    }

    private boolean canInvokeAction(boolean z) {
        boolean semIsKeyguardShowingAndNotOccluded = SepWrapper.KeyguardManager.semIsKeyguardShowingAndNotOccluded(this.mKeyguardManager);
        boolean isKeyguardLocked = this.mKeyguardManager.isKeyguardLocked();
        boolean isCameraPreviewOn = this.mCameraAvailabilityMonitor.isCameraPreviewOn();
        String str = TAG;
        Log.d(str, "canInvokeAction: isInteractive : " + z + ", isKeyguardShowAndNotOccluded : " + semIsKeyguardShowingAndNotOccluded + ", isKeyGuardLocked : " + isKeyguardLocked + ", isCameraPreviewOn : " + isCameraPreviewOn);
        if (!z || semIsKeyguardShowingAndNotOccluded) {
            return true;
        }
        return (!isKeyguardLocked || isCameraPreviewOn || isTopActivityHasAppDefinedRemoteAction()) ? false : true;
    }

    private InvocationResult handleSingleDoubleClick(PenActionTriggerType penActionTriggerType, boolean z) {
        InvocationResult consumedAtKeyguard = new InvocationResult().setConsumedAtKeyguard();
        boolean isInteractive = this.mPowerManager.isInteractive();
        if (!canInvokeAction(isInteractive)) {
            Log.d(TAG, "handleSingleDoubleClick : skip");
            return new InvocationResult().setConsumed(false);
        } else if (canUnlockScreen(isInteractive, z)) {
            Log.d(TAG, "handleSingleDoubleClick : unlockLockScreen");
            unlockLockScreen(this.mIsSatisfySpenUnlockCondition, penActionTriggerType);
            return consumedAtKeyguard;
        } else if (canInvokeMediaAction()) {
            Log.d(TAG, "handleSingleDoubleClick : invokeMediaAction");
            return invokeMediaControlAction(penActionTriggerType).setConsumed(true);
        } else if (isInteractive) {
            Log.d(TAG, "handleSingleDoubleClick : Interactive state, skip");
            return new InvocationResult().setConsumed(false);
        } else {
            Log.d(TAG, "handleSingleDoubleClick : screenOn");
            screenOn();
            return new InvocationResult().setConsumedAtScreenOffState();
        }
    }

    private InvocationResult handleGlobalAction(final PenActionTriggerType penActionTriggerType, boolean z) {
        boolean isInteractive = this.mPowerManager.isInteractive();
        boolean isCameraPreviewOn = this.mCameraAvailabilityMonitor.isCameraPreviewOn();
        boolean isCameraForGlobalAction = isCameraForGlobalAction(penActionTriggerType);
        if (isCameraForGlobalAction && isInteractive && isCameraPreviewOn) {
            Log.d(TAG, "handleGlobalAction : Camera preview is running");
            return new InvocationResult().setConsumed(false);
        }
        PenActionRuleManager penActionRuleManager = PenActionRuleManager.getInstance();
        if (!penActionRuleManager.isActionEnabled(penActionRuleManager.getBaseRuleId(), penActionTriggerType)) {
            String str = TAG;
            Log.d(str, "handleGlobalAction : Global action is disabled. triggerType = " + penActionTriggerType);
            return new InvocationResult().setConsumed(true);
        }
        PenActionRule baseRule = PenActionRuleManager.getInstance().getBaseRule();
        PenActionData action = baseRule != null ? baseRule.getAction(this.mContext, penActionTriggerType) : null;
        if (action == null) {
            Log.d(TAG, "handleGlobalAction : No global action defined");
            return new InvocationResult().setConsumed(true);
        } else if ((!isInteractive || this.mKeyguardManager.isKeyguardLocked()) && !(action instanceof AppLaunchActionData)) {
            String str2 = TAG;
            Log.d(str2, "handleGlobalAction : not app launch type action data." + action.getClass().getSimpleName());
            return new InvocationResult().setConsumed(true);
        } else if (isAppLaunchingBlocked()) {
            Log.e(TAG, "handleGlobalAction : app launching is blocked");
            return new InvocationResult().setConsumed(true);
        } else if (!this.mKeyguardManager.isKeyguardLocked()) {
            if (!isInteractive) {
                screenOn();
                new Handler().postDelayed(new Runnable() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.5
                    @Override // java.lang.Runnable
                    public void run() {
                        Log.d(ScreenOffAndLockStateActionInvoker.TAG, "handleGlobalAction : Launches global action on screen off state.");
                        ScreenOffAndLockStateActionInvoker.this.invokeGlobalAction(penActionTriggerType);
                    }
                }, 1000L);
                return new InvocationResult().setConsumedAtScreenOffState();
            }
            return new InvocationResult().setConsumed(false);
        } else {
            InvocationResult consumedAtKeyguard = new InvocationResult().setConsumedAtKeyguard();
            if (canUnlockScreen(isInteractive, z)) {
                unlockKeyguardAndInvokeGlobalAction(penActionTriggerType);
                return consumedAtKeyguard;
            } else if (!this.mKeyguardManager.isKeyguardSecure()) {
                unlockKeyguardAndInvokeGlobalAction(penActionTriggerType);
                return consumedAtKeyguard;
            } else {
                if (isCameraForGlobalAction) {
                    screenOn();
                    new Handler().postDelayed(new Runnable() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.4
                        @Override // java.lang.Runnable
                        public void run() {
                            Log.d(ScreenOffAndLockStateActionInvoker.TAG, "handleGlobalAction : SECURE CAMERA");
                            ScreenOffAndLockStateActionInvoker.this.invokeSecureCamera();
                        }
                    }, isInteractive ? 0 : 1000);
                } else {
                    setPendingIntentAfterUnlock(penActionTriggerType);
                }
                return consumedAtKeyguard;
            }
        }
    }

    private boolean isMediaControlTriggerType(PenActionTriggerType penActionTriggerType) {
        return penActionTriggerType == PenActionTriggerType.SINGLE_CLICK || penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK || penActionTriggerType == PenActionTriggerType.GESTURE_LEFT || penActionTriggerType == PenActionTriggerType.GESTURE_RIGHT || penActionTriggerType == PenActionTriggerType.GESTURE_UP || penActionTriggerType == PenActionTriggerType.GESTURE_DOWN;
    }

    private boolean isAppLaunchingBlocked() {
        SemLockPatternUtils semLockPatternUtils = new SemLockPatternUtils(this.mContext);
        int focusedUserId = CommonUtils.getFocusedUserId(this.mContext);
        boolean isFmmLockEnabled = semLockPatternUtils.isFmmLockEnabled(focusedUserId);
        boolean isRemoteMobileManagerLockEnabled = semLockPatternUtils.isRemoteMobileManagerLockEnabled(focusedUserId);
        boolean isCarrierLockEnabled = semLockPatternUtils.isCarrierLockEnabled(focusedUserId);
        boolean isAppSwitchingBlocked = CommonUtils.isAppSwitchingBlocked(this.mContext);
        boolean z = !CommonUtils.isSetupWizardCompleted(this.mContext);
        if (isFmmLockEnabled || isRemoteMobileManagerLockEnabled || isCarrierLockEnabled || isAppSwitchingBlocked || z) {
            Log.i(TAG, "isAppLaunchingBlocked : fmm=" + isFmmLockEnabled + ", rmm=" + isRemoteMobileManagerLockEnabled + ", carrier=" + isCarrierLockEnabled + ", recentKeyLocked=" + isAppSwitchingBlocked + ", setupWizard=" + z);
            return true;
        }
        return false;
    }

    private boolean isTopActivityHasAppDefinedRemoteAction() {
        PenActionRuleManager penActionRuleManager = PenActionRuleManager.getInstance();
        ConditionMatchState currentMatchState = penActionRuleManager.getCurrentMatchState();
        if (penActionRuleManager.getAppDefinedRule(currentMatchState.mForegroundPackageName, currentMatchState.mForegorundActivityName) != null) {
            Log.v(TAG, "isTopActivityHasAppDefinedRemoteAction : true");
            return true;
        }
        return false;
    }

    public void setKeyguardLockState(boolean z) {
        String str = TAG;
        Log.d(str, "setKeyguardLockState : isKeyguardLocked is " + z);
        this.mIsKeyguardLocked = z;
        if (z) {
            BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
            BleSpenManager.ConnectionState connectionState = BleSpenManager.ConnectionState.DISCONNECTED;
            if (bleSpenManager != null) {
                connectionState = bleSpenManager.getConnectionState(BleSpenPairedSpenManager.getInstance(this.mContext).getBundledSpenInstanceId());
            }
            Log.d(str, "setKeyguardLockState : connectionState is " + connectionState);
            if (connectionState == BleSpenManager.ConnectionState.CONNECTED) {
                setEnableSpenUnlockCondition(true);
                return;
            }
            return;
        }
        setEnableSpenUnlockCondition(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setEnableSpenUnlockCondition(boolean z) {
        String str = TAG;
        Log.d(str, "setEnableSpenUnlockCondition : isEnabled is " + z);
        this.mIsSatisfySpenUnlockCondition = z;
    }

    private boolean isSpenUnlockSettingEnabled() {
        return SettingsPreferenceManager.getInstance(this.mContext).getSpenUnlockEnabled();
    }

    private void unlockLockScreen(boolean z, PenActionTriggerType penActionTriggerType) {
        String str;
        if (CommonUtils.isScreenOffActivityRunning(this.mContext)) {
            Log.d(TAG, "unlockLockScreen : Black memo is started");
            screenOn();
            return;
        }
        Log.d(TAG, "unlockLockScreen");
        if (z) {
            RemoteSpenTrustAgentService.sendGrantTrust(this.mContext, 14400000L);
        }
        new Handler().postDelayed(new Runnable() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.6
            @Override // java.lang.Runnable
            public void run() {
                ScreenOffAndLockStateActionInvoker.this.screenOn();
            }
        }, 200L);
        new Handler().postDelayed(new Runnable() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.7
            @Override // java.lang.Runnable
            public void run() {
                SepWrapper.KeyguardManager.semDismissKeyguard(ScreenOffAndLockStateActionInvoker.this.mKeyguardManager);
            }
        }, 1000L);
        if (z) {
            if (penActionTriggerType == PenActionTriggerType.SINGLE_CLICK) {
                str = this.mKeyguardManager.isKeyguardSecure() ? SAUtils.AirCommandSpenRemote.SINGLE_SECURITY : SAUtils.AirCommandSpenRemote.SINGLE_NO_SECURITY;
            } else if (penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
                str = this.mKeyguardManager.isKeyguardSecure() ? SAUtils.AirCommandSpenRemote.DOUBLE_SECURITY : SAUtils.AirCommandSpenRemote.DOUBLE_NO_SECURITY;
            } else if (penActionTriggerType == PenActionTriggerType.LONG_CLICK) {
                str = this.mKeyguardManager.isKeyguardSecure() ? SAUtils.AirCommandSpenRemote.LONG_SECURITY : SAUtils.AirCommandSpenRemote.LONG_NO_SECURITY;
            } else {
                str = Constants.packageName.NONE;
            }
            SAUtils.insertEventLog(SAUtils.AirCommandSpenRemote.SCREEN_ID, SAUtils.AirCommandSpenRemote.EVENT_ID_SPEN_UNLOCK, str, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void screenOn() {
        Log.v(TAG, "screenOn");
        try {
            SepWrapper.PowerManager.semWakeUp(this.mPowerManager, SystemClock.uptimeMillis(), ((Integer) ReflectionUtils.getStaticObjectField(PowerManager.class, "WAKE_REASON_SPEN")).intValue());
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "screenOn : e = " + e, e);
        }
    }

    private int getScreenOffTimeOut() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "screen_off_timeout", 15000);
    }

    private InvocationResult invokeMediaControlAction(PenActionTriggerType penActionTriggerType) {
        PenActionRule mediaControlRule = PenActionRuleManager.getInstance().getMediaControlRule();
        PenActionData action = mediaControlRule != null ? mediaControlRule.getAction(this.mContext, penActionTriggerType) : null;
        if (action != null) {
            this.mPenActionInvoker.invokeAction(this.mContext, action);
            return new InvocationResult(true, action);
        }
        return new InvocationResult().setConsumed(false);
    }

    private boolean isCameraForGlobalAction(PenActionTriggerType penActionTriggerType) {
        PenActionData action = PenActionRuleManager.getInstance().getBaseRule().getAction(this.mContext, penActionTriggerType);
        if (action instanceof AppLaunchActionData) {
            AppLaunchActionData appLaunchActionData = (AppLaunchActionData) action;
            return appLaunchActionData.getPackageName().equals(com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_PACKAGE_NAME) && appLaunchActionData.getComponentName().equals(com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_ACTIVITY_NAME);
        }
        return false;
    }

    private void unlockKeyguardAndInvokeGlobalAction(final PenActionTriggerType penActionTriggerType) {
        Log.d(TAG, "unlockKeyguardAndInvokeGlobalAction");
        unlockLockScreen(true, penActionTriggerType);
        new Handler().postDelayed(new Runnable() { // from class: com.samsung.remotespen.main.invocation.ScreenOffAndLockStateActionInvoker.8
            @Override // java.lang.Runnable
            public void run() {
                ScreenOffAndLockStateActionInvoker.this.invokeGlobalAction(penActionTriggerType);
            }
        }, 1500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeSecureCamera() {
        SepWrapper.ActivityManager.semKeepKeyguardWaitingForActivityDrawn((ActivityManager) this.mContext.getSystemService("activity"));
        Intent intent = new Intent();
        intent.setClassName(com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_PACKAGE_NAME, com.samsung.remotespen.util.Constants.SAMSUNG_CAMERA_ACTIVITY_NAME);
        intent.setAction("android.intent.action.MAIN");
        intent.putExtra("isSecure", true);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(1006698496);
        this.mContext.startActivity(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeGlobalAction(PenActionTriggerType penActionTriggerType) {
        PenActionData action = PenActionRuleManager.getInstance().getBaseRule().getAction(this.mContext, penActionTriggerType);
        if (action != null) {
            InvocationDispatchListener invocationDispatchListener = this.mInvocationDispatchListener;
            if (invocationDispatchListener != null ? invocationDispatchListener.onDispatchInvocation(action) : false) {
                return;
            }
            this.mPenActionInvoker.invokeAction(this.mContext, action);
            return;
        }
        Log.e(TAG, "invokeGlobalAction : global action is null!");
    }

    private void setPendingIntentAfterUnlock(PenActionTriggerType penActionTriggerType) {
        PendingIntent makePendingIntentForGlobalAction = makePendingIntentForGlobalAction(penActionTriggerType);
        if (makePendingIntentForGlobalAction != null) {
            Intent intent = new Intent();
            intent.putExtra("afterKeyguardGone", true);
            SepWrapper.KeyguardManager.semSetPendingIntentAfterUnlock(this.mKeyguardManager, makePendingIntentForGlobalAction, intent);
            screenOn();
        }
    }

    private PendingIntent makePendingIntentForGlobalAction(PenActionTriggerType penActionTriggerType) {
        PenActionData penActionData;
        PenActionRule baseRule = PenActionRuleManager.getInstance().getBaseRule();
        PenActionRuleManager penActionRuleManager = PenActionRuleManager.getInstance();
        if (penActionRuleManager.isActionEnabled(penActionRuleManager.getBaseRuleId(), penActionTriggerType)) {
            penActionData = baseRule.getAction(this.mContext, penActionTriggerType);
        } else {
            Log.d(TAG, "makePendingIntentForGlobalAction : Global action is disabled");
            penActionData = null;
        }
        if (penActionData instanceof AppLaunchActionData) {
            AppShortcut appShortcut = ((AppLaunchActionData) penActionData).getAppShortcut();
            if (appShortcut == null) {
                Log.i(TAG, "makePendingIntentForGlobalAction : shortCut is null");
                return null;
            }
            return Launcher.makeAppLaunchPendingIntent(this.mContext, appShortcut);
        }
        String simpleName = penActionData != null ? penActionData.getClass().getSimpleName() : null;
        Log.i(TAG, "makePendingIntentForGlobalAction : not app launch type action data. " + simpleName);
        return null;
    }
}
