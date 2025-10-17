package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class SwipeDownMonitor extends AbsSwipeMonitor {
    private static final String TAG = "SwipeDownMonitor";

    public SwipeDownMonitor(DavinciEventMonitor davinciEventMonitor) {
        super(davinciEventMonitor);
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsSwipeMonitor
    public boolean isReversed(Event event) {
        return event.deltaY > 0.0f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsSwipeMonitor
    public boolean isSlightMotion(Event event) {
        return event.absDeltaY < 0.02f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsSwipeMonitor
    public boolean isMovedOverAllowed(Event event) {
        return event.absDeltaY < event.absDeltaX;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsSwipeMonitor
    public boolean isMovedTooFarAway(Event event) {
        EventHistoryManager rawEventHistoryManager = getRawEventHistoryManager();
        return rawEventHistoryManager != null && Math.abs(rawEventHistoryManager.getPositionX()) > 0.3f && event.absDeltaX >= event.absDeltaY;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.AbsSwipeMonitor
    public void onUpdateTravelDistance(GestureScore gestureScore, Event event) {
        if (event.deltaY < 0.0f) {
            gestureScore.increaseTravelDistance(event.absDeltaY);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.SWIPE_DOWN;
    }
}
