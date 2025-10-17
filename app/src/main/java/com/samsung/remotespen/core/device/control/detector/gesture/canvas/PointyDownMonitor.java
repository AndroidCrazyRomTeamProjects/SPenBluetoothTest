package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.graphics.PointF;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class PointyDownMonitor extends AbsPointyMonitor {
    private static String TAG = "PointyDownMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public int getDirToExpect() {
        return -1;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public int getMaxAcuteDegree() {
        return R.styleable.AppCompatTheme_textAppearanceSearchResultTitle;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getMaxImpurityRatio() {
        return 0.7f;
    }

    public PointyDownMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.POINTY_DOWN;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public boolean isReversed(Event event) {
        return this.mDirToExpect > 0 ? event.deltaY < 0.0f : event.deltaY > 0.0f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsPointyMonitor
    public PointF getUnitVector() {
        PointF pointF = AbsPointyMonitor.UNIT_VECTOR;
        return new PointF(pointF.x * 0.0f, pointF.y * (-1.0f));
    }
}
