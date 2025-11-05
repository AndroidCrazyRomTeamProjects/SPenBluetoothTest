package com.samsung.remotespen.core.remoteaction.FeatureWrapper;

import android.util.Log;

import com.samsung.android.feature.SemCscFeature;
import com.samsung.aboutpage.Constants;

/* loaded from: classes.dex */
public class CscFeatureWrapper implements FeatureWrapper {
    public static final String TAG = "CscFeatureWrapper";

    @Override // com.samsung.remotespen.core.remoteaction.FeatureWrapper.FeatureWrapper
    public String getString(String str) {
        try {
            return SemCscFeature.getInstance().getString(str);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "getString : e=" + e);
            return Constants.packageName.NONE;
        }
    }
}
