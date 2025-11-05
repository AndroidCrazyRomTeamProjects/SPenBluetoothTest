package com.samsung.remotespen.core.penaction.manager;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.SpenFunctionalityManager;
import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerCategory;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public class DefaultActionDetector {
    private static final String TAG = "DefaultActionDetector";

    public static PenActionData getDefaultAppDefinedActionData(Context context, String str, String str2, PenActionTriggerType penActionTriggerType) {
        RemoteActionManager remoteActionManager = RemoteActionManager.getInstance(context);
        if (remoteActionManager == null) {
            String str3 = TAG;
            Log.e(str3, "getDefaultAppDefinedActionData : failed to get remote action manager : " + str + " / " + str2);
            return null;
        }
        RemoteAction remoteAction = remoteActionManager.getRemoteAction(str, str2);
        if (remoteAction == null) {
            String str4 = TAG;
            Log.e(str4, "getDefaultAppDefinedActionData : failed to get remote action : " + str + " / " + str2);
            return null;
        }
        Action action = buildDefaultAppDefinedActionMap(remoteAction).get(penActionTriggerType);
        if (action == null) {
            action = determineLegacyButtonClickAction(remoteAction, penActionTriggerType);
        }
        if (action != null) {
            return new AppDefinedActionData(str, str2, action.getId());
        }
        if (penActionTriggerType.getCategory() == PenActionTriggerCategory.BUTTON && remoteAction.isOnlyConsistedWithMotionOnlyActions()) {
            return null;
        }
        if (penActionTriggerType.getCategory() == PenActionTriggerCategory.GESTURE && remoteAction.isOnlyConsistedWithButtonOnlyActions()) {
            return null;
        }
        return PenActionHelper.createSimpleActionData(PenActionDataType.DO_NOTHING);
    }

    /* renamed from: com.samsung.remotespen.core.penaction.manager.DefaultActionDetector$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType;

        static {
            int[] iArr = new int[PenActionTriggerType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType = iArr;
            try {
                iArr[PenActionTriggerType.SINGLE_CLICK.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.DOUBLE_CLICK.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_UP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_DOWN.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_LEFT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_RIGHT.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_POINTY_UP.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_POINTY_DOWN.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_POINTY_LEFT.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_POINTY_RIGHT.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.GESTURE_SHAKE.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.SECONDARY_SINGLE_CLICK.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.SECONDARY_DOUBLE_CLICK.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[PenActionTriggerType.SECONDARY_LONG_CLICK.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
        }
    }

    public static PenActionData getDefaultMediaActionData(PenActionTriggerType penActionTriggerType) {
        switch (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[penActionTriggerType.ordinal()]) {
            case 1:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_PLAY_PAUSE);
            case 2:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_NEXT_TRACK);
            case 3:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_VOLUME_UP);
            case 4:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_VOLUME_DOWN);
            case 5:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_PREV_TRACK);
            case 6:
                return PenActionHelper.createSimpleActionData(PenActionDataType.MEDIA_CONTROL_NEXT_TRACK);
            default:
                return null;
        }
    }

    public static PenActionData getDefaultCameraActionData(PenActionTriggerType penActionTriggerType) {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[penActionTriggerType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                return null;
            }
            return PenActionHelper.createSimpleActionData(PenActionDataType.DO_NOTHING);
        }
        return new CameraControlActionData(CameraControlActionData.ActionType.SHUTTER);
    }

    public static PenActionData getDefaultBaseActionData(Context context, PenActionTriggerType penActionTriggerType) {
        if (SpenFunctionalityManager.getInstance(context).isAnyPenSupportGlobalGesture()) {
            switch (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[penActionTriggerType.ordinal()]) {
                case 7:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_HOME_KEY);
                case 8:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.LAUNCH_SMART_SELECT);
                case 9:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_BACK_KEY);
                case 10:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_RECENT_KEY);
                case 11:
                    if (SpenFunctionalityManager.getInstance(context).isAnyPenSupportAirMouse()) {
                        return PenActionHelper.createSimpleActionData(PenActionDataType.AIR_MOUSE_ENABLE);
                    }
                    return PenActionHelper.createSimpleActionData(PenActionDataType.LAUNCH_SCREEN_WRITE);
                case 12:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_BACK_KEY);
                case 13:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_HOME_KEY);
                case 14:
                    return PenActionHelper.createSimpleActionData(PenActionDataType.INJECT_RECENT_KEY);
                default:
                    return null;
            }
        }
        return null;
    }

    private static Action determineLegacyButtonClickAction(RemoteAction remoteAction, PenActionTriggerType penActionTriggerType) {
        Action action;
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerType[penActionTriggerType.ordinal()];
        int i2 = 1;
        if (i == 1) {
            i2 = 0;
        } else if (i != 2) {
            return null;
        }
        if (remoteAction.getActionCount() >= i2 + 1 && (action = remoteAction.getAction(i2)) != null && isButtonCategoryAction(action)) {
            return action;
        }
        return null;
    }

    private static boolean isButtonCategoryAction(Action action) {
        int actionCategory = action.getActionCategory();
        return actionCategory == 0 || actionCategory == 1;
    }

    private static HashMap<PenActionTriggerType, Action> buildDefaultAppDefinedActionMap(RemoteAction remoteAction) {
        HashMap<PenActionTriggerType, Action> hashMap = new HashMap<>();
        Iterator<Action> it = getSortedActionsByPriority(remoteAction).iterator();
        while (it.hasNext()) {
            Action next = it.next();
            Iterator<String> it2 = next.getPreferedDefaultGestureActions().iterator();
            while (it2.hasNext()) {
                PenActionTriggerType convertRemoteActionGestureNameToPenActionTriggerType = convertRemoteActionGestureNameToPenActionTriggerType(it2.next());
                if (!hashMap.containsKey(convertRemoteActionGestureNameToPenActionTriggerType)) {
                    hashMap.put(convertRemoteActionGestureNameToPenActionTriggerType, next);
                }
            }
        }
        return hashMap;
    }

    private static PenActionTriggerType convertRemoteActionGestureNameToPenActionTriggerType(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1110539224:
                if (str.equals(Action.Preference.GESTURE_CIRCLE_CCW)) {
                    c = 0;
                    break;
                }
                break;
            case -326696768:
                if (str.equals(Action.Preference.GESTURE_LONG_PRESS)) {
                    c = 1;
                    break;
                }
                break;
            case -88919616:
                if (str.equals(Action.Preference.GESTURE_SWIPE_UP)) {
                    c = 2;
                    break;
                }
                break;
            case 94750088:
                if (str.equals(Action.Preference.GESTURE_CLICK)) {
                    c = 3;
                    break;
                }
                break;
            case 447091335:
                if (str.equals(Action.Preference.GESTURE_SWIPE_DOWN)) {
                    c = 4;
                    break;
                }
                break;
            case 447319532:
                if (str.equals(Action.Preference.GESTURE_SWIPE_LEFT)) {
                    c = 5;
                    break;
                }
                break;
            case 795460163:
                if (str.equals(Action.Preference.GESTURE_CIRCLE_CW)) {
                    c = 6;
                    break;
                }
                break;
            case 987664599:
                if (str.equals(Action.Preference.GESTURE_SWIPE_RIGHT)) {
                    c = 7;
                    break;
                }
                break;
            case 1374143386:
                if (str.equals(Action.Preference.GESTURE_DOUBLE_CLICK)) {
                    c = '\b';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return PenActionTriggerType.GESTURE_CIRCLE_CCW;
            case 1:
                return PenActionTriggerType.LONG_CLICK;
            case 2:
                return PenActionTriggerType.GESTURE_UP;
            case 3:
                return PenActionTriggerType.SINGLE_CLICK;
            case 4:
                return PenActionTriggerType.GESTURE_DOWN;
            case 5:
                return PenActionTriggerType.GESTURE_LEFT;
            case 6:
                return PenActionTriggerType.GESTURE_CIRCLE_CW;
            case 7:
                return PenActionTriggerType.GESTURE_RIGHT;
            case '\b':
                return PenActionTriggerType.DOUBLE_CLICK;
            default:
                String str2 = TAG;
                Log.e(str2, "convertRemoteActionGestureNameToPenActionTriggerType : unexpected gestureName : " + str);
                return null;
        }
    }

    private static ArrayList<Action> getSortedActionsByPriority(RemoteAction remoteAction) {
        int i;
        ArrayList<Action> arrayList = new ArrayList<>();
        ArrayList arrayList2 = new ArrayList();
        int actionCount = remoteAction.getActionCount();
        for (int i2 = 0; i2 < actionCount; i2++) {
            Action action = remoteAction.getAction(i2);
            if (action != null) {
                int priority = action.getPriority();
                if (priority == 0) {
                    arrayList2.add(action);
                } else {
                    int size = arrayList.size() - 1;
                    while (true) {
                        if (size < 0) {
                            i = 0;
                            break;
                        } else if (priority >= arrayList.get(size).getPriority()) {
                            i = size + 1;
                            break;
                        } else {
                            size--;
                        }
                    }
                    arrayList.add(i, action);
                }
            }
        }
        arrayList.addAll(arrayList2);
        return arrayList;
    }
}
