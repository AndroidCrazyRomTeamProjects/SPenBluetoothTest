package com.samsung.remotespen.util.reflections;

import android.view.ViewTreeObserver;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class ViewTreeObserverReflection extends AbstractBaseReflection {
    private static final String TAG = "ViewTreeObserverReflection";
    private Object mListener;

    @Override // com.samsung.remotespen.util.reflections.AbstractBaseReflection
    public String getBaseClassName() {
        return "android.view.ViewTreeObserver";
    }

    public void addOnComputeInternalInsetsListener(ViewTreeObserver viewTreeObserver, Object obj) {
        Class<?>[] clsArr = {loadClassIfNeeded("android.view.ViewTreeObserver$OnComputeInternalInsetsListener")};
        this.mListener = obj;
        invokeNormalMethod(viewTreeObserver, "addOnComputeInternalInsetsListener", clsArr, obj);
        Log.v(TAG, "addOnComputeInternalInsetsListener");
    }

    public void removeOnComputeInternalInsetsListener(ViewTreeObserver viewTreeObserver, Object obj) {
        invokeNormalMethod(viewTreeObserver, "removeOnComputeInternalInsetsListener", new Class[]{loadClassIfNeeded("android.view.ViewTreeObserver$OnComputeInternalInsetsListener")}, this.mListener);
    }
}
