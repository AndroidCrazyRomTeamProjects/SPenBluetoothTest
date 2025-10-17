package com.samsung.remotespen.core.device.control.detector;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenAirGestureActionDetector.java */
/* loaded from: classes.dex */
public class GestureDetectionThread extends HandlerThread {
    private static final String TAG = GestureDetectionThread.class.getSimpleName();
    private Handler mHandler;
    private Object mLooperReadySyncObj;

    public GestureDetectionThread(String str, Object obj) {
        super(str);
        this.mLooperReadySyncObj = obj;
    }

    @Override // android.os.HandlerThread
    public synchronized void onLooperPrepared() {
        Log.d(TAG, "onLooperPrepared : looper ready");
        super.onLooperPrepared();
        this.mHandler = new Handler(getLooper());
        synchronized (this.mLooperReadySyncObj) {
            this.mLooperReadySyncObj.notifyAll();
        }
    }

    public synchronized Handler getHandler() {
        return this.mHandler;
    }

    @Override // android.os.HandlerThread, java.lang.Thread, java.lang.Runnable
    public void run() {
        super.run();
        Log.d(TAG, "run : thread finished");
        synchronized (this) {
            this.mHandler = null;
        }
    }
}
