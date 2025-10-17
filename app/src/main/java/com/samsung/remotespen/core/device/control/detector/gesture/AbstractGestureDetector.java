package com.samsung.remotespen.core.device.control.detector.gesture;

import android.util.Log;

import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import java.util.HashSet;
import java.util.List;

/* loaded from: classes.dex */
public abstract class AbstractGestureDetector {
    public static final int EVENT_END = 2;
    public static final int EVENT_MOVE = 1;
    public static final int EVENT_START = 0;
    private static final float MAX_IMPURITY_RATIO = 0.3f;
    private static final float MAX_IMPURITY_RATIO_OF_SWIPE_UP_DOWN = 0.7f;
    private static final String TAG = "AbstractGestureDetector";
    private GestureListener mGestureListener;
    public EventHistoryManager mRawEventHistoryManager;
    public MonitorState mLastMonitorState = MonitorState.IDLE;
    public BleSpenGestureEvent mDetectedGesture = null;
    public RepeatMode mRepeatMode = RepeatMode.REPEAT_END_BY_END;

    /* loaded from: classes.dex */
    public interface GestureListener {
        void onGesture(BleSpenGestureEvent bleSpenGestureEvent);
    }

    /* loaded from: classes.dex */
    public static class Noise {
        public boolean isImpurity = false;
        public boolean isHovered = false;
        public boolean isApproached = false;
        public boolean isScreenTouching = false;
    }

    /* loaded from: classes.dex */
    public enum RepeatMode {
        REPEAT_END_BY_MOVE,
        REPEAT_END_BY_END
    }

    public abstract BleSpenGestureEvent getMostProbableGesture();

    public abstract void initGestureMonitors();

    public abstract void onMove(int i, float f, float f2, Noise noise);

    public abstract void onPauseMonitoring(List<BleSpenGestureType> list);

    public abstract void onResumeMonitoring();

    public void setBoostGestures(HashSet<BleSpenGestureType> hashSet) {
    }

    public AbstractGestureDetector() {
        initEventHistoryManager();
        initGestureMonitors();
    }

    private void initEventHistoryManager() {
        this.mRawEventHistoryManager = new EventHistoryManager();
    }

    public HashSet<BleSpenGestureType> getBoostGestures() {
        return new HashSet<>();
    }

    public void setGestureListener(GestureListener gestureListener) {
        this.mGestureListener = gestureListener;
    }

    public void notifyGestureDetectionResult(BleSpenGestureEvent bleSpenGestureEvent) {
        this.mDetectedGesture = bleSpenGestureEvent.m19clone();
        GestureListener gestureListener = this.mGestureListener;
        if (gestureListener != null) {
            gestureListener.onGesture(bleSpenGestureEvent);
        }
    }

    public boolean isHighImpurityGesture(BleSpenGestureType bleSpenGestureType) {
        float impurityEventCount = this.mRawEventHistoryManager.getImpurityEventCount() / this.mRawEventHistoryManager.getActualEventCount();
        Log.d(TAG, "impurityRatio = " + impurityEventCount);
        return impurityEventCount >= ((bleSpenGestureType == BleSpenGestureType.SWIPE_UP || bleSpenGestureType == BleSpenGestureType.SWIPE_DOWN || bleSpenGestureType == BleSpenGestureType.POINTY_UP || bleSpenGestureType == BleSpenGestureType.POINTY_DOWN || bleSpenGestureType == BleSpenGestureType.SHAKE) ? 0.7f : 0.3f);
    }

    public boolean checkNeedToNotifyDetection(MonitorState monitorState) {
        MonitorState monitorState2 = this.mLastMonitorState;
        MonitorState monitorState3 = MonitorState.STAY_DETECTED;
        if (monitorState2 != monitorState3) {
            return monitorState == monitorState3 || monitorState == MonitorState.FINISH_DETECTED;
        }
        return false;
    }

    public void pauseDetection(List<BleSpenGestureType> list) {
        onPauseMonitoring(list);
    }

    public void resumeDetection() {
        onResumeMonitoring();
    }

    public void onMove(int i, float f, float f2, boolean z) {
        Noise noise = new Noise();
        noise.isImpurity = z;
        onMove(i, f, f2, noise);
    }

    public BleSpenGestureType getDetectedGesture() {
        return this.mDetectedGesture.getGestureType();
    }

    public BleSpenGestureEvent.Action getDetectedGestureState() {
        return this.mDetectedGesture.getAction();
    }
}
