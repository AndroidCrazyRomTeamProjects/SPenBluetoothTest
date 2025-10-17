package com.samsung.remotespen.core.device.control.detector.gesture;

/* loaded from: classes.dex */
public class Utils {
    private static final double M_PI = 3.141592d;

    public static float getDistance(float f, float f2) {
        return (float) Math.sqrt((f * f) + (f2 * f2));
    }

    public static int getAngle3p(float f, float f2, float f3, float f4) {
        return (int) ((Math.acos(((f * f3) + (f2 * f4)) / (((float) Math.sqrt((f * f) + (f2 * f2))) * ((float) Math.sqrt((f3 * f3) + (f4 * f4))))) * 180.0d) / M_PI);
    }

    public static int getShape(int[] iArr) {
        return iArr[0] & 15;
    }

    public static int getAngle(int[] iArr) {
        return iArr[1];
    }
}
