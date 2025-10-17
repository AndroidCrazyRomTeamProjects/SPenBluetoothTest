package com.samsung.remotespen.core.device.control.detector;

import android.content.Context;
import com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector;
import com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasGestureDetector;
import com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciGestureDetector;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.util.debug.Log;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class GestureDetectorFactory {
    private static final String TAG = "GestureDetectorFactory";

    public static AbstractGestureDetector getGestureDetector(Context context, SpenModelName spenModelName) {
        AbstractGestureDetector davinciGestureDetector;
        if (BleSpenDeviceFactory.getInstance(spenModelName).getApplicationFeature().isSupportGlobalGestureAction()) {
            davinciGestureDetector = new CanvasGestureDetector();
        } else {
            davinciGestureDetector = new DavinciGestureDetector();
        }
        String str = TAG;
        Log.d(str, "getGestureDetector : gestureDetector = " + davinciGestureDetector.getClass().getSimpleName());
        return davinciGestureDetector;
    }
}
