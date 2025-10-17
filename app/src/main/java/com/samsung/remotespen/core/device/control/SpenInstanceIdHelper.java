package com.samsung.remotespen.core.device.control;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class SpenInstanceIdHelper {
    private static final String TAG = "SpenInstanceIdHelper";
    private Context mContext;
    private BleSpenInstanceId mInstanceId;

    public static SpenInstanceIdHelper from(Context context, BleSpenInstanceId bleSpenInstanceId) {
        if (bleSpenInstanceId == null) {
            return null;
        }
        return new SpenInstanceIdHelper(context, bleSpenInstanceId);
    }

    public static SpenInstanceIdHelper from(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return from(context, createInstanceId(context, str));
    }

    private SpenInstanceIdHelper(Context context, BleSpenInstanceId bleSpenInstanceId) {
        this.mContext = context.getApplicationContext();
        this.mInstanceId = bleSpenInstanceId;
    }

    public static BleSpenInstanceId createInstanceId(Context context, SpenModelName spenModelName, String str) {
        if (spenModelName == null) {
            String str2 = TAG;
            Log.e(str2, "createInstanceId : model name is null! address=" + str);
            return null;
        } else if (spenModelName.equals(ModelFeatures.getSpenModelName(context))) {
            if (!TextUtils.isEmpty(str)) {
                String str3 = TAG;
                Log.i(str3, "createInstanceId : Address is given in bundled SPen : " + spenModelName + " / " + str);
            }
            return new BundledSpenInstanceId(spenModelName);
        } else {
            Assert.notNull(str);
            return new UnbundledSpenInstanceId(spenModelName, str);
        }
    }

    public static BleSpenInstanceId createInstanceId(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "createInstanceId : empty uid string");
            return null;
        }
        String[] split = str.split(",");
        return createInstanceId(context, split.length >= 1 ? SpenModelName.fromPermanentName(split[0]) : null, split.length >= 2 ? split[1] : null);
    }

    public String getUidString() {
        BleSpenInstanceId bleSpenInstanceId = this.mInstanceId;
        if (bleSpenInstanceId instanceof BundledSpenInstanceId) {
            return ((BundledSpenInstanceId) bleSpenInstanceId).toUidString();
        }
        if (bleSpenInstanceId instanceof UnbundledSpenInstanceId) {
            return ((UnbundledSpenInstanceId) bleSpenInstanceId).toUidString();
        }
        return null;
    }

    public String getSpenAddress() {
        if (this.mInstanceId.isBundledSpen()) {
            return BleUtils.readBundledBleSpenAddressFromEfs(this.mContext);
        }
        return ((UnbundledSpenInstanceId) this.mInstanceId).getSpenAddress();
    }
}
