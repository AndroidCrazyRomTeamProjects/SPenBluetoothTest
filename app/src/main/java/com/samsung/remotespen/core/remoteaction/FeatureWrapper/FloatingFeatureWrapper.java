package com.samsung.remotespen.core.remoteaction.FeatureWrapper;

import android.util.Log;

import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.aboutpage.Constants;

/* loaded from: classes.dex */
public class FloatingFeatureWrapper implements FeatureWrapper {
    public static final String TAG = "FloatingFeatureWrapper";

    @Override // com.samsung.remotespen.core.remoteaction.FeatureWrapper.FeatureWrapper
    public String getString(String str) {
        try {
            return SemFloatingFeature.getInstance().getString(str);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "getString : e=" + e);
            return Constants.packageName.NONE;
        }
    }
}
