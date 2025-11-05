package com.samsung.remotespen.core.penaction.manager;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.R;

/* compiled from: PenActionRuleManager.java */
/* loaded from: classes.dex */
class DefaultRuleEnableStateManager {
    private String TAG = DefaultRuleEnableStateManager.class.getSimpleName();
    private Context mContext;
    private String[] mQualifiedAppPkgList;

    public DefaultRuleEnableStateManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mQualifiedAppPkgList = applicationContext.getResources().getStringArray(R.array.remotespen_default_rule_enabled_package_list);
    }

    public boolean shouldEnableRuleByDefault(String str) {
        if (str == null) {
            Log.e(this.TAG, "shouldEnableRuleByDefault : pkg name is null");
            return false;
        } else if (this.mQualifiedAppPkgList == null) {
            Log.e(this.TAG, "shouldEnableRuleByDefault : qualified app list is null");
            return false;
        } else {
            int i = 0;
            while (true) {
                String[] strArr = this.mQualifiedAppPkgList;
                if (i >= strArr.length) {
                    return false;
                }
                if (str.equals(strArr[i])) {
                    return true;
                }
                i++;
            }
        }
    }
}
