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
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.device.data.BleSpenGestureSize;
import java.util.List;

/* loaded from: classes.dex */
public abstract class AbsCircleMonitor extends CanvasEventMonitor {
    private static final int CIRCLE_ALLOWED_CRITICAL_COUNT = 2;
    private static final int CIRCLE_ALLOWED_NOT_OK_COUNT = 5;
    private static final float CIRCLE_SATISFACTION_RATIO_H_IN_LOW_WEIGHT_CASE = 0.55f;
    private static final float CIRCLE_SATISFACTION_RATIO_H_IN_NORMAL_WEIGHT_CASE = 0.35f;
    private static final float CIRCLE_SATISFACTION_RATIO_W_IN_LOW_WEIGHT_CASE = 1.8181819f;
    private static final float CIRCLE_SATISFACTION_RATIO_W_IN_NORMAL_WEIGHT_CASE = 2.857143f;
    private static final float CIRCLE_TOO_SMALL_SIZE = 0.8f;
    private static final float MIN_DISTANCE = 0.001f;
    private static final int MIN_REVERSED_COUNT = 2;
    private static final int ROUND_SATISFACTION_ANGLE = 65;
    private float mDistanceToClose;
    private int mEndIndex;
    private float mMinDeltaDistance;
    private Event mPrevEvent;
    private int mReversedPointCount;

    public abstract int getNegativeDirection();

    public abstract int getPositiveDirection();

    public AbsCircleMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
        this.mReversedPointCount = 0;
        this.mDistanceToClose = Float.MAX_VALUE;
        this.mEndIndex = 0;
        this.mMinDeltaDistance = 0.0f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onInitialize() {
        super.onInitialize();
        this.mPrevEvent = null;
        this.mReversedPointCount = 0;
        this.mEndIndex = 0;
        this.mDistanceToClose = Float.MAX_VALUE;
        this.mMinDeltaDistance = getAverageDeltaDistance(getRawEventHistoryManager()) * 0.3f;
    }

