package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class CircleCwMonitor extends AbsCircleMonitor {
    private static final String TAG = "CircleCwMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor
    public int getNegativeDirection() {
        return 2;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsCircleMonitor
    public int getPositiveDirection() {
        return 4;
    }

    public CircleCwMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.CIRCLE_CW;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public String getTag() {
        return TAG;
    }
}
