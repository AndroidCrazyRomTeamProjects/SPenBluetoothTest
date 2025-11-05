package com.samsung.remotespen.core.penaction.manager;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.KeyInjectionActionData;
import com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerCategory;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import com.samsung.util.constants.SpenComponents;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.shortcut.AppShortcut;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class PenActionHelper {
    private static final String TAG = "PenActionHelper";
    private static ActionDataTypeVisitor mActionDataTypeVisitor = new ActionDataTypeVisitor();

    public static boolean haveRemoteActions() {
        PenActionRuleManager penActionRuleManager = PenActionRuleManager.getInstance();
        if (penActionRuleManager.isInitialized()) {
            PenActionRule penActionRule = null;
            ArrayList<PenActionRule> rule = penActionRuleManager.getRule(penActionRuleManager.getCurrentMatchState());
            if (rule != null && rule.size() > 0) {
                penActionRule = rule.get(0);
            }
            return penActionRule != null && (penActionRule.getActionType() == PenActionRule.Type.APP_DEFINED || penActionRule.getActionType() == PenActionRule.Type.MEDIA_CONTROL || penActionRule.getActionType() == PenActionRule.Type.CAMERA_CONTROL);
        }
        return false;
    }

    public static boolean isPenActionDataAvailable(Context context, String str, String str2, PenActionTriggerCategory penActionTriggerCategory) {
        return createUserSelectablePenActionDataList(context, str, str2, penActionTriggerCategory, false).size() > 0;
    }

    public static ArrayList<PenActionData> createUserSelectablePenActionDataList(Context context, String str, String str2, PenActionTriggerCategory penActionTriggerCategory, boolean z) {
        ArrayList<PenActionData> createPenActionDataList = createPenActionDataList(context, str, str2, penActionTriggerCategory, false);
        if (z) {
            createPenActionDataList.add(createSimpleActionData(PenActionDataType.DO_NOTHING));
        }
        return createPenActionDataList;
    }

    public static PenActionDataType getActioDataType(PenActionData penActionData) {
        if (penActionData == null) {
            return null;
        }
        return (PenActionDataType) penActionData.accept(null, mActionDataTypeVisitor);
    }

    public static PenActionData createSimpleActionData(PenActionDataType penActionDataType) {
        switch (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[penActionDataType.ordinal()]) {
            case 1:
                return new DoNothingActionData();
            case 2:
                return new MediaControlActionData(MediaControlActionData.ActionType.PLAY_PAUSE);
            case 3:
                return new MediaControlActionData(MediaControlActionData.ActionType.PREV_TRACK);
            case 4:
                return new MediaControlActionData(MediaControlActionData.ActionType.NEXT_TRACK);
            case 5:
                return new MediaControlActionData(MediaControlActionData.ActionType.VOLUME_UP);
            case 6:
                return new MediaControlActionData(MediaControlActionData.ActionType.VOLUME_DOWN);
            case 7:
                return new KeyInjectionActionData(3);
            case 8:
                return new KeyInjectionActionData(4);
            case 9:
                return new KeyInjectionActionData(187);
            case 10:
                ComponentName componentName = SpenComponents.AIR_MOUSE;
                return new AppLaunchActionData(new AppShortcut(0, componentName.getPackageName(), componentName.getClassName(), "S", "null"));
            case 11:
                ComponentName componentName2 = SpenComponents.SCREEN_WRITE;
                return new AppLaunchActionData(new AppShortcut(0, componentName2.getPackageName(), componentName2.getClassName(), "S", "null"));
            case 12:
                ComponentName componentName3 = SpenComponents.SMART_SELECT;
                return new AppLaunchActionData(new AppShortcut(0, componentName3.getPackageName(), componentName3.getClassName(), "S", "null"));
            default:
                String str = TAG;
                Log.e(str, "createSimpleActionData : Unexpected type - " + penActionDataType);
                Assert.fail();
                return null;
        }
    }

    /* renamed from: com.samsung.remotespen.core.penaction.manager.PenActionHelper$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType;

        static {
            int[] iArr = new int[BleSpenGestureType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType = iArr;
            try {
                iArr[BleSpenGestureType.UNKNOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.SWIPE_UP.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.SWIPE_DOWN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.SWIPE_LEFT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.SWIPE_RIGHT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.CIRCLE_CW.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.CIRCLE_CCW.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.SHAKE.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_RIGHT.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_LEFT.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_UP.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_DOWN.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.TRIANGLE.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            int[] iArr2 = new int[PenActionDataType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType = iArr2;
            try {
                iArr2[PenActionDataType.DO_NOTHING.ordinal()] = 1;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.MEDIA_CONTROL_PLAY_PAUSE.ordinal()] = 2;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.MEDIA_CONTROL_PREV_TRACK.ordinal()] = 3;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.MEDIA_CONTROL_NEXT_TRACK.ordinal()] = 4;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.MEDIA_CONTROL_VOLUME_UP.ordinal()] = 5;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.MEDIA_CONTROL_VOLUME_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.INJECT_HOME_KEY.ordinal()] = 7;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.INJECT_BACK_KEY.ordinal()] = 8;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.INJECT_RECENT_KEY.ordinal()] = 9;
            } catch (NoSuchFieldError unused22) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.AIR_MOUSE_ENABLE.ordinal()] = 10;
            } catch (NoSuchFieldError unused23) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.LAUNCH_SCREEN_WRITE.ordinal()] = 11;
            } catch (NoSuchFieldError unused24) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$manager$PenActionDataType[PenActionDataType.LAUNCH_SMART_SELECT.ordinal()] = 12;
            } catch (NoSuchFieldError unused25) {
            }
        }
    }

    public static PenActionTriggerType convertToPenActionTriggerType(BleSpenGestureType bleSpenGestureType) {
        switch (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[bleSpenGestureType.ordinal()]) {
            case 1:
                return PenActionTriggerType.GESTURE_UNKNOWN;
            case 2:
                return PenActionTriggerType.GESTURE_UP;
            case 3:
                return PenActionTriggerType.GESTURE_DOWN;
            case 4:
                return PenActionTriggerType.GESTURE_LEFT;
            case 5:
                return PenActionTriggerType.GESTURE_RIGHT;
            case 6:
                return PenActionTriggerType.GESTURE_CIRCLE_CW;
            case 7:
                return PenActionTriggerType.GESTURE_CIRCLE_CCW;
            case 8:
                return PenActionTriggerType.GESTURE_SHAKE;
            case 9:
                return PenActionTriggerType.GESTURE_POINTY_RIGHT;
            case 10:
                return PenActionTriggerType.GESTURE_POINTY_LEFT;
            case 11:
                return PenActionTriggerType.GESTURE_POINTY_UP;
            case 12:
                return PenActionTriggerType.GESTURE_POINTY_DOWN;
            case 13:
                return PenActionTriggerType.GESTURE_TRIANGLE;
            default:
                Assert.fail("Unexpected gesture type : " + bleSpenGestureType);
                return null;
        }
    }

    private static ArrayList<PenActionData> createPenActionDataList(Context context, String str, String str2, PenActionTriggerCategory penActionTriggerCategory, boolean z) {
        RemoteAction remoteAction;
        ArrayList<PenActionData> arrayList = new ArrayList<>();
        RemoteActionManager remoteActionManager = RemoteActionManager.getInstance(context);
        if (remoteActionManager == null || (remoteAction = remoteActionManager.getRemoteAction(str, str2)) == null) {
            return arrayList;
        }
        int actionCount = remoteAction.getActionCount();
        for (int i = 0; i < actionCount; i++) {
            Action action = remoteAction.getAction(i);
            if (action != null) {
                boolean isCategoryMatched = isCategoryMatched(penActionTriggerCategory, action.getActionCategory());
                if (!z && action.getHide()) {
                    isCategoryMatched = false;
                }
                if (isCategoryMatched) {
                    arrayList.add(new AppDefinedActionData(str, str2, action.getId()));
                }
            }
        }
        return arrayList;
    }

    private static boolean isCategoryMatched(PenActionTriggerCategory penActionTriggerCategory, int i) {
        if (i != 0) {
            return i != 1 ? i == 2 && penActionTriggerCategory == PenActionTriggerCategory.GESTURE : penActionTriggerCategory == PenActionTriggerCategory.BUTTON;
        }
        return true;
    }
}
