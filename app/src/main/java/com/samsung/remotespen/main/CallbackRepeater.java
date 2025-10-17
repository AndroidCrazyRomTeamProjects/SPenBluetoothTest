package com.samsung.remotespen.main;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.util.KeyInjectionBooster;
import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class CallbackRepeater {
    private static final String TAG = "CallbackRepeater";
    private Context mContext;
    private KeyInjectionBooster mKeyInjectionBooster;
    private Listener mListener;
    private long mRepeatInterval;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRepeatCallback = new Runnable() { // from class: com.samsung.remotespen.main.CallbackRepeater.1
        @Override // java.lang.Runnable
        public void run() {
            if (CallbackRepeater.this.mListener != null) {
                CallbackRepeater.this.mListener.onRepeat(CallbackRepeater.this);
            }
            CallbackRepeater.this.mHandler.postDelayed(this, CallbackRepeater.this.mRepeatInterval);
        }
    };

    /* loaded from: classes.dex */
    public interface Listener {
        void onRepeat(CallbackRepeater callbackRepeater);

        void onRepeatStopped();
    }

    public CallbackRepeater(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void startRepeat(long j, Listener listener) {
        String str = TAG;
        Log.d(str, "startRepeat : interval = " + j);
        if (this.mKeyInjectionBooster == null) {
            this.mKeyInjectionBooster = new KeyInjectionBooster(this.mContext);
        }
        this.mKeyInjectionBooster.start();
        Assert.notNull(listener);
        if (this.mListener != null) {
            Log.e(str, "startRepeat : already running. listener=" + this.mListener);
        }
        this.mRepeatInterval = j;
        this.mListener = listener;
        this.mHandler.removeCallbacks(this.mRepeatCallback);
        this.mHandler.postDelayed(this.mRepeatCallback, this.mRepeatInterval);
    }

    public void stopRepeat() {
        boolean isWorking = isWorking();
        String str = TAG;
        Log.d(str, "stopRepeat : isWorking=" + isWorking);
        this.mHandler.removeCallbacks(this.mRepeatCallback);
        if (isWorking) {
            this.mListener.onRepeatStopped();
            KeyInjectionBooster keyInjectionBooster = this.mKeyInjectionBooster;
            if (keyInjectionBooster != null) {
                keyInjectionBooster.stop();
            }
        }
        this.mListener = null;
    }

    public boolean isWorking() {
        return this.mListener != null;
    }
}
