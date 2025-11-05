package com.samsung.remotespen.core.penaction.manager;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.KeyInjectionActionData;
import com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor;
import com.samsung.util.constants.SpenComponents;
import java.util.ArrayList;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: PenActionHelper.java */
/* loaded from: classes.dex */
public class ActionDataTypeVisitor implements SimpleActionDataVisitor {
    private static final String TAG = "ActionDataTypeVisitor";

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, KeyInjectionActionData keyInjectionActionData) {
        ArrayList<Integer> keyCodeArray = keyInjectionActionData.getKeyCodeArray();
        if (!keyCodeArray.isEmpty()) {
            int intValue = keyCodeArray.get(0).intValue();
            if (intValue == 3) {
                return PenActionDataType.INJECT_HOME_KEY;
            }
            if (intValue == 4) {
                return PenActionDataType.INJECT_BACK_KEY;
            }
            if (intValue == 187) {
                return PenActionDataType.INJECT_RECENT_KEY;
            }
        } else {
            Log.e(TAG, "visit : Empty keycode");
        }
        return PenActionDataType.INJECT_CUSTOM_KEY;
    }

    /* compiled from: PenActionHelper.java */
    /* renamed from: com.samsung.remotespen.core.penaction.manager.ActionDataTypeVisitor$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType;

        static {
            int[] iArr = new int[MediaControlActionData.ActionType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType = iArr;
            try {
                iArr[MediaControlActionData.ActionType.PLAY_PAUSE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[MediaControlActionData.ActionType.NEXT_TRACK.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[MediaControlActionData.ActionType.PREV_TRACK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[MediaControlActionData.ActionType.VOLUME_UP.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[MediaControlActionData.ActionType.VOLUME_DOWN.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, MediaControlActionData mediaControlActionData) {
        MediaControlActionData.ActionType actionType = mediaControlActionData.getActionType();
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[actionType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        if (i == 5) {
                            return PenActionDataType.MEDIA_CONTROL_VOLUME_DOWN;
                        }
                        String str = TAG;
                        Log.e(str, "visit : unknown media action type - " + actionType);
                        return PenActionDataType.MEDIA_CONTROL_UNKNOWN;
                    }
                    return PenActionDataType.MEDIA_CONTROL_VOLUME_UP;
                }
                return PenActionDataType.MEDIA_CONTROL_PREV_TRACK;
            }
            return PenActionDataType.MEDIA_CONTROL_NEXT_TRACK;
        }
        return PenActionDataType.MEDIA_CONTROL_PLAY_PAUSE;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, AppDefinedActionData appDefinedActionData) {
        return PenActionDataType.APP_DEFINED;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, AppLaunchActionData appLaunchActionData) {
        if (SpenComponents.AIR_MOUSE.getClassName().equals(appLaunchActionData.getComponentName())) {
            return PenActionDataType.AIR_MOUSE_ENABLE;
        }
        if (SpenComponents.SCREEN_WRITE.getClassName().equals(appLaunchActionData.getComponentName())) {
            return PenActionDataType.LAUNCH_SCREEN_WRITE;
        }
        if (SpenComponents.SMART_SELECT.getClassName().equals(appLaunchActionData.getComponentName())) {
            return PenActionDataType.LAUNCH_SMART_SELECT;
        }
        return PenActionDataType.APP_LAUNCH;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, CameraControlActionData cameraControlActionData) {
        return PenActionDataType.CAMERA_CONTROL;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.SimpleActionDataVisitor
    public Object visit(Context context, DoNothingActionData doNothingActionData) {
        return PenActionDataType.DO_NOTHING;
    }
}
