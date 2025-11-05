package com.samsung.util;

import android.os.Build;

/* loaded from: classes.dex */
public class TestHelper {
    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }
}
