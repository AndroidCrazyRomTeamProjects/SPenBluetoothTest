package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class CircleCwMonitor extends AbsCircleMonitor {
    private static final String TAG = "CircleCwMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsCircleMonitor
    public int getNegativeDirection() {
        return 2;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsCircleMonitor
    public int getPositiveDirection() {
        return 4;
    }

    public CircleCwMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.CIRCLE_CW;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }
}
