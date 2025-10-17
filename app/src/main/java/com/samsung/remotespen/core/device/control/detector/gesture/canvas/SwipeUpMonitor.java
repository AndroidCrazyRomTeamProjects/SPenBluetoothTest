package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class SwipeUpMonitor extends AbsSwipeMonitor {
    private static final String TAG = "SwipeUpMonitor";

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getMaxImpurityRatio() {
        return 0.7f;
    }

    public SwipeUpMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsSwipeMonitor
    public boolean isReversed(Event event) {
        return event.deltaY < 0.0f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsSwipeMonitor
    public boolean isMovedOverAllowed(Event event) {
        return event.absDeltaY < event.absDeltaX;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.AbsSwipeMonitor
    public boolean isMovedTooFarAway(Event event) {
        EventHistoryManager eventHistoryManager = getEventHistoryManager();
        return eventHistoryManager != null && Math.abs(eventHistoryManager.getPositionX()) > 300.0f && event.absDeltaX >= event.absDeltaY;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.SWIPE_UP;
    }
}
