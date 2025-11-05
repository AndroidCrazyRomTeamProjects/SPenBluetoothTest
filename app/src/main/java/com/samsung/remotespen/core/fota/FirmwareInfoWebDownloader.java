package com.samsung.remotespen.core.fota;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.fota.FirmwareVersionChecker;
import com.samsung.util.features.SpenModelName;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: FirmwareVersionChecker.java */
/* loaded from: classes.dex */
public class FirmwareInfoWebDownloader extends FileDownloader {
    private static final String FIRMWARE_INFO_SERVER_URL = "https://wsu-dms.samsungdm.com/common/support/firmware/downloadUrlList.do?loc=global&prd_mdl_name=";
    private static final String TAG = "FirmwareInfoWebDownloader";
    private static final String TARGET_PATH_FIRMWARE_FOTA_TEST;
    private static final String TARGET_PATH_GO_TO_ANDROMEDA_TEST;
    private static final HashMap<SpenModelName, String> mServerUrlModelName;
    private FirmwareVersionChecker.FinishListener mFinishListener;
    private String mFirmwareVersion;
    private SpenModelName mSpenModelName;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getPath());
        String str = File.separator;
        sb.append(str);
        sb.append("spen_firmware_fota.test");
        TARGET_PATH_FIRMWARE_FOTA_TEST = sb.toString();
        TARGET_PATH_GO_TO_ANDROMEDA_TEST = Environment.getExternalStorageDirectory().getPath() + str + "go_to_andromeda.test";
        HashMap<SpenModelName, String> hashMap = new HashMap<>();
        mServerUrlModelName = hashMap;
        hashMap.put(SpenModelName.EXT1, "EJ-P5450");
        hashMap.put(SpenModelName.CANVAS, "RB-SPEN");
        hashMap.put(SpenModelName.RAINBOW, "RB-SPEN");
    }

    public FirmwareInfoWebDownloader(SpenModelName spenModelName, String str) {
        this.mSpenModelName = spenModelName;
        this.mFirmwareVersion = str;
    }

    public void download(FirmwareVersionChecker.FinishListener finishListener) {
        this.mFinishListener = finishListener;
        startDownload();
    }

    private String getFirmwareInfoServerUrl(SpenModelName spenModelName, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.packageName.NONE);
        sb.append(isTestModeEnabled() ? "TEST-" : Constants.packageName.NONE);
        String str2 = sb.toString() + mServerUrlModelName.get(spenModelName);
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[spenModelName.ordinal()];
        if (i != 1) {
            if (i != 2 && i != 3) {
                return null;
            }
            if (Integer.parseInt(str) >= 129) {
                str2 = str2 + "-DM";
            } else if (Integer.parseInt(str) >= 64 && Integer.parseInt(str) < 129) {
                str2 = str2 + "-531";
            } else {
                str2 = str2 + Constants.packageName.NONE;
            }
        }
        Log.i(TAG, "getFirmwareInfoServerUrl : " + spenModelName + ", " + str2);
        return FIRMWARE_INFO_SERVER_URL + str2;
    }

    /* compiled from: FirmwareVersionChecker.java */
    /* renamed from: com.samsung.remotespen.core.fota.FirmwareInfoWebDownloader$1  reason: invalid class name */
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

    private boolean isTestModeEnabled() {
        return new File(TARGET_PATH_FIRMWARE_FOTA_TEST).exists() || new File(TARGET_PATH_GO_TO_ANDROMEDA_TEST).exists();
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public String getDownloadURL() {
        return getFirmwareInfoServerUrl(this.mSpenModelName, this.mFirmwareVersion);
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void connectionSuccess(HttpsURLConnection httpsURLConnection) {
        try {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(httpsURLConnection.getInputStream(), Charset.forName("UTF-8"));
                try {
                    FirmwareInfo parseXml = XmlParser.parseXml(inputStreamReader);
                    inputStreamReader.close();
                    FirmwareVersionChecker.putFirmwareInfoToHashMap(this.mSpenModelName, parseXml);
                    this.mFinishListener.onFinish(true, parseXml);
                } catch (Throwable th) {
                    try {
                        inputStreamReader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "connectionSuccess : e = " + e);
                this.mFinishListener.onFinish(false, getEmptyFirmwareInfo());
            }
        } catch (Throwable th3) {
            if (1 != 0) {
                FirmwareVersionChecker.putFirmwareInfoToHashMap(this.mSpenModelName, null);
            }
            this.mFinishListener.onFinish(true, null);
            throw th3;
        }
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void connectionFail() {
        Log.i(TAG, "connectionFail: HttpsURLConnection response is fail");
        this.mFinishListener.onFinish(false, getEmptyFirmwareInfo());
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void exception(Exception exc) {
        String str = TAG;
        Log.e(str, "exception : e = " + exc);
        this.mFinishListener.onFinish(false, getEmptyFirmwareInfo());
    }

    private FirmwareInfo getEmptyFirmwareInfo() {
        return new FirmwareInfo(Constants.packageName.NONE, Constants.packageName.NONE, null, SystemClock.elapsedRealtime());
    }
}
