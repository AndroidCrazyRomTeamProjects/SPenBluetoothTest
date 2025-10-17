package com.samsung.remotespen.main.invocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.condition.ConditionMatchState;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.remotespen.core.penaction.manager.PenActionRuleManager;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerCategory;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.main.invocation.NotRecognizedGestureGuidePanelTrigger;
import com.samsung.remotespen.ui.gateway.SubWindowInfoProvider;
import com.samsung.remotespen.ui.view.ActionInfo;
import com.samsung.remotespen.ui.view.GuidePanelController;
import com.samsung.remotespen.util.MediaControlStateMonitor;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class GuidePanelTrigger {
    private static final String TAG = "GuidePanelTrigger";
    private BleSpenManager mBleSpenManager;
    private Context mContext;
    private GuidePanelController mGuidePanelController;
    private NotRecognizedGestureGuidePanelTrigger mNotRecognizedGestureGuidePanelTrigger;
    private NotSpenActionScreenGuidePanelTrigger mNotSpenActionScreenGuidePanelTrigger;
    private PenActionRuleManager mPenActionRuleManager;
    private SubWindowInfoProvider mSubWindowInfoProvider;
    private Handler mHandler = new Handler();
    private Runnable mGuidePanelShowCallback = new Runnable() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.1
        @Override // java.lang.Runnable
        public void run() {
            Log.d(GuidePanelTrigger.TAG, "mHoverDetectionCallback : 300ms elapsed from hover enter event");
            GuidePanelTrigger.this.showRemoteActionGuidePanel(false);
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                Log.v(GuidePanelTrigger.TAG, "Screen Off");
                GuidePanelTrigger.this.mNotSpenActionScreenGuidePanelTrigger.resetCounting();
                GuidePanelTrigger.this.mNotRecognizedGestureGuidePanelTrigger.resetCounting();
            }
        }
    };
    private SubWindowInfoProvider.FloatingIconEventListener mFloatingIconEventListener = new SubWindowInfoProvider.FloatingIconEventListener() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.3
        @Override // com.samsung.remotespen.ui.gateway.SubWindowInfoProvider.FloatingIconEventListener
        public void onHover(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action == 9) {
                Log.v(GuidePanelTrigger.TAG, "onHover : event is ACTION_HOVER_ENTER");
                GuidePanelTrigger.this.registerGuidePanelShowTimer();
            } else if (action == 10) {
                Log.v(GuidePanelTrigger.TAG, "onHover : event is ACTION_HOVER_EXIT");
                GuidePanelTrigger.this.unregisterGuidePanelShowTimer();
                GuidePanelTrigger.this.mGuidePanelController.dismissGuidePanel(true, true);
            }
        }

        @Override // com.samsung.remotespen.ui.gateway.SubWindowInfoProvider.FloatingIconEventListener
        public void onTouch(MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0) {
                Log.v(GuidePanelTrigger.TAG, "onTouch : event is ACTION_DOWN");
                GuidePanelTrigger.this.unregisterGuidePanelShowTimer();
                GuidePanelTrigger.this.mGuidePanelController.dismissGuidePanel(false, true);
            }
        }
    };

    public GuidePanelTrigger(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        this.mBleSpenManager = BleSpenManager.getInstance(this.mContext);
        this.mPenActionRuleManager = PenActionRuleManager.getInstance();
        this.mGuidePanelController = new GuidePanelController(this.mContext);
        SubWindowInfoProvider subWindowInfoProvider = SubWindowInfoProvider.getInstance(this.mContext);
        this.mSubWindowInfoProvider = subWindowInfoProvider;
        subWindowInfoProvider.setFloatingIconEventListener(this.mFloatingIconEventListener);
        this.mSubWindowInfoProvider.setGuidePanelEventListener(new SubWindowInfoProvider.GuidePanelEventListener() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.4
            @Override // com.samsung.remotespen.ui.gateway.SubWindowInfoProvider.GuidePanelEventListener
            public void onGuidePanelDismiss() {
                if (!GuidePanelTrigger.this.mGuidePanelController.isGuidePanelShowing() || GuidePanelTrigger.this.mGuidePanelController.isGestureFailGuidePanelShowing()) {
                    return;
                }
                GuidePanelTrigger.this.unregisterGuidePanelShowTimer();
                GuidePanelTrigger.this.mGuidePanelController.dismissGuidePanel(true, true);
            }

            @Override // com.samsung.remotespen.ui.gateway.SubWindowInfoProvider.GuidePanelEventListener
            public void onGestureFailGuidePanelDismiss() {
                GuidePanelTrigger.this.mGuidePanelController.dismissSpenGestureFailAnimTextGuidePanel(true);
            }
        });
        this.mNotSpenActionScreenGuidePanelTrigger = new NotSpenActionScreenGuidePanelTrigger(this.mContext, new Runnable() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.5
            @Override // java.lang.Runnable
            public void run() {
                GuidePanelTrigger.this.onShowButtonNoActionGuidePopup();
            }
        });
        this.mNotRecognizedGestureGuidePanelTrigger = new NotRecognizedGestureGuidePanelTrigger(this.mContext, new NotRecognizedGestureGuidePanelTrigger.Listener() { // from class: com.samsung.remotespen.main.invocation.GuidePanelTrigger.6
            @Override // com.samsung.remotespen.main.invocation.NotRecognizedGestureGuidePanelTrigger.Listener
            public boolean showGestureFailWithGotoHelpGuide() {
                GuidePanelTrigger.this.mSubWindowInfoProvider.processBleFloatingIconGestureFailAnimationStart();
                return GuidePanelTrigger.this.onShowGestureFailWithGoToHelpGuidePopup();
            }

            @Override // com.samsung.remotespen.main.invocation.NotRecognizedGestureGuidePanelTrigger.Listener
            public boolean showGestureFailTextGuide(GuidePanelController.BadGestureReason badGestureReason) {
                GuidePanelTrigger.this.mSubWindowInfoProvider.processBleFloatingIconGestureFailAnimationStart();
                return GuidePanelTrigger.this.onShowGestureFailTextGuidePopup(badGestureReason);
            }

            @Override // com.samsung.remotespen.main.invocation.NotRecognizedGestureGuidePanelTrigger.Listener
            public boolean showGestureFailAnimTextGuide(GuidePanelController.BadGestureReason badGestureReason) {
                GuidePanelTrigger.this.mSubWindowInfoProvider.processBleFloatingIconGestureFailAnimationStart();
                return GuidePanelTrigger.this.onShowGestureFailAnimTextGuidePopup(badGestureReason);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void release() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mGuidePanelController.release();
    }

    public void onConfigurationChanged(Configuration configuration) {
        Log.v(TAG, "onConfigurationChanged");
        if (this.mGuidePanelController.isGuidePanelShowing()) {
            unregisterGuidePanelShowTimer();
            this.mGuidePanelController.dismissGuidePanel(false, true);
            this.mSubWindowInfoProvider.processBleFloatingIconOutAnimationStart();
        }
        this.mGuidePanelController.updateGuidePanelLayout();
    }

    public void dismissGestureFailGuidePanel() {
        this.mGuidePanelController.dismissSpenGestureFailAnimTextGuidePanel(false);
    }

    public void updateState() {
        this.mNotRecognizedGestureGuidePanelTrigger.updateState();
        this.mNotSpenActionScreenGuidePanelTrigger.updateState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerGuidePanelShowTimer() {
        this.mHandler.removeCallbacks(this.mGuidePanelShowCallback);
        this.mHandler.postDelayed(this.mGuidePanelShowCallback, 300L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterGuidePanelShowTimer() {
        this.mHandler.removeCallbacks(this.mGuidePanelShowCallback);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onShowButtonNoActionGuidePopup() {
        initGuidePanel();
        GuidePanelController guidePanelController = this.mGuidePanelController;
        if (guidePanelController != null) {
            guidePanelController.showButtonNoActionGuidePanel(true);
        } else {
            Log.e(TAG, "onShowButtonNoActionGuidePopup : Guide panel controller is null");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onShowGestureFailWithGoToHelpGuidePopup() {
        initGuidePanel();
        GuidePanelController guidePanelController = this.mGuidePanelController;
        if (guidePanelController == null) {
            Log.e(TAG, "onShowGestureFailWithGoToHelpGuidePopup : Guide panel controller is null");
            return false;
        }
        return guidePanelController.showSpenGestureFailWithGoToHelpGuidePanel(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onShowGestureFailTextGuidePopup(GuidePanelController.BadGestureReason badGestureReason) {
        initGuidePanel();
        GuidePanelController guidePanelController = this.mGuidePanelController;
        if (guidePanelController == null) {
            Log.e(TAG, "onShowGestureFailTextGuidePopup : Guide panel controller is null");
            return false;
        }
        return guidePanelController.showSpenGestureFailTextGuidePanel(badGestureReason, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onShowGestureFailAnimTextGuidePopup(GuidePanelController.BadGestureReason badGestureReason) {
        initGuidePanel();
        GuidePanelController guidePanelController = this.mGuidePanelController;
        if (guidePanelController == null) {
            Log.e(TAG, "onShowGestureFailAnimTextGuidePopup : Guide panel controller is null");
            return false;
        }
        return guidePanelController.showSpenGestureFailAnimTextGuidePanel(badGestureReason, false);
    }

    public boolean initGuidePanel() {
        if (!this.mPenActionRuleManager.isInitialized()) {
            Log.d(TAG, "initGuidePanel : return false because mPenActionRuleManager didn't initialized.");
            return false;
        }
        PenActionRule penActionRule = null;
        ConditionMatchState currentMatchState = this.mPenActionRuleManager.getCurrentMatchState();
        ArrayList<PenActionRule> rule = this.mPenActionRuleManager.getRule(currentMatchState);
        if (rule != null && rule.size() > 0) {
            penActionRule = rule.get(0);
        }
        if (penActionRule == null) {
            Log.d(TAG, "initGuidePanel : rule object is null although it was got through pen action rule manager.");
            return false;
        }
        setGuidePanelRemoteActionInfo(penActionRule, currentMatchState.mForegroundPackageName, currentMatchState.mForegorundActivityName);
        return true;
    }

    public boolean showRemoteActionGuidePanel(boolean z) {
        ConditionMatchState currentMatchState = this.mPenActionRuleManager.getCurrentMatchState();
        String str = currentMatchState.mForegroundPackageName;
        ArrayList<PenActionRule> rule = this.mPenActionRuleManager.getRule(currentMatchState);
        PenActionRule penActionRule = (rule == null || rule.size() <= 0) ? null : rule.get(0);
        if (penActionRule == null) {
            Log.d(TAG, "showRemoteActionGuidePanel : rule object is null although it was got through pen action rule manager.");
            return false;
        }
        PenActionRule.Type actionType = penActionRule.getActionType();
        if (actionType != PenActionRule.Type.CAMERA_CONTROL && actionType != PenActionRule.Type.MEDIA_CONTROL && actionType != PenActionRule.Type.APP_DEFINED) {
            String str2 = TAG;
            Log.d(str2, "showRemoteActionGuidePanel : rule is not proper type. ruleType=" + actionType);
            return false;
        } else if (QualifiedCameraAppListManager.getInstance(this.mContext).isUnqualifiedCameraAppCase(penActionRule, str)) {
            Log.d(TAG, "showRemoteActionGuidePanel : Not qualified camera app.");
            return false;
        } else {
            if (actionType == PenActionRule.Type.MEDIA_CONTROL) {
                str = MediaControlStateMonitor.getInstance(this.mContext).getActiveNotificationPackageName();
            }
            if (!z || this.mPenActionRuleManager.addGuideUiShownPackageToList(penActionRule.getId(), str)) {
                setGuidePanelRemoteActionInfo(penActionRule, currentMatchState.mForegroundPackageName, currentMatchState.mForegorundActivityName);
                if (this.mGuidePanelController.showRemoteActionGuidePanel(true)) {
                    this.mPenActionRuleManager.setGuideUiShownTime(penActionRule.getId(), System.currentTimeMillis());
                    this.mPenActionRuleManager.addGuideUiShownPackageToList(penActionRule.getId(), str);
                    Log.v(TAG, "showRemoteActionGuidePanel : return true");
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    private void setGuidePanelRemoteActionInfo(PenActionRule penActionRule, String str, String str2) {
        if (penActionRule != null) {
            CharSequence appName = getAppName(penActionRule);
            PenActionData action = penActionRule.getAction(this.mContext, PenActionTriggerType.SINGLE_CLICK);
            ActionInfo actionInfo = action != null ? new ActionInfo(action) : null;
            PenActionData action2 = penActionRule.getAction(this.mContext, PenActionTriggerType.DOUBLE_CLICK);
            ActionInfo actionInfo2 = action2 != null ? new ActionInfo(action2) : null;
            PenActionData action3 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_UP);
            ActionInfo actionInfo3 = action3 != null ? new ActionInfo(action3) : null;
            PenActionData action4 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_DOWN);
            ActionInfo actionInfo4 = action4 != null ? new ActionInfo(action4) : null;
            PenActionData action5 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_LEFT);
            ActionInfo actionInfo5 = action5 != null ? new ActionInfo(action5) : null;
            PenActionData action6 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_RIGHT);
            ActionInfo actionInfo6 = action6 != null ? new ActionInfo(action6) : null;
            PenActionData action7 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_CIRCLE_CW);
            ActionInfo actionInfo7 = action7 != null ? new ActionInfo(action7) : null;
            PenActionData action8 = penActionRule.getAction(this.mContext, PenActionTriggerType.GESTURE_CIRCLE_CCW);
            this.mGuidePanelController.setRemoteActionInfo(new GuidePanelController.RemoteActionInfo(appName, penActionRule.getActionType(), penActionRule.isSwiftActionDeclared(), actionInfo, actionInfo2, actionInfo3, actionInfo4, actionInfo5, actionInfo6, actionInfo7, action8 != null ? new ActionInfo(action8) : null), str, str2);
            return;
        }
        Log.e(TAG, "setGuidePanelRemoteActionInfo : Rule is null");
    }

    private CharSequence getAppName(PenActionRule penActionRule) {
        String str = PenActionRuleManager.getInstance().getCurrentMatchState().mForegroundPackageName;
        int i = AnonymousClass7.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionRule$Type[penActionRule.getActionType().ordinal()];
        if (i != 1) {
            if (i == 2) {
                PackageManager packageManager = this.mContext.getPackageManager();
                try {
                    return packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 128)).toString();
                } catch (PackageManager.NameNotFoundException unused) {
                    return this.mContext.getString(R.string.remotespen_none);
                }
            }
            return penActionRule.getDisplayName(this.mContext);
        }
        return this.mContext.getString(R.string.remotespen_header_media);
    }

    /* renamed from: com.samsung.remotespen.main.invocation.GuidePanelTrigger$7  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass7 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionRule$Type;

        static {
            int[] iArr = new int[PenActionRule.Type.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionRule$Type = iArr;
            try {
                iArr[PenActionRule.Type.MEDIA_CONTROL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionRule$Type[PenActionRule.Type.CAMERA_CONTROL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public void onDoNothingAction(PenActionRule penActionRule, PenActionTriggerType penActionTriggerType) {
        if (penActionRule == null) {
            Log.e(TAG, "onDoNothingAction : rule is null.");
            return;
        }
        if (penActionRule.isSwiftActionDeclared() && penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
            this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(true, penActionTriggerType, false);
        }
        initGuidePanel();
        if (penActionTriggerType == PenActionTriggerType.SINGLE_CLICK || penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
            if (this.mGuidePanelController.showDoNothingGuidePanel(true)) {
                this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, false);
            }
        } else if (isKnownGestureTriggerType(penActionTriggerType) && this.mGuidePanelController.showDoNothingForGestureGuidePanel(penActionTriggerType, true)) {
            this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, false);
        }
    }

    public InvocationResult onPenActionTrigger(PenActionTriggerType penActionTriggerType, boolean z, boolean z2) {
        if (z) {
            if (penActionTriggerType == PenActionTriggerType.LONG_CLICK) {
                this.mSubWindowInfoProvider.processMinimizeAirCommandForAirAction();
            } else if (z2) {
                Log.d(TAG, "onPenActionTrigger : unqualified camera app");
                this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, false);
            } else {
                this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(true, penActionTriggerType, false);
            }
        } else {
            this.mSubWindowInfoProvider.processBleFloatingBtnNoRemoteActionAnimation();
        }
        String str = TAG;
        Log.v(str, "onPenActionTrigger : isSuccess = " + z);
        return null;
    }

    public void onNoActionTrigger(PenActionRule penActionRule, PenActionTriggerType penActionTriggerType) {
        if (penActionRule == null) {
            Log.e(TAG, "onNoActionTrigger : rule is null.");
        } else if (penActionRule.getActionType() == PenActionRule.Type.MEDIA_CONTROL) {
            initGuidePanel();
            if (isKnownGestureTriggerType(penActionTriggerType) && this.mGuidePanelController.showGestureNoActionGuidePanel(penActionTriggerType, true)) {
                this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, true);
            }
        } else if (penActionTriggerType.getCategory() == PenActionTriggerCategory.GESTURE) {
            initGuidePanel();
            PenActionRuleManager penActionRuleManager = PenActionRuleManager.getInstance();
            if (!Boolean.valueOf(penActionRuleManager.isActionEnabled(penActionRuleManager.getBaseRuleId(), penActionTriggerType)).booleanValue()) {
                if (this.mGuidePanelController.showGlobalGestureOffGuidePanel(penActionTriggerType, true)) {
                    this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, false);
                }
            } else if (this.mGuidePanelController.showGestureNoActionGuidePanel(penActionTriggerType, true)) {
                this.mSubWindowInfoProvider.processBleFloatingIconAnimationStart(false, penActionTriggerType, false);
            }
        } else if (penActionTriggerType.getCategory() == PenActionTriggerCategory.BUTTON) {
            if (penActionTriggerType == PenActionTriggerType.SINGLE_CLICK) {
                this.mNotSpenActionScreenGuidePanelTrigger.increaseClickCount(1);
            } else if (penActionTriggerType == PenActionTriggerType.DOUBLE_CLICK) {
                this.mNotSpenActionScreenGuidePanelTrigger.increaseClickCount(2);
            }
            this.mSubWindowInfoProvider.processBleFloatingBtnNoRemoteActionAnimation();
        } else {
            String str = TAG;
            Log.d(str, "onNoActionTrigger : Unhandled trigger type : " + penActionTriggerType);
        }
    }

    public void onGestureFailTrigger(BleSpenGestureQuality bleSpenGestureQuality) {
        this.mSubWindowInfoProvider.processBleFloatingIconGestureFailAnimationStart();
        this.mNotRecognizedGestureGuidePanelTrigger.increaseNotRecognizedGestureCount(1, bleSpenGestureQuality);
    }

    private boolean isKnownGestureTriggerType(PenActionTriggerType penActionTriggerType) {
        return penActionTriggerType.getCategory() == PenActionTriggerCategory.GESTURE && penActionTriggerType != PenActionTriggerType.GESTURE_UNKNOWN;
    }
}
