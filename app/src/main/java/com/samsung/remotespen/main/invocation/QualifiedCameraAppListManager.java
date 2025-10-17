package com.samsung.remotespen.main.invocation;

import android.content.Context;
import android.text.TextUtils;
import com.samsung.remotespen.core.penaction.manager.PenActionRule;
import com.samsung.util.debug.Assert;
import java.util.HashSet;

/* loaded from: classes.dex */
public class QualifiedCameraAppListManager {
    private static final String TAG = "QualifiedCameraAppListManager";
    private static QualifiedCameraAppListManager sInstance;
    private HashSet<String> mQualifiedCameraAppPkgList;

    public static QualifiedCameraAppListManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QualifiedCameraAppListManager(context);
        }
        return sInstance;
    }

    private QualifiedCameraAppListManager(Context context) {
        this.mQualifiedCameraAppPkgList = loadAppPackageList(context, R.array.remotespen_qualifed_camera_app_list);
    }

    public boolean isUnqualifiedCameraAppCase(PenActionRule penActionRule, String str) {
        return (penActionRule == null || penActionRule.getActionType() != PenActionRule.Type.CAMERA_CONTROL || isQualifiedApp(str)) ? false : true;
    }

    public boolean isQualifiedApp(String str) {
        if (str == null) {
            return false;
        }
        if (str.startsWith("com.samsung.")) {
            return true;
        }
        return this.mQualifiedCameraAppPkgList.contains(str);
    }

    private HashSet<String> loadAppPackageList(Context context, int i) {
        String[] stringArray = context.getResources().getStringArray(i);
        HashSet<String> hashSet = new HashSet<>();
        for (String str : stringArray) {
            Assert.e(!TextUtils.isEmpty(str), "Camera app package name is empty");
            Assert.e(!hashSet.contains(str), "Already registered pkg - " + str);
            hashSet.add(str);
        }
        return hashSet;
    }
}
