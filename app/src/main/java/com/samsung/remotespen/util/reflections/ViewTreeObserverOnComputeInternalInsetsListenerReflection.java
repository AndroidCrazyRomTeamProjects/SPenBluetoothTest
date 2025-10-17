package com.samsung.remotespen.util.reflections;

import java.lang.reflect.Method;

/* loaded from: classes.dex */
public class ViewTreeObserverOnComputeInternalInsetsListenerReflection extends AbstractProxyReflection {
    private static final String ORIGINAL_CLASS_NAME = "android.view.ViewTreeObserver$OnComputeInternalInsetsListener";

    public void onComputeInternalInsets(Object obj) {
    }

    public ViewTreeObserverOnComputeInternalInsetsListenerReflection() {
        super(ORIGINAL_CLASS_NAME);
    }

    @Override // com.samsung.remotespen.util.reflections.AbstractProxyReflection
    public Object invokeInternal(Object obj, Method method, Object[] objArr) {
        if ("onComputeInternalInsets".equals(method.getName())) {
            onComputeInternalInsets(objArr[0]);
            return null;
        }
        return null;
    }
}
