package com.samsung.remotespen.util;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.samsung.util.debug.Log;

/* loaded from: classes.dex */
public class KeyguardStateChangeDetector {
    public static final String ACTION_KEYGUARD_STATE_UPDATE = "com.samsung.keyguard.KEYGUARD_STATE_UPDATE";
    private static final String TAG = "KeyguardStateChangeDetector";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.util.KeyguardStateChangeDetector.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            KeyguardStateChangeDetector.this.onReceive(intent);
        }
    };
    private Context mContext;
    private Listener mListener;
    private boolean mPreviousLockState;

    /* loaded from: classes.dex */
    public interface Listener {
        void onKeyguardStateUpdated(boolean z);
    }

    public KeyguardStateChangeDetector(Context context) {
        this.mContext = context;
        this.mPreviousLockState = ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked();
    }

    public void registerReceiver(Listener listener) {
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter(ACTION_KEYGUARD_STATE_UPDATE));
        this.mListener = listener;
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mListener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onReceive(Intent intent) {
        if (intent.getAction().equals(ACTION_KEYGUARD_STATE_UPDATE)) {
            boolean z = intent.getExtras().getBoolean("showing");
            String str = TAG;
            Log.d(str, "onReceive : isKeyguardLocked is : " + z + " mPreviousLockState is " + this.mPreviousLockState);
            if (z != this.mPreviousLockState) {
                this.mListener.onKeyguardStateUpdated(z);
                this.mPreviousLockState = z;
            }
        }
    }
}
