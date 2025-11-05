package com.samsung.remotespen.core.device.util.diagnosis;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.diagnosis.ExternalBroadcastReceiver;
import com.samsung.util.ActToolHelper;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class DiagnosisManager {
    private static final int MAX_SESSION_HISTORY_COUNT = 20;
    private static final String TAG = "DiagnosisManager";
    private static DiagnosisManager sInstance;
    private BleStateCallback mBleStateCallback;
    private SpenModelName mBundleSpenModelName;
    private Context mContext;
    private SpenSession mCurSpenSession;
    private ExternalBroadcastReceiver mExternalBroadcastReceiver;
    private HqmManager mHqmManager;
    private boolean mIsMonitoring = false;
    private ArrayList<SpenSession> mSessionTable = new ArrayList<>();

    /* loaded from: classes.dex */
    public enum ButtonClickType {
        SINGLE,
        DOUBLE,
        LONG
    }

    /* loaded from: classes.dex */
    public enum ConnectionFailReason {
        CANCELLED,
        TICTOC_FAIL,
        OUT_OF_RETRY_COUNT,
        BLE_NOT_ENABLED
    }

    /* loaded from: classes.dex */
    public enum DisconnectReason {
        USER,
        AIRPLANE,
        UPSM,
        LINK_LOSS
    }

    public static synchronized DiagnosisManager getInstance(Context context) {
        DiagnosisManager diagnosisManager;
        synchronized (DiagnosisManager.class) {
            if (sInstance == null) {
                sInstance = new DiagnosisManager(context);
            }
            diagnosisManager = sInstance;
        }
        return diagnosisManager;
    }

    private DiagnosisManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mExternalBroadcastReceiver = new ExternalBroadcastReceiver(this.mContext);
        this.mBundleSpenModelName = ModelFeatures.getSpenModelName(this.mContext);
        this.mHqmManager = new HqmManager(this.mContext);
    }

    public synchronized void startMonitoring(BleStateCallback bleStateCallback) {
        Assert.notNull(bleStateCallback);
        Assert.e(!this.mIsMonitoring);
        this.mExternalBroadcastReceiver.registerReceiver(new ExternalBroadcastReceiver.Listener() { // from class: com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager.1
            @Override // com.samsung.remotespen.core.device.util.diagnosis.ExternalBroadcastReceiver.Listener
            public void onAirplaneModeChanged(boolean z) {
                DiagnosisManager.this.onAirplaneModeChanged(z);
            }

            @Override // com.samsung.remotespen.core.device.util.diagnosis.ExternalBroadcastReceiver.Listener
            public void onEnablingEmergencyMode() {
                DiagnosisManager.this.onEnablingEmergencyMode();
            }
        });
        this.mBleStateCallback = bleStateCallback;
        this.mIsMonitoring = true;
        this.mHqmManager.startMonitoring(bleStateCallback);
    }

    public synchronized void stopMonitoring() {
        if (this.mIsMonitoring) {
            this.mIsMonitoring = false;
            this.mBleStateCallback = null;
            this.mExternalBroadcastReceiver.unregisterReceiver();
            this.mHqmManager.stopMonitoring();
        }
    }

    public synchronized void notifyConnectionStarted(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyConnectionStarted : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            Log.d(TAG, "notifyConnectionStarted");
            SpenSession spenSession = new SpenSession();
            this.mCurSpenSession = spenSession;
            spenSession.markStartConnection();
            if (this.mSessionTable.size() >= 20) {
                this.mSessionTable.remove(0);
            }
            this.mSessionTable.add(this.mCurSpenSession);
        }
    }

    public synchronized void notifyConnectionFinished(SpenModelName spenModelName, BleOpResultData bleOpResultData) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyConnectionFinished : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            String str = TAG;
            Log.d(str, "notifyConnectionFinished");
            SpenSession spenSession = this.mCurSpenSession;
            if (spenSession != null) {
                spenSession.markFinishConnection(bleOpResultData);
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                boolean z = resultCode == BleOpResultCode.SUCCESS;
                ConnectionFailReason convertConnectionFailReason = convertConnectionFailReason(resultCode);
                if (convertConnectionFailReason == ConnectionFailReason.TICTOC_FAIL) {
                    ActToolHelper.notifyEvent(this.mContext, ActToolHelper.EVENT_SPEN_CONN_FAIL);
                }
                this.mHqmManager.notifyConnectionFinished(z, convertConnectionFailReason, this.mCurSpenSession.mIsTicTocPerformed);
            } else {
                Log.e(str, "notifyConnectionFinished : session is null");
            }
        }
    }

    public synchronized void notifyTicTocPerformedDuringConnection(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyTicTocPerformedDuringConnection : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            String str = TAG;
            Log.d(str, "notifyTicTocPerformedDuringConnection");
            SpenSession spenSession = this.mCurSpenSession;
            if (spenSession != null) {
                spenSession.markTicTocPerformed();
            } else {
                Log.e(str, "notifyTicTocPerformedDuringConnection : session is null");
            }
        }
    }

    public synchronized void notifyDisconnect(SpenModelName spenModelName, int i) {
        DisconnectReason disconnectReason;
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyDisconnect : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            Log.d(TAG, "notifyDisconnect");
            SpenSession spenSession = this.mCurSpenSession;
            if (spenSession != null) {
                if (i == 8) {
                    disconnectReason = DisconnectReason.LINK_LOSS;
                } else if (i == 0) {
                    DisconnectReason disconnectReason2 = spenSession.mCandidateDisconnectReason;
                    DisconnectReason disconnectReason3 = DisconnectReason.AIRPLANE;
                    if (disconnectReason2 != disconnectReason3 && disconnectReason2 != (disconnectReason3 = DisconnectReason.UPSM)) {
                        disconnectReason = DisconnectReason.USER;
                    }
                    disconnectReason = disconnectReason3;
                } else {
                    disconnectReason = null;
                }
                this.mHqmManager.notifyDisconnect(disconnectReason);
                this.mCurSpenSession.markDisconnect(disconnectReason, i);
                this.mCurSpenSession = null;
            }
        }
    }

    public synchronized void notifyStartReconnectionDueToLinkLoss(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyStartReconnectionDueToLinkLoss : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            Log.d(TAG, "notifyStartReconnectionDueToLinkLoss");
            this.mHqmManager.notifyStartReconnectionDueToLinkLoss();
        }
    }

    public synchronized void notifyBatteryLevelChanged(SpenModelName spenModelName, int i, int i2) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyBatteryLevelChanged : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            this.mHqmManager.notifyBatteryLevelChanged(i, i2);
            SpenSession spenSession = this.mCurSpenSession;
            if (spenSession != null) {
                spenSession.markBatteryLevelChanged(i, i2);
            }
        }
    }

    public synchronized void notifySpenInsertionStateChanged(boolean z) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifySpenInsertionStateChanged : Not monitoring state");
        } else if (this.mBundleSpenModelName == null) {
        } else {
            this.mHqmManager.notifySpenInsertionStateChanged(z);
            SpenSession spenSession = this.mCurSpenSession;
            if (spenSession != null) {
                spenSession.markSpenInsertionState(z);
            }
        }
    }

    public synchronized void notifyButtonSingleClicked(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyButtonSingleClicked : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            this.mHqmManager.notifyButtonClicked(ButtonClickType.SINGLE);
        }
    }

    public synchronized void notifyButtonDoubleClicked(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyButtonDoubleClicked : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            this.mHqmManager.notifyButtonClicked(ButtonClickType.DOUBLE);
        }
    }

    public synchronized void notifyButtonLongClicked(SpenModelName spenModelName) {
        if (!this.mIsMonitoring) {
            Log.e(TAG, "notifyButtonLongClicked : Not monitoring state");
            return;
        }
        SpenModelName spenModelName2 = this.mBundleSpenModelName;
        if (spenModelName2 != null && spenModelName2.equals(spenModelName)) {
            this.mHqmManager.notifyButtonClicked(ButtonClickType.LONG);
        }
    }

    public synchronized void dump() {
        Log.d(TAG, "------ DUMP event history -----");
        Iterator<SpenSession> it = this.mSessionTable.iterator();
        while (it.hasNext()) {
            it.next().dump();
            Log.d(TAG, "------ end of session -----");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void onAirplaneModeChanged(boolean z) {
        if (z) {
            if (this.mCurSpenSession != null && isBleConnected()) {
                Log.i(TAG, "onAirplaneModeChanged : Candidate = AIRPLANE");
                this.mCurSpenSession.markCandidateDisconnectReason(DisconnectReason.AIRPLANE);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void onEnablingEmergencyMode() {
        if (this.mCurSpenSession != null && isBleConnected()) {
            Log.i(TAG, "onEnablingEmergencyMode : Candidate = UPSM");
            this.mCurSpenSession.markCandidateDisconnectReason(DisconnectReason.UPSM);
        }
    }

    private boolean isBleConnected() {
        BleStateCallback bleStateCallback = this.mBleStateCallback;
        if (bleStateCallback == null) {
            return false;
        }
        return bleStateCallback.isConnected();
    }

    /* renamed from: com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode;

        static {
            int[] iArr = new int[BleOpResultCode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode = iArr;
            try {
                iArr[BleOpResultCode.TICTOC_FAIL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[BleOpResultCode.CANCELLED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[BleOpResultCode.OUT_OF_RETRY_COUNT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[BleOpResultCode.BLE_NOT_ENABLED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[BleOpResultCode.SUCCESS.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    private ConnectionFailReason convertConnectionFailReason(BleOpResultCode bleOpResultCode) {
        int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[bleOpResultCode.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        return null;
                    }
                    return ConnectionFailReason.BLE_NOT_ENABLED;
                }
                return ConnectionFailReason.OUT_OF_RETRY_COUNT;
            }
            return ConnectionFailReason.CANCELLED;
        }
        return ConnectionFailReason.TICTOC_FAIL;
    }
}
