package com.samsung.remotespen.core.remoteaction.util;

import android.text.TextUtils;
import com.samsung.remotespen.core.remoteaction.Action;

/* loaded from: classes.dex */
public class RemoteActionUtil {
    public static Action.KeyShortcut getKeyShortcut(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] split = str.split("\\+");
        if (split.length == 0) {
            return null;
        }
        Action.KeyShortcut keyShortcut = new Action.KeyShortcut();
        for (String str2 : split) {
            keyShortcut.addKey(str2);
        }
        return keyShortcut;
    }
}
