package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class StateMonitor extends DavinciEventMonitor {
    private static final int MIN_EVENT_COUNT_IN_GESTURE = 4;
    private static final int STAY_RECOGNIZE_COUNT = 4;
    private static final String TAG = "StateMonitor";
    private static final int THRESHOLD_DEGREE_3P_IN_LINE = 177;
    private static final float THRESHOLD_MIN_DIFF = 0.01f;
    private int mCheckingStay;
    private int mEventCount;
    private Event mOldEvent;

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void updateTravelDistance(GestureScore gestureScore, Event event) {
    }

    public StateMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
        this.mOldEvent = null;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.UNKNOWN;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public void onInitialize() {
        this.mEventCount = 0;
        this.mCheckingStay = 0;
        this.mOldEvent = null;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public MonitorState onJudge(Event event, int[] iArr) {
        int i = this.mEventCount;
        if (i == 0 && event.absDeltaX <= THRESHOLD_MIN_DIFF && event.absDeltaY <= THRESHOLD_MIN_DIFF) {
            Log.v(TAG, "onJudge : Event is filtered, Event  " + event);
            return MonitorState.FILTERING;
        }
        int i2 = i + 1;
        this.mEventCount = i2;
        if (i2 > 4) {
            if (event.absDeltaX <= THRESHOLD_MIN_DIFF && event.absDeltaY <= THRESHOLD_MIN_DIFF) {
                int i3 = this.mCheckingStay + 1;
                this.mCheckingStay = i3;
                if (i3 >= 4) {
                    this.mCheckingStay = 4;
                    Log.v(TAG, "onJudge : Is Staying.. ");
                    return MonitorState.STAY_DETECTED;
                }
                Log.v(TAG, "onJudge : Filtering staying event : " + this.mCheckingStay);
                return MonitorState.FILTERING;
            }
            this.mCheckingStay = 0;
        }
        Event event2 = this.mOldEvent;
        if (event2 != null) {
            int angle3p = Utils.getAngle3p(-event2.deltaX, -event2.deltaY, event.deltaX, event.deltaY);
            iArr[0] = (angle3p > THRESHOLD_DEGREE_3P_IN_LINE ? 1 : checkShape3P(this.mOldEvent, event)) | iArr[0];
            iArr[1] = angle3p;
            StringBuilder sb = new StringBuilder();
            sb.append("onJudge : is CCW = ");
            sb.append((iArr[0] & 15) == 2);
            Log.d(TAG, sb.toString());
            Log.d(TAG, "onJudge : angle = " + iArr[1]);
        }
        this.mOldEvent = event;
        return MonitorState.MONITORING;
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
}
