package com.samsung.remotespen.main.invocation;

import android.content.Context;
import android.media.session.MediaController;
import android.util.Log;

import com.samsung.launcher.Launcher;
import com.samsung.remotespen.core.penaction.actiondata.AppDefinedActionData;
import com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData;
import com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.DoNothingActionData;
import com.samsung.remotespen.core.penaction.actiondata.KeyInjectionActionData;
import com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData;
import com.samsung.remotespen.core.penaction.actiondata.PenActionData;
import com.samsung.remotespen.core.penaction.manager.PenActionRuleManager;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteActionManager;
import com.samsung.remotespen.util.MediaControlStateMonitor;
import com.samsung.util.CommonUtils;
import com.samsung.util.KeyEventInjector;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.shortcut.AppShortcut;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class PenActionInvoker {
    private static final String TAG = "PenActionInvoker";
    private Context mContext;
    private KeyEventInjector mKeyEventInjector = new KeyEventInjector();

    /* loaded from: classes.dex */
    public enum InvocationType {
        START,
        FINISH
    }

    public void invokeAction(Context context, PenActionData penActionData) {
        invokeAction(context, penActionData, InvocationType.START);
        invokeAction(context, penActionData, InvocationType.FINISH);
    }

    public boolean invokeAction(Context context, PenActionData penActionData, InvocationType invocationType) {
        if (penActionData == null) {
            Log.e(TAG, "invokeAction : action data is null!");
            return false;
        }
        String str = TAG;
        Log.d(str, "invokeAction : " + penActionData.getClass().getSimpleName() + ", type=" + invocationType);
        this.mContext = context.getApplicationContext();
        if (penActionData instanceof KeyInjectionActionData) {
            return invokeKeyInjectionActionData(context, (KeyInjectionActionData) penActionData, invocationType);
        }
        if (penActionData instanceof AppDefinedActionData) {
            return invokeAppDefinedActionData(context, (AppDefinedActionData) penActionData, invocationType);
        }
        if (penActionData instanceof CameraControlActionData) {
            return invokeCameraControlActionData(context, (CameraControlActionData) penActionData, invocationType);
        }
        if (penActionData instanceof MediaControlActionData) {
            return invokeMediaControlActionData(context, (MediaControlActionData) penActionData, invocationType);
        }
        if (penActionData instanceof AppLaunchActionData) {
            return invokeShortcutLaunchActionData(context, (AppLaunchActionData) penActionData, invocationType);
        }
        return penActionData instanceof DoNothingActionData;
    }

    private boolean invokeShortcutLaunchActionData(Context context, AppLaunchActionData appLaunchActionData, InvocationType invocationType) {
        AppShortcut appShortcut;
        if (invocationType != InvocationType.START || (appShortcut = appLaunchActionData.getAppShortcut()) == null) {
            return false;
        }
        return Launcher.startShortcut(context, appShortcut);
    }

    private boolean invokeKeyInjectionActionData(Context context, KeyInjectionActionData keyInjectionActionData, InvocationType invocationType) {
        if (invocationType == InvocationType.START) {
            ArrayList<Integer> keyCodeArray = keyInjectionActionData.getKeyCodeArray();
            if (keyCodeArray.isEmpty()) {
                return true;
            }
            this.mKeyEventInjector.injectKeyEvent(context, keyCodeArray);
            return true;
        }
        return false;
    }

    private boolean invokeAppDefinedActionData(Context context, AppDefinedActionData appDefinedActionData, InvocationType invocationType) {
        List<Integer> keyList;
        Action action = appDefinedActionData.getAction(RemoteActionManager.getInstance(context));
        if (action == null) {
            Log.e(TAG, "invokeAppDefinedActionData : No remote action");
            return false;
        }
        Action.KeyShortcut keyShortCut = action.getKeyShortCut();
        if (keyShortCut == null || (keyList = keyShortCut.getKeyList()) == null || keyList.size() <= 0) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$main$invocation$PenActionInvoker$InvocationType[invocationType.ordinal()];
        if (i == 1) {
            this.mKeyEventInjector.injectKeyEvent(context, keyList, 0);
        } else if (i == 2) {
            this.mKeyEventInjector.injectKeyEvent(context, keyList, 1);
        } else {
            Assert.fail("Unexpected invocation type: " + invocationType);
        }
        return true;
    }

    /* renamed from: com.samsung.remotespen.main.invocation.PenActionInvoker$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$invocation$PenActionInvoker$InvocationType;

        static {
            int[] iArr = new int[InvocationType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$invocation$PenActionInvoker$InvocationType = iArr;
            try {
                iArr[InvocationType.START.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$main$invocation$PenActionInvoker$InvocationType[InvocationType.FINISH.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    private boolean invokeCameraControlActionData(Context context, CameraControlActionData cameraControlActionData, InvocationType invocationType) {
        if (invocationType == InvocationType.START) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(Integer.valueOf(cameraControlActionData.getKeyEvent()));
            if (arrayList.size() > 0) {
                this.mKeyEventInjector.injectKeyEvent(context, arrayList);
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean invokeMediaControlActionData(Context context, MediaControlActionData mediaControlActionData, InvocationType invocationType) {
        if (invocationType == InvocationType.START) {
            if (CommonUtils.isDuringCall(context)) {
                Log.d(TAG, "invokeMediaControlActionData : skip invocation during call state");
                return false;
            }
            Assert.notNull(PenActionRuleManager.getInstance().getCurrentMatchState());
            List<MediaController> activeSessions = MediaControlStateMonitor.getInstance(context).getActiveSessions();
            String str = TAG;
            Log.d(str, "invokeMediaControlActionData : active session cnt = " + activeSessions.size());
            int keyEvent = mediaControlActionData.getKeyEvent();
            if (!activeSessions.isEmpty()) {
                this.mKeyEventInjector.injectKeyEvent(context, keyEvent);
                return true;
            }
        }
        return false;
    }
}
