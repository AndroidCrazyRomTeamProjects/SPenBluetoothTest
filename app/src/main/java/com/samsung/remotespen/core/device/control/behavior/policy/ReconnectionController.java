package com.samsung.remotespen.core.device.control.behavior.policy;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleConnTriggerCode;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.util.features.SpenModelName;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: AbsPenBehaviorPolicy.java */
/* loaded from: classes.dex */
public class ReconnectionController {
    private static final String TAG = "ReconnectionController";
    private BleOffReason mBleOffReason;
    private AbsPenBehaviorPolicy.Callback mCallback;
    private Context mContext;
    private Handler mHandler = new Handler();
    private Runnable mReconnectCallback = new Runnable() { // from class: com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController.1
        @Override // java.lang.Runnable
        public void run() {
            if (!ReconnectionController.this.mCallback.isDisconnected()) {
                String str = ReconnectionController.TAG;
                Log.w(str, "mReconnectCallback : Already connected or connecting. so canceled reconnection. reason = " + ReconnectionController.this.mReconnectReason);
                return;
            }
            String str2 = ReconnectionController.TAG;
            Log.i(str2, "mReconnectCallback : Starting reserved connect. reason = " + ReconnectionController.this.mReconnectReason);
            BleConnTriggerCode bleConnTriggerCode = BleConnTriggerCode.UNKNOWN;
            int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$ReconnectReason[ReconnectionController.this.mReconnectReason.ordinal()];
            if (i == 1) {
                DiagnosisManager.getInstance(ReconnectionController.this.mContext).notifyStartReconnectionDueToLinkLoss(ReconnectionController.this.mSpenModelName);
                bleConnTriggerCode = BleConnTriggerCode.RECONNECTION_AFTER_LINK_LOSS;
            } else if (i == 2) {
                bleConnTriggerCode = BleConnTriggerCode.RECONNECTION_AFTER_LMP_RESPONSE_TIMEOUT;
            } else if (i == 3) {
                bleConnTriggerCode = BleConnTriggerCode.RECONNECTION_AFTER_BLE_OFF;
            }
            ReconnectionController.this.mCallback.startPenConnectionTransaction(new BleConnReqData(ReconnectionController.this.mSpenModelName, bleConnTriggerCode, ReconnectionController.this.mSpenAddress));
        }
    };
    private ReconnectReason mReconnectReason;
    private String mSpenAddress;
    private SpenModelName mSpenModelName;

    /* compiled from: AbsPenBehaviorPolicy.java */
    /* loaded from: classes.dex */
    public enum BleOffReason {
        AIRPLANE,
        UPSM
    }

    /* compiled from: AbsPenBehaviorPolicy.java */
    /* loaded from: classes.dex */
    public enum ReconnectReason {
        LINK_LOSS,
        BLE_OFF,
        LMP_RESPONSE_TIMEOUT
    }

    public ReconnectionController(Context context, AbsPenBehaviorPolicy.Callback callback) {
        this.mContext = context.getApplicationContext();
        this.mCallback = callback;
    }

    public void setBleOffReason(BleOffReason bleOffReason) {
        String str = TAG;
        Log.d(str, "setBleOffReason : " + bleOffReason);
        this.mBleOffReason = bleOffReason;
    }

    public void reserveReconnection(SpenModelName spenModelName, String str, int i, ReconnectReason reconnectReason) {
        String str2 = TAG;
        Log.i(str2, "reserveReconnection : delay=" + i + ", modelName :" + spenModelName);
        this.mReconnectReason = reconnectReason;
        this.mSpenModelName = spenModelName;
        this.mSpenAddress = str;
        this.mHandler.removeCallbacks(this.mReconnectCallback);
        this.mHandler.postDelayed(this.mReconnectCallback, (long) i);
    }

    public void cancelReconnection() {
        Log.d(TAG, "cancelReconnection");
        this.mHandler.removeCallbacks(this.mReconnectCallback);
    }

    public boolean canReserveReconnection() {
        BleOffReason bleOffReason = this.mBleOffReason;
        if (bleOffReason == null) {
            return true;
        }
        int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$BleOffReason[bleOffReason.ordinal()];
        if (i == 1 || i == 2) {
            String str = TAG;
            Log.d(str, "canReserveReconnection : BLE off reason is " + this.mBleOffReason.name());
            return false;
        }
        return true;
    }

    /* compiled from: AbsPenBehaviorPolicy.java */
    /* renamed from: com.samsung.remotespen.core.device.control.behavior.policy.ReconnectionController$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$BleOffReason;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$ReconnectReason;

        static {
            int[] iArr = new int[BleOffReason.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$BleOffReason = iArr;
            try {
                iArr[BleOffReason.AIRPLANE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$BleOffReason[BleOffReason.UPSM.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            int[] iArr2 = new int[ReconnectReason.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$ReconnectReason = iArr2;
            try {
                iArr2[ReconnectReason.LINK_LOSS.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$ReconnectReason[ReconnectReason.LMP_RESPONSE_TIMEOUT.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$behavior$policy$ReconnectionController$ReconnectReason[ReconnectReason.BLE_OFF.ordinal()] = 3;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public static ReconnectReason convertStatus(Integer num) {
        if (num == null) {
            return null;
        }
        if (num.intValue() == 8) {
            return ReconnectReason.LINK_LOSS;
        }
        if (num.intValue() == 34) {
            return ReconnectReason.LMP_RESPONSE_TIMEOUT;
        }
        return null;
    }
}
