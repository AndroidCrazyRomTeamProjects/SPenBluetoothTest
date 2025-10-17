package com.samsung.remotespen.core.device.util.diagnosis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.util.constants.CommonIntent;
import com.samsung.util.debug.Assert;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: DiagnosisManager.java */
/* loaded from: classes.dex */
public class ExternalBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION_EMERGENCY_STATE_CHANGED = "com.samsung.intent.action.EMERGENCY_STATE_CHANGED";
    private static final String TAG = ExternalBroadcastReceiver.class.getSimpleName();
    private Context mContext;
    private Listener mListener;

    /* compiled from: DiagnosisManager.java */
    /* loaded from: classes.dex */
    public interface Listener {
        void onAirplaneModeChanged(boolean z);

        void onEnablingEmergencyMode();
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String str = TAG;
        Log.d(str, "onReceive : action = " + action);
        if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
            if (this.mListener != null) {
                this.mListener.onAirplaneModeChanged(intent.getBooleanExtra("state", false));
            }
        } else if ("com.samsung.intent.action.EMERGENCY_STATE_CHANGED".equals(action)) {
            int intExtra = intent.getIntExtra(CommonIntent.INTENT_EXTRA_REASON, 0);
            if (intExtra == 2) {
                Log.d(str, "onReceive : emergency mode enabling");
                Listener listener = this.mListener;
                if (listener != null) {
                    listener.onEnablingEmergencyMode();
                    return;
                }
                return;
            }
            Log.d(str, "onReceive : emergencyReason=" + intExtra);
        }
    }

    public ExternalBroadcastReceiver(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerReceiver(Listener listener) {
        Assert.e(this.mListener == null);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("com.samsung.intent.action.EMERGENCY_STATE_CHANGED");
        this.mContext.registerReceiver(this, intentFilter);
        this.mListener = listener;
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this);
        this.mListener = null;
    }
}
