package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciGestureQuaility;

/* loaded from: classes.dex */
public abstract class AbsSwipeMonitor extends DavinciEventMonitor {
    private static final int ELAPSED_TIME_TOO_FAST = 200;
    private static final int ELAPSED_TIME_TOO_SLOW = 800;
    public static final float MAX_ALLOWED_DIST_FROM_FIRST = 0.3f;
    public static final float MIN_DISTANCE = 0.02f;
    private static final int SWIPE_ALLOWED_CRITICAL_COUNT = 4;
    private static final int SWIPE_ALLOWED_NOT_OK_COUNT = 10;
    private static final float SWIPE_SATISFACTION_MIN_DISTANCE = 0.4f;
    private static final int SWIPE_SATISFACTION_MIN_OK_COUNT = 2;
    private long mStartTime;

    public abstract boolean isMovedOverAllowed(Event event);

    public abstract boolean isMovedTooFarAway(Event event);

    public abstract boolean isReversed(Event event);

    public abstract boolean isSlightMotion(Event event);

    public abstract void onUpdateTravelDistance(GestureScore gestureScore, Event event);

    public AbsSwipeMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void onInitialize() {
        Log.d(getTag(), "onInitialize");
        this.mStartTime = System.currentTimeMillis();
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public MonitorState onJudge(Event event, int[] iArr) {
        GestureScore gestureScore = getGestureScore();
        JudgeScore judgeScore = gestureScore.getJudgeScore();
        String tag = getTag();
        Log.v(tag, "onJudge : Diff = " + event.deltaX + " ," + event.deltaY);
        if (isReversed(event)) {
            judgeScore.countCritical();
            String tag2 = getTag();
            Log.v(tag2, "onJudge : [Critical +" + judgeScore.getCriticalCount() + "] REVERSE");
        } else if (isSlightMotion(event)) {
            judgeScore.countNotOk();
            String tag3 = getTag();
            Log.v(tag3, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] SLIGHT MOTION");
        } else if (isMovedOverAllowed(event)) {
            judgeScore.countNotOk();
            String tag4 = getTag();
            Log.v(tag4, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] ALLOWED_DIST_IN_ONCE");
        } else if (isMovedTooFarAway(event)) {
            judgeScore.countNotOk();
            String tag5 = getTag();
            Log.v(tag5, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] MAX_ALLOWED_DIST_FROM_FIRST");
        } else {
            judgeScore.countOk();
            String tag6 = getTag();
            Log.v(tag6, "onJudge : [OK +" + judgeScore.getOkCount() + "]");
        }
        if (judgeScore.getCriticalCount() >= 4) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getNotOkCount() >= 10) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getOkCount() >= 2 || gestureScore.getTravelDistance() > 0.4000000059604645d) {
            setGestureQuality();
            setJudgeState(JudgeState.SATISFACTION);
        } else {
            setJudgeState(JudgeState.INTERESTED);
        }
        return MonitorState.MONITORING;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void updateTravelDistance(GestureScore gestureScore, Event event) {
        onUpdateTravelDistance(gestureScore, event);
        gestureScore.increaseRawTravelDistance(Utils.getDistance(event.deltaX, event.deltaY));
    }

    private void setGestureQuality() {
        DavinciGestureQuaility.GestureQuality gestureQuality;
        long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
        GestureScore gestureScore = getGestureScore();
        if (currentTimeMillis > 800) {
            gestureQuality = DavinciGestureQuaility.GestureQuality.TOO_SLOW;
        } else if (currentTimeMillis <= 200) {
            gestureQuality = DavinciGestureQuaility.GestureQuality.TOO_FAST;
        } else {
            gestureQuality = DavinciGestureQuaility.GestureQuality.GOOD;
        }
        gestureScore.setGestureQuality(new DavinciGestureQuaility(gestureQuality));
    }
}
