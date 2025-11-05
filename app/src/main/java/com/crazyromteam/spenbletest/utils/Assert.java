package com.crazyromteam.spenbletest.utils;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

/**
 * Ported from Samsung Air Command source Assert utility.
 */
public final class Assert {
    private static final String TAG = "Assert";
    private static final boolean SHOULD_FORCE_CLOSE_WHEN_FAILED = !isShippingBinary();

    private Assert() {
        // Utility class
    }

    public static class AssertionFailedException extends IllegalStateException {
        public AssertionFailedException(String message) {
            super(message);
        }
    }

    public static void e(boolean expression) {
        e(expression, null);
    }

    public static void e(boolean expression, String message) {
        if (!expression) {
            assertionFailed(message);
        }
    }

    public static void fail() {
        e(false, null);
    }

    public static void fail(String message) {
        e(false, message);
    }

    public static void notNull(Object obj) {
        if (obj == null) {
            assertionFailed("The object is null!");
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            assertionFailed("The object is null! - " + message);
        }
    }

    public static void ensureMainThread() {
        int myTid = Process.myTid();
        boolean isMainThread = Process.myPid() == myTid;
        e(isMainThread, myTid + " is not main thread!");
    }

    public static void ensureWorkerThread() {
        int myTid = Process.myTid();
        boolean isWorkerThread = Process.myPid() != myTid;
        e(isWorkerThread, myTid + " is not worker thread!");
    }

    private static void assertionFailed(String message) {
        AssertionFailedException exception = new AssertionFailedException(message);
        if (message == null) {
            Log.e(TAG, "FATAL : Assertion failed", exception);
        } else {
            Log.e(TAG, "FATAL : Assertion failed : " + message, exception);
        }
        if (SHOULD_FORCE_CLOSE_WHEN_FAILED) {
            throw exception;
        }
    }

    private static boolean isShippingBinary() {
        String value = getSystemProperty("ro.product_ship", null);
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        String trimmedValue = value.trim();
        return "TRUE".equalsIgnoreCase(trimmedValue) || "1".equals(trimmedValue);
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            return (String) systemProperties
                    .getMethod("get", String.class, String.class)
                    .invoke(null, key, defaultValue);
        } catch (Exception e) {
            Log.w(TAG, "Unable to read system property " + key, e);
            return defaultValue;
        }
    }
}
