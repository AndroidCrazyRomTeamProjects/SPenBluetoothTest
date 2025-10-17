package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class CircleCcwMonitor extends AbsCircleMonitor {
    private static final String TAG = "CircleCcwMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor
    public int getNegativeDirection() {
        return 4;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor
    public int getPositiveDirection() {
        return 2;
    }

    public CircleCcwMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.CIRCLE_CCW;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public String getTag() {
        return TAG;
    }
}
