package com.samsung.remotespen.core.penaction.manager;

import android.util.Log;

import com.crazyromteam.spenbletest.utils.Assert;

/* loaded from: classes.dex */
public class RuleEnableManager {
    private static final String TAG = "RuleEnableManager";

    public static String getKey(String str) {
        return getKey(str, "F739B1A025DC84E6");
    }

    private static String getKey(String str, String str2) {
        Assert.e(str2.length() == 16);
        int length = str.length();
        char[] cArr = new char[4];
        for (int i = 0; i < length; i++) {
            int i2 = i % 4;
            cArr[i2] = (char) (cArr[i2] + str.charAt(i));
        }
        for (int i3 = 0; i3 < 4; i3++) {
            cArr[i3] = (char) (cArr[i3] & 255);
        }
        return charArrayToString(cArr, str2);
    }

    private static String charArrayToString(char[] cArr, String str) {
        if (cArr == null) {
            return null;
        }
        char[] charArray = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer(cArr.length);
        for (char c : cArr) {
            if (c > 255) {
                Log.e(TAG, "charArrayToString : contains too large value : " + Integer.valueOf(c));
                return null;
            }
            stringBuffer.append(charArray[(c >> 4) & 15]);
            stringBuffer.append(charArray[c & 15]);
        }
        return stringBuffer.toString();
    }
}
