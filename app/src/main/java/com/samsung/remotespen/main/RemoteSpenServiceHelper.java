package com.samsung.remotespen.main;

import android.content.Context;
import com.samsung.remotespen.RemoteSpenService;

/* loaded from: classes.dex */
public class RemoteSpenServiceHelper {
    public static boolean isServiceRunning() {
        return RemoteSpenService.isServiceRunning();
    }

    public static boolean startService(Context context) {
        return RemoteSpenService.startService(context);
    }

    public static boolean startService(Context context, boolean z) {
        return RemoteSpenService.startService(context, z);
    }

    public static boolean isRemoteSpenMainControllerReady() {
        return RemoteSpenService.isRemoteSpenMainControllerReady();
    }
}
