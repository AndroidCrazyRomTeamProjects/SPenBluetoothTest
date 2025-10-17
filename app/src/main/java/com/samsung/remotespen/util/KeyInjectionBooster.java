package com.samsung.remotespen.util;

import android.content.Context;
import android.os.SemSystemProperties;
import com.samsung.android.os.SemDvfsManager;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class KeyInjectionBooster {
    private static final String CHIP_NAME_EXYNOS = "exynos";
    private static final String TAG = "KeyInjectionBooster";
    private SemDvfsManager mCpuMinBooster;

    public KeyInjectionBooster(Context context) {
        prepareBooster(context);
    }

    private void prepareBooster(Context context) {
        if (!isExynosChipset()) {
            Log.i(TAG, "prepareBooster : It is not Exynos. need not to prepare KeyInjection booster");
        } else {
            this.mCpuMinBooster = SemDvfsManager.createInstance(context, "AIR_COMMAND", 21);
        }
    }

    private boolean isExynosChipset() {
        String lowerCase = SemSystemProperties.get("ro.hardware.chipname").toLowerCase();
        String str = TAG;
        Log.i(str, "prepareBooster : ChipName is " + lowerCase);
        return lowerCase != null && lowerCase.startsWith(CHIP_NAME_EXYNOS);
    }

    public void start() {
        if (this.mCpuMinBooster == null) {
            return;
        }
        Log.i(TAG, "start : CpuMinBooster acquire");
        this.mCpuMinBooster.acquire();
    }

    public void stop() {
        if (this.mCpuMinBooster == null) {
            return;
        }
        Log.i(TAG, "stop : CpuMinBooster release");
        this.mCpuMinBooster.release();
    }
}
