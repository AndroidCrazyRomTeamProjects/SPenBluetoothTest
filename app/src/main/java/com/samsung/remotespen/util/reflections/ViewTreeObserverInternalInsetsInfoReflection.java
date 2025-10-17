package com.samsung.remotespen.util.reflections;

import android.graphics.Rect;
import android.graphics.Region;

/* loaded from: classes.dex */
public class ViewTreeObserverInternalInsetsInfoReflection extends AbstractBaseReflection {
    public int TOUCHABLE_INSETS_REGION;

    @Override // com.samsung.remotespen.util.reflections.AbstractBaseReflection
    public String getBaseClassName() {
        return "android.view.ViewTreeObserver$InternalInsetsInfo";
    }

    @Override // com.samsung.remotespen.util.reflections.AbstractBaseReflection
    public void loadStaticFields() {
        this.TOUCHABLE_INSETS_REGION = getIntStaticValue("TOUCHABLE_INSETS_REGION");
    }

    public void setTouchableInsets(Object obj, int i) {
        invokeNormalMethod(obj, "setTouchableInsets", new Class[]{Integer.TYPE}, Integer.valueOf(i));
    }

    public void setTouchableRegion(Object obj, int i) {
        setNormalValue(obj, "touchableRegion", Integer.valueOf(i));
    }

    public Region getTouchableRegion(Object obj) {
        Object normalValue = getNormalValue(obj, "touchableRegion");
        if (normalValue == null) {
            return null;
        }
        return (Region) normalValue;
    }

    public void visibleInsetsSetEmpty(Object obj) {
        Rect rect = (Rect) getNormalValue(obj, "visibleInsets");
        if (rect != null) {
            rect.setEmpty();
        }
    }

    public void contentInsetsSetEmpty(Object obj) {
        Rect rect = (Rect) getNormalValue(obj, "contentInsets");
        if (rect != null) {
            rect.setEmpty();
        }
    }
}
