package com.samsung.remotespen.core.device.control.factory.davinci;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.util.CommonUtils;

/* loaded from: classes.dex */
public class DavinciWacomChargingDriver extends WacomChargingDriver {
    public static final String CMD_10MIN_CHARGE = "8";
    public static final String CMD_CONTINUOUS_CHARGE = "4";
    public static final String CMD_DISABLE_CHARGER = "0";
    public static final String CMD_ENABLE_CHARGER = "1";
    public static final String CMD_RESET = "2";
    public static final String CMD_START_CHARGE = "3";
    public static final String CMD_STOP_CHARGE = "5";
    public static final String CMD_WACOM_DSP_RESET = "7";
    private static final int DEFAULT_CHARGING_DURATION = 60000;
    public static final int MIN_WACOM_COMMAND_PATTERN_SIGNAL_DURATION = 450;
    private static final int MIN_WACOM_DSP_RESET_DURATION = 400;
    public static String TAG = "DavinciWacomChargingDriver";

    public DavinciWacomChargingDriver(Context context) {
        this(context, 200L, 20L);
    }

    public DavinciWacomChargingDriver(Context context, long j, long j2) {
        super(context, j, j2);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void turnOnWacomChargingModule() {
        Log.d(TAG, "turnOnWacomChargingModule");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$turnOnWacomChargingModule$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$turnOnWacomChargingModule$0() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("1");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void turnOffWacomChargingModule() {
        Log.d(TAG, "turnOffWacomChargingModule");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$turnOffWacomChargingModule$1();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$turnOffWacomChargingModule$1() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("0");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void resetSpen() {
        Log.d(TAG, "resetSpen");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$resetSpen$2();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resetSpen$2() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("3");
        CommonUtils.sleep(450L);
        writeBleSpenCommand("2");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public int resetWacomDsp(final boolean z) {
        Log.d(TAG, "resetWacomDsp");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$resetWacomDsp$3(z);
            }
        });
        return 400;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resetWacomDsp$3(boolean z) {
        ensureMinTransactionInterval();
        writeBleSpenCommand("7");
        if (z) {
            CommonUtils.sleep(400L);
        }
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public int startCharge() {
        Log.d(TAG, "startCharge");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$startCharge$4();
            }
        });
        return DEFAULT_CHARGING_DURATION;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startCharge$4() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("3");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public boolean startContinuousCharge() {
        Log.d(TAG, "startContinuousCharge");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$startContinuousCharge$5();
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startContinuousCharge$5() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("3");
        CommonUtils.sleep(450L);
        writeBleSpenCommand("4");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void stopCharge() {
        Log.d(TAG, "stopCharge");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                DavinciWacomChargingDriver.this.lambda$stopCharge$6();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$stopCharge$6() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("5");
        markEndTransaction();
    }
}
