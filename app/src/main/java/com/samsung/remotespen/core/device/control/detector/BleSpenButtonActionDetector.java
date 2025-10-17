package com.samsung.remotespen.core.device.control.detector;

import com.samsung.remotespen.core.device.control.detector.ButtonActionDetector;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonId;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BleSpenButtonActionDetector {
    private static final String TAG = "BleSpenButtonActionDetector";
    private ArrayList<Listener> mListenerArray = new ArrayList<>();
    private HashMap<BleSpenButtonId, ButtonActionDetector> mActionDetectorMap = new HashMap<>();

    /* loaded from: classes.dex */
    public interface Listener {
        void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent);
    }

    public BleSpenButtonActionDetector(Listener listener) {
        ButtonActionDetector.Listener listener2 = new ButtonActionDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.detector.BleSpenButtonActionDetector.1
            @Override // com.samsung.remotespen.core.device.control.detector.ButtonActionDetector.Listener
            public void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent) {
                Iterator it = BleSpenButtonActionDetector.this.mListenerArray.iterator();
                while (it.hasNext()) {
                    ((Listener) it.next()).onButtonEvent(bleSpenButtonEvent);
                }
            }
        };
        this.mActionDetectorMap.put(BleSpenButtonId.PRIMARY, new ButtonActionDetector(listener2));
        this.mActionDetectorMap.put(BleSpenButtonId.SECONDARY, new ButtonActionDetector(listener2));
        this.mListenerArray.add(listener);
    }

    public void dispatchSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
        if (bleSpenSensorEvent.getSensorType() != BleSpenSensorType.BUTTON) {
            return;
        }
        BleSpenButtonEvent bleSpenButtonEvent = (BleSpenButtonEvent) bleSpenSensorEvent;
        this.mActionDetectorMap.get(bleSpenButtonEvent.getButtonId()).dispatchSensorEvent(bleSpenButtonEvent);
    }

    public void setHoverEnterState(boolean z) {
        for (ButtonActionDetector buttonActionDetector : this.mActionDetectorMap.values()) {
            buttonActionDetector.setHoverEnterState(z);
        }
    }

    public void setPenButtonPressedOnHover(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mActionDetectorMap.get(bleSpenButtonId).setPenButtonPressedOnHover(z);
    }

    public void enableDoubleClickHoldDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mActionDetectorMap.get(bleSpenButtonId).enableDoubleClickHoldDetection(z);
    }

    public void enableDoubleClickDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
        this.mActionDetectorMap.get(bleSpenButtonId).enableDoubleClickDetection(z);
    }

    public void setDoubleClickWaitInterval(int i) {
        for (ButtonActionDetector buttonActionDetector : this.mActionDetectorMap.values()) {
            buttonActionDetector.setDoubleClickWaitInterval(i);
        }
    }

    public void cancelDetection() {
        for (ButtonActionDetector buttonActionDetector : this.mActionDetectorMap.values()) {
            buttonActionDetector.cancelDetection();
        }
    }

    public void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
        for (ButtonActionDetector buttonActionDetector : this.mActionDetectorMap.values()) {
            buttonActionDetector.setDetectorParams(sensorActionDetectorParams);
        }
    }

    public void getDetectorParams(BleSpenButtonId bleSpenButtonId, SensorActionDetectorParams sensorActionDetectorParams) {
        this.mActionDetectorMap.get(bleSpenButtonId).getDetectorParams(sensorActionDetectorParams);
    }
}
