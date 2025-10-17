package com.samsung.remotespen.core.device.control.detector.gesture;

import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;

/* loaded from: classes.dex */
public class GestureScore {
    private int mEndIndex;
    private double mRawTravelDistance;
    private int mStartIndex;
    private double mTravelDistance;
    private BleSpenGestureType mType;
    private float mExtraWeightPoint = 1.0f;
    private JudgeScore mJudgeScore = new JudgeScore();
    private BleSpenGestureQuality mGestureQuality = new BleSpenGestureQuality();

    public GestureScore(BleSpenGestureType bleSpenGestureType) {
        this.mType = bleSpenGestureType;
    }

    public BleSpenGestureType getType() {
        return this.mType;
    }

    public void setStartIndex(int i) {
        this.mStartIndex = i;
    }

    public void setEndIndex(int i) {
        this.mEndIndex = i;
    }

    public int getStartIndex() {
        return this.mStartIndex;
    }

    public int getEndIndex() {
        return this.mEndIndex;
    }

    public double getTravelDistance() {
        return this.mTravelDistance;
    }

    public double getRawTravelDistance() {
        return this.mRawTravelDistance;
    }

    public void setTravelDistance(double d) {
        this.mTravelDistance = d;
    }

    public double increaseTravelDistance(double d) {
        double d2 = this.mTravelDistance + d;
        this.mTravelDistance = d2;
        return d2;
    }

    public double increaseRawTravelDistance(double d) {
        double d2 = this.mRawTravelDistance + d;
        this.mRawTravelDistance = d2;
        return d2;
    }

    public JudgeScore getJudgeScore() {
        return this.mJudgeScore;
    }

    public void setGestureQuality(BleSpenGestureQuality bleSpenGestureQuality) {
        this.mGestureQuality = bleSpenGestureQuality;
    }

    public BleSpenGestureQuality getGestureQuality() {
        return this.mGestureQuality;
    }

    public void setExtraWeightPoint(float f) {
        this.mExtraWeightPoint = f;
    }

    public double getWeight() {
        return this.mExtraWeightPoint * ((this.mJudgeScore.getOkCount() - this.mJudgeScore.getNotOkCount()) + (this.mJudgeScore.getCriticalCount() * (-2)));
    }

    public void reset() {
        this.mTravelDistance = 0.0d;
        this.mRawTravelDistance = 0.0d;
        this.mStartIndex = -1;
        this.mEndIndex = -1;
        this.mJudgeScore.reset();
        this.mGestureQuality.reset();
    }

    /* renamed from: clone */
    public GestureScore m14clone() {
        GestureScore gestureScore = new GestureScore(this.mType);
        gestureScore.mTravelDistance = this.mTravelDistance;
        gestureScore.mRawTravelDistance = this.mRawTravelDistance;
        gestureScore.mStartIndex = this.mStartIndex;
        gestureScore.mEndIndex = this.mEndIndex;
        gestureScore.mJudgeScore = this.mJudgeScore.m15clone();
        gestureScore.mGestureQuality = this.mGestureQuality;
        return gestureScore;
    }

    public String toString() {
        return this.mType + ":[TravelDist : " + this.mTravelDistance + "][Start : " + this.mStartIndex + "][End : " + this.mEndIndex + "] - " + this.mJudgeScore + this.mGestureQuality + "[Weight : " + getWeight() + "]";
    }
}
