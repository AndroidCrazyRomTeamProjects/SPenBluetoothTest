package com.samsung.remotespen.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.samsung.util.CommonUtils;
import com.samsung.util.SpenGestureManagerWrapper;

/* loaded from: classes.dex */
public class MagneticSensorDetector {
    private static final String TAG = "MagneticSensorDetector";
    private static MagneticSensorDetector sInstance;
    private final float mBasicMagneticSensorData;
    private final Context mContext;
    private final SensorEventListener mMagneticSensorEventListenerForDetached = new SensorEventListener() { // from class: com.samsung.remotespen.util.MagneticSensorDetector.1
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == 14) {
                MagneticSensorDetector magneticSensorDetector = MagneticSensorDetector.this;
                float[] fArr = sensorEvent.values;
                if (magneticSensorDetector.isMagneticDetected(fArr[1], fArr[0])) {
                    CommonUtils.printScoverState(MagneticSensorDetector.this.mContext);
                    Log.d(MagneticSensorDetector.TAG, "onSensorChanged : set spen pdct low sensitivity enable");
                    MagneticSensorDetector.this.mSpenGestureManagerWrapper.setSpenPdctLowSensitivityEnable();
                }
                MagneticSensorDetector.this.unregisterMagneticSensorEventListener();
            }
        }
    };
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private final SpenGestureManagerWrapper mSpenGestureManagerWrapper;

    private MagneticSensorDetector(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mSpenGestureManagerWrapper = new SpenGestureManagerWrapper(applicationContext);
        float basicMagneticSensorData = CommonUtils.getBasicMagneticSensorData();
        this.mBasicMagneticSensorData = basicMagneticSensorData;
        String str = TAG;
        Log.i(str, "MagneticSensorDetector mBasicMagneticSensorData=" + basicMagneticSensorData);
    }

    public static synchronized MagneticSensorDetector getInstance(Context context) {
        MagneticSensorDetector magneticSensorDetector;
        synchronized (MagneticSensorDetector.class) {
            if (sInstance == null) {
                sInstance = new MagneticSensorDetector(context);
            }
            magneticSensorDetector = sInstance;
        }
        return magneticSensorDetector;
    }

    public boolean isMagneticDetected(float f, float f2) {
        float f3 = f - f2;
        boolean z = f3 > this.mBasicMagneticSensorData;
        String str = TAG;
        Log.i(str, "onSensorChanged event y=" + f + ", x=" + f2 + ", lastMagneticSensorData=" + f3 + ", isSpenPdctLowSensitivityEnable=" + z);
        return z;
    }

    public void start() {
        Log.d(TAG, "start");
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSensorManager = sensorManager;
        this.mSensor = sensorManager.getDefaultSensor(14);
    }

    public void stop() {
        Log.d(TAG, "stop");
    }

    public synchronized void registerMagneticSensorEventListener() {
        if (this.mSensorManager != null) {
            unregisterMagneticSensorEventListener();
            Log.d(TAG, "registerMagneticSensorEventListener");
            this.mSensorManager.registerListener(this.mMagneticSensorEventListenerForDetached, this.mSensor, 3);
        }
    }

    public synchronized void unregisterMagneticSensorEventListener() {
        if (this.mSensorManager != null) {
            Log.d(TAG, "unregisterMagneticSensorEventListener");
            this.mSensorManager.unregisterListener(this.mMagneticSensorEventListenerForDetached, this.mSensor);
        }
    }
}
