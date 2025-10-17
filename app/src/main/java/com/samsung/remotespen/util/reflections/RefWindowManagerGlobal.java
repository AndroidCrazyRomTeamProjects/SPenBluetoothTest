package com.samsung.remotespen.util.reflections;

import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class RefWindowManagerGlobal extends AbstractBaseReflection {
    private static final String TAG = "RefWindowManagerGlobal";
    private static RefWindowManagerGlobal sInstance = null;
    public static final String sWindowManagerGlobalString = "android.view.WindowManagerGlobal";
    private Object mWindowManagerGlobalInstance;

    public RefWindowManagerGlobal(String str) {
        super(str);
        this.mWindowManagerGlobalInstance = null;
    }

    public static synchronized RefWindowManagerGlobal get(String str) {
        RefWindowManagerGlobal refWindowManagerGlobal;
        synchronized (RefWindowManagerGlobal.class) {
            if (sInstance == null) {
                sInstance = new RefWindowManagerGlobal(str);
            }
            refWindowManagerGlobal = sInstance;
        }
        return refWindowManagerGlobal;
    }

    public void trimMemory(int i) {
        try {
            invokeNormalMethod(getWindowManagerGlobalInstance(), "trimMemory", new Class[]{Integer.TYPE}, Integer.valueOf(i));
        } catch (Exception unused) {
            Log.e(TAG, "RefWindowManagerGlobal - trimMemory");
        }
    }

    private Object getWindowManagerGlobalInstance() {
        if (this.mWindowManagerGlobalInstance == null) {
            try {
                this.mWindowManagerGlobalInstance = Class.forName(sWindowManagerGlobalString).getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            } catch (Exception unused) {
                Log.e(TAG, "RefWindowManagerGlobal - getWindowManagerGlobalInstance");
            }
        }
        return this.mWindowManagerGlobalInstance;
    }

    @Override // com.samsung.remotespen.util.reflections.AbstractBaseReflection
    public String getBaseClassName() {
        String name = getWindowManagerGlobalInstance().getClass().getName();
        String str = TAG;
        Log.d(str, "RefWindowManagerGlobal - getBaseClassName : " + name);
        return name;
    }
}
