package com.samsung.remotespen.util;

import java.text.NumberFormat;

/* loaded from: classes.dex */
public class BatteryPolicy {
    public static final int LOWER_WORKABLE_TEMPERATURE = -14;
    public static final int SPEN_BATTERY_LEVEL_MAX = 100;
    public static final int SPEN_BATTERY_LEVEL_MIN = 1;
    public static final int SPEN_LOW_BATTERY_THRESHOLD_BUNDLED = 10;
    public static final int SPEN_LOW_BATTERY_THRESHOLD_UNBUNDLED = 10;
    public static final int UPPER_WORKABLE_TEMPERATURE = 54;

    public static int getLowerWorkableTemperature() {
        return -14;
    }

    public static int getUpperWorkableTemperature() {
        return 54;
    }

    public static boolean isLessThanLowBatteryThreshold(int i, boolean z) {
        if (z) {
            if (i >= 0 && i <= 10) {
                return true;
            }
        } else if (i >= 0 && i <= 10) {
            return true;
        }
        return false;
    }

    public static boolean isWorkableTemperature(int i) {
        return -14 < i && i < 54;
    }

    public static String getPercentageString(int i) {
        return NumberFormat.getPercentInstance().format(Math.max(i, 1) / 100.0f);
    }
}
