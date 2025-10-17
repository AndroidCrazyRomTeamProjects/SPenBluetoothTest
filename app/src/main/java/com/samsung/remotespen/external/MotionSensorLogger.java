package com.samsung.remotespen.external;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenAirMotionEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SensorCmdDispatcher.java */
/* loaded from: classes.dex */
public class MotionSensorLogger {
    private static final String TAG = "MotionSensorLogger";
    private Context mContext;
    private String mLogcatTag = MotionSensorLogger.class.getSimpleName();
    private BleSpenManager.SensorEventListener mSensorEventListener = new BleSpenManager.SensorEventListener() { // from class: com.samsung.remotespen.external.MotionSensorLogger.1
        @Override // com.samsung.remotespen.core.device.BleSpenManager.SensorEventListener
        public void onSpenSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent) {
            MotionSensorLogger.this.onSpenSensorEvent(bleSpenSensorEvent);
        }
    };

    public MotionSensorLogger(Context context) {
        Log.d(TAG, TAG);
        this.mContext = context.getApplicationContext();
    }

    public void startLogging(String str) {
        String str2 = TAG;
        Log.d(str2, "startLogging");
        this.mLogcatTag = str;
        BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
        if (bleSpenManager == null) {
            Log.e(str2, "startLogging : Not supports BLE SPen");
            return;
        }
        bleSpenManager.unregisterSensorEventListener(this.mSensorEventListener);
        bleSpenManager.registerSensorEventListener(this.mSensorEventListener);
    }

    public void stopLogging() {
        String str = TAG;
        Log.d(str, "stopLogging");
        BleSpenManager bleSpenManager = BleSpenManager.getInstance(this.mContext);
        if (bleSpenManager == null) {
            Log.e(str, "stopLogging : Not supports BLE SPen");
        } else {
            bleSpenManager.unregisterSensorEventListener(this.mSensorEventListener);
        }
    }

    public void release() {
        stopLogging();
    }

    /* compiled from: SensorCmdDispatcher.java */
    /* renamed from: com.samsung.remotespen.external.MotionSensorLogger$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType;

        static {
            int[] iArr = new int[BleSpenSensorType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType = iArr;
            try {
                iArr[BleSpenSensorType.BUTTON.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[BleSpenSensorType.AIR_GESTURE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
        int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[bleSpenSensorEvent.getSensorType().ordinal()];
        if (i == 1) {
            String str = this.mLogcatTag;
            android.util.Log.d(str, "Button : " + ((BleSpenButtonEvent) bleSpenSensorEvent).getAction());
        } else if (i != 2) {
        } else {
            BleSpenAirMotionEvent bleSpenAirMotionEvent = (BleSpenAirMotionEvent) bleSpenSensorEvent;
            String str2 = this.mLogcatTag;
            android.util.Log.d(str2, "Air motion : x=" + bleSpenAirMotionEvent.getX() + ", y=" + bleSpenAirMotionEvent.getY());
        }
    }
}
