package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;

/* loaded from: classes.dex */
public class SpenAdvertiseMonitor {
    private static final String TAG = "SpenAdvertiseMonitor";

    public static boolean canSpenAdvertise(Context context, SpenModelName spenModelName) {
        BleSpenDeviceFeature deviceFeature = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature();
        if (deviceFeature.isSupportEasyConnect()) {
            return true;
        }
        if (deviceFeature.isSupportWacomCharger()) {
            if (CommonUtils.isExhibitionMode(context)) {
                Log.d(TAG, "canSpenAdvertise : exhibition mode");
                return true;
            } else if (deviceFeature.isSupportStandbyMode() && !SettingsValueManager.getInstance(context).isKeepConnectedEnabled()) {
                Log.d(TAG, "canSpenAdvertise : standby mode");
                return true;
            } else {
                return SpenInsertionEventDetector.getInstance(context).isInserted();
            }
        }
        return false;
    }
}
