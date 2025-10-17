package com.samsung.remotespen.util;

import android.view.InputDevice;
import android.view.MotionEvent;

/* loaded from: classes.dex */
public class UnrecommendedSpenHelper {
    private static final String UNUSED_SPEN = "sec_unused_e-pen";

    public static boolean isUnrecommendedSpenEvent(MotionEvent motionEvent) {
        return isUnrecommendedSpenName(getPenName(motionEvent));
    }

    public static boolean isUnrecommendedSpenName(String str) {
        return UNUSED_SPEN.equals(str);
    }

    private static String getPenName(MotionEvent motionEvent) {
        InputDevice device = motionEvent.getDevice();
        if (device != null) {
            return device.getName();
        }
        return null;
    }
}
