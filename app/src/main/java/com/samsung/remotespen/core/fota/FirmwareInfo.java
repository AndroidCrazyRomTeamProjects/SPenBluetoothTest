package com.samsung.remotespen.core.fota;

import android.text.TextUtils;
import com.samsung.aboutpage.Constants;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: classes.dex */
public class FirmwareInfo {
    private static final String PRIMARY_DEFAULT_LOCALE = "ENG";
    private static final String SECONDARY_DEFAULT_LOCALE = "KOR";
    private HashMap<String, ArrayList<DescriptionInfo>> mDescription;
    private long mDownloadTimestamp;
    private String mDownloadUrl;
    private String mFirmwareVersion;

    /* loaded from: classes.dex */
    public static class DescriptionInfo {
        public String description;
        public String locale;
        public String region;

        public DescriptionInfo(String str, String str2) {
            this(str, Constants.packageName.NONE, str2);
        }

        public DescriptionInfo(String str, String str2, String str3) {
            this.locale = str;
            this.region = str2;
            this.description = str3;
        }
    }

    public FirmwareInfo(String str, String str2, HashMap<String, ArrayList<DescriptionInfo>> hashMap, long j) {
        this.mFirmwareVersion = str;
        this.mDownloadUrl = str2;
        this.mDescription = hashMap;
        this.mDownloadTimestamp = j;
    }

    public String getFirmwareVersion() {
        return this.mFirmwareVersion;
    }

    public String getDescription(String str, String str2) {
        HashMap<String, ArrayList<DescriptionInfo>> hashMap = this.mDescription;
        if (hashMap == null) {
            return Constants.packageName.NONE;
        }
        ArrayList<DescriptionInfo> arrayList = hashMap.get(str);
        if (arrayList == null || arrayList.size() == 0) {
            String defaultLocaleDescription = getDefaultLocaleDescription(PRIMARY_DEFAULT_LOCALE);
            return TextUtils.isEmpty(defaultLocaleDescription) ? getDefaultLocaleDescription(SECONDARY_DEFAULT_LOCALE) : defaultLocaleDescription;
        }
        int size = arrayList.size();
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= size) {
                break;
            } else if (arrayList.get(i2).region.equals(str2)) {
                i = i2;
                break;
            } else {
                i2++;
            }
        }
        return arrayList.get(i).description;
    }

    private String getDefaultLocaleDescription(String str) {
        return (this.mDescription.get(str) != null && this.mDescription.get(str).size() > 0) ? this.mDescription.get(str).get(0).description : Constants.packageName.NONE;
    }

    public String getDownloadUrl() {
        return this.mDownloadUrl;
    }

    public long getDownloadTimestamp() {
        return this.mDownloadTimestamp;
    }
}
