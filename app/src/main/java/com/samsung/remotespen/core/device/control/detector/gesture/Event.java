package com.samsung.remotespen.core.device.control.detector.gesture;

import android.os.SystemClock;
import com.samsung.aboutpage.Constants;

/* loaded from: classes.dex */
public class Event {
    public float absDeltaX;
    public float absDeltaY;
    public float deltaX;
    public float deltaY;
    public boolean isApproached;
    public boolean isHoverEntered;
    public boolean isScreenTouched;
    public double travelLength;
    public float x;
    public float y;
    public boolean isImpurity = false;
    public long timestamp = SystemClock.elapsedRealtime();

    public Event(float f, float f2) {
        this.deltaX = f;
        this.deltaY = f2;
        this.absDeltaX = Math.abs(f);
        this.absDeltaY = Math.abs(f2);
    }

    public void setImpurity() {
        this.isImpurity = true;
    }

    public void setHoverEntered() {
        this.isHoverEntered = true;
    }

    public void setApproached() {
        this.isApproached = true;
    }

    public void setScreenTouchedFlag() {
        this.isScreenTouched = true;
    }

    public String toString() {
        return Constants.packageName.NONE + this.deltaX + ", " + this.deltaY;
    }
}
