package com.samsung.remotespen.core.device.data;

/* loaded from: classes.dex */
public class BleSpenGestureQuality {
    private static final String TAG = "BleSpenGestureQuality";
    public BleSpenGestureApproachState mGestureApproachState;
    public BleSpenGestureHoverState mGestureHoverState;
    public BleSpenGestureImpurity mGestureImpurity;
    public BleSpenGestureSize mGestureSize;
    public BleSpenGestureSpeed mGestureSpeed;
    public BleSpenGestureScreenTouchState mGestureTouchState;

    public BleSpenGestureQuality() {
        resetQualityFactors();
    }

    public void reset() {
        resetQualityFactors();
    }

    private void resetQualityFactors() {
        this.mGestureSize = BleSpenGestureSize.NONE;
        this.mGestureSpeed = BleSpenGestureSpeed.NONE;
        this.mGestureImpurity = BleSpenGestureImpurity.NONE;
        this.mGestureHoverState = BleSpenGestureHoverState.NONE;
        this.mGestureApproachState = BleSpenGestureApproachState.NONE;
        this.mGestureTouchState = BleSpenGestureScreenTouchState.NONE;
    }

    public BleSpenGestureSize getGestureSize() {
        return this.mGestureSize;
    }

    public void setGestureSize(BleSpenGestureSize bleSpenGestureSize) {
        this.mGestureSize = bleSpenGestureSize;
    }

    public BleSpenGestureSpeed getGestureSpeed() {
        return this.mGestureSpeed;
    }

    public void setGestureSpeed(BleSpenGestureSpeed bleSpenGestureSpeed) {
        this.mGestureSpeed = bleSpenGestureSpeed;
    }

    public BleSpenGestureImpurity getGestureImpurity() {
        return this.mGestureImpurity;
    }

    public void setGestureImpurity(BleSpenGestureImpurity bleSpenGestureImpurity) {
        this.mGestureImpurity = bleSpenGestureImpurity;
    }

    public BleSpenGestureHoverState getGestureHoverState() {
        return this.mGestureHoverState;
    }

    public void setGestureHoverState(BleSpenGestureHoverState bleSpenGestureHoverState) {
        this.mGestureHoverState = bleSpenGestureHoverState;
    }

    public BleSpenGestureApproachState getGestureApproachState() {
        return this.mGestureApproachState;
    }

    public void setGestureApproachState(BleSpenGestureApproachState bleSpenGestureApproachState) {
        this.mGestureApproachState = bleSpenGestureApproachState;
    }

    public BleSpenGestureScreenTouchState getGestureTouchState() {
        return this.mGestureTouchState;
    }

    public void setGestureTouchState(BleSpenGestureScreenTouchState bleSpenGestureScreenTouchState) {
        this.mGestureTouchState = bleSpenGestureScreenTouchState;
    }

    public boolean isCleanGesture() {
        return (getGestureImpurity() == BleSpenGestureImpurity.HIGH_IMPURITY || getGestureHoverState() == BleSpenGestureHoverState.HOVERED || getGestureApproachState() == BleSpenGestureApproachState.APPROACHED || getGestureTouchState() == BleSpenGestureScreenTouchState.TOUCHED) ? false : true;
    }

    public String toString() {
        return "[Gesture Size : " + this.mGestureSize + "][Gesture Speed : " + this.mGestureSpeed + "][Gesture Impurity : " + this.mGestureImpurity + "][Gesture HoverState : " + this.mGestureHoverState + "][Gesture ApproachState : " + this.mGestureApproachState + "][Gesture TouchState : " + this.mGestureTouchState + "]";
    }
}
