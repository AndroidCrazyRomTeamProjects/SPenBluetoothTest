package com.samsung.remotespen.core.device.chargepolicy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.samsung.remotespen.util.BatteryPolicy;

/* loaded from: classes.dex */
public abstract class BleSpenChargeController {
    private static final int CHARGE_BLOCKING_BATT_TEMPERATURE_LOWER = BatteryPolicy.getLowerWorkableTemperature() - 1;
    private static final int CHARGE_BLOCKING_BATT_TEMPERATURE_UPPER = BatteryPolicy.getUpperWorkableTemperature() + 1;
    private static final int CHARGE_ENSURING_BATT_TEMPERATURE_LOWER = BatteryPolicy.getLowerWorkableTemperature() + 1;
    private static final int CHARGE_ENSURING_BATT_TEMPERATURE_UPPER = BatteryPolicy.getUpperWorkableTemperature() - 1;
    private static final String TAG = "BleSpenChargeController";
    public Context mContext;

    /* loaded from: classes.dex */
    public interface ChargeEnableStateListener {
        void onTemperatureStateChanged(int i, boolean z);
    }

    public abstract void setChargeEnableStateListener(ChargeEnableStateListener chargeEnableStateListener);

    public abstract void start();

    public abstract void stop();

    public BleSpenChargeController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public boolean isChargeBlockingBattTemperature(int i) {
        return i <= CHARGE_BLOCKING_BATT_TEMPERATURE_LOWER || i >= CHARGE_BLOCKING_BATT_TEMPERATURE_UPPER;
    }

    public boolean isChargeableBattTemperature(int i) {
        return i >= CHARGE_ENSURING_BATT_TEMPERATURE_LOWER && i <= CHARGE_ENSURING_BATT_TEMPERATURE_UPPER;
    }

    public int getPhoneBatteryTemperatureFromIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "getPhoneBatteryTemperatureFromIntent : intent is null");
            return 0;
        }
        return intent.getIntExtra("temperature", 0) / 10;
    }
}
