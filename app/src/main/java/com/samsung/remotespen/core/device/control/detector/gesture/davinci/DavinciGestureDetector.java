package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciGestureQuaility;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class DavinciGestureDetector extends AbstractGestureDetector {
    private static final int MAX_MOTION_MOVEMENT_VALUE = 2700;
    private static final float MIN_LEAD_RATIO = 0.5f;
    private static final String TAG = "DavinciGestureDetector";
    private DavinciEventMonitor mGestureMonitor;
    private MonitorState mLastMonitorState = MonitorState.MONITORING;
    private boolean mReady = true;

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onPauseMonitoring(List<BleSpenGestureType> list) {
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onResumeMonitoring() {
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void initGestureMonitors() {
        StateMonitor stateMonitor = new StateMonitor(new SwipeLeftMonitor(new SwipeRightMonitor(new SwipeUpMonitor(new SwipeDownMonitor(new CircleCwMonitor(new CircleCcwMonitor(null)))))));
        this.mGestureMonitor = stateMonitor;
        stateMonitor.setRawEventHistoryManager(this.mRawEventHistoryManager);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public void onMove(int i, float f, float f2, AbstractGestureDetector.Noise noise) {
        BleSpenGestureEvent bleSpenGestureEvent;
        BleSpenGestureEvent bleSpenGestureEvent2;
        if (i == 0) {
            readyToStart();
        } else if (this.mReady) {
            if (this.mRepeatMode == AbstractGestureDetector.RepeatMode.REPEAT_END_BY_END) {
                BleSpenGestureEvent bleSpenGestureEvent3 = this.mDetectedGesture;
                if (bleSpenGestureEvent3 != null) {
                    if (i == 1) {
                        return;
                    }
                    if (i == 2) {
                        bleSpenGestureEvent3.setAction(BleSpenGestureEvent.Action.FINISH_REPEAT);
                        notifyGestureDetectionResult(this.mDetectedGesture);
                        this.mReady = false;
                        return;
                    }
                }
            } else if (i == 2 && (bleSpenGestureEvent = this.mDetectedGesture) != null && this.mLastMonitorState == MonitorState.STAY_DETECTED) {
                bleSpenGestureEvent.setAction(BleSpenGestureEvent.Action.FINISH_REPEAT);
                notifyGestureDetectionResult(this.mDetectedGesture);
                this.mReady = false;
                return;
            }
            Event event = new Event(f, f2);
            if (noise.isImpurity) {
                event.setImpurity();
            }
            MonitorState monitorState = MonitorState.MONITORING;
            if (i != 2) {
                this.mRawEventHistoryManager.addEvent(event);
                monitorState = this.mGestureMonitor.judge(event, new int[2]);
            }
            if (monitorState == MonitorState.FILTERING) {
                this.mRawEventHistoryManager.removeLast();
            }
            Log.d(TAG, this.mGestureMonitor.judgeStateToString());
            if (this.mRepeatMode == AbstractGestureDetector.RepeatMode.REPEAT_END_BY_MOVE && (bleSpenGestureEvent2 = this.mDetectedGesture) != null) {
                MonitorState monitorState2 = this.mLastMonitorState;
                MonitorState monitorState3 = MonitorState.STAY_DETECTED;
                if (monitorState2 == monitorState3 && monitorState != monitorState3) {
                    bleSpenGestureEvent2.setAction(BleSpenGestureEvent.Action.FINISH_REPEAT);
                    notifyGestureDetectionResult(this.mDetectedGesture);
                    this.mReady = false;
                    return;
                }
            }
            if (i == 2) {
                monitorState = MonitorState.FINISH_DETECTED;
            }
            if (checkNeedToNotifyDetection(monitorState)) {
                this.mGestureMonitor.onInitialize();
                BleSpenGestureEvent mostProbableGesture = getMostProbableGesture();
                if (monitorState == MonitorState.STAY_DETECTED) {
                    mostProbableGesture.setAction(BleSpenGestureEvent.Action.START_REPEAT);
                    notifyGestureDetectionResult(mostProbableGesture);
                } else {
                    mostProbableGesture.setAction(BleSpenGestureEvent.Action.NO_REPEAT);
                    notifyGestureDetectionResult(mostProbableGesture);
                }
            }
            this.mLastMonitorState = monitorState;
        }
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector
    public BleSpenGestureEvent getMostProbableGesture() {
        ArrayList<GestureScore> arrayList = new ArrayList();
        this.mGestureMonitor.getOngoingGestureScores(arrayList);
        ArrayList<GestureScore> arrayList2 = new ArrayList();
        this.mGestureMonitor.getDetectedGestureScores(arrayList2);
        double d = Double.MIN_VALUE;
        GestureScore gestureScore = null;
        for (GestureScore gestureScore2 : arrayList) {
            Log.d(TAG, "ONGOING : " + gestureScore2);
            if (gestureScore2.getTravelDistance() > d) {
                d = gestureScore2.getTravelDistance();
                gestureScore = gestureScore2.m14clone();
            }
        }
        for (GestureScore gestureScore3 : arrayList2) {
            Log.d(TAG, "DETECTED : " + gestureScore3);
            if (gestureScore3.getTravelDistance() > d) {
                d = gestureScore3.getTravelDistance();
                gestureScore = gestureScore3.m14clone();
            }
        }
        Log.d(TAG, "CANDIDATE GESTURE : " + gestureScore);
        BleSpenGestureEvent bleSpenGestureEvent = new BleSpenGestureEvent();
        bleSpenGestureEvent.setGestureStartTime(this.mRawEventHistoryManager.getEventCount() > 0 ? this.mRawEventHistoryManager.getEvent(0).timestamp : 0L);
        if (gestureScore == null) {
            return bleSpenGestureEvent;
        }
        double rawTravelDistance = gestureScore.getRawTravelDistance() / this.mRawEventHistoryManager.getTravelLength();
        Log.d(TAG, "dedicationRatio : " + (100.0d * rawTravelDistance));
        if (rawTravelDistance < 0.5d) {
            Log.d(TAG, "dedicationRatio is less then 50%, discard CANDIDATE GESTURE");
            return bleSpenGestureEvent;
        } else if (isHighImpurityGesture(gestureScore.getType())) {
            bleSpenGestureEvent.setGestureQuality(new DavinciGestureQuaility(DavinciGestureQuaility.GestureQuality.HIGH_IMPURITY));
            return bleSpenGestureEvent;
        } else {
            bleSpenGestureEvent.setGestureType(gestureScore.getType());
            bleSpenGestureEvent.setGestureQuality(gestureScore.getGestureQuality());
            return bleSpenGestureEvent;
        }
    }

    private void readyToStart() {
        this.mReady = true;
        this.mRawEventHistoryManager.clear();
        this.mGestureMonitor.reset();
        this.mLastMonitorState = MonitorState.IDLE;
        this.mDetectedGesture = null;
    }
}
