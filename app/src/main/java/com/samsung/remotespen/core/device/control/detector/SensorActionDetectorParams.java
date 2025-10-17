package com.samsung.remotespen.core.device.control.detector;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import java.util.HashSet;

/* loaded from: classes.dex */
public class SensorActionDetectorParams {
    public int doubleClickAndHoldWaitInterval;
    public HashSet<BleSpenGestureType> mBoostGestureSet = new HashSet<>();

    public void setGestureBoosted(BleSpenGestureType bleSpenGestureType, boolean z) {
        if (z) {
            this.mBoostGestureSet.add(bleSpenGestureType);
        } else {
            this.mBoostGestureSet.remove(bleSpenGestureType);
        }
    }

    public void clearAllBoostedGestures() {
        this.mBoostGestureSet.clear();
    }
}
