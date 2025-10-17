package com.samsung.remotespen.core.device.control.detector;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;
import com.samsung.util.sep.SemFloatingFeatureWrapper;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenButtonActionDetector.java */
/* loaded from: classes.dex */
public class ButtonActionDetector {
    private static final int BUTTON_PRESS_DURATION_FOR_LONG_CLICK_DETECTION = 800;
    private static final int DOUBLE_CLICK_AND_HOLD_WAIT_INTERVAL = 200;
    private static final int DOUBLE_CLICK_WAIT_INTERVAL_FOR_CI_CHANGE = 250;
    private static final int DOUBLE_CLICK_WAIT_INTERVAL_FOR_LEGACY = 300;
    private static final boolean SUPPORT_CI_CHANGE_FOR_SPEN = SemFloatingFeatureWrapper.getBoolean("SEC_FLOATING_FEATURE_BLUETOOTH_SUPPORT_CHANGE_CI_FOR_SPEN");
    private static final String TAG = "ButtonActionDetector";
    private long mButtonDownTime;
    private Runnable mClickActionHandler;
    private Runnable mDoubleClickHoldActionHandler;
    private Listener mListener;
    private Runnable mLongClickActionHandler;
    private boolean mIsHoverEntered = false;
    private boolean mIsPenButtonPressedOnHover = false;
    private boolean mIsDoubleClickDetectionEnabled = true;
    private boolean mIsDoubleClickHoldDetectionEnabled = false;
    private int mDoubleClickWaitInterval = getDefaultDoubleClickInterval();
    private int mDoubleClickAndHoldWaitInterval = DOUBLE_CLICK_AND_HOLD_WAIT_INTERVAL;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsDetectionCancelled = false;
    private boolean mIsLongClickStarted = false;
    private boolean mIsDoubleClickHoldModeStarted = false;

