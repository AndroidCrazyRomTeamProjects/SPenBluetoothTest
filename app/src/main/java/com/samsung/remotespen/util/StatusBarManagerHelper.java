package com.samsung.remotespen.util;

import android.app.StatusBarManager;
import android.content.Context;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class StatusBarManagerHelper {
    private static final String TAG = "StatusBarManagerHelper";

    public static void setIcon(Context context, String str, int i, int i2, String str2) {
        try {
            ReflectionUtils.invokeMethod(getStatusBarManager(context), "setIcon", str, Integer.valueOf(i), Integer.valueOf(i2), str2);
        } catch (Exception e) {
            String str3 = TAG;
            Log.e(str3, "setIcon : e = " + e + ", " + e.getCause(), e);
        }
    }

    public static void removeIcon(Context context, String str) {
        try {
            ReflectionUtils.invokeMethod(getStatusBarManager(context), "removeIcon", str);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "removeIcon : e = " + e + ", " + e.getCause(), e);
        }
    }

    public static void setIconVisibility(Context context, String str, boolean z) {
        try {
            ReflectionUtils.invokeMethod(getStatusBarManager(context), "setIconVisibility", str, Boolean.valueOf(z));
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "removeIcon : e = " + e + ", " + e.getCause(), e);
        }
    }

    private static StatusBarManager getStatusBarManager(Context context) {
        return (StatusBarManager) context.getSystemService("statusbar");
    }
}
