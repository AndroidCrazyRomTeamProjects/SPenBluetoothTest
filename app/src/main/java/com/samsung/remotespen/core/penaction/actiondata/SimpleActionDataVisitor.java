package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;

/* loaded from: classes.dex */
public interface SimpleActionDataVisitor {
    Object visit(Context context, AppDefinedActionData appDefinedActionData);

    Object visit(Context context, AppLaunchActionData appLaunchActionData);

    Object visit(Context context, CameraControlActionData cameraControlActionData);

    Object visit(Context context, DoNothingActionData doNothingActionData);

    Object visit(Context context, KeyInjectionActionData keyInjectionActionData);

    Object visit(Context context, MediaControlActionData mediaControlActionData);
}
