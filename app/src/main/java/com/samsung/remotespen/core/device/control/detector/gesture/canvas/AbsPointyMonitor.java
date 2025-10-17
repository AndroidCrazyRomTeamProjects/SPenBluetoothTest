package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public abstract class AbsPointyMonitor extends CanvasEventMonitor {
    public static final float ERROR_FACTOR_THRESHOLD = 0.1f;
    private static final float MAX_BALANCED_FACTOR_IN_HIGH_WEIGHT_CASE = 5.0f;
    private static final float MAX_BALANCED_FACTOR_IN_NORMAL_WEIGHT_CASE = 3.0f;
    private static final float MIN_BALANCED_FACTOR_IN_HIGH_WEIGHT_CASE = 0.2f;
    private static final float MIN_BALANCED_FACTOR_IN_NORMAL_WEIGHT_CASE = 0.33333334f;
    private static final float MIN_DEEP_RATIO = 0.3f;
    private static final float MIN_RANGE = 0.4f;
    public static final int NEGATIVE_DIRECTION = -1;
    private static final int POINTY_ALLOWED_CRITICAL_COUNT = 1;
    public static final int POSITIVE_DIRECTION = 1;
    private static final int THRESHOLD_DEGREE_ACUTE = 130;
    private static final int THRESHOLD_POINTY_DEGREE = 60;
    public static final PointF UNIT_VECTOR = new PointF(1.0f, 1.0f);
    public int mDirToExpect;
    private ArrayList<Event> mInverseEvents;
    private PointF mInversePoint;
    private boolean mIsReversed;
    private Event mLastEvent;
    private int mPointyShape;
    private Event mStartEvent;
    private int mStartIndex;

    public abstract int getDirToExpect();

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getErrorFactorThreshold() {
        return 0.1f;
    }

    public int getMaxAcuteDegree() {
        return 130;
    }

    public abstract PointF getUnitVector();

    public abstract boolean isReversed(Event event);

    public AbsPointyMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
        this.mIsReversed = false;
        this.mInverseEvents = new ArrayList<>();
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onInitialize() {
        super.onInitialize();
        this.mIsReversed = false;
        this.mStartEvent = null;
        this.mLastEvent = null;
        this.mDirToExpect = getDirToExpect();
        EventHistoryManager eventHistoryManager = getEventHistoryManager();
        this.mStartIndex = eventHistoryManager.getEventCount() + (-1) < 0 ? 0 : eventHistoryManager.getEventCount() - 1;
        this.mInversePoint = new PointF();
        this.mInverseEvents.clear();
        this.mPointyShape = 0;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onJudge(Event event, int[] iArr) {
        super.onJudge(event, iArr);
        String tag = getTag();
        GestureScore gestureScore = getGestureScore();
        JudgeScore judgeScore = gestureScore.getJudgeScore();
        int angle = Utils.getAngle(iArr);
        int shape = Utils.getShape(iArr);
        if (!this.mIsReversed && isReversed(event)) {
            Log.v(tag, "onJudge : reverse");
            if (getJudgeState() == JudgeState.SATISFACTION) {
                judgeScore.countCritical();
            } else if (getJudgeState() == JudgeState.INTERESTED && angle < getMaxAcuteDegree() && angle > 0) {
                Log.v(tag, "onJudge : Inverted");
                updateDirToExpect();
                this.mIsReversed = true;
                this.mPointyShape = shape;
                judgeScore.countOk();
                Log.v(tag, "onJudge : [OK +" + judgeScore.getOkCount() + "]");
                EventHistoryManager eventHistoryManager = getEventHistoryManager();
                this.mInverseEvents.add(eventHistoryManager.getEvent(eventHistoryManager.getEventCount() - 2));
            } else {
                judgeScore.countCritical();
                Log.v(tag, "onJudge : [Critical +" + judgeScore.getCriticalCount() + "] REVERSE");
            }
        } else if (this.mIsReversed && (shape == 4 || shape == 2)) {
            if (angle < 100 || (this.mPointyShape != shape && angle < getMaxAcuteDegree())) {
                judgeScore.countCritical();
                Log.v(tag, "onJudge : [Critical +" + judgeScore.getCriticalCount() + "] CIRCLE SHAPE");
            } else {
                EventHistoryManager eventHistoryManager2 = getEventHistoryManager();
                this.mInverseEvents.add(eventHistoryManager2.getEvent(eventHistoryManager2.getEventCount() - 2));
            }
        } else {
            setJudgeState(JudgeState.INTERESTED);
            judgeScore.countOk();
            Log.v(tag, "onJudge : [OK +" + judgeScore.getOkCount() + "]");
        }
        if (judgeScore.getCriticalCount() >= 1) {
            Log.v(tag, "onJudge : N/A. Critical Count = " + judgeScore.getCriticalCount());
            setJudgeState(JudgeState.NOT_APPLICABLE);
            onInitialize();
        }
        if (getJudgeState() == JudgeState.INTERESTED || getJudgeState() == JudgeState.SATISFACTION) {
            this.mLastEvent = event;
            this.mStartEvent = getEventHistoryManager().getEvent(this.mStartIndex);
            gestureScore.setStartIndex(this.mStartIndex);
        }
        if (this.mIsReversed) {
            updateDirToExpect();
            setJudgeState(JudgeState.SATISFACTION);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void getOngoingGestureScores(List<GestureScore> list) {
        if (this.mIsReversed) {
            updateInversePoint();
            if (isBalanced() && isDeepEnough()) {
                GestureScore gestureScore = getGestureScore();
                JudgeScore judgeScore = gestureScore.getJudgeScore();
                float calculateAngle = calculateAngle();
                if (calculateAngle < 60.0f) {
                    if (calculateAngleWithOppositeAxis() - calculateAngle > MAX_BALANCED_FACTOR_IN_HIGH_WEIGHT_CASE) {
                        judgeScore.countOk();
                    }
                    gestureScore.setStartIndex(this.mStartIndex);
                    setJudgeState(JudgeState.SATISFACTION);
                    setGestureQuality();
                    list.add(getGestureScore());
                }
            }
        }
        this.mNextMonitor.getOngoingGestureScores(list);
    }

    private void updateInversePoint() {
        int size = this.mInverseEvents.size();
        if (size == 1) {
            this.mInversePoint.x = this.mInverseEvents.get(0).x;
            this.mInversePoint.y = this.mInverseEvents.get(0).y;
            return;
        }
        PointF pointF = this.mInversePoint;
        pointF.x = 0.0f;
        pointF.y = 0.0f;
        for (int i = 0; i < this.mInverseEvents.size(); i++) {
            Event event = this.mInverseEvents.get(i);
            PointF pointF2 = this.mInversePoint;
            pointF2.x += event.x;
            pointF2.y += event.y;
        }
        PointF pointF3 = this.mInversePoint;
        float f = size;
        pointF3.x /= f;
        pointF3.y /= f;
    }

    private boolean isDeepEnough() {
        PointF unitVector = getUnitVector();
        Event event = this.mStartEvent;
        float f = event.x;
        Event event2 = this.mLastEvent;
        PointF pointF = new PointF(f + event2.x, event.y + event2.y);
        PointF directionVector = getDirectionVector();
        float distance = Utils.getDistance(directionVector.x, directionVector.y) / Utils.getDistance(pointF.x, pointF.y);
        RectF eventRange = getEventHistoryManager().getEventRange();
        float width = 1.0f / (eventRange.width() > eventRange.height() ? eventRange.width() : eventRange.height());
        eventRange.left *= width;
        eventRange.top *= width;
        eventRange.right *= width;
        eventRange.bottom *= width;
        float f2 = eventRange.left;
        float f3 = unitVector.x;
        float f4 = eventRange.top;
        float f5 = unitVector.y;
        RectF rectF = new RectF(f2 * f3, f4 * f5, eventRange.right * f3, eventRange.bottom * f5);
        float abs = Math.abs(((rectF.right - rectF.left) + rectF.top) - rectF.bottom);
        Log.d(getTag(), "isDeepEnough: delta = " + abs + ", deepRatio = " + distance);
        if (abs <= MIN_RANGE || distance <= 0.3f) {
            Log.d(getTag(), "isDeepEnough : not enough depth");
            return false;
        }
        return true;
    }

    private PointF getDirectionVector() {
        Event event = this.mStartEvent;
        float f = event.x;
        Event event2 = this.mLastEvent;
        float f2 = (float) ((f + event2.x) * 0.5d);
        PointF pointF = this.mInversePoint;
        return new PointF(pointF.x - f2, pointF.y - ((float) ((event.y + event2.y) * 0.5d)));
    }

    private float calculateAngle() {
        PointF directionVector = getDirectionVector();
        String tag = getTag();
        Log.d(tag, "pointyDir : " + directionVector);
        PointF unitVector = getUnitVector();
        String tag2 = getTag();
        Log.d(tag2, "axisPoint : " + unitVector);
        int angle3p = Utils.getAngle3p(unitVector.x, unitVector.y, directionVector.x, directionVector.y);
        String tag3 = getTag();
        Log.d(tag3, "calculateAngle : angle = " + angle3p);
        return angle3p;
    }

    private float calculateAngleWithOppositeAxis() {
        PointF directionVector = getDirectionVector();
        PointF oppositeUnitVector = getOppositeUnitVector();
        PointF pointF = new PointF(oppositeUnitVector.x * (-1.0f), oppositeUnitVector.y * (-1.0f));
        int angle3p = Utils.getAngle3p(oppositeUnitVector.x, oppositeUnitVector.y, directionVector.x, directionVector.y);
        int angle3p2 = Utils.getAngle3p(pointF.x, pointF.y, directionVector.x, directionVector.y);
        return angle3p < angle3p2 ? angle3p : angle3p2;
    }

    private boolean isBalanced() {
        String tag = getTag();
        Log.v(tag, "isBalanced : startEvent = " + this.mStartEvent);
        String tag2 = getTag();
        Log.v(tag2, "isBalanced : mInversePoint = " + this.mInversePoint);
        String tag3 = getTag();
        Log.v(tag3, "isBalanced : lastEvent = " + this.mLastEvent);
        PointF pointF = this.mInversePoint;
        float f = pointF.x;
        Event event = this.mStartEvent;
        float distance = Utils.getDistance(f - event.x, pointF.y - event.y);
        PointF pointF2 = this.mInversePoint;
        float f2 = pointF2.x;
        Event event2 = this.mLastEvent;
        float distance2 = Utils.getDistance(f2 - event2.x, pointF2.y - event2.y);
        String tag4 = getTag();
        Log.v(tag4, "isBalanced : len1 = " + distance + ", len2 = " + distance2);
        float minBalancedFactor = getMinBalancedFactor();
        float maxBalancedFactor = getMaxBalancedFactor();
        float f3 = distance2 != 0.0f ? distance / distance2 : maxBalancedFactor;
        String tag5 = getTag();
        Log.d(tag5, "isBalanced : ratio = " + f3);
        if (f3 <= minBalancedFactor || f3 >= maxBalancedFactor) {
            Log.d(getTag(), "isBalanced : Balanced broken.");
            return false;
        }
        return true;
    }

    private float getMaxBalancedFactor() {
        return this.mBoostWeight == CanvasEventMonitor.BoostWeight.HIGH ? MAX_BALANCED_FACTOR_IN_HIGH_WEIGHT_CASE : MAX_BALANCED_FACTOR_IN_NORMAL_WEIGHT_CASE;
    }

    private float getMinBalancedFactor() {
        return this.mBoostWeight == CanvasEventMonitor.BoostWeight.HIGH ? MIN_BALANCED_FACTOR_IN_HIGH_WEIGHT_CASE : MIN_BALANCED_FACTOR_IN_NORMAL_WEIGHT_CASE;
    }

    public void updateDirToExpect() {
        this.mDirToExpect = this.mDirToExpect == 1 ? -1 : 1;
    }

    private PointF getOppositeUnitVector() {
        PointF unitVector = getUnitVector();
        PointF pointF = new PointF();
        pointF.x = unitVector.x == 0.0f ? 1.0f : 0.0f;
        pointF.y = unitVector.y == 0.0f ? 1.0f : 0.0f;
        return pointF;
    }
}
