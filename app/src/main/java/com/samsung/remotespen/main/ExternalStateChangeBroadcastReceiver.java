package com.samsung.remotespen.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.remotespen.util.Constants;
import com.samsung.util.constants.CommonIntent;
import com.crazyromteam.spenbletest.utils.Assert;

/* loaded from: classes.dex */
public class ExternalStateChangeBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = ExternalStateChangeBroadcastReceiver.class.getSimpleName();
    private Context mContext;
    private Listener mListener;

    /* loaded from: classes.dex */
    public interface Listener {
        void onAirplaneModeChanged(boolean z);

        void onBleStateChanged(int i, int i2);

        void onBluetoothStateChanged(int i, int i2);

        void onEmergencyStateChanged(int i);

        void onIrisLedStateChanged(boolean z);

        void onLocaleChanged();

        void onPowerOffEvent();

        void onScreenOnOffStateChanged(boolean z);

        void onThemeApplyingStarted();

        void onUserFocusChanged(boolean z);

        void onUserSwitched();
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (this.mListener == null) {
            Log.e(TAG, "onReceive : listener is null!");
            return;
        }
        String action = intent.getAction();
        String str = TAG;
        Log.d(str, "onReceive : action = " + action);
        if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
            this.mListener.onPowerOffEvent();
        } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
            this.mListener.onAirplaneModeChanged(intent.getBooleanExtra("state", false));
        } else if ("android.intent.action.SCREEN_ON".equals(action)) {
            this.mListener.onScreenOnOffStateChanged(true);
        } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
            this.mListener.onScreenOnOffStateChanged(false);
        } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
            this.mListener.onLocaleChanged();
        } else if ("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED".equals(action)) {
            if (!intent.hasExtra("android.bluetooth.adapter.extra.STATE")) {
                Log.e(str, "onReceive : EXTRA_STATE is not present!");
            }
            int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
            this.mListener.onBleStateChanged(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10), intExtra);
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            if (!intent.hasExtra("android.bluetooth.adapter.extra.STATE")) {
                Log.e(str, "onReceive : EXTRA_STATE(Bluetooth) is not present!");
            }
            int intExtra2 = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
            this.mListener.onBluetoothStateChanged(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10), intExtra2);
        } else if ("android.intent.action.USER_FOREGROUND".equals(action)) {
            this.mListener.onUserFocusChanged(true);
        } else if ("android.intent.action.USER_BACKGROUND".equals(action)) {
            this.mListener.onUserFocusChanged(false);
        } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
            this.mListener.onUserSwitched();
        } else if (CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED.equals(action)) {
            this.mListener.onEmergencyStateChanged(intent.getIntExtra(CommonIntent.INTENT_EXTRA_REASON, 0));
        } else if (Constants.INTENT_ACTION_THEME_APPLY_START.equals(action)) {
            this.mListener.onThemeApplyingStarted();
        } else if (CommonIntent.INTENT_ACTION_IRIS_LED_ON.equals(action)) {
            this.mListener.onIrisLedStateChanged(true);
        } else if (CommonIntent.INTENT_ACTION_IRIS_LED_OFF.equals(action)) {
            this.mListener.onIrisLedStateChanged(false);
        }
    }

    public ExternalStateChangeBroadcastReceiver(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerReceiver(Listener listener) {
        Assert.e(this.mListener == null);
        Assert.notNull(listener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_FOREGROUND");
        intentFilter.addAction("android.intent.action.USER_BACKGROUND");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction(CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED);
        intentFilter.addAction(Constants.INTENT_ACTION_THEME_APPLY_START);
        intentFilter.addAction(CommonIntent.INTENT_ACTION_IRIS_LED_ON);
        intentFilter.addAction(CommonIntent.INTENT_ACTION_IRIS_LED_OFF);
        this.mContext.registerReceiver(this, intentFilter);
        this.mListener = listener;
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this);
        this.mListener = null;
    }
}
