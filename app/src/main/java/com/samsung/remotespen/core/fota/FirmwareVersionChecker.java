package com.samsung.remotespen.core.fota;

import android.os.SystemClock;
import android.util.Log;

import com.samsung.util.AppVersion;
import com.samsung.util.features.SpenModelName;
import java.util.HashMap;

/* loaded from: classes.dex */
public class FirmwareVersionChecker {
    private static String FIRMWARE_UPDATE_AVAILABLE_MIN_VERSION = "0.100.0";
    private static long FIRMWARE_VERSION_CHECK_INTERVAL = 21600000;
    private static final String TAG = "FirmwareVersionChecker";
    private FinishListener mFinishListener;
    private SpenModelName mSpenModelName;
    private static final String FIRMWARE_UPDATE_AVAILABLE_MIN_VERSION_FOR_BUNDLED_SPEN = String.valueOf(51);
    private static HashMap<SpenModelName, FirmwareInfo> mFirmwareInfoHashMap = new HashMap<>();

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(boolean z, FirmwareInfo firmwareInfo);
    }

    public FirmwareVersionChecker(SpenModelName spenModelName) {
        this.mSpenModelName = spenModelName;
    }

    public void checkFirmwareUpgradeAvailable(boolean z, String str, FinishListener finishListener) {
        if (finishListener == null) {
            Log.e(TAG, "checkFirmwareUpgradeAvailable: finishListener is null");
            return;
        }
        FirmwareInfo firmwareInfoFromHashMap = getFirmwareInfoFromHashMap(this.mSpenModelName);
        if (z && firmwareInfoFromHashMap != null && !isExpiredFirmwareInfo(firmwareInfoFromHashMap)) {
            Log.i(TAG, "checkFirmwareUpgradeAvailable : use cached firmware info.");
            finishListener.onFinish(true, firmwareInfoFromHashMap);
            return;
        }
        String str2 = TAG;
        Log.i(str2, "checkFirmwareUpgradeAvailable : " + this.mSpenModelName + ", firmwareVersion= " + str);
        this.mFinishListener = finishListener;
        new FirmwareInfoWebDownloader(this.mSpenModelName, str).download(this.mFinishListener);
    }

    public boolean isNewerFirmware(String str, String str2) {
        String str3 = TAG;
        Log.i(str3, "isNewerFirmware : newVersion = " + str + ", baseVersion = " + str2);
        AppVersion create = AppVersion.create(str2);
        return !isUpgradeUnsupportedVersion(create) && create.compareTo(str) == -1;
    }

    /* renamed from: com.samsung.remotespen.core.fota.FirmwareVersionChecker$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName;

        static {
            int[] iArr = new int[SpenModelName.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName = iArr;
            try {
                iArr[SpenModelName.EXT1.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.CANVAS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.RAINBOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private boolean isUpgradeUnsupportedVersion(AppVersion appVersion) {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[this.mSpenModelName.ordinal()];
        return i != 1 ? (i == 2 || i == 3) && appVersion.compareTo(FIRMWARE_UPDATE_AVAILABLE_MIN_VERSION_FOR_BUNDLED_SPEN) == -1 : appVersion.compareTo(FIRMWARE_UPDATE_AVAILABLE_MIN_VERSION) == -1;
    }

    public static void clearFirmwareInfoToHashMap() {
        mFirmwareInfoHashMap.clear();
    }

    public static void putFirmwareInfoToHashMap(SpenModelName spenModelName, FirmwareInfo firmwareInfo) {
        mFirmwareInfoHashMap.put(spenModelName, firmwareInfo);
    }

    public static FirmwareInfo getFirmwareInfoFromHashMap(SpenModelName spenModelName) {
        return mFirmwareInfoHashMap.get(spenModelName);
    }

    private boolean isExpiredFirmwareInfo(FirmwareInfo firmwareInfo) {
        return SystemClock.elapsedRealtime() - firmwareInfo.getDownloadTimestamp() >= FIRMWARE_VERSION_CHECK_INTERVAL;
    }
}