    /* compiled from: BleSpenButtonActionDetector.java */
    /* loaded from: classes.dex */
    public interface Listener {
        void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent);
    }

    public ButtonActionDetector(Listener listener) {
        Assert.notNull(listener);
        this.mListener = listener;
    }

    /* compiled from: BleSpenButtonActionDetector.java */
    /* renamed from: com.samsung.remotespen.core.device.control.detector.ButtonActionDetector$4  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass4 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action;

        static {
            int[] iArr = new int[BleSpenButtonEvent.Action.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action = iArr;
            try {
                iArr[BleSpenButtonEvent.Action.SINGLE_CLICKED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.LONG_CLICK_STARTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.LONG_CLICK_FINISHED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.BUTTON_DOWN.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.BUTTON_UP.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public void dispatchSensorEvent(BleSpenButtonEvent bleSpenButtonEvent) {
        BleSpenButtonEvent.Action action = bleSpenButtonEvent.getAction();
        int i = AnonymousClass4.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[action.ordinal()];
        if (i == 1) {
            onSingleClicked(bleSpenButtonEvent);
        } else if (i == 2) {
            onLongClickStarted(bleSpenButtonEvent);
        } else if (i == 3) {
            onLongClickFinished(bleSpenButtonEvent);
        } else if (i == 4) {
            onButtonDown(bleSpenButtonEvent);
        } else if (i == 5) {
            cancelLongClickHandler();
            cancelDoubleClickHoldHandler();
            if (this.mIsLongClickStarted) {
                onLongClickFinished(bleSpenButtonEvent);
                this.mButtonDownTime = 0L;
            } else if (this.mIsDoubleClickHoldModeStarted) {
                onDoubleClickHoldFinished(bleSpenButtonEvent);
                this.mButtonDownTime = 0L;
            } else if (this.mButtonDownTime == 0) {
                Log.i(TAG, "dispatchSensorEvent : Ignoring button up event");
            } else if (this.mIsDetectionCancelled) {
                Log.i(TAG, "dispatchSensorEvent : button detection cancelled");
                this.mButtonDownTime = 0L;
            } else {
                long elapsedRealtime = SystemClock.elapsedRealtime() - this.mButtonDownTime;
                String str = TAG;
                Log.d(str, "dispatchSensorEvent : button press duration = " + elapsedRealtime);
                this.mButtonDownTime = 0L;
                onSingleClicked(bleSpenButtonEvent);
            }
        } else {
            String str2 = TAG;
            Log.e(str2, "dispatchSensorEvent : Unexpected button event : " + action);
        }
    }

    public void setHoverEnterState(boolean z) {
        if (this.mIsHoverEntered != z) {
            this.mIsHoverEntered = z;
            if (!z || this.mButtonDownTime <= 0) {
                return;
            }
            Log.d(TAG, "setHoverEnterState : hover detected. cancels button detection");
            cancelDetection();
        }
    }

    public void setPenButtonPressedOnHover(boolean z) {
        if (this.mIsPenButtonPressedOnHover != z) {
            this.mIsPenButtonPressedOnHover = z;
            String str = TAG;
            Log.d(str, "setPenButtonPressedOnHover : " + this.mIsPenButtonPressedOnHover);
        }
    }

    public void enableDoubleClickHoldDetection(boolean z) {
        this.mIsDoubleClickHoldDetectionEnabled = z;
        String str = TAG;
        Log.d(str, "enableDoubleClickHoldDetection : enabled=" + this.mIsDoubleClickHoldDetectionEnabled);
    }

    public void enableDoubleClickDetection(boolean z) {
        this.mIsDoubleClickDetectionEnabled = z;
        if (z) {
            String str = TAG;
            Log.d(str, "enableDoubleClickDetection: enabled=" + this.mIsDoubleClickDetectionEnabled + ", doubleClickWaitInterval=" + this.mDoubleClickWaitInterval);
            return;
        }
        String str2 = TAG;
        Log.d(str2, "enableDoubleClickDetection: enabled=" + this.mIsDoubleClickDetectionEnabled);
    }

    public void setDoubleClickWaitInterval(int i) {
        this.mDoubleClickWaitInterval = i;
        String str = TAG;
        Log.d(str, "setDoubleClickWaitInterval: mDoubleClickWaitInterval is changed to " + i);
    }

    public void cancelDetection() {
        if (this.mIsDetectionCancelled) {
            return;
        }
        Log.d(TAG, "cancelDetection");
        this.mIsDetectionCancelled = true;
        cancelLongClickHandler();
        cancelDoubleClickHoldHandler();
        if (isWaitingNextActionAfterClick()) {
            this.mClickActionHandler = null;
        }
    }

    public void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        this.mDoubleClickAndHoldWaitInterval = sensorActionDetectorParams.doubleClickAndHoldWaitInterval;
    }

    public void getDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        sensorActionDetectorParams.doubleClickAndHoldWaitInterval = this.mDoubleClickAndHoldWaitInterval;
    }

    private void onButtonDown(final BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onButtonDown : " + bleSpenButtonEvent.getButtonId());
        if (isHovering()) {
            Log.e(str, "onButtonDown : Hover is entered. ignores the click event");
            this.mClickActionHandler = null;
        } else if (isPenButtonPressedOnHover()) {
            Log.e(str, "onButtonDown : Pen button is pressed on hover. ignores the long click event");
            this.mLongClickActionHandler = null;
        } else {
            this.mButtonDownTime = SystemClock.elapsedRealtime();
            cancelLongClickHandler();
            cancelDoubleClickHoldHandler();
            if (isWaitingNextActionAfterClick()) {
                if (this.mIsDoubleClickHoldDetectionEnabled) {
                    Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.detector.ButtonActionDetector.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Log.d(ButtonActionDetector.TAG, "mDoubleClickHoldActionHandler : timer expired");
                            ButtonActionDetector.this.cancelSingleClickHandler();
                            ButtonActionDetector.this.mDoubleClickHoldActionHandler = null;
                            ButtonActionDetector.this.mButtonDownTime = 0L;
                            if (ButtonActionDetector.this.mIsDetectionCancelled) {
                                Log.d(ButtonActionDetector.TAG, "onButtonDown : Double click and hold cancelled");
                                return;
                            }
                            Log.d(ButtonActionDetector.TAG, "onButtonDown : Double click and hold started");
                            bleSpenButtonEvent.setEventTime(SystemClock.elapsedRealtime());
                            ButtonActionDetector.this.onDoubleClickHoldStarted(bleSpenButtonEvent);
                        }
                    };
                    this.mDoubleClickHoldActionHandler = runnable;
                    if (this.mIsDoubleClickDetectionEnabled) {
                        this.mHandler.postDelayed(runnable, this.mDoubleClickAndHoldWaitInterval);
                        return;
                    } else {
                        runnable.run();
                        return;
                    }
                }
                return;
            }
            this.mIsDetectionCancelled = false;
            Runnable runnable2 = new Runnable() { // from class: com.samsung.remotespen.core.device.control.detector.ButtonActionDetector.2
                @Override // java.lang.Runnable
                public void run() {
                    Log.d(ButtonActionDetector.TAG, "mLongClickActionHandler : Long click handler expired");
                    ButtonActionDetector.this.mLongClickActionHandler = null;
                    ButtonActionDetector.this.mButtonDownTime = 0L;
                    if (ButtonActionDetector.this.mIsDetectionCancelled) {
                        Log.d(ButtonActionDetector.TAG, "onButtonDown : Long click cancelled");
                        return;
                    }
                    Log.d(ButtonActionDetector.TAG, "onButtonDown : Long click started");
                    bleSpenButtonEvent.setEventTime(SystemClock.elapsedRealtime());
                    ButtonActionDetector.this.onLongClickStarted(bleSpenButtonEvent);
                }
            };
            this.mLongClickActionHandler = runnable2;
            this.mHandler.postDelayed(runnable2, 800L);
        }
    }

    private void cancelLongClickHandler() {
        Runnable runnable = this.mLongClickActionHandler;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mLongClickActionHandler = null;
        }
    }

    private void cancelDoubleClickHoldHandler() {
        Runnable runnable = this.mDoubleClickHoldActionHandler;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mDoubleClickHoldActionHandler = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelSingleClickHandler() {
        Log.d(TAG, "cancelSingleClickHandler");
        Runnable runnable = this.mClickActionHandler;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mClickActionHandler = null;
        }
    }

    private void onSingleClicked(final BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onSingleClicked : " + bleSpenButtonEvent.getButtonId());
        if (isHovering()) {
            Log.e(str, "onSingleClicked : Hover is entered. ignores the click event");
            this.mClickActionHandler = null;
        } else if (isPenButtonPressedOnHover()) {
            Log.e(str, "onSingleClicked : Pen button is pressed on hover. ignores the click event");
            this.mClickActionHandler = null;
        } else if (isWaitingNextActionAfterClick()) {
            Log.d(str, "onSingleClicked : double clicked");
            cancelSingleClickHandler();
            cancelDoubleClickHoldHandler();
            this.mListener.onButtonEvent(createButtonEvent(BleSpenButtonEvent.Action.DOUBLE_CLICKED, bleSpenButtonEvent));
        } else {
            this.mClickActionHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.control.detector.ButtonActionDetector.3
                @Override // java.lang.Runnable
                public void run() {
                    if (ButtonActionDetector.this.mDoubleClickHoldActionHandler != null) {
                        Log.d(ButtonActionDetector.TAG, "mClickActionHandler : double click and hold action is working");
                        return;
                    }
                    Log.d(ButtonActionDetector.TAG, "mClickActionHandler : single clicked");
                    ButtonActionDetector.this.mListener.onButtonEvent(ButtonActionDetector.this.createButtonEvent(BleSpenButtonEvent.Action.SINGLE_CLICKED, bleSpenButtonEvent));
                    ButtonActionDetector.this.mClickActionHandler = null;
                }
            };
            this.mHandler.postDelayed(this.mClickActionHandler, shouldWaitNextActionAfterClick() ? this.mDoubleClickWaitInterval : 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLongClickStarted(BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onLongClickStarted : " + bleSpenButtonEvent.getButtonId());
        if (isHovering()) {
            Log.e(str, "onLongClickStarted : Hover is entered. ignores the long click event");
        } else if (isPenButtonPressedOnHover()) {
            Log.e(str, "onLongClickStarted : Pen button is pressed on hover state. ignores the long click event");
        } else {
            this.mIsLongClickStarted = true;
            this.mListener.onButtonEvent(createButtonEvent(BleSpenButtonEvent.Action.LONG_CLICK_STARTED, bleSpenButtonEvent));
        }
    }

    private void onLongClickFinished(BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onLongClickFinished : " + bleSpenButtonEvent.getButtonId());
        if (!this.mIsLongClickStarted) {
            Log.e(str, "onLongClickFinished : long click not triggered");
        }
        this.mListener.onButtonEvent(createButtonEvent(BleSpenButtonEvent.Action.LONG_CLICK_FINISHED, bleSpenButtonEvent));
        this.mIsLongClickStarted = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDoubleClickHoldStarted(BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onDoubleClickHoldStarted : " + bleSpenButtonEvent.getButtonId());
        if (isHovering()) {
            Log.e(str, "onDoubleClickHoldStarted : Hover is entered. ignores the event");
        } else if (isPenButtonPressedOnHover()) {
            Log.e(str, "onDoubleClickHoldStarted : Pen button is pressed on hover state. ignores the event");
        } else {
            this.mIsDoubleClickHoldModeStarted = true;
            this.mListener.onButtonEvent(createButtonEvent(BleSpenButtonEvent.Action.DOUBLE_CLICK_HOLD_STARTED, bleSpenButtonEvent));
        }
    }

    private void onDoubleClickHoldFinished(BleSpenButtonEvent bleSpenButtonEvent) {
        String str = TAG;
        Log.d(str, "onDoubleClickHoldFinished : " + bleSpenButtonEvent.getButtonId());
        if (!this.mIsDoubleClickHoldModeStarted) {
            Log.e(str, "onDoubleClickHoldFinished : Double click and hold action was not triggered");
        }
        this.mListener.onButtonEvent(createButtonEvent(BleSpenButtonEvent.Action.DOUBLE_CLICK_HOLD_FINISHED, bleSpenButtonEvent));
        this.mIsDoubleClickHoldModeStarted = false;
    }

    private boolean isHovering() {
        return this.mIsHoverEntered;
    }

    private boolean isPenButtonPressedOnHover() {
        return this.mIsPenButtonPressedOnHover;
    }

    private boolean isWaitingNextActionAfterClick() {
        return this.mClickActionHandler != null;
    }

    private int getDefaultDoubleClickInterval() {
        if (SUPPORT_CI_CHANGE_FOR_SPEN) {
            return DOUBLE_CLICK_WAIT_INTERVAL_FOR_CI_CHANGE;
        }
        return 300;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public BleSpenButtonEvent createButtonEvent(BleSpenButtonEvent.Action action, BleSpenButtonEvent bleSpenButtonEvent) {
        if (bleSpenButtonEvent == null) {
            Assert.fail("reference event is null!");
            Log.e(TAG, "createButtonEvent : reference event is null!");
            return null;
        }
        BleSpenButtonEvent bleSpenButtonEvent2 = new BleSpenButtonEvent(bleSpenButtonEvent);
        bleSpenButtonEvent2.setAction(action);
        bleSpenButtonEvent2.setEventTime(SystemClock.elapsedRealtime());
        return bleSpenButtonEvent2;
    }

    private boolean shouldWaitNextActionAfterClick() {
        return this.mIsDoubleClickDetectionEnabled || this.mIsDoubleClickHoldDetectionEnabled;
    }
}
