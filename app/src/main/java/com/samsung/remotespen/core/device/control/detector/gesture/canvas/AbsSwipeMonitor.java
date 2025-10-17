package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;

/* loaded from: classes.dex */
public abstract class AbsSwipeMonitor extends CanvasEventMonitor {
    private static final float ERROR_FACTOR_THRESHOLD = 0.4f;
    public static final float MAX_ALLOWED_DIST_FROM_FIRST = 300.0f;
    private static final int SWIPE_ALLOWED_CRITICAL_COUNT = 1;
    private static final int SWIPE_ALLOWED_NOT_OK_COUNT = 2;
    private static final int SWIPE_SATISFACTION_MIN_OK_COUNT = 1;

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public long getElapsedTimeTooFast() {
        return 230L;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public long getElapsedTimeTooSlow() {
        return 830L;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getErrorFactorThreshold() {
        return ERROR_FACTOR_THRESHOLD;
    }

    public abstract boolean isMovedOverAllowed(Event event);

    public abstract boolean isMovedTooFarAway(Event event);

    public abstract boolean isReversed(Event event);

    public AbsSwipeMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onJudge(Event event, int[] iArr) {
        super.onJudge(event, iArr);
        JudgeScore judgeScore = getGestureScore().getJudgeScore();
        int angle = Utils.getAngle(iArr);
        int shape = Utils.getShape(iArr);
        String tag = getTag();
        Log.v(tag, "onJudge : Diff = " + event.deltaX + " ," + event.deltaY);
        if (shape == 4 || shape == 2) {
            if (angle < 110) {
                judgeScore.countCritical();
            } else {
                judgeScore.countNotOk();
            }
            String tag2 = getTag();
            Log.v(tag2, "onJudge : [Critical +" + judgeScore.getCriticalCount() + "] CIRCLE SHAPE");
        } else if (isReversed(event)) {
            judgeScore.countCritical();
            String tag3 = getTag();
            Log.v(tag3, "onJudge : [Critical +" + judgeScore.getCriticalCount() + "] REVERSE");
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
        if (judgeScore.getCriticalCount() >= 1) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getNotOkCount() >= 2) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getOkCount() > 1) {
            setJudgeState(JudgeState.SATISFACTION);
        } else {
            setJudgeState(JudgeState.INTERESTED);
        }
    }
}
