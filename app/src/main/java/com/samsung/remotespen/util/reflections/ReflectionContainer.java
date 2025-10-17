package com.samsung.remotespen.util.reflections;

import android.content.Context;
import java.io.File;

/* loaded from: classes.dex */
public class ReflectionContainer {
    public static void init(Context context) {
        String path;
        File cacheDir = context.getCacheDir();
        if (cacheDir == null || (path = cacheDir.getPath()) == null) {
            return;
        }
        System.setProperty("dexmaker.dexcache", path);
    }

    public static ViewTreeObserverReflection getViewTreeObserver() {
        return ReflectionViewContainer.getViewTreeObserver();
    }

    public static ViewTreeObserverInternalInsetsInfoReflection getViewTreeObserverInternalInsetsInfo() {
        return ReflectionViewContainer.getViewTreeObserverInternalInsetsInfoReflection();
    }
}
