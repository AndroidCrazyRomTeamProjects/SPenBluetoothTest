package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import android.graphics.RectF;
import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciGestureQuaility;

/* loaded from: classes.dex */
public abstract class AbsCircleMonitor extends DavinciEventMonitor {
    public static final int CIRCLE_ALLOWED_CRITICAL_COUNT = 3;
    public static final int CIRCLE_ALLOWED_NOT_OK_COUNT = 5;
    public static final int CIRCLE_SATISFACTION_MIN_OK_COUNT = 6;
    public static final float CIRCLE_SATISFACTION_RATIO_H = 0.35f;
    public static final float CIRCLE_SATISFACTION_RATIO_W = 2.857143f;
    private static final float CIRCLE_TOO_SMALL_SIZE = 0.3f;
    private static final int ELAPSED_TIME_TOO_FAST = 450;
    private static final int ELAPSED_TIME_TOO_SLOW = 1500;
    public static final float MIN_DISTANCE = 0.001f;
    public static final int ROUND_BREAK_AWAY_ANGLE = 100;
    public static final int ROUND_SATISFACTION_ANGLE = 90;
    private static final String TAG = "AbsCircleMonitor";
    private long mStartTime;

    public abstract int getNegativeDirection();

    public abstract int getPositiveDirection();

    public AbsCircleMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void onInitialize() {
        this.mStartTime = System.currentTimeMillis();
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public MonitorState onJudge(Event event, int[] iArr) {
        String tag = getTag();
        JudgeScore judgeScore = getGestureScore().getJudgeScore();
        int shape = Utils.getShape(iArr);
        if (shape == getPositiveDirection()) {
            judgeScore.countOk();
        } else if (shape == getNegativeDirection()) {
            if (getJudgeState() == JudgeState.SATISFACTION) {
                judgeScore.countNotOk();
                Log.v(tag, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] Negative direction");
            } else {
                judgeScore.countCritical();
                Log.v(tag, "onJudge : [Critical +" + judgeScore.getNotOkCount() + "] Negative direction");
            }
        } else if (shape == 1) {
            judgeScore.countNotOk();
            Log.v(tag, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] SHAPE_3P_IN_LINE");
        } else if (event.absDeltaX < 0.001f && event.absDeltaY < 0.001f) {
            judgeScore.countNotOk();
            Log.v(tag, "onJudge : [NotOK +" + judgeScore.getNotOkCount() + "] MIN_DISTANCE");
        }
        if (judgeScore.getCriticalCount() >= 3) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getNotOkCount() >= 5) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else if (judgeScore.getOkCount() >= 6) {
            int calculateAngle = calculateAngle(getRawEventHistoryManager());
            JudgeState judgeState = getJudgeState();
            JudgeState judgeState2 = JudgeState.SATISFACTION;
            if (judgeState == judgeState2 && calculateAngle > 100) {
                setJudgeState(JudgeState.NOT_APPLICABLE);
            } else {
                boolean hasValidRatio = hasValidRatio(getRawEventHistoryManager().getEventRange());
                if (hasValidRatio && calculateAngle < 90) {
                    setJudgeState(judgeState2);
                    setGestureQuality();
                } else if (!hasValidRatio || getJudgeState() != judgeState2) {
                    setJudgeState(JudgeState.INTERESTED);
                }
            }
        } else {
            setJudgeState(JudgeState.INTERESTED);
        }
        Log.v(tag, "onJudge : " + judgeScore.toString());
        Log.v(tag, "onJudge : JudgeState = " + getJudgeState());
        Log.v(tag, "onJudge : ----------------------------------------------------");
        return MonitorState.MONITORING;
    }

