package com.samsung.remotespen.core.device.control.connection.advertisement;

import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class AdvertisementData {
    public int deviceId;
    public byte deviceType;
    public boolean dismissPopup;
    public boolean doNotShowPopup;
    public boolean isAccountMatched;
    public boolean isConnected;
    public boolean isDeviceMatched;
    public byte sequenceNumber;
    public BleSpenFrequency spenFrequency;
    public SpenModelName spenModelName;
    public byte spenModelNameId;
    public boolean isEmptyDeviceHash = true;
    public boolean isEmptyAccountHash = true;
}
