package com.samsung.remotespen.core.device.control.factory.crown;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.util.CommonUtils;

/* loaded from: classes.dex */
public class CrownWacomChargingDriver extends WacomChargingDriver {
    private static final String CMD_DISABLE_CHARGER = "0";
    private static final String CMD_ENABLE_CHARGER = "1";
    private static final String CMD_FAST_CHARGE = "3";
    private static final String CMD_RESET = "2";
    private static final String CMD_SLOW_CHARGE = "4";
    private static final String CMD_WACOM_DSP_RESET = "7";
    private static final int DEFAULT_CHARGING_DURATION = 60000;
    private static final int MIN_WACOM_DSP_RESET_DURATION = 400;
    private static final String TAG = "CrownWacomChargingDriver";
    private static CrownWacomChargingDriver sInstance;

    public static synchronized CrownWacomChargingDriver getInstance(Context context) {
        CrownWacomChargingDriver crownWacomChargingDriver;
        synchronized (CrownWacomChargingDriver.class) {
            if (sInstance == null) {
                sInstance = new CrownWacomChargingDriver(context);
            }
            crownWacomChargingDriver = sInstance;
        }
        return crownWacomChargingDriver;
    }

    private CrownWacomChargingDriver(Context context) {
        super(context, 200L, 20L);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void turnOnWacomChargingModule() {
        Log.d(TAG, "turnOnWacomChargingModule");
        ensureMinTransactionInterval();
        writeBleSpenCommand("1");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void turnOffWacomChargingModule() {
        Log.d(TAG, "turnOffWacomChargingModule");
        ensureMinTransactionInterval();
        writeBleSpenCommand("0");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public int startCharge() {
        Log.d(TAG, "startCharge");
        turnOffWacomChargingModule();
        turnOnWacomChargingModule();
        ensureCommandWriteInterval();
        writeBleSpenCommand("3");
        markEndTransaction();
        return DEFAULT_CHARGING_DURATION;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public boolean startContinuousCharge() {
        Log.e(TAG, "startContinuousCharge : Not supported operation");
        startCharge();
        return false;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void stopCharge() {
        turnOffWacomChargingModule();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void resetSpen() {
        Log.d(TAG, "resetSpen");
        turnOffWacomChargingModule();
        turnOnWacomChargingModule();
        ensureMinTransactionInterval();
        writeBleSpenCommand("2");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public int resetWacomDsp(boolean z) {
        Log.d(TAG, "resetWacomDsp");
        ensureMinTransactionInterval();
        writeBleSpenCommand("7");
        if (z) {
            CommonUtils.sleep(400L);
        }
        markEndTransaction();
        return 400;
    }
}
