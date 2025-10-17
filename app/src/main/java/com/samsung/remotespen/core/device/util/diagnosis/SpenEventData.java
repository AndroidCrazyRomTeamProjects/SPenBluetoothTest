package com.samsung.remotespen.core.device.util.diagnosis;

import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DiagnosisManager.java */
/* loaded from: classes.dex */
public class SpenEventData {
    public SpenEventType mEventType;
    public long mTimestamp;
    public String mValue;

    public SpenEventData(SpenEventType spenEventType) {
        this(spenEventType, null);
    }

    public SpenEventData(SpenEventType spenEventType, String str) {
        this.mEventType = spenEventType;
        this.mValue = str;
        this.mTimestamp = System.currentTimeMillis();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date(this.mTimestamp)));
        sb.append(" ");
        sb.append(this.mEventType);
        if (this.mValue != null) {
            sb.append(" ");
            sb.append(this.mValue);
        }
        return sb.toString();
    }
}
