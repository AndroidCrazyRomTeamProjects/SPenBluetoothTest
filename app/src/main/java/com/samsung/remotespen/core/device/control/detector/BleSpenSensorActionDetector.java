package com.samsung.remotespen.core.device.control.detector;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.BleSpenAirGestureActionDetector;
import com.samsung.remotespen.core.device.control.detector.BleSpenButtonActionDetector;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonId;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class BleSpenSensorActionDetector {
    private static final String TAG = "BleSpenSensorActionDetector";
    private final int DEFAULT_MOTION_STAY_RANGE_THRESHOLD;
    private BleSpenAirGestureActionDetector mAirGestureDetector;
    private BleSpenButtonActionDetector mButtonEventDetector;
    private GestureHistoryManager mGestureHistoryMgr;
    private int mMotionStayRangeThreshold;
    private ArrayList<Listener> mActionListenerArray = new ArrayList<>();
    private BleSpenButtonActionDetector.Listener mButtonClickListener = new BleSpenButtonActionDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector.1
        @Override // com.samsung.remotespen.core.device.control.detector.BleSpenButtonActionDetector.Listener
        public void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent) {
            Assert.notNull(BleSpenSensorActionDetector.this.mActionListenerArray);
            int i = AnonymousClass3.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[bleSpenButtonEvent.getAction().ordinal()];
            if (i == 1 || i == 2) {
                BleSpenSensorActionDetector.this.mAirGestureDetector.cancelGestureDetection();
            } else {
                BleSpenSensorActionDetector.this.mGestureHistoryMgr.markEndTime();
                BleSpenSensorActionDetector.this.mGestureHistoryMgr.markGestureType(bleSpenButtonEvent.getAction().toString());
            }
            Iterator it = BleSpenSensorActionDetector.this.mActionListenerArray.iterator();
            while (it.hasNext()) {
                ((Listener) it.next()).onButtonEvent(bleSpenButtonEvent);
            }
        }
    };
    private BleSpenAirGestureActionDetector.Listener mAirGestureListener = new BleSpenAirGestureActionDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector.2
        @Override // com.samsung.remotespen.core.device.control.detector.BleSpenAirGestureActionDetector.Listener
        public void onAirGestureActionEvent(BleSpenGestureEvent bleSpenGestureEvent) {
            Assert.notNull(BleSpenSensorActionDetector.this.mActionListenerArray);
            Iterator it = BleSpenSensorActionDetector.this.mActionListenerArray.iterator();
            while (it.hasNext()) {
                ((Listener) it.next()).onAirGestureActionEvent(bleSpenGestureEvent);
            }
        }
    };

    /* loaded from: classes.dex */
    public interface Listener {
        void onAirGestureActionEvent(BleSpenGestureEvent bleSpenGestureEvent);

        void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent);
    }

    public void pauseGestureDetection(List<BleSpenGestureType> list) {
        this.mAirGestureDetector.pauseDetection(list);
    }

    public void resumeAllGestureDetection() {
        this.mAirGestureDetector.resumeDetection();
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector$3  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass3 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action;

        static {
            int[] iArr = new int[BleSpenButtonEvent.Action.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action = iArr;
            try {
                iArr[BleSpenButtonEvent.Action.DOUBLE_CLICK_HOLD_STARTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.LONG_CLICK_STARTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public BleSpenSensorActionDetector(Context context, SpenModelName spenModelName) {
        int motionStayRangeThreshold = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature().getMotionStayRangeThreshold();
        this.DEFAULT_MOTION_STAY_RANGE_THRESHOLD = motionStayRangeThreshold;
        this.mMotionStayRangeThreshold = motionStayRangeThreshold;
        this.mButtonEventDetector = new BleSpenButtonActionDetector(this.mButtonClickListener);
        this.mAirGestureDetector = new BleSpenAirGestureActionDetector(context, spenModelName, this.mMotionStayRangeThreshold, this.mAirGestureListener);
        this.mGestureHistoryMgr = GestureHistoryManager.getInstance();
    }

    public void sendSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
        this.mAirGestureDetector.dispatchSensorEvent(bleSpenSensorEvent);
        if (shouldCancelButtonDetection(bleSpenSensorEvent)) {
            this.mButtonEventDetector.cancelDetection();
        }
        this.mButtonEventDetector.dispatchSensorEvent(bleSpenSensorEvent);
    }

    private boolean shouldCancelButtonDetection(BleSpenSensorEvent bleSpenSensorEvent) {
        Point detectedGestureAreaSize = this.mAirGestureDetector.getDetectedGestureAreaSize();
        int i = detectedGestureAreaSize.x;
        int i2 = this.mMotionStayRangeThreshold;
        if (i > i2 || detectedGestureAreaSize.y > i2) {
            String str = TAG;
            Log.v(str, "shouldCancelButtonDetection : Ignored by Gesture Size(" + detectedGestureAreaSize + ")");
            return true;
        } else if (bleSpenSensorEvent.isApproached()) {
            Log.v(TAG, "shouldCancelButtonDetection : Ignored by Approached.");
            return true;
        } else {
            return false;
        }
    }

    public void initialize() {
        this.mAirGestureDetector.initialize();
    }

    public void release() {
        this.mAirGestureDetector.release();
    }

    public void registerListener(Listener listener) {
        Assert.notNull(listener);
        this.mActionListenerArray.add(listener);
    }

    public void unregisterListener(Listener listener) {
        Assert.notNull(listener);
        this.mActionListenerArray.remove(listener);
    }

    public void setMotionStayRangeThresholdScale(float f) {
        int i = (int) (this.DEFAULT_MOTION_STAY_RANGE_THRESHOLD * f);
        String str = TAG;
        Log.d(str, "setMotionStayRangeThresholdScale : scale=" + f + " threshold=" + i);
        this.mMotionStayRangeThreshold = i;
        this.mAirGestureDetector.setMinimumValidGestureSize(i);
    }

    public void setHoverEnterState(boolean z) {
        this.mButtonEventDetector.setHoverEnterState(z);
        this.mAirGestureDetector.setHoverEnterState(z);
    }

    public void setPenButtonPressedOnHover(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mButtonEventDetector.setPenButtonPressedOnHover(bleSpenButtonId, z);
        this.mAirGestureDetector.setPenButtonPressedOnHover(z);
    }

    public void setScreenTouchState(boolean z) {
        this.mAirGestureDetector.setScreenTouchState(z);
    }

    public void enableDoubleClickHoldDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mButtonEventDetector.enableDoubleClickHoldDetection(bleSpenButtonId, z);
    }

    public void enableDoubleClickDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mButtonEventDetector.enableDoubleClickDetection(bleSpenButtonId, z);
    }

    public void setDoubleClickWaitInterval(int i) {
        this.mButtonEventDetector.setDoubleClickWaitInterval(i);
    }

    public void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        if (sensorActionDetectorParams == null) {
            Log.e(TAG, "setDetectorParams : params is null");
            return;
        }
        this.mButtonEventDetector.setDetectorParams(sensorActionDetectorParams);
        this.mAirGestureDetector.setDetectorParams(sensorActionDetectorParams);
    }

    public SensorActionDetectorParams getDetectorParams(BleSpenButtonId bleSpenButtonId) {
        SensorActionDetectorParams sensorActionDetectorParams = new SensorActionDetectorParams();
        this.mButtonEventDetector.getDetectorParams(bleSpenButtonId, sensorActionDetectorParams);
        this.mAirGestureDetector.getDetectorParams(sensorActionDetectorParams);
        return sensorActionDetectorParams;
    }

    public void setMaxMotionMovementValue(int i) {
        this.mAirGestureDetector.setMaxMotionMovementValue(i);
    }
}
