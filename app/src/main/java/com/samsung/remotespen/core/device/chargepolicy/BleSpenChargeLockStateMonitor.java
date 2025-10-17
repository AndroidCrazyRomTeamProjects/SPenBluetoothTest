package com.samsung.remotespen.core.device.chargepolicy;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.samsung.remotespen.util.reflections.BleSpenChargeLockStateChangedListenerReflection;
import com.samsung.util.SpenGestureManagerWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class BleSpenChargeLockStateMonitor {
    private static final String TAG = "BleSpenChargeLockStateMonitor";
    private static BleSpenChargeLockStateMonitor sInstance;
    private Object mBleSpenChargeLockStateChangedListenerProxy;
    private Context mContext;
    private SpenGestureManagerWrapper mSpenGestureManager;
    private List<StateChangedListener> mStateChangeListeners = Collections.synchronizedList(new ArrayList());
    private Handler mHandler = new Handler();
    private BleSpenChargeLockStateChangedListenerReflection.ChargeLockStateChangedListener mChargeLockStateChangedListener = new BleSpenChargeLockStateChangedListenerReflection.ChargeLockStateChangedListener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeLockStateMonitor.1
        @Override // com.samsung.remotespen.util.reflections.BleSpenChargeLockStateChangedListenerReflection.ChargeLockStateChangedListener
        public void onChanged(final boolean z) {
            BleSpenChargeLockStateMonitor.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeLockStateMonitor.1.1
                @Override // java.lang.Runnable
                public void run() {
                    BleSpenChargeLockStateMonitor.this.invokeListeners(z);
                }
            });
        }
    };

    /* loaded from: classes.dex */
    public interface StateChangedListener {
        void onChanged(boolean z);
    }

    private BleSpenChargeLockStateMonitor(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized BleSpenChargeLockStateMonitor getInstance(Context context) {
        BleSpenChargeLockStateMonitor bleSpenChargeLockStateMonitor;
        synchronized (BleSpenChargeLockStateMonitor.class) {
            if (sInstance == null) {
                sInstance = new BleSpenChargeLockStateMonitor(context);
            }
            bleSpenChargeLockStateMonitor = sInstance;
        }
        return bleSpenChargeLockStateMonitor;
    }

    public synchronized void registerListener(StateChangedListener stateChangedListener) {
        if (this.mStateChangeListeners.indexOf(stateChangedListener) >= 0) {
            Log.d(TAG, "registerListener : Listener is already registered");
            return;
        }
        this.mStateChangeListeners.add(stateChangedListener);
        if (this.mStateChangeListeners.size() == 1) {
            registerSpenChargeLockStateChangedListener();
        }
    }

    public synchronized void unregisterListener(StateChangedListener stateChangedListener) {
        if (this.mStateChangeListeners.indexOf(stateChangedListener) < 0) {
            return;
        }
        this.mStateChangeListeners.remove(stateChangedListener);
        if (this.mStateChangeListeners.size() == 0) {
            unregisterSpenChargeLockStateChangedListener();
        }
    }

    private void registerSpenChargeLockStateChangedListener() {
        Log.v(TAG, "registerSpenChargeLockStateChangedListener");
        this.mBleSpenChargeLockStateChangedListenerProxy = BleSpenChargeLockStateChangedListenerReflection.newInstance(this.mChargeLockStateChangedListener);
        SpenGestureManagerWrapper spenGestureManagerWrapper = new SpenGestureManagerWrapper(this.mContext);
        this.mSpenGestureManager = spenGestureManagerWrapper;
        spenGestureManagerWrapper.registerSpenChargeLockStateChangedListener(this.mBleSpenChargeLockStateChangedListenerProxy);
    }

    private void unregisterSpenChargeLockStateChangedListener() {
        SpenGestureManagerWrapper spenGestureManagerWrapper;
        Log.v(TAG, "unregisterSpenChargeLockStateChangedListener");
        Object obj = this.mBleSpenChargeLockStateChangedListenerProxy;
        if (obj != null && (spenGestureManagerWrapper = this.mSpenGestureManager) != null) {
            spenGestureManagerWrapper.unregisterSpenChargeLockStateChangedListener(obj);
        }
        this.mBleSpenChargeLockStateChangedListenerProxy = null;
        this.mSpenGestureManager = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void invokeListeners(boolean z) {
        for (StateChangedListener stateChangedListener : this.mStateChangeListeners) {
            stateChangedListener.onChanged(z);
        }
    }
}
