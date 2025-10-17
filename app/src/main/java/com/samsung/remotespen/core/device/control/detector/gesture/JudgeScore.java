package com.samsung.remotespen.core.device.control.detector.gesture;

/* loaded from: classes.dex */
public class JudgeScore {
    private int mCritical;
    private int mNotOk;
    private int mOk;

    public void reset() {
        this.mOk = 0;
        this.mNotOk = 0;
        this.mCritical = 0;
    }

    public void countOk() {
        this.mOk++;
    }

    public void countNotOk() {
        this.mNotOk++;
    }

    public void countCritical() {
        this.mCritical++;
    }

    public int getOkCount() {
        return this.mOk;
    }

    public int getNotOkCount() {
        return this.mNotOk;
    }

    public int getCriticalCount() {
        return this.mCritical;
    }

    /* renamed from: clone */
    public JudgeScore m15clone() {
        JudgeScore judgeScore = new JudgeScore();
        judgeScore.mOk = this.mOk;
        judgeScore.mNotOk = this.mNotOk;
        judgeScore.mCritical = this.mCritical;
        return judgeScore;
    }

    public String toString() {
        return "[OK : " + this.mOk + "][NOK : " + this.mNotOk + "][Critical : " + this.mCritical + "]";
    }
}
