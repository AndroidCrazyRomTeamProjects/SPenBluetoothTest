package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.text.TextUtils;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.data.BleSpenGestureApproachState;
import com.samsung.remotespen.core.device.data.BleSpenGestureHoverState;
import com.samsung.remotespen.core.device.data.BleSpenGestureImpurity;
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.device.data.BleSpenGestureScreenTouchState;
import com.samsung.remotespen.core.device.data.BleSpenGestureSize;
import com.samsung.remotespen.core.device.data.BleSpenGestureSpeed;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.util.graph.VertexPath;
import com.samsung.remotespen.util.graph.VertexPathNormalizer;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public abstract class CanvasEventMonitor {
    private static final int DEFAULT_ELAPSED_TIME_TOO_FAST = 500;
    private static final int DEFAULT_ELAPSED_TIME_TOO_SLOW = 1550;
    public static final float DEFAULT_ERROR_FACTOR_THRESHOLD = 0.03f;
    private static final float EXTRA_WEIGHT_POINT = 2.5f;
    private static final float MAX_IMPURITY_RATIO = 0.3f;
    public static final float MAX_IMPURITY_RATIO_OF_UP_DOWN = 0.7f;
    public static final int SHAPE_3P_CCW = 2;
    public static final int SHAPE_3P_CW = 4;
    public static final int SHAPE_3P_IN_LINE = 1;
    public static final int SHAPE_NONE = 0;
    private static String TAG = "CanvasEventMonitor";
    private static final int THRESHOLD_DEGREE_3P_IN_LINE = 177;
    private GestureScore mDetectedGestureScore;
    private EventHistoryManager mEventHistoryManager;
    private BleSpenGestureType mMe;
    public CanvasEventMonitor mNextMonitor;
    private GestureScore mOnGoingGestureScore;
    private EventHistoryManager mRawEventHistoryManager;
    public long mStartTime;
    private boolean mStopMonitor;
    private JudgeState mState = JudgeState.IDLE;
    public VertexPath mOriginalPath = null;
    public VertexPath mNormalizedPath = null;
    private int mNormalizedVertexCount = 0;
    private Event mOldEvent = null;
    public BoostWeight mBoostWeight = BoostWeight.NORMAL;

    /* loaded from: classes.dex */
    public enum BoostWeight {
        HIGH,
        NORMAL,
        LOW
    }

    public long getElapsedTimeTooFast() {
        return 500L;
    }

    public long getElapsedTimeTooSlow() {
        return 1550L;
    }

    public float getErrorFactorThreshold() {
        return 0.03f;
    }

    public float getMaxImpurityRatio() {
        return 0.3f;
    }

    public abstract String getTag();

    public abstract BleSpenGestureType whoAmI();

    public CanvasEventMonitor(CanvasEventMonitor canvasEventMonitor) {
        BleSpenGestureType whoAmI = whoAmI();
        this.mMe = whoAmI;
        this.mNextMonitor = canvasEventMonitor;
        this.mOnGoingGestureScore = new GestureScore(whoAmI);
        this.mOnGoingGestureScore.setGestureQuality(new BleSpenGestureQuality());
        this.mEventHistoryManager = new EventHistoryManager();
    }

    public void markStartTime(long j) {
        this.mStartTime = j;
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.markStartTime(j);
        }
    }

    public final void setRawEventHistoryManager(EventHistoryManager eventHistoryManager) {
        this.mRawEventHistoryManager = eventHistoryManager;
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.setRawEventHistoryManager(eventHistoryManager);
        }
    }

    public final EventHistoryManager getRawEventHistoryManager() {
        return this.mRawEventHistoryManager;
    }

    public final EventHistoryManager getEventHistoryManager() {
        return this.mEventHistoryManager;
    }

    public final BleSpenGestureType getType() {
        return this.mMe;
    }

    public void pauseMonitoring(List<BleSpenGestureType> list) {
        Iterator<BleSpenGestureType> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            if (this.mMe.equals(it.next())) {
                this.mStopMonitor = true;
                break;
            }
        }
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.pauseMonitoring(list);
        }
    }

    public void resumeMonitoring() {
        this.mStopMonitor = false;
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.resumeMonitoring();
        }
    }

    public void reset() {
        this.mState = JudgeState.IDLE;
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.reset();
        }
        this.mOnGoingGestureScore.reset();
        this.mDetectedGestureScore = null;
        onInitialize();
    }

    public void setBoostWeight(BoostWeight boostWeight) {
        this.mBoostWeight = boostWeight;
        getGestureScore().setExtraWeightPoint(boostWeight == BoostWeight.HIGH ? EXTRA_WEIGHT_POINT : 1.0f);
    }

    public JudgeState getJudgeState() {
        return this.mState;
    }

    public void setJudgeState(JudgeState judgeState) {
        this.mState = judgeState;
    }

    public final String judgeStateToString() {
        if (this.mNextMonitor != null) {
            return judgeStateToString(this) + this.mNextMonitor.judgeStateToString();
        }
        return judgeStateToString(this);
    }

    private String judgeStateToString(CanvasEventMonitor canvasEventMonitor) {
        if (canvasEventMonitor.getType() == BleSpenGestureType.UNKNOWN) {
            return Constants.packageName.NONE;
        }
        return "[" + canvasEventMonitor.getType().toString() + ": " + canvasEventMonitor.getJudgeState().toString() + "], ";
    }

    public int getNormalizedVertexCount() {
        return this.mNormalizedVertexCount;
    }

    public void makeNormalizedVertexPath(VertexPath vertexPath, VertexPath vertexPath2) {
        this.mNormalizedPath = vertexPath2;
        float errorFactorThreshold = getErrorFactorThreshold();
        if (Math.abs(0.03f - errorFactorThreshold) > 1.0E-6d) {
            this.mNormalizedPath = VertexPathNormalizer.getAccuratelyNormalizedPath(vertexPath, errorFactorThreshold);
        }
    }

    public void judge(VertexPath vertexPath, VertexPath vertexPath2) {
        if (this.mStopMonitor) {
            CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
            if (canvasEventMonitor != null) {
                canvasEventMonitor.judge(vertexPath, vertexPath2);
                return;
            }
            return;
        }
        this.mOriginalPath = vertexPath;
        makeNormalizedVertexPath(vertexPath, vertexPath2);
        this.mNormalizedVertexCount = this.mNormalizedPath.getVertexCount();
        String tag = tag();
        Log.v(tag, "normalizedVertexPath vertexCount : " + this.mNormalizedVertexCount);
        this.mEventHistoryManager.clear();
        onInitialize();
        for (int i = 0; i < this.mNormalizedPath.getVertexCount(); i++) {
            VertexPath.Vertex relativeVertex = this.mNormalizedPath.getRelativeVertex(i);
            Event event = new Event(relativeVertex.x, relativeVertex.y);
            this.mEventHistoryManager.addEvent(event);
            String tag2 = tag();
            Log.v(tag2, "normalized : " + event);
            judge(event, new int[2]);
        }
        CanvasEventMonitor canvasEventMonitor2 = this.mNextMonitor;
        if (canvasEventMonitor2 != null) {
            canvasEventMonitor2.judge(vertexPath, vertexPath2);
        }
    }

    public final void judge(Event event, int[] iArr) {
        int eventCount;
        EventHistoryManager eventHistoryManager = this.mEventHistoryManager;
        if (eventHistoryManager == null) {
            Log.e(tag(), "judge : EventHistoryManager doesn't exist!!. Unable to judge gesture");
            eventCount = 1;
        } else {
            eventCount = eventHistoryManager.getEventCount();
        }
        JudgeState judgeState = getJudgeState();
        if (getJudgeState() == JudgeState.IDLE || getJudgeState() == JudgeState.NOT_APPLICABLE) {
            this.mOnGoingGestureScore.reset();
            Log.v(tag(), "onJudge : JudgeState is " + getJudgeState() + ", Reset JudgeScore!!");
            String tag = tag();
            StringBuilder sb = new StringBuilder();
            sb.append("onJudge : event count = ");
            int i = eventCount + (-1);
            sb.append(i);
            Log.v(tag, sb.toString());
            this.mOnGoingGestureScore.setStartIndex(i);
        }
        onJudge(event, iArr);
        Log.v(getTag(), "onJudge : " + getGestureScore().toString());
        Log.v(getTag(), "onJudge : JudgeState = " + getJudgeState());
        Log.v(getTag(), "onJudge : ----------------------------------------------------");
        int i2 = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[getJudgeState().ordinal()];
        if (i2 == 1 || i2 == 2) {
            EventHistoryManager eventHistoryManager2 = this.mEventHistoryManager;
            if (eventHistoryManager2 != null) {
                eventCount = eventHistoryManager2.getEventCount();
            }
            this.mOnGoingGestureScore.setEndIndex(eventCount - 1);
            updateTravelDistance(this.mOnGoingGestureScore, event);
        } else if (i2 == 3 && judgeState == JudgeState.SATISFACTION) {
            recordCurrentGestureScore();
            Log.v(tag(), "Record Score : " + this.mDetectedGestureScore);
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState;

        static {
            int[] iArr = new int[JudgeState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState = iArr;
            try {
                iArr[JudgeState.INTERESTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[JudgeState.SATISFACTION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[JudgeState.NOT_APPLICABLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private void recordCurrentGestureScore() {
        this.mDetectedGestureScore = this.mOnGoingGestureScore.m14clone();
        this.mOnGoingGestureScore.reset();
    }

    public GestureScore getGestureScore() {
        return this.mOnGoingGestureScore;
    }

    public void getOngoingGestureScores(List<GestureScore> list) {
        if (this.mState == JudgeState.SATISFACTION) {
            setGestureQuality();
            list.add(this.mOnGoingGestureScore);
        }
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.getOngoingGestureScores(list);
        }
    }

    public void getDetectedGestureScores(List<GestureScore> list) {
        GestureScore gestureScore = this.mDetectedGestureScore;
        if (gestureScore != null) {
            list.add(gestureScore);
        }
        CanvasEventMonitor canvasEventMonitor = this.mNextMonitor;
        if (canvasEventMonitor != null) {
            canvasEventMonitor.getDetectedGestureScores(list);
        }
    }

    public void onInitialize() {
        this.mOldEvent = null;
    }

    public void updateTravelDistance(GestureScore gestureScore, Event event) {
        String tag = getTag();
        Log.v(tag, "updateTravelDistance : event = " + event);
        int startIndex = gestureScore.getStartIndex();
        int endIndex = gestureScore.getEndIndex();
        VertexPath.Vertex absVertex = this.mNormalizedPath.getAbsVertex(startIndex);
        VertexPath.Vertex absVertex2 = this.mNormalizedPath.getAbsVertex(endIndex);
        int index = this.mOriginalPath.getIndex(absVertex);
        int index2 = this.mOriginalPath.getIndex(absVertex2);
        EventHistoryManager rawEventHistoryManager = getRawEventHistoryManager();
        String tag2 = tag();
        Log.v(tag2, "updateTravelDistance : startIndex->endIndex : " + index + " -> " + index2);
        gestureScore.setTravelDistance(rawEventHistoryManager.getTravelLength(index, index2));
    }

    public void setGestureQuality() {
        BleSpenGestureQuality gestureQuality = getGestureScore().getGestureQuality();
        setGestureSpeed(gestureQuality);
        setGestureSize(gestureQuality);
        setGestureImpurity(gestureQuality);
        setGestureHoverState(gestureQuality);
        setGestureApproachState(gestureQuality);
        setGestureTouchState(gestureQuality);
    }

    public void setGestureSpeed(BleSpenGestureQuality bleSpenGestureQuality) {
        long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
        String tag = getTag();
        Log.d(tag, "setGestureSpeed : elapsedTime = " + currentTimeMillis);
        if (currentTimeMillis > getElapsedTimeTooSlow()) {
            bleSpenGestureQuality.setGestureSpeed(BleSpenGestureSpeed.TOO_SLOW);
        } else if (currentTimeMillis <= getElapsedTimeTooFast()) {
            bleSpenGestureQuality.setGestureSpeed(BleSpenGestureSpeed.TOO_FAST);
        } else {
            bleSpenGestureQuality.setGestureSpeed(BleSpenGestureSpeed.GOOD);
        }
    }

    public void setGestureSize(BleSpenGestureQuality bleSpenGestureQuality) {
        bleSpenGestureQuality.setGestureSize(BleSpenGestureSize.GOOD);
    }

    public void setGestureImpurity(BleSpenGestureQuality bleSpenGestureQuality) {
        if (isHighImpurityGesture()) {
            bleSpenGestureQuality.setGestureImpurity(BleSpenGestureImpurity.HIGH_IMPURITY);
        } else {
            bleSpenGestureQuality.setGestureImpurity(BleSpenGestureImpurity.GOOD);
        }
    }

    public void setGestureHoverState(BleSpenGestureQuality bleSpenGestureQuality) {
        if (this.mRawEventHistoryManager.getHoverEnteredEventCount() > 0) {
            bleSpenGestureQuality.setGestureHoverState(BleSpenGestureHoverState.HOVERED);
        } else {
            bleSpenGestureQuality.setGestureHoverState(BleSpenGestureHoverState.GOOD);
        }
    }

    public void setGestureApproachState(BleSpenGestureQuality bleSpenGestureQuality) {
        if (this.mRawEventHistoryManager.getApproachedEventCount() > 0) {
            bleSpenGestureQuality.setGestureApproachState(BleSpenGestureApproachState.APPROACHED);
        } else {
            bleSpenGestureQuality.setGestureApproachState(BleSpenGestureApproachState.GOOD);
        }
    }

    public void setGestureTouchState(BleSpenGestureQuality bleSpenGestureQuality) {
        if (this.mRawEventHistoryManager.getScreenTouchedEventCount() > 0) {
            bleSpenGestureQuality.setGestureTouchState(BleSpenGestureScreenTouchState.TOUCHED);
        } else {
            bleSpenGestureQuality.setGestureTouchState(BleSpenGestureScreenTouchState.GOOD);
        }
    }

    private boolean isHighImpurityGesture() {
        float impurityEventCount = this.mRawEventHistoryManager.getImpurityEventCount() / this.mRawEventHistoryManager.getActualEventCount();
        String str = TAG;
        Log.d(str, "impurityRatio = " + impurityEventCount);
        return impurityEventCount >= getMaxImpurityRatio();
    }

    private String tag() {
        if (TextUtils.isEmpty(getTag())) {
            return TAG;
        }
        return TAG + "|" + getTag();
    }

    private int checkShape3P(Event event, Event event2) {
        float f = event.deltaX;
        float f2 = event.deltaY;
        float f3 = ((f - 0.0f) * ((event2.deltaY + f2) - 0.0f)) - (((event2.deltaX + f) - 0.0f) * (f2 - 0.0f));
        if (f3 > 0.0f) {
            return 2;
        }
        return f3 < 0.0f ? 4 : 0;
    }

    public void onJudge(Event event, int[] iArr) {
        Event event2 = this.mOldEvent;
        if (event2 != null) {
            int angle3p = Utils.getAngle3p(-event2.deltaX, -event2.deltaY, event.deltaX, event.deltaY);
            iArr[0] = (angle3p > THRESHOLD_DEGREE_3P_IN_LINE ? 1 : checkShape3P(this.mOldEvent, event)) | iArr[0];
            iArr[1] = angle3p;
            String tag = tag();
            Log.v(tag, "onJudge : shape = " + iArr[0]);
            String tag2 = tag();
            Log.v(tag2, "onJudge : angle = " + iArr[1]);
        }
        this.mOldEvent = event;
    }
}
