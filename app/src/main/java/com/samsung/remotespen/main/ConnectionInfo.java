package com.samsung.remotespen.main;

import android.os.Process;
import com.samsung.remotespen.core.device.BleSpenManager;
import java.text.SimpleDateFormat;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleConnectionHistoryManager.java */
/* loaded from: classes.dex */
public class ConnectionInfo {
    private String mAddress;
    private int mBatteryLevel;
    private BleSpenManager.StateChangeInfo mDisconnectInfo;
    private String mFirmwareVersion;
    private Boolean mPenInsertedWhenDisconnect;
    private String mPenName;
    private long mConnectingTime = -1;
    private long mConnectedTime = -1;
    private long mDisconnectedTime = -1;

    public long getConnectingTime() {
        return this.mConnectingTime;
    }

    public long getConnectedTime() {
        return this.mConnectedTime;
    }

    public long getDisconnectedTime() {
        return this.mDisconnectedTime;
    }

    public void markConnecting() {
        this.mConnectingTime = System.currentTimeMillis();
    }

    public void markConnected(String str, String str2, String str3) {
        this.mAddress = str;
        this.mPenName = str3;
        this.mFirmwareVersion = str2;
        this.mBatteryLevel = -1;
        this.mConnectedTime = System.currentTimeMillis();
        this.mDisconnectedTime = -1L;
    }

    public void markDisconnected(BleSpenManager.StateChangeInfo stateChangeInfo, boolean z) {
        this.mDisconnectedTime = System.currentTimeMillis();
        this.mDisconnectInfo = stateChangeInfo;
        this.mPenInsertedWhenDisconnect = Boolean.valueOf(z);
    }

    public void setBatteryLevel(int i) {
        this.mBatteryLevel = i;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PID=%5d", Integer.valueOf(Process.myPid())));
        sb.append(getFormattedNameAndValueString(" Addr", this.mAddress, 17));
        sb.append(getFormattedNameAndValueString(" FW", this.mFirmwareVersion, 4));
        sb.append(getFormattedNameAndValueString(" Batt", Integer.valueOf(this.mBatteryLevel), 3));
        sb.append(getFormattedNameAndValueString(" ConnStart", getTimeString(this.mConnectingTime), 14));
        sb.append(getFormattedNameAndValueString(" Connected", getTimeString(this.mConnectedTime), 14));
        sb.append(getFormattedNameAndValueString(" Disconn", getTimeString(this.mDisconnectedTime), 14));
        long j = this.mDisconnectedTime;
        if (j > 0) {
            long j2 = this.mConnectedTime;
            if (j2 > 0) {
                sb.append(getFormattedNameAndValueString(" ConnDur", ((j - j2) / 1000) + "s", 6));
            }
        }
        Boolean bool = this.mPenInsertedWhenDisconnect;
        if (bool != null) {
            sb.append(getFormattedNameAndValueString(" penInserted", bool, 5));
        }
        if (this.mPenName != null) {
            sb.append(" penName=" + this.mPenName);
        }
        if (this.mDisconnectInfo != null) {
            sb.append(" disconnInfo=" + this.mDisconnectInfo.toString());
        }
        return sb.toString();
    }

    private String getTimeString(long j) {
        return j <= 0 ? "N/A" : new SimpleDateFormat("MM-dd HH:mm:ss").format(Long.valueOf(j));
    }

    private String getFormattedNameAndValueString(String str, Object obj, int i) {
        String obj2 = obj != null ? obj.toString() : "null";
        return String.format("%s=%-" + i + "s", str, obj2);
    }
}