    /* JADX WARN: Removed duplicated region for block: B:38:0x0117  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0126  */
    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void onJudge(com.samsung.remotespen.core.device.control.detector.gesture.Event r8, int[] r9) {
        /*
            Method dump skipped, instructions count: 359
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsCircleMonitor.onJudge(com.samsung.remotespen.core.device.control.detector.gesture.Event, int[]):void");
    }

    private float getAverageDeltaDistance(EventHistoryManager eventHistoryManager) {
        return eventHistoryManager.getAverageDelta(0, eventHistoryManager.getEventCount() - 1);
    }

    private void updateEndIndex(Event event) {
        Event event2 = getEventHistoryManager().getEvent(getGestureScore().getStartIndex());
        int eventCount = getEventHistoryManager().getEventCount() - 1;
        PointF pointF = new PointF(event.x - event2.x, event.y - event2.y);
        float abs = Math.abs(Utils.getDistance(pointF.x, pointF.y));
        if (abs < this.mDistanceToClose) {
            this.mDistanceToClose = abs;
            int endIndex = getGestureScore().getEndIndex() + 1;
            this.mEndIndex = endIndex;
            if (endIndex + 1 <= eventCount) {
                this.mEndIndex = endIndex + 1;
            }
        }
    }

    private boolean isClosedCompletely() {
        PointF crossPoint;
        EventHistoryManager eventHistoryManager = getEventHistoryManager();
        int eventCount = eventHistoryManager.getEventCount() - 1;
        int findFirstIndex = findFirstIndex(eventHistoryManager, this.mEndIndex);
        int i = this.mEndIndex;
        if (i < eventCount) {
            i++;
        }
        for (int i2 = findFirstIndex > 0 ? findFirstIndex - 1 : 0; i2 < findFirstIndex + 1; i2++) {
            int i3 = this.mEndIndex - 2;
            while (i3 < i) {
                Event event = eventHistoryManager.getEvent(i2);
                int i4 = i2 + 1;
                Event event2 = eventHistoryManager.getEvent(i4);
                Event event3 = eventHistoryManager.getEvent(i3);
                int i5 = i3 + 1;
                Event event4 = eventHistoryManager.getEvent(i5);
                RectF containRange = getContainRange(event, event2);
                RectF containRange2 = getContainRange(event3, event4);
                if (containRange.intersect(containRange2) && (crossPoint = getCrossPoint(event, event2, event3, event4)) != null && getIntersectRange(containRange, containRange2).contains(crossPoint.x, crossPoint.y)) {
                    if (Utils.getDistance(event2.x - crossPoint.x, event2.y - crossPoint.y) + Utils.getDistance(crossPoint.x - event3.x, crossPoint.y - event3.y) + eventHistoryManager.getTravelLength(i4, i3) < eventHistoryManager.getTravelLength() * 0.20000000298023224d) {
                        Log.d(getTag(), "isClosedCompletely : closed circle but circle travel length not exceed 20% of total length");
                        return false;
                    }
                    Log.d(getTag(), "isClosedCompletely : Circle is closed completely");
                    return true;
                }
                i3 = i5;
            }
        }
        return false;
    }

    private RectF getIntersectRange(RectF rectF, RectF rectF2) {
        RectF rectF3 = new RectF();
        rectF3.left = Math.max(rectF.left, rectF2.left);
        rectF3.top = Math.max(rectF.top, rectF2.top);
        rectF3.right = Math.min(rectF.right, rectF2.right);
        rectF3.bottom = Math.min(rectF.bottom, rectF2.bottom);
        return rectF3;
    }

    private RectF getContainRange(Event event, Event event2) {
        RectF rectF = new RectF();
        rectF.left = Math.min(event.x, event2.x);
        rectF.top = Math.min(event.y, event2.y);
        rectF.right = Math.max(event.x, event2.x);
        rectF.bottom = Math.max(event.y, event2.y);
        return rectF;
    }

    private PointF getCrossPoint(Event event, Event event2, Event event3, Event event4) {
        float f = event.x;
        float f2 = event2.x;
        float f3 = f != f2 ? (event2.y - event.y) / (f2 - f) : 1000000.0f;
        float f4 = event.y - (f * f3);
        float f5 = event3.x;
        float f6 = event4.x;
        float f7 = f5 != f6 ? (event4.y - event3.y) / (f6 - f5) : 1000000.0f;
        float f8 = event3.y - (f5 * f7);
        float f9 = f3 - f7;
        if (Math.abs(f9) < 1.0E-6d) {
            return null;
        }
        float f10 = (f8 - f4) / f9;
        return new PointF(f10, (f3 * f10) + f4);
    }

    private RectF getCircleRange(int i, int i2) {
        EventHistoryManager eventHistoryManager = getEventHistoryManager();
        float f = Float.MIN_VALUE;
        float f2 = Float.MAX_VALUE;
        float f3 = Float.MAX_VALUE;
        float f4 = Float.MIN_VALUE;
        while (i <= i2) {
            Event event = eventHistoryManager.getEvent(i);
            float f5 = event.x;
            float f6 = event.y;
            if (f2 > f5) {
                f2 = f5;
            } else if (f < f5) {
                f = f5;
            }
            if (f3 > f6) {
                f3 = f6;
            } else if (f4 < f6) {
                f4 = f6;
            }
            i++;
        }
        return new RectF(f2, f3, f, f4);
    }

    private int findFirstIndex(EventHistoryManager eventHistoryManager, int i) {
        int startIndex = getGestureScore().getStartIndex();
        Event event = eventHistoryManager.getEvent(i);
        float f = Float.MAX_VALUE;
        for (int startIndex2 = getGestureScore().getStartIndex(); startIndex2 < getGestureScore().getStartIndex() + getCircleSatisfactionMinOkCount(); startIndex2++) {
            Event event2 = eventHistoryManager.getEvent(startIndex2);
            PointF pointF = new PointF(event.x - event2.x, event.y - event2.y);
            float abs = Math.abs(Utils.getDistance(pointF.x, pointF.y));
            if (abs < f) {
                startIndex = startIndex2;
                f = abs;
            }
        }
        return startIndex;
    }

    private boolean isHalfCircle(EventHistoryManager eventHistoryManager, int i, int i2) {
        RectF eventRange = eventHistoryManager.getEventRange();
        float width = 1.0f / eventRange.width();
        float height = 1.0f / eventRange.height();
        EventHistoryManager eventHistoryManager2 = new EventHistoryManager();
        for (int i3 = 0; i3 <= i2; i3++) {
            Event event = eventHistoryManager.getEvent(i3);
            eventHistoryManager2.addEvent(new Event(event.deltaX * width, event.deltaY * height));
        }
        int calculateStartCenterEnd3pAngle = calculateStartCenterEnd3pAngle(eventHistoryManager2, i, i2);
        Log.d(getTag(), "isHalfCircle : angle = " + calculateStartCenterEnd3pAngle);
        return calculateStartCenterEnd3pAngle > 30;
    }

    private int calculateStartCenterEnd3pAngle(EventHistoryManager eventHistoryManager, int i, int i2) {
        PointF centerPoint = getCenterPoint(eventHistoryManager, i, i2);
        Event event = eventHistoryManager.getEvent(i2);
        float f = centerPoint.x;
        float f2 = centerPoint.y;
        int angle3p = Utils.getAngle3p(-f, -f2, event.x - f, event.y - f2);
        String tag = getTag();
        Log.d(tag, "calculateStartCenterEnd3pAngle : Angle=" + angle3p);
        return angle3p;
    }

    private static PointF getCenterPoint(EventHistoryManager eventHistoryManager, int i, int i2) {
        int i3 = (i2 - i) + 1;
        double d = eventHistoryManager.getEvent(i2).travelLength / 2.0d;
        int i4 = (i3 / 2) + i;
        int i5 = 0;
        if (eventHistoryManager.getEvent(i4).travelLength < d) {
            do {
                i4++;
                if (i4 >= i3) {
                    break;
                }
            } while (eventHistoryManager.getEvent(i4).travelLength < d);
            i5 = i4;
            break;
        }
        while (i4 >= i) {
            if (eventHistoryManager.getEvent(i4).travelLength >= d) {
                i5 = i4;
                break;
            }
            i4--;
        }
        PointF pointF = new PointF(0.0f, 0.0f);
        if (i5 > 0) {
            Event event = eventHistoryManager.getEvent(i5 - 1);
            Event event2 = eventHistoryManager.getEvent(i5);
            double d2 = event2.travelLength;
            double d3 = event.travelLength;
            float f = (float) ((d - d3) / (d2 - d3));
            pointF.x = event.x + (event2.deltaX * f);
            pointF.y = event.y + (event2.deltaY * f);
        }
        return pointF;
    }

    private int getCircleSatisfactionMinOkCount() {
        int normalizedVertexCount = (int) (getNormalizedVertexCount() * 0.5f);
        if (normalizedVertexCount > 4) {
            return normalizedVertexCount;
        }
        return 4;
    }

    private boolean hasValidRatio(RectF rectF) {
        float width = rectF.width() / rectF.height();
        String tag = getTag();
        Log.d(tag, "hasValidRatio : ratio = " + width);
        if (width < getCircleSatisfactionRatioH() || width > getCircleSatisfactionRatioW()) {
            String tag2 = getTag();
            Log.d(tag2, "hasValidRatio : Not Satisfying ratio = " + width);
            return false;
        }
        return true;
    }

    private float getCircleSatisfactionRatioH() {
        if (this.mBoostWeight == CanvasEventMonitor.BoostWeight.LOW) {
            return CIRCLE_SATISFACTION_RATIO_H_IN_LOW_WEIGHT_CASE;
        }
        return 0.35f;
    }

    private float getCircleSatisfactionRatioW() {
        if (this.mBoostWeight == CanvasEventMonitor.BoostWeight.LOW) {
            return CIRCLE_SATISFACTION_RATIO_W_IN_LOW_WEIGHT_CASE;
        }
        return 2.857143f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void setGestureSize(BleSpenGestureQuality bleSpenGestureQuality) {
        RectF eventRange = getRawEventHistoryManager().getEventRange();
        float width = eventRange.width();
        float height = eventRange.height();
        String tag = getTag();
        Log.d(tag, "setGestureQuality: width = " + width + ", height = " + height);
        if (width <= CIRCLE_TOO_SMALL_SIZE && height <= CIRCLE_TOO_SMALL_SIZE) {
            bleSpenGestureQuality.setGestureSize(BleSpenGestureSize.TOO_SMALL);
        } else {
            bleSpenGestureQuality.setGestureSize(BleSpenGestureSize.GOOD);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getErrorFactorThreshold() {
        RectF eventRange = getRawEventHistoryManager().getEventRange();
        float width = eventRange.width();
        float height = eventRange.height();
        Log.v(getTag(), "getErrorFactorThreshold : width = " + width + ", height = " + height);
        if (width <= 0.5d || height <= 0.5d) {
            double min = Math.min(width, height);
            float f = (min > 1.5d ? 1.0f : (float) (min / 1.5d)) * 0.015f;
            Log.v(getTag(), "getErrorFactorThreshold : threshold = " + f);
            return f;
        }
        return 0.03f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void getOngoingGestureScores(List<GestureScore> list) {
        JudgeScore judgeScore = getGestureScore().getJudgeScore();
        JudgeState judgeState = getJudgeState();
        JudgeState judgeState2 = JudgeState.INTERESTED;
        if (judgeState == judgeState2 && judgeScore.getOkCount() > getCircleSatisfactionMinOkCount()) {
            int findFirstIndex = findFirstIndex(getEventHistoryManager(), this.mEndIndex);
            boolean hasValidRatio = hasValidRatio(getCircleRange(findFirstIndex, this.mEndIndex));
            boolean isClosedCompletely = isClosedCompletely();
            int calculateStartCenterEnd3pAngle = calculateStartCenterEnd3pAngle(getEventHistoryManager(), findFirstIndex, this.mEndIndex);
            boolean isHalfCircle = isHalfCircle(getEventHistoryManager(), findFirstIndex, this.mEndIndex);
            String tag = getTag();
            Log.v(tag, "getOngoingGestureScores : startCenterEnd3pAngle = " + calculateStartCenterEnd3pAngle);
            String tag2 = getTag();
            Log.v(tag2, "getOngoingGestureScores : mReversedPointCount = " + this.mReversedPointCount);
            if (calculateStartCenterEnd3pAngle < 65 && this.mReversedPointCount >= 2) {
                if (hasValidRatio) {
                    for (int i = 1; i <= this.mReversedPointCount - 2; i++) {
                        judgeScore.countOk();
                    }
                }
                if (isHalfCircle || !hasValidRatio) {
                    judgeScore.countCritical();
                    judgeScore.countCritical();
                    judgeScore.countCritical();
                }
                if (hasValidRatio) {
                    judgeScore.countOk();
                }
                if (isClosedCompletely) {
                    judgeScore.countOk();
                    judgeScore.countOk();
                    judgeScore.countOk();
                    judgeScore.countOk();
                    judgeScore.countOk();
                }
                setJudgeState(JudgeState.SATISFACTION);
                setGestureQuality();
                list.add(getGestureScore());
            } else if (!hasValidRatio || getJudgeState() != JudgeState.SATISFACTION) {
                setJudgeState(judgeState2);
            }
        }
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.getOngoingGestureScores(list);
        }
    }
}
