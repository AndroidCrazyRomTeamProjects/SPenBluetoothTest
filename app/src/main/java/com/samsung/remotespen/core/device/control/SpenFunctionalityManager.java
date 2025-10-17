package com.samsung.remotespen.core.device.control;

import android.content.Context;
import com.samsung.remotespen.core.device.control.factory.BleSpenApplicationFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;
import com.samsung.util.settings.SpenUsageDetectionManager;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class SpenFunctionalityManager {
    private static SpenFunctionalityManager sInstance;
    private Context mContext;
    private BleSpenPairedSpenManager mPairedSpenManager;

    public static synchronized SpenFunctionalityManager getInstance(Context context) {
        SpenFunctionalityManager spenFunctionalityManager;
        synchronized (SpenFunctionalityManager.class) {
            if (sInstance == null) {
                sInstance = new SpenFunctionalityManager(context);
            }
            spenFunctionalityManager = sInstance;
        }
        return spenFunctionalityManager;
    }

    private SpenFunctionalityManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mPairedSpenManager = BleSpenPairedSpenManager.getInstance(applicationContext);
    }

    public static boolean isProvideBundleSpen(Context context) {
        return ModelFeatures.isProvideBundleSpen(context);
    }

    public boolean isAnyPenSupportAirAction() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenApplicationFeature applicationFeature = BleSpenDeviceFactory.getInstance(it.next()).getApplicationFeature();
                if (applicationFeature != null && applicationFeature.isSupportAirAction()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private ArrayList<SpenModelName> getAvailableSpenModelNames() {
        ArrayList<SpenModelName> arrayList = new ArrayList<>();
        Iterator<BleSpenInstanceId> it = this.mPairedSpenManager.getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            SpenModelName spenModelName = it.next().getSpenModelName();
            if (!arrayList.contains(spenModelName)) {
                arrayList.add(spenModelName);
            }
        }
        return arrayList;
    }

    public boolean isAnyPenSupportAirMouse() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenApplicationFeature applicationFeature = BleSpenDeviceFactory.getInstance(it.next()).getApplicationFeature();
                if (applicationFeature != null && applicationFeature.isSupportAirMouse()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isAnyPenSupportAppGesture() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenApplicationFeature applicationFeature = BleSpenDeviceFactory.getInstance(it.next()).getApplicationFeature();
                if (applicationFeature != null && applicationFeature.isSupportAppGestureAction()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isAnyPenSupportGlobalGesture() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenApplicationFeature applicationFeature = BleSpenDeviceFactory.getInstance(it.next()).getApplicationFeature();
                if (applicationFeature != null && applicationFeature.isSupportGlobalGestureAction()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isAnyPenSupportSecondaryButton() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenDeviceFeature deviceFeature = BleSpenDeviceFactory.getInstance(it.next()).getDeviceFeature();
                if (deviceFeature != null && deviceFeature.isSupportSecondaryButton()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isAnyPenSupportBlindCharge() {
        if (isSpenDigitizerEnabled()) {
            Iterator<SpenModelName> it = getAvailableSpenModelNames().iterator();
            while (it.hasNext()) {
                BleSpenApplicationFeature applicationFeature = BleSpenDeviceFactory.getInstance(it.next()).getApplicationFeature();
                if (applicationFeature != null && applicationFeature.isSupportBlindCharge()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean isSpenDigitizerEnabled() {
        return SettingsValueManager.getInstance(this.mContext).isSpenDigitizerEnabled();
    }

    public boolean isSpenUsageDetected() {
        return SpenUsageDetectionManager.getInstance(this.mContext).isSpenUsageDetected();
    }

    public boolean isUnbundledSpenPairingDetected() {
        return this.mPairedSpenManager.isUnbundledSpenPairingDetected();
    }

    public boolean isSupportGlobalGesture(SpenModelName spenModelName) {
        return BleSpenDeviceFactory.getInstance(spenModelName).getApplicationFeature().isSupportGlobalGestureAction();
    }

    public boolean isAnyPenSupportFmm() {
        Iterator<BleSpenInstanceId> it = BleSpenPairedSpenManager.getInstance(this.mContext).getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            if (BleSpenDeviceFactory.getInstance(it.next().getSpenModelName()).getApplicationFeature().isSupportFmm()) {
                return true;
            }
        }
        return false;
    }
}
