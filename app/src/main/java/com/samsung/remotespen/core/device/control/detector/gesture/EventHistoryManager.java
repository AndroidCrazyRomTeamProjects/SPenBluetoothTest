package com.samsung.remotespen.core.device.control.detector.gesture;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public class EventHistoryManager {
    private static final String TAG = "EventHistoryManager";
    private int mApproachedEventCount;
    private int mEventTotalCount;
    private int mHoverEnteredEventCount;
    private int mImpurityEventCount;
    private float mPositionX;
    private float mPositionY;
    private int mScreenTouchedEventCount;
    private List<Event> mEvents = Collections.synchronizedList(new ArrayList());
    private float mMinX = Float.MAX_VALUE;
    private float mMinY = Float.MAX_VALUE;
    private float mMaxX = Float.MIN_VALUE;
    private float mMaxY = Float.MIN_VALUE;
    private double mTravelLength = 0.0d;

    public void addEvent(Event event) {
        this.mPositionX += event.deltaX;
        this.mPositionY += event.deltaY;
        Log.v(TAG, "addEvent : Position = " + this.mPositionX + ", " + this.mPositionY);
        event.x = this.mPositionX;
        event.y = this.mPositionY;
        this.mEvents.add(event);
        updateMinMaxRange();
        double d = this.mTravelLength;
        float f = event.deltaX;
        float f2 = event.deltaY;
        double sqrt = d + Math.sqrt((f * f) + (f2 * f2));
        this.mTravelLength = sqrt;
        event.travelLength = sqrt;
        this.mEventTotalCount++;
        if (event.isImpurity) {
            this.mImpurityEventCount++;
        }
        if (event.isHoverEntered) {
            this.mHoverEnteredEventCount++;
        }
        if (event.isApproached) {
            this.mApproachedEventCount++;
        }
        if (event.isScreenTouched) {
            this.mScreenTouchedEventCount++;
        }
    }

    private void updateMinMaxRange() {
        float f = this.mMinX;
        float f2 = this.mPositionX;
        if (f > f2) {
            this.mMinX = f2;
        } else if (this.mMaxX < f2) {
            this.mMaxX = f2;
        }
        float f3 = this.mMinY;
        float f4 = this.mPositionY;
        if (f3 > f4) {
            this.mMinY = f4;
        } else if (this.mMaxY < f4) {
            this.mMaxY = f4;
        }
        String str = TAG;
        Log.v(str, "Rect = " + this.mMinX + ", " + this.mMinY + ", " + this.mMaxX + ", " + this.mMaxY);
    }

    public Event getEvent(int i) {
        return this.mEvents.get(i);
    }

    public Event getLastEvent() {
        int eventCount = getEventCount() - 1;
        if (eventCount > -1) {
            return this.mEvents.get(eventCount);
        }
        return new Event(0.0f, 0.0f);
    }

    public int getEventCount() {
        return this.mEvents.size();
    }

    public void removeLast() {
        this.mEvents.remove(getEventCount() - 1);
    }

    public List<Event> getAllEvents() {
        return this.mEvents;
    }

    public int getActualEventCount() {
        return this.mEventTotalCount;
    }

    public int getImpurityEventCount() {
        return this.mImpurityEventCount;
    }

    public int getHoverEnteredEventCount() {
        return this.mHoverEnteredEventCount;
    }

    public int getApproachedEventCount() {
        return this.mApproachedEventCount;
    }

    public int getScreenTouchedEventCount() {
        return this.mScreenTouchedEventCount;
    }

    public void clear() {
        this.mEvents.clear();
        this.mPositionX = 0.0f;
        this.mPositionY = 0.0f;
        this.mMinX = Float.MAX_VALUE;
        this.mMinY = Float.MAX_VALUE;
        this.mMaxX = Float.MIN_VALUE;
        this.mMaxY = Float.MIN_VALUE;
        this.mTravelLength = 0.0d;
        this.mEventTotalCount = 0;
        this.mImpurityEventCount = 0;
        this.mHoverEnteredEventCount = 0;
        this.mApproachedEventCount = 0;
        this.mScreenTouchedEventCount = 0;
    }

    public float getPositionX() {
        return this.mPositionX;
    }

    public float getPositionY() {
        return this.mPositionY;
    }

    public double getTravelLength() {
        return this.mTravelLength;
    }

    public RectF getEventRange() {
        return new RectF(this.mMinX, this.mMinY, this.mMaxX, this.mMaxY);
    }

    public int getEventIndex(float f, float f2, int i) {
        String str = TAG;
        Log.d(str, "getEventIndex : candidateIndex = " + i);
        Log.d(str, "getEventIndex : getEventCount() = " + getEventCount());
        while (i < getEventCount()) {
            Event event = getEvent(i);
            if (event.x == f && event.y == f2) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public double getTravelLength(int i, int i2) {
        return getEvent(i2).travelLength - getEvent(i).travelLength;
    }

    public float getAverageDelta(int i, int i2) {
        int size = this.mEvents.size();
        float f = 0.0f;
        if (this.mEvents.isEmpty() || size < i || size < i2) {
            return 0.0f;
        }
        for (int i3 = i; i3 <= i2; i3++) {
            f += Utils.getDistance(this.mEvents.get(i3).deltaX, this.mEvents.get(i3).deltaY);
        }
        return f / ((i2 - i) + 1);
    }

    public int getIndexOfNPercentTravelLength(float f) {
        double d = this.mTravelLength * f;
        int i = 0;
        while (i < this.mEventTotalCount && this.mEvents.get(i).travelLength <= d) {
            i++;
        }
        return i;
    }
}
