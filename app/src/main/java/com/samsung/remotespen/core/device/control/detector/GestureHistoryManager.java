package com.samsung.remotespen.core.device.control.detector;

import android.content.Context;
import com.samsung.remotespen.core.device.control.detector.GestureHistoryInfo;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.util.StringArray;
import com.samsung.util.debug.Log;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class GestureHistoryManager {
    private static final int HISTORY_DUMP_VERSION = 1;
    private static final int MAX_HISTORY_COUNT = 50;
    private static final String TAG = "GestureHistoryManager";
    private static GestureHistoryManager sInstance;
    private GestureHistoryInfo mGestureHistoryInfo;
    private StringArray mPastHistory = new StringArray();

    public static synchronized GestureHistoryManager getInstance() {
        GestureHistoryManager gestureHistoryManager;
        synchronized (GestureHistoryManager.class) {
            if (sInstance == null) {
                sInstance = new GestureHistoryManager();
            }
            gestureHistoryManager = sInstance;
        }
        return gestureHistoryManager;
    }

    private GestureHistoryManager() {
        if (this.mGestureHistoryInfo == null) {
            this.mGestureHistoryInfo = new GestureHistoryInfo();
        }
    }

    public synchronized void markRawEvent(int i, int i2) {
        this.mGestureHistoryInfo.addRawVertex(new GestureHistoryInfo.Vertex(i, i2));
    }

    public synchronized void markState(String str) {
        this.mGestureHistoryInfo.setState(str);
    }

    public synchronized void markStartTime() {
        this.mGestureHistoryInfo.setStartDate();
    }

    public synchronized void markEndTime() {
        this.mGestureHistoryInfo.setEndDate();
    }

    public synchronized void markGestureType(String str) {
        String str2 = TAG;
        Log.v(str2, "markGestureType : " + str);
        this.mGestureHistoryInfo.setGestureType(str);
        ensurePastHistoryCountNotOverflow();
        this.mPastHistory.add(this.mGestureHistoryInfo.toString());
        this.mGestureHistoryInfo.init();
    }

    public synchronized StringArray getHistory(Context context, SpenModelName spenModelName) {
        StringArray stringArray;
        stringArray = new StringArray();
        if (spenModelName != null) {
            BleSpenDeviceFeature deviceFeature = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature();
            stringArray.add("penType=" + deviceFeature.getPenTypeString() + ", maxMovement=" + deviceFeature.getMaxMotionMovementValue() + ", minGestureSize=" + deviceFeature.getMotionStayRangeThreshold() + ", historyDumpVersion=1");
        } else {
            stringArray.add("penType=no_bundle_spen, historyDumpVersion=1");
        }
        stringArray.addAll(this.mPastHistory);
        return stringArray;
    }

    public synchronized void clearHistory() {
        String str = TAG;
        Log.d(str, "clearHistory : clears " + this.mPastHistory.size() + " items");
        this.mPastHistory.clear();
    }

    private synchronized void ensurePastHistoryCountNotOverflow() {
        while (this.mPastHistory.size() >= 50) {
            this.mPastHistory.remove(0);
        }
    }
}
