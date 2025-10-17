package com.samsung.remotespen.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import com.samsung.util.CommonUtils;
import com.samsung.util.SpenGestureManagerWrapper;
import com.samsung.util.debug.Log;
import com.samsung.util.usage.SAUtils;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class SpenInsertionEventDetector {
    private static final String ACTION_SPEN_INSERT = "com.samsung.pen.INSERT";
    private static final String TAG = "SpenInsertionEventDetector";
    private static SpenInsertionEventDetector sInstance;
    private Context mContext;
    private Boolean mIsInserted;
    private SpenGestureManagerWrapper mSpenGestureManagerWrapper;
    private ArrayList<Listener> mListenerArray = new ArrayList<>();
    private Long mInsertedTime = null;
    private Long mRemovedTime = null;
    private Long mInsertedSystemClockTime = null;
    private Long mRemovedSystemClockTime = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.util.SpenInsertionEventDetector.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.samsung.pen.INSERT".equals(intent.getAction())) {
                boolean booleanExtra = intent.getBooleanExtra("penInsert", false);
                boolean booleanExtra2 = intent.getBooleanExtra("isBoot", false);
                synchronized (SpenInsertionEventDetector.this) {
                    SpenInsertionEventDetector.this.mIsInserted = Boolean.valueOf(booleanExtra);
                }
                synchronized (SpenInsertionEventDetector.this.mListenerArray) {
                    String str = SpenInsertionEventDetector.TAG;
                    Log.i(str, "onReceive : isInserted = " + booleanExtra + ", boot = " + booleanExtra2);
                    Iterator it = new ArrayList(SpenInsertionEventDetector.this.mListenerArray).iterator();
                    while (it.hasNext()) {
                        Listener listener = (Listener) it.next();
                        if (!SpenInsertionEventDetector.this.mListenerArray.contains(listener)) {
                            String str2 = SpenInsertionEventDetector.TAG;
                            Log.d(str2, "onReceive listener = " + listener.toString() + " is unregistered.");
                        } else {
                            listener.onInsertEvent(booleanExtra);
                        }
                    }
                }
                String packageName = CommonUtils.getTopMostActivity(SpenInsertionEventDetector.this.mContext).getPackageName();
                if (booleanExtra) {
                    SpenInsertionEventDetector.this.mInsertedTime = Long.valueOf(System.currentTimeMillis());
                    SpenInsertionEventDetector.this.mInsertedSystemClockTime = Long.valueOf(SystemClock.elapsedRealtime());
                    if (booleanExtra2) {
                        return;
                    }
                    SAUtils.insertEventLog(SAUtils.AirCommandMain.SCREEN_ID, SAUtils.AirCommandMain.EVENT_ID_SPEN_USING_TIME, packageName, Long.valueOf(SpenInsertionEventDetector.this.mRemovedSystemClockTime == null ? 0L : SpenInsertionEventDetector.this.mInsertedSystemClockTime.longValue() - SpenInsertionEventDetector.this.mRemovedSystemClockTime.longValue()));
                    return;
                }
                SpenInsertionEventDetector.this.mRemovedTime = Long.valueOf(System.currentTimeMillis());
                SpenInsertionEventDetector.this.mRemovedSystemClockTime = Long.valueOf(SystemClock.elapsedRealtime());
                if (booleanExtra2) {
                    return;
                }
                SAUtils.insertEventLog(SAUtils.AirCommandMain.SCREEN_ID, SAUtils.AirCommandMain.EVENT_ID_REMOVE_SPEN, packageName, null);
            }
        }
    };

    /* loaded from: classes.dex */
    public interface Listener {
        void onInsertEvent(boolean z);
    }

    public static synchronized SpenInsertionEventDetector getInstance(Context context) {
        SpenInsertionEventDetector spenInsertionEventDetector;
        synchronized (SpenInsertionEventDetector.class) {
            if (sInstance == null) {
                sInstance = new SpenInsertionEventDetector(context);
            }
            spenInsertionEventDetector = sInstance;
        }
        return spenInsertionEventDetector;
    }

    private SpenInsertionEventDetector(Context context) {
        this.mContext = context.getApplicationContext();
        this.mSpenGestureManagerWrapper = new SpenGestureManagerWrapper(this.mContext);
        startMonitor();
    }

    public synchronized void registerListener(Listener listener) {
        synchronized (this.mListenerArray) {
            String str = TAG;
            Log.v(str, "registerListener listener=" + listener);
            if (!this.mListenerArray.contains(listener)) {
                this.mListenerArray.add(listener);
            } else {
                Log.e(str, "registerListener : already registered listener", new Exception());
            }
        }
    }

    public synchronized void unregisterListener(Listener listener) {
        synchronized (this.mListenerArray) {
            Log.v(TAG, "unregisterListener");
            this.mListenerArray.remove(listener);
        }
    }

    public synchronized boolean isInserted() {
        Boolean bool = this.mIsInserted;
        if (bool == null) {
            return this.mSpenGestureManagerWrapper.isSpenInserted();
        }
        return bool.booleanValue();
    }

    public Long getInsertedTime() {
        return this.mInsertedTime;
    }

    public Long getRemovedTime() {
        return this.mRemovedTime;
    }

    public Long getInsertedSystemClockTime() {
        return this.mInsertedSystemClockTime;
    }

    public Long getRemovedSystemClockTime() {
        return this.mRemovedSystemClockTime;
    }

    public void injectInsertionEvent(boolean z) {
        Intent intent = new Intent("com.samsung.pen.INSERT");
        intent.putExtra("penInsert", z);
        this.mBroadcastReceiver.onReceive(this.mContext, intent);
    }

    private void startMonitor() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.pen.INSERT");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private synchronized void stopMonitor() {
        this.mIsInserted = null;
        try {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "stopMonitor : e=" + e);
        }
    }
}
