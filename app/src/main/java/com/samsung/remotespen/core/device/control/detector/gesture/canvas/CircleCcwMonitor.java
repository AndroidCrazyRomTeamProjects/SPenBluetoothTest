package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class CircleCcwMonitor extends AbsCircleMonitor {
    private static final String TAG = "CircleCcwMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsCircleMonitor
    public int getNegativeDirection() {
        return 4;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsCircleMonitor
    public int getPositiveDirection() {
        return 2;
    }

    public CircleCcwMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.CIRCLE_CCW;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }
}