    private static boolean hasValidRatio(RectF rectF) {
        float width = rectF.width() / rectF.height();
        String str = TAG;
        Log.d(str, "hasValidRatio : ratio = " + width);
        if (width < 0.35f || width > 2.857143f) {
            Log.d(str, "hasValidRatio : Not Satisfying ratio = " + width);
            return false;
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x004f  */
    /* JADX WARN: Removed duplicated region for block: B:26:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static int calculateAngle(com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager r9) {
        /*
            r0 = 180(0xb4, float:2.52E-43)
            if (r9 != 0) goto Lc
            java.lang.String r9 = com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor.TAG
            java.lang.String r1 = "calculateAngle : eventHistoryManager is null"
            com.samsung.util.debug.Log.e(r9, r1)
            return r0
        Lc:
            int r1 = r9.getEventCount()
            int r2 = r9.getEventCount()
            int r2 = r2 + (-1)
            com.samsung.remotespen.core.device.control.detector.gesture.Event r2 = r9.getEvent(r2)
            double r3 = r2.travelLength
            r5 = 4611686018427387904(0x4000000000000000, double:2.0)
            double r3 = r3 / r5
            r5 = 0
            int r6 = r1 / 2
            com.samsung.remotespen.core.device.control.detector.gesture.Event r7 = r9.getEvent(r6)
            double r7 = r7.travelLength
            int r7 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r7 >= 0) goto L3c
        L2c:
            int r6 = r6 + 1
            if (r6 >= r1) goto L4d
            com.samsung.remotespen.core.device.control.detector.gesture.Event r7 = r9.getEvent(r6)
            double r7 = r7.travelLength
            int r7 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r7 < 0) goto L3b
            goto L48
        L3b:
            goto L2c
        L3c:
            if (r6 < 0) goto L4d
            com.samsung.remotespen.core.device.control.detector.gesture.Event r1 = r9.getEvent(r6)
            double r7 = r1.travelLength
            int r1 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r1 < 0) goto L4a
        L48:
            r5 = r6
            goto L4d
        L4a:
            int r6 = r6 + (-1)
            goto L3c
        L4d:
            if (r5 <= 0) goto L8f
            int r0 = r5 + (-1)
            com.samsung.remotespen.core.device.control.detector.gesture.Event r0 = r9.getEvent(r0)
            com.samsung.remotespen.core.device.control.detector.gesture.Event r9 = r9.getEvent(r5)
            double r5 = r9.travelLength
            double r7 = r0.travelLength
            double r5 = r5 - r7
            double r3 = r3 - r7
            double r3 = r3 / r5
            float r1 = (float) r3
            float r3 = r0.x
            float r4 = r9.deltaX
            float r4 = r4 * r1
            float r3 = r3 + r4
            float r0 = r0.y
            float r9 = r9.deltaY
            float r9 = r9 * r1
            float r0 = r0 + r9
            float r9 = -r3
            float r1 = -r0
            float r4 = r2.x
            float r4 = r4 - r3
            float r2 = r2.y
            float r2 = r2 - r0
            int r0 = com.samsung.remotespen.core.device.control.detector.gesture.Utils.getAngle3p(r9, r1, r4, r2)
            java.lang.String r9 = com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "checkIsRound : Angle="
            r1.append(r2)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            com.samsung.util.debug.Log.d(r9, r1)
        L8f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor.calculateAngle(com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager):int");
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void updateTravelDistance(GestureScore gestureScore, Event event) {
        double distance = Utils.getDistance(event.deltaX, event.deltaY);
        gestureScore.increaseTravelDistance(distance);
        gestureScore.increaseRawTravelDistance(distance);
    }

    private void setGestureQuality() {
        DavinciGestureQuaility.GestureQuality gestureQuality;
        long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
        GestureScore gestureScore = getGestureScore();
        if (currentTimeMillis > 1500) {
            gestureQuality = DavinciGestureQuaility.GestureQuality.TOO_SLOW;
        } else if (currentTimeMillis <= 450) {
            gestureQuality = DavinciGestureQuaility.GestureQuality.TOO_FAST;
        } else if (getRawEventHistoryManager().getEventRange().width() <= 0.3f && getRawEventHistoryManager().getEventRange().height() <= 0.3f) {
            gestureQuality = DavinciGestureQuaility.GestureQuality.TOO_SMALL;
        } else {
            gestureQuality = DavinciGestureQuaility.GestureQuality.GOOD;
        }
        gestureScore.setGestureQuality(new DavinciGestureQuaility(gestureQuality));
    }
}
