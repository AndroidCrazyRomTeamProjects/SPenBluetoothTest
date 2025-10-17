package com.samsung.remotespen.core.device.util.diagnosis;

import android.util.Log;

import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DiagnosisManager.java */
/* loaded from: classes.dex */
public class SpenSession {
    private static final int MAX_EVENT_COUNT = 100;
    private static final String TAG = "SpenSession";
    public DiagnosisManager.DisconnectReason mCandidateDisconnectReason;
    public long mConnFinishTime;
    public long mConnStartTime;
    public BleOpResultData mConnectionResultData;
    public long mDisconnectTime;
    public ArrayList<SpenEventData> mEventArray = new ArrayList<>();
    public boolean mIsTicTocPerformed;

    private void addEvent(SpenEventType spenEventType) {
        addEvent(spenEventType, null);
    }

    private void addEvent(SpenEventType spenEventType, String str) {
        if (this.mEventArray.size() >= 100) {
            this.mEventArray.remove(0);
        }
        this.mEventArray.add(new SpenEventData(spenEventType, str));
    }

    public void markStartConnection() {
        this.mConnStartTime = System.currentTimeMillis();
        addEvent(SpenEventType.CONNECTING_STARTED);
    }

    public void markFinishConnection(BleOpResultData bleOpResultData) {
        this.mConnFinishTime = System.currentTimeMillis();
        this.mConnectionResultData = bleOpResultData;
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        if (resultCode == BleOpResultCode.SUCCESS) {
            addEvent(SpenEventType.CONNECTED);
        } else {
            addEvent(SpenEventType.CONNECTION_FAILED, resultCode != null ? resultCode.name() : null);
        }
    }

    public void markTicTocPerformed() {
        this.mIsTicTocPerformed = true;
        addEvent(SpenEventType.CONNECTING_TICTOC);
    }

    public void markDisconnect(DiagnosisManager.DisconnectReason disconnectReason, int i) {
        this.mDisconnectTime = System.currentTimeMillis();
        SpenEventType spenEventType = SpenEventType.DISCONNECTED;
        addEvent(spenEventType, disconnectReason + ", gatt=" + i);
    }

    public void markBatteryLevelChanged(int i, int i2) {
        SpenEventType spenEventType = SpenEventType.BATTERY_LEVEL_CHANGED;
        addEvent(spenEventType, i + " -> " + i2);
    }

    public void markCandidateDisconnectReason(DiagnosisManager.DisconnectReason disconnectReason) {
        this.mCandidateDisconnectReason = disconnectReason;
        addEvent(SpenEventType.DISCONNECT_EVENT, disconnectReason.name());
    }

    public void markSpenInsertionState(boolean z) {
        if (z) {
            addEvent(SpenEventType.PEN_INSERTED);
        } else {
            addEvent(SpenEventType.PEN_DETACHED);
        }
    }

    public void dump() {
        Log.v(TAG, "dump : Spen event data");
        Iterator<SpenEventData> it = this.mEventArray.iterator();
        while (it.hasNext()) {
            String str = TAG;
            Log.v(str, "dump : " + it.next().toString());
        }
    }
}
