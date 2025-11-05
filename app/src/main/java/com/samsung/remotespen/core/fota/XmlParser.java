package com.samsung.remotespen.core.fota;

import android.os.SystemClock;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.fota.FirmwareInfo;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/* compiled from: FirmwareVersionChecker.java */
/* loaded from: classes.dex */
class XmlParser {
    private static final String TAG = "XmlParser";
    private static final String XML_TAG_DESCRIPTION = "Description";
    private static final String XML_TAG_DESCRIPTION_KOR = "DescriptionKor";
    private static final String XML_TAG_DOWNLOAD_URL = "DownloadURL";
    private static final String XML_TAG_FW_VERSION = "FWVersion";

    public static FirmwareInfo parseXml(Reader reader) {
        HashMap hashMap = new HashMap();
        try {
            XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setInput(reader);
            int next = newPullParser.next();
            String str = Constants.packageName.NONE;
            String str2 = null;
            while (next != 1) {
                if (next == 2) {
                    String name = newPullParser.getName();
                    if (newPullParser.next() != 4) {
                        next = newPullParser.next();
                    } else {
                        char c = 65535;
                        switch (name.hashCode()) {
                            case -554557934:
                                if (name.equals(XML_TAG_DESCRIPTION_KOR)) {
                                    c = 3;
                                    break;
                                }
                                break;
                            case -56677412:
                                if (name.equals(XML_TAG_DESCRIPTION)) {
                                    c = 2;
                                    break;
                                }
                                break;
                            case 456719271:
                                if (name.equals(XML_TAG_DOWNLOAD_URL)) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case 1349144263:
                                if (name.equals(XML_TAG_FW_VERSION)) {
                                    c = 0;
                                    break;
                                }
                                break;
                        }
                        if (c == 0) {
                            str = newPullParser.getText();
                        } else if (c == 1) {
                            str2 = newPullParser.getText();
                        } else if (c == 2) {
                            parseDescription(newPullParser.getText(), hashMap);
                        } else if (c == 3) {
                            FirmwareInfo.DescriptionInfo descriptionInfo = new FirmwareInfo.DescriptionInfo("KOR", newPullParser.getText());
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(descriptionInfo);
                            hashMap.put("KOR", arrayList);
                        }
                    }
                }
                next = newPullParser.next();
            }
            return new FirmwareInfo(str, str2, hashMap, SystemClock.elapsedRealtime());
        } catch (Exception e) {
            Log.e(TAG, "parseXml : e = " + e);
            return null;
        }
    }

    private static void parseDescription(String str, HashMap<String, ArrayList<FirmwareInfo.DescriptionInfo>> hashMap) {
        String[] split = str.split("</[A-Z]+>|</[A-Z]+\\-[A-Z]+>");
        Pattern compile = Pattern.compile("<[A-Z]+>|<[A-Z]+\\-[A-Z]+>");
        Pattern compile2 = Pattern.compile("[A-Z]+");
        Pattern compile3 = Pattern.compile("\\-[A-Z]+");
        for (String str2 : split) {
            Matcher matcher = compile.matcher(str2);
            if (matcher.find()) {
                String group = matcher.group();
                Matcher matcher2 = compile2.matcher(group);
                if (matcher2.find()) {
                    String group2 = matcher2.group();
                    Matcher matcher3 = compile3.matcher(group);
                    String replace = matcher3.find() ? matcher3.group().replace("-", Constants.packageName.NONE) : Constants.packageName.NONE;
                    String substring = str2.substring(matcher.end());
                    substring.replaceAll("\r|\n", Constants.packageName.NONE);
                    FirmwareInfo.DescriptionInfo descriptionInfo = new FirmwareInfo.DescriptionInfo(group2, replace, substring);
                    ArrayList<FirmwareInfo.DescriptionInfo> arrayList = hashMap.get(group2);
                    if (arrayList == null) {
                        arrayList = new ArrayList<>();
                    }
                    arrayList.add(descriptionInfo);
                    hashMap.put(group2, arrayList);
                }
            }
        }
    }
}
