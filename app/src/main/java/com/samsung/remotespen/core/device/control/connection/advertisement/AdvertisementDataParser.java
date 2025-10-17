package com.samsung.remotespen.core.device.control.connection.advertisement;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.util.SecurityUtils;
import com.samsung.util.ViewHelper;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class AdvertisementDataParser {
    private static final String TAG = "AdvertisementDataParser";

    public AdvertisementData parseSpenAdvertisementData(Context context, byte[] bArr) {
        if (bArr.length < 20) {
            String str = TAG;
            Log.e(str, "parseSpenAdvertisementData : unexpected advertisement data len. len=" + bArr.length);
            return null;
        }
        AdvertisementData advertisementData = new AdvertisementData();
        advertisementData.deviceType = bArr[0];
        byte b = bArr[1];
        advertisementData.spenFrequency = (b & 32) != 0 ? BleSpenFrequency.FOLD : BleSpenFrequency.DEFAULT;
        advertisementData.doNotShowPopup = (b & 4) != 0;
        advertisementData.dismissPopup = (b & 2) != 0;
        advertisementData.isConnected = (b & 1) != 0;
        advertisementData.sequenceNumber = bArr[2];
        advertisementData.deviceId = ((bArr[3] & 255) << 8) | (bArr[4] & 255);
        byte b2 = (byte) (bArr[5] & 7);
        advertisementData.spenModelNameId = b2;
        advertisementData.spenModelName = getSpenModelNameById(b2);
        byte[] extractByteArray = extractByteArray(bArr, 6, 14);
        advertisementData.isEmptyDeviceHash = SecurityUtils.isDeviceEmpty(extractByteArray);
        advertisementData.isEmptyAccountHash = SecurityUtils.isAccountEmpty(extractByteArray);
        advertisementData.isDeviceMatched = SecurityUtils.isLastConnectedDevice(context, extractByteArray);
        advertisementData.isAccountMatched = SecurityUtils.isLastConnectedAccount(context, extractByteArray);
        dumpAdvertisementDataToLog(advertisementData);
        return advertisementData;
    }

    private void dumpAdvertisementDataToLog(AdvertisementData advertisementData) {
        String str = TAG;
        Log.d(str, "dumpAdvertisementDataToLog : , spenFrequency=" + advertisementData.spenFrequency + ", doNotShowPop=" + advertisementData.doNotShowPopup + ", dismissPopup=" + advertisementData.dismissPopup + ", isConn=" + advertisementData.isConnected + ", seq=" + getHexaString(advertisementData.sequenceNumber) + ", deviceId=" + getHexaString(advertisementData.deviceId) + ", model=" + getHexaString(advertisementData.spenModelNameId) + ViewHelper.QUALIFIER_DELIMITER + advertisementData.spenModelName + ", de=" + advertisementData.isEmptyDeviceHash + ", dm=" + advertisementData.isDeviceMatched + ", ae=" + advertisementData.isEmptyAccountHash + ", am=" + advertisementData.isAccountMatched);
    }

    private SpenModelName getSpenModelNameById(byte b) {
        if (b != 0) {
            return null;
        }
        return SpenModelName.EXT1;
    }

    private byte[] extractByteArray(byte[] bArr, int i, int i2) {
        byte[] bArr2 = new byte[i2];
        System.arraycopy(bArr, i, bArr2, 0, i2);
        return bArr2;
    }

    private String getHexaString(int i) {
        return String.format("0x%x", Integer.valueOf(i));
    }
}
