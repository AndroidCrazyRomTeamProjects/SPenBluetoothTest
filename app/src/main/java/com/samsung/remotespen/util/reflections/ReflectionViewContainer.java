package com.samsung.remotespen.util.reflections;

/* loaded from: classes.dex */
public class ReflectionViewContainer {
    private static ViewTreeObserverInternalInsetsInfoReflection sViewTreeObserverInternalInsetsInfoReflection;
    private static ViewTreeObserverReflection sViewTreeObserverReflection;

    public static ViewTreeObserverReflection getViewTreeObserver() {
        if (sViewTreeObserverReflection == null) {
            sViewTreeObserverReflection = new ViewTreeObserverReflection();
        }
        return sViewTreeObserverReflection;
    }

    public static ViewTreeObserverInternalInsetsInfoReflection getViewTreeObserverInternalInsetsInfoReflection() {
        if (sViewTreeObserverInternalInsetsInfoReflection == null) {
            sViewTreeObserverInternalInsetsInfoReflection = new ViewTreeObserverInternalInsetsInfoReflection();
        }
        return sViewTreeObserverInternalInsetsInfoReflection;
    }
}
