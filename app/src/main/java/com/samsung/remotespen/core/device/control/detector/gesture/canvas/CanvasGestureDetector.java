package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.util.graph.VertexPath;
import com.samsung.remotespen.util.graph.VertexPathNormalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class CanvasGestureDetector extends AbstractGestureDetector {
    private static final float MIN_ALLOWED_DIFF = 1.0E-5f;
    private static final float MIN_LEAD_RATIO = 0.6f;
    private static final float MIN_STAY_DETECT_TIME = 250.0f;
    private static final String TAG = "CanvasGestureDetector";
    private static final float THRESHOLD_MIN_DIFF = 0.01f;
    private CanvasEventMonitor mGestureMonitor;
    private EventHistoryManager mOriginEventHistoryManager;
    private VertexPath mVertexPath = new VertexPath();
    private Long mStayStartTime = null;
    private HashSet<BleSpenGestureType> mBoostGesture = new HashSet<>();

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void initGestureMonitors() {
        this.mGestureMonitor = new SwipeLeftMonitor(new SwipeRightMonitor(new SwipeUpMonitor(new SwipeDownMonitor(new CircleCwMonitor(new CircleCcwMonitor(new PointyLeftMonitor(new PointyRightMonitor(new PointyUpMonitor(new PointyDownMonitor(new ShakeMonitor(null)))))))))));
        this.mOriginEventHistoryManager = new EventHistoryManager();
        this.mGestureMonitor.setRawEventHistoryManager(this.mRawEventHistoryManager);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onMove(int i, float f, float f2, AbstractGestureDetector.Noise noise) {
        if (i == 0) {
            readyToStart();
        }
        if (this.mDetectedGesture != null) {
            if (i == 2 || noise.isScreenTouching) {
                String str = TAG;
                Log.d(str, "onMove : FINISH_REPEAT. eventType = " + i + ", isScreenTouching = " + noise.isScreenTouching);
                this.mDetectedGesture.setAction(BleSpenGestureEvent.Action.FINISH_REPEAT);
                notifyGestureDetectionResult(this.mDetectedGesture);
                return;
            } else if (i == 1) {
                return;
            }
        }
        Event event = new Event(f, f2);
        if (noise.isImpurity) {
            event.setImpurity();
        }
        if (noise.isHovered) {
            event.setHoverEntered();
        }
        if (noise.isApproached) {
            event.setApproached();
        }
        if (noise.isScreenTouching) {
            event.setScreenTouchedFlag();
        }
        MonitorState realtimeMonitorState = getRealtimeMonitorState(i, f, f2);
        if (realtimeMonitorState != MonitorState.STAY_DETECTED) {
            if (this.mOriginEventHistoryManager.getEventCount() == 0) {
                this.mOriginEventHistoryManager.addEvent(event);
            } else if (this.mOriginEventHistoryManager.getLastEvent() != null && event.deltaX != 0.0f && event.deltaY != 0.0f) {
                this.mOriginEventHistoryManager.addEvent(event);
            }
        }
        if (checkNeedToNotifyDetection(realtimeMonitorState)) {
            int filterStartNoise = filterStartNoise();
            int filterEndNoise = filterEndNoise();
            String str2 = TAG;
            Log.v(str2, "onMove : startIndex = " + filterStartNoise + ", endIndex = " + filterEndNoise);
            this.mRawEventHistoryManager.clear();
            while (filterStartNoise <= filterEndNoise) {
                Event event2 = this.mOriginEventHistoryManager.getEvent(filterStartNoise);
                this.mRawEventHistoryManager.addEvent(event2);
                this.mVertexPath.appendRelativeVertex(event2.deltaX, event2.deltaY);
                filterStartNoise++;
            }
            this.mGestureMonitor.onInitialize();
            CanvasEventMonitor canvasEventMonitor = this.mGestureMonitor;
            VertexPath vertexPath = this.mVertexPath;
            canvasEventMonitor.judge(vertexPath, VertexPathNormalizer.getAccuratelyNormalizedPath(vertexPath, 0.03f));
            BleSpenGestureEvent mostProbableGesture = getMostProbableGesture();
            if (realtimeMonitorState == MonitorState.STAY_DETECTED) {
                mostProbableGesture.setAction(BleSpenGestureEvent.Action.START_REPEAT);
                notifyGestureDetectionResult(mostProbableGesture);
            } else {
                mostProbableGesture.setAction(BleSpenGestureEvent.Action.NO_REPEAT);
                notifyGestureDetectionResult(mostProbableGesture);
            }
        }
        this.mLastMonitorState = realtimeMonitorState;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onPauseMonitoring(List<BleSpenGestureType> list) {
        this.mGestureMonitor.pauseMonitoring(list);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onResumeMonitoring() {
        this.mGestureMonitor.resumeMonitoring();
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public BleSpenGestureEvent getMostProbableGesture() {
        ArrayList<GestureScore> arrayList = new ArrayList();
        this.mGestureMonitor.getOngoingGestureScores(arrayList);
        ArrayList<GestureScore> arrayList2 = new ArrayList();
        this.mGestureMonitor.getDetectedGestureScores(arrayList2);
        Double valueOf = Double.valueOf(Double.MIN_VALUE);
        GestureScore gestureScore = null;
        Double d = valueOf;
        for (GestureScore gestureScore2 : arrayList) {
            Log.d(TAG, "ONGOING : " + gestureScore2);
            double travelDistance = gestureScore2.getTravelDistance();
            double weight = gestureScore2.getWeight();
            if (Math.abs(valueOf.doubleValue() - travelDistance) < 9.999999747378752E-6d) {
                if (weight > d.doubleValue()) {
                    d = Double.valueOf(weight);
                    gestureScore = gestureScore2.m14clone();
                } else if (weight == d.doubleValue() && gestureScore != null) {
                    if (gestureScore2.getJudgeScore().getNotOkCount() + gestureScore2.getJudgeScore().getCriticalCount() < gestureScore.getJudgeScore().getNotOkCount() + gestureScore.getJudgeScore().getCriticalCount()) {
                        d = Double.valueOf(weight);
                        gestureScore = gestureScore2.m14clone();
                    }
                }
            } else if (travelDistance > valueOf.doubleValue()) {
                valueOf = Double.valueOf(travelDistance);
                d = Double.valueOf(weight);
                gestureScore = gestureScore2.m14clone();
            }
        }
        for (GestureScore gestureScore3 : arrayList2) {
            Log.d(TAG, "DETECTED : " + gestureScore3);
            Double valueOf2 = Double.valueOf(gestureScore3.getTravelDistance());
            double weight2 = gestureScore3.getWeight();
            if (Math.abs(valueOf.doubleValue() - valueOf2.doubleValue()) < 9.999999747378752E-6d) {
                if (weight2 > d.doubleValue()) {
                    d = Double.valueOf(weight2);
                    gestureScore = gestureScore3.m14clone();
                } else if (weight2 == d.doubleValue() && gestureScore != null) {
                    if (gestureScore3.getJudgeScore().getNotOkCount() + gestureScore3.getJudgeScore().getCriticalCount() < gestureScore.getJudgeScore().getNotOkCount() + gestureScore.getJudgeScore().getCriticalCount()) {
                        d = Double.valueOf(weight2);
                        gestureScore = gestureScore3.m14clone();
                    }
                }
            } else if (valueOf2.doubleValue() > valueOf.doubleValue()) {
                Double valueOf3 = Double.valueOf(weight2);
                gestureScore = gestureScore3.m14clone();
                d = valueOf3;
                valueOf = valueOf2;
            }
        }
        String str = TAG;
        Log.d(str, "CANDIDATE GESTURE : " + gestureScore);
        BleSpenGestureEvent bleSpenGestureEvent = new BleSpenGestureEvent();
        bleSpenGestureEvent.setGestureStartTime(this.mRawEventHistoryManager.getEventCount() > 0 ? this.mRawEventHistoryManager.getEvent(0).timestamp : 0L);
        if (gestureScore == null) {
            return bleSpenGestureEvent;
        }
        BleSpenGestureQuality gestureQuality = gestureScore.getGestureQuality();
        double travelDistance2 = gestureScore.getTravelDistance() / this.mOriginEventHistoryManager.getTravelLength();
        Log.d(str, "dedicationRatio : " + (100.0d * travelDistance2));
        if (travelDistance2 < 0.6000000238418579d) {
            Log.d(str, "dedicationRatio is less then 60.000004%, discard CANDIDATE GESTURE");
            return bleSpenGestureEvent;
        }
        bleSpenGestureEvent.setGestureQuality(gestureQuality);
        if (gestureQuality.isCleanGesture()) {
            bleSpenGestureEvent.setGestureType(gestureScore.getType());
            return bleSpenGestureEvent;
        }
        return bleSpenGestureEvent;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void setBoostGestures(HashSet<BleSpenGestureType> hashSet) {
        String str = TAG;
        Log.v(str, "setBoostGestures : boostGesture = " + hashSet);
        this.mBoostGesture = (HashSet) hashSet.clone();
        HashSet hashSet2 = new HashSet();
        Iterator<BleSpenGestureType> it = hashSet.iterator();
        while (it.hasNext()) {
            int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[it.next().ordinal()];
            if (i == 1 || i == 2 || i == 3 || i == 4) {
                hashSet2.add(BleSpenGestureType.CIRCLE_CW);
                hashSet2.add(BleSpenGestureType.CIRCLE_CCW);
            }
        }
        String str2 = TAG;
        Log.v(str2, "setBoostGestures : lowWeightGesture = " + hashSet2);
        for (CanvasEventMonitor canvasEventMonitor = this.mGestureMonitor; canvasEventMonitor != null; canvasEventMonitor = canvasEventMonitor.mNextMonitor) {
            CanvasEventMonitor.BoostWeight boostWeight = CanvasEventMonitor.BoostWeight.NORMAL;
            if (hashSet.contains(canvasEventMonitor.getType())) {
                boostWeight = CanvasEventMonitor.BoostWeight.HIGH;
            }
            if (hashSet2.contains(canvasEventMonitor.getType())) {
                boostWeight = CanvasEventMonitor.BoostWeight.LOW;
            }
            canvasEventMonitor.setBoostWeight(boostWeight);
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasGestureDetector$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType;

        static {
            int[] iArr = new int[BleSpenGestureType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType = iArr;
            try {
                iArr[BleSpenGestureType.POINTY_LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_UP.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenGestureType[BleSpenGestureType.POINTY_DOWN.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public HashSet<BleSpenGestureType> getBoostGestures() {
        return (HashSet) this.mBoostGesture.clone();
    }

    private MonitorState getRealtimeMonitorState(int i, float f, float f2) {
        if (i == 2) {
            Log.v(TAG, "getRealtimeMonitorState : Event end");
            return MonitorState.FINISH_DETECTED;
        }
        float abs = Math.abs(f);
        float abs2 = Math.abs(f2);
        if (abs <= THRESHOLD_MIN_DIFF && abs2 <= THRESHOLD_MIN_DIFF) {
            MonitorState monitorState = this.mLastMonitorState;
            MonitorState monitorState2 = MonitorState.IDLE;
            if (monitorState == monitorState2) {
                Log.d(TAG, "getRealtimeMonitorState : Filtering staying event before starting to monitor");
                return monitorState2;
            }
            if (this.mStayStartTime == null) {
                this.mStayStartTime = Long.valueOf(this.mOriginEventHistoryManager.getLastEvent().timestamp);
            }
            if (((float) (this.mOriginEventHistoryManager.getLastEvent().timestamp - this.mStayStartTime.longValue())) > MIN_STAY_DETECT_TIME) {
                Log.v(TAG, "getRealtimeMonitorState : Is Staying.. ");
                return MonitorState.STAY_DETECTED;
            }
        } else {
            this.mStayStartTime = null;
        }
        return MonitorState.MONITORING;
    }

    private void readyToStart() {
        this.mRawEventHistoryManager.clear();
        this.mOriginEventHistoryManager.clear();
        this.mGestureMonitor.reset();
        this.mDetectedGesture = null;
        this.mVertexPath.clear();
        this.mLastMonitorState = MonitorState.IDLE;
        this.mStayStartTime = null;
        this.mGestureMonitor.markStartTime(System.currentTimeMillis());
    }

    private int filterStartNoise() {
        int i = 0;
        if (this.mOriginEventHistoryManager.getEventCount() - 1 < 10) {
            return 0;
        }
        int indexOfNPercentTravelLength = this.mOriginEventHistoryManager.getIndexOfNPercentTravelLength(0.1f);
        float averageDelta = this.mOriginEventHistoryManager.getAverageDelta(indexOfNPercentTravelLength, this.mOriginEventHistoryManager.getIndexOfNPercentTravelLength(0.9f));
        float averageDelta2 = this.mOriginEventHistoryManager.getAverageDelta(0, indexOfNPercentTravelLength);
        String str = TAG;
        Log.v(str, "filterStartNoise : averageDelta = " + averageDelta + ", averageStartDelta = " + averageDelta2);
        double d = ((double) averageDelta) * 0.3d;
        if (Math.abs(averageDelta - averageDelta2) < d) {
            Log.v(str, "filterStartNoise : Skip filtering start noise");
            return 0;
        }
        for (int i2 = 1; i2 < indexOfNPercentTravelLength; i2++) {
            Event event = this.mOriginEventHistoryManager.getEvent(i2);
            if (Utils.getDistance(event.deltaX, event.deltaY) > d) {
                break;
            }
            i = i2;
        }
        return i;
    }

    private int filterEndNoise() {
        int eventCount = this.mOriginEventHistoryManager.getEventCount() - 1;
        if (eventCount < 10) {
            return eventCount;
        }
        int indexOfNPercentTravelLength = this.mOriginEventHistoryManager.getIndexOfNPercentTravelLength(0.1f);
        int indexOfNPercentTravelLength2 = this.mOriginEventHistoryManager.getIndexOfNPercentTravelLength(0.9f);
        float averageDelta = this.mOriginEventHistoryManager.getAverageDelta(indexOfNPercentTravelLength, indexOfNPercentTravelLength2);
        float averageDelta2 = this.mOriginEventHistoryManager.getAverageDelta(indexOfNPercentTravelLength2, eventCount);
        String str = TAG;
        Log.v(str, "filterEndNoise : averageDelta = " + averageDelta + ", averageEndDelta = " + averageDelta2);
        double d = (double) averageDelta;
        if (Math.abs(averageDelta - averageDelta2) < 0.4d * d) {
            Log.v(str, "filterEndNoise : Skip filtering end noise");
            return eventCount;
        }
        while (indexOfNPercentTravelLength2 < eventCount) {
            Event event = this.mOriginEventHistoryManager.getEvent(indexOfNPercentTravelLength2);
            double distance = Utils.getDistance(event.deltaX, event.deltaY);
            if (distance < 0.5d * d || distance > 1.5d * d) {
                return indexOfNPercentTravelLength2;
            }
            indexOfNPercentTravelLength2++;
        }
        return eventCount;
    }
}
