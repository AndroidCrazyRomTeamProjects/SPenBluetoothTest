package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.graphics.PointF;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class PointyLeftMonitor extends AbsPointyMonitor {
    private static String TAG = "PointyLeftMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public int getDirToExpect() {
        return -1;
    }

    public PointyLeftMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.POINTY_LEFT;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public boolean isReversed(Event event) {
        return this.mDirToExpect > 0 ? event.deltaX < 0.0f : event.deltaX > 0.0f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public PointF getUnitVector() {
        PointF pointF = AbsPointyMonitor.UNIT_VECTOR;
        return new PointF(pointF.x * (-1.0f), pointF.y * 0.0f);
    }
}
