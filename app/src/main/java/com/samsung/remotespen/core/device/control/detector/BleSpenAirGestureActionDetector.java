package com.samsung.remotespen.core.device.control.detector;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.data.BleSpenAirMotionEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class BleSpenAirGestureActionDetector implements AbstractGestureDetector.GestureListener {
    private static final String TAG = "BleSpenAirGestureActionDetector";
    private GestureDetectionThread mGestureDetectionThread;
    private AbstractGestureDetector mGestureDetector;
    private GestureHistoryManager mGestureHistoryMgr;
    private final Listener mListener;
    private int mMaxMotionMovementValue;
    private int mMinimumValidGestureSize;
    private boolean mIsHoverEntered = false;
    private boolean mIsScreenTouching = false;
    private Session mCurrentSession = new Session();

    /* loaded from: classes.dex */
    public interface Listener {
        void onAirGestureActionEvent(BleSpenGestureEvent bleSpenGestureEvent);
    }

    /* loaded from: classes.dex */
    public static class Session {
        private Rect mGestureAreaRect;
        private ArrayList<Item> mItemArray;
        private Point mLastPointingPosition;
        private CloseReason mSessionCloseReason;

        /* loaded from: classes.dex */
        public enum CloseReason {
            BUTTON_UP,
            CANCEL
        }

        private Session() {
            this.mItemArray = new ArrayList<>();
            this.mSessionCloseReason = null;
            this.mLastPointingPosition = new Point(0, 0);
            this.mGestureAreaRect = new Rect(0, 0, 0, 0);
        }

        /* loaded from: classes.dex */
        public static class Item {
            public int dx;
            public int dy;

            public Item(BleSpenAirMotionEvent bleSpenAirMotionEvent) {
                this.dx = bleSpenAirMotionEvent.getX();
                this.dy = bleSpenAirMotionEvent.getY();
            }
        }

        public synchronized void addAirMotionEvent(BleSpenAirMotionEvent bleSpenAirMotionEvent) {
            Item item = new Item(bleSpenAirMotionEvent);
            this.mItemArray.add(item);
            this.mLastPointingPosition.offset(item.dx, item.dy);
            Point point = this.mLastPointingPosition;
            Rect rect = this.mGestureAreaRect;
            rect.left = Math.min(rect.left, point.x);
            rect.right = Math.max(rect.right, point.x);
            rect.top = Math.min(rect.top, point.y);
            rect.bottom = Math.max(rect.bottom, point.y);
        }

        public synchronized void closeSession(CloseReason closeReason) {
            String str = BleSpenAirGestureActionDetector.TAG;
            Log.d(str, "closeSession : reason=" + closeReason);
            this.mSessionCloseReason = closeReason;
        }

        public synchronized boolean isDetectionCancelled() {
            return this.mSessionCloseReason == CloseReason.CANCEL;
        }

        public synchronized boolean isSessionClosed() {
            return this.mSessionCloseReason != null;
        }

        public synchronized Point getDetectedGestureAreaSize() {
            Rect detectedGestureAreaRect;
            detectedGestureAreaRect = getDetectedGestureAreaRect();
            return new Point(detectedGestureAreaRect.width(), detectedGestureAreaRect.height());
        }

        private synchronized Rect getDetectedGestureAreaRect() {
            return this.mGestureAreaRect;
        }
    }

    public BleSpenAirGestureActionDetector(Context context, SpenModelName spenModelName, int i, Listener listener) {
        this.mMinimumValidGestureSize = i;
        this.mListener = listener;
        this.mMaxMotionMovementValue = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature().getMaxMotionMovementValue();
        AbstractGestureDetector gestureDetector = GestureDetectorFactory.getGestureDetector(context, spenModelName);
        this.mGestureDetector = gestureDetector;
        gestureDetector.setGestureListener(this);
        this.mGestureHistoryMgr = GestureHistoryManager.getInstance();
    }

    public void initialize() {
        Assert.e(this.mGestureDetectionThread == null);
        Object obj = new Object();
        GestureDetectionThread gestureDetectionThread = new GestureDetectionThread(TAG, obj);
        this.mGestureDetectionThread = gestureDetectionThread;
        gestureDetectionThread.start();
        synchronized (obj) {
            try {
                obj.wait(1000L);
            } catch (InterruptedException e) {
                String str = TAG;
                Log.e(str, "initialize : e=" + e);
            }
        }
        boolean z = this.mGestureDetectionThread.getHandler() != null;
        String str2 = TAG;
        Log.v(str2, "initialize : handler ready = " + z);
    }

    public void release() {
        GestureDetectionThread gestureDetectionThread = this.mGestureDetectionThread;
        if (gestureDetectionThread != null) {
            gestureDetectionThread.quitSafely();
            this.mGestureDetectionThread = null;
        }
    }

    public void pauseDetection(List<BleSpenGestureType> list) {
        this.mGestureDetector.pauseDetection(list);
    }

    public void resumeDetection() {
        this.mGestureDetector.resumeDetection();
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.AbstractGestureDetector.GestureListener
    public synchronized void onGesture(BleSpenGestureEvent bleSpenGestureEvent) {
        Point detectedGestureAreaSize = this.mCurrentSession.getDetectedGestureAreaSize();
        String str = TAG;
        Log.i(str, "onGesture : " + bleSpenGestureEvent.toString() + ", size=" + detectedGestureAreaSize);
        this.mGestureHistoryMgr.markState(bleSpenGestureEvent.getAction().name());
        this.mGestureHistoryMgr.markEndTime();
        this.mGestureHistoryMgr.markGestureType(bleSpenGestureEvent.getGestureType().toString());
        if (this.mCurrentSession.isDetectionCancelled()) {
            Log.i(str, "onGesture : gesture detection cancelled. So discard the detected gesture");
            return;
        }
        int i = detectedGestureAreaSize.x;
        int i2 = this.mMinimumValidGestureSize;
        if (i < i2 && detectedGestureAreaSize.y < i2) {
            Log.i(str, "onGesture : gesture is too small. dx=" + detectedGestureAreaSize.x + " dy=" + detectedGestureAreaSize.y + "  threshold=" + this.mMinimumValidGestureSize);
            cancelGestureDetection();
            return;
        }
        this.mListener.onAirGestureActionEvent(bleSpenGestureEvent);
    }

    public synchronized void dispatchSensorEvent(final BleSpenSensorEvent bleSpenSensorEvent) {
        String str = TAG;
        Log.v(str, "dispatchSensorEvent : getSensorType = " + bleSpenSensorEvent.getSensorType());
        Handler handler = this.mGestureDetectionThread.getHandler();
        if (handler == null) {
            Log.e(str, "dispatchSensorEvent : Gesture detection thread is not running");
        } else {
            handler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.control.detector.BleSpenAirGestureActionDetector.1
                @Override // java.lang.Runnable
                public void run() {
                    int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[bleSpenSensorEvent.getSensorType().ordinal()];
                    if (i != 1) {
                        if (i != 2) {
                            return;
                        }
                        AbstractGestureDetector.Noise noise = new AbstractGestureDetector.Noise();
                        noise.isApproached = bleSpenSensorEvent.isApproached();
                        noise.isScreenTouching = BleSpenAirGestureActionDetector.this.mIsScreenTouching;
                        noise.isHovered = BleSpenAirGestureActionDetector.this.mIsHoverEntered;
                        int i2 = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[((BleSpenButtonEvent) bleSpenSensorEvent).getAction().ordinal()];
                        if (i2 != 1) {
                            if (i2 != 2) {
                                return;
                            }
                            BleSpenAirGestureActionDetector.this.mGestureDetector.onMove(2, 0.0f, 0.0f, noise);
                            return;
                        }
                        synchronized (BleSpenAirGestureActionDetector.this) {
                            BleSpenAirGestureActionDetector.this.mCurrentSession = new Session();
                        }
                        BleSpenAirGestureActionDetector.this.mGestureDetector.onMove(0, 0.0f, 0.0f, noise);
                        BleSpenAirGestureActionDetector.this.mGestureHistoryMgr.markStartTime();
                        return;
                    }
                    BleSpenAirMotionEvent bleSpenAirMotionEvent = (BleSpenAirMotionEvent) bleSpenSensorEvent;
                    synchronized (BleSpenAirGestureActionDetector.this) {
                        BleSpenAirGestureActionDetector.this.mCurrentSession.addAirMotionEvent(bleSpenAirMotionEvent);
                    }
                    boolean isImpurity = bleSpenAirMotionEvent.isImpurity();
                    int x = bleSpenAirMotionEvent.getX();
                    int y = bleSpenAirMotionEvent.getY();
                    PointF scaleMotionEvent = BleUtils.scaleMotionEvent(new PointF(x, y), BleSpenAirGestureActionDetector.this.mMaxMotionMovementValue);
                    float f = scaleMotionEvent.x;
                    float f2 = scaleMotionEvent.y;
                    String str2 = BleSpenAirGestureActionDetector.TAG;
                    Log.v(str2, "dispatchSensorEvent : scaledDx = " + f + ", scaledDy = " + f2 + ", isImpurity = " + isImpurity + " isHoverEntered = " + BleSpenAirGestureActionDetector.this.mIsHoverEntered + " isApproached = " + bleSpenAirMotionEvent.isApproached() + ", isScreenTouching = " + BleSpenAirGestureActionDetector.this.mIsScreenTouching);
                    BleSpenAirGestureActionDetector.this.mGestureHistoryMgr.markRawEvent(x, y);
                    AbstractGestureDetector.Noise noise2 = new AbstractGestureDetector.Noise();
                    noise2.isImpurity = isImpurity;
                    noise2.isApproached = bleSpenAirMotionEvent.isApproached();
                    noise2.isScreenTouching = BleSpenAirGestureActionDetector.this.mIsScreenTouching;
                    noise2.isHovered = BleSpenAirGestureActionDetector.this.mIsHoverEntered;
                    BleSpenAirGestureActionDetector.this.mGestureDetector.onMove(1, f, f2, noise2);
                }
            });
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.BleSpenAirGestureActionDetector$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType;

        static {
            int[] iArr = new int[BleSpenSensorType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType = iArr;
            try {
                iArr[BleSpenSensorType.AIR_GESTURE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[BleSpenSensorType.BUTTON.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            int[] iArr2 = new int[BleSpenButtonEvent.Action.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action = iArr2;
            try {
                iArr2[BleSpenButtonEvent.Action.BUTTON_DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.BUTTON_UP.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public synchronized void cancelGestureDetection() {
        if (!this.mCurrentSession.isDetectionCancelled()) {
            Log.d(TAG, "cancelGestureDetection");
            this.mCurrentSession.closeSession(Session.CloseReason.CANCEL);
        }
    }

    public void setScreenTouchState(boolean z) {
        this.mIsScreenTouching = z;
    }

    public void setHoverEnterState(boolean z) {
        this.mIsHoverEntered = z;
    }

    public void setPenButtonPressedOnHover(boolean z) {
        this.mIsHoverEntered = z;
    }

    public synchronized Point getDetectedGestureAreaSize() {
        return this.mCurrentSession.getDetectedGestureAreaSize();
    }

    public void setMinimumValidGestureSize(int i) {
        this.mMinimumValidGestureSize = i;
    }

    public void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        this.mGestureDetector.setBoostGestures(sensorActionDetectorParams.mBoostGestureSet);
    }

    public void getDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        sensorActionDetectorParams.mBoostGestureSet = this.mGestureDetector.getBoostGestures();
    }

    public void setMaxMotionMovementValue(int i) {
        this.mMaxMotionMovementValue = i;
    }
}
