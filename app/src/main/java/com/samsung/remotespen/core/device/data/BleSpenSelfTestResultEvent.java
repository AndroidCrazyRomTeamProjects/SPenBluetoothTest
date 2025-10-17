package com.samsung.remotespen.core.device.data;


import android.util.Log;

/* loaded from: classes.dex */
public class BleSpenSelfTestResultEvent extends BleSpenSensorEvent {
    private static final float ACC_SELF_MAX = 1700.0f;
    private static final float ACC_SELF_MIN = 50.0f;
    private static final float GYRO_SELF_DIFF_MAX = 700.0f;
    private static final float GYRO_SELF_DIFF_MIN = 150.0f;
    private static final float GYRO_SELF_MAX = 40.0f;
    private static final float GYRO_SELF_MIN = -40.0f;
    private static final String TAG = "BleSpenSelfTestResultEvent";
    private static final int VALUE_1G = 8192;
    private static final float VECTOR_SUM_MAX = 1.2f;
    private static final float VECTOR_SUM_MIN = 0.8f;
    private XYZ mGyroSelfDiff;
    private SelfTestData mSelfTestData;
    private double mVectorSum;

    /* loaded from: classes.dex */
    public static class SelfTestData {
        public XYZ accRaw;
        public XYZ accSelfMinus;
        public XYZ accSelfPlus;
        public XYZ gyroSelf;
        public XYZ gyroSelfBias;
        public XYZ gyroZro;

        public String toString() {
            return "Acc Raw : " + this.accRaw + "\nACC Self(+) : " + this.accSelfPlus + "\nACC Self(-) : " + this.accSelfMinus + "\nGYRO ZRO : " + this.gyroZro + "\nGYRO Self : " + this.gyroSelf + "\nGYRO Self Bias : " + this.gyroSelfBias + "\n";
        }
    }

    /* loaded from: classes.dex */
    public static class XYZ {
        public float x;
        public float y;
        public float z;

        public XYZ(float f, float f2, float f3) {
            this.x = f;
            this.y = f2;
            this.z = f3;
        }

        public String toString() {
            return "x=" + this.x + ", y=" + this.y + ", z=" + this.z;
        }
    }

    public BleSpenSelfTestResultEvent(SelfTestData selfTestData, long j, BleSpenSensorType bleSpenSensorType, BleSpenSensorId bleSpenSensorId, String str) {
        super(j, bleSpenSensorType, bleSpenSensorId, str);
        this.mSelfTestData = selfTestData;
        XYZ xyz = selfTestData.accRaw;
        float f = xyz.x;
        float f2 = xyz.y;
        float f3 = xyz.z;
        this.mVectorSum = Math.sqrt(((f * f) + (f2 * f2)) + (f3 * f3)) / 8192.0d;
        XYZ xyz2 = selfTestData.gyroSelfBias;
        float f4 = xyz2.x;
        XYZ xyz3 = selfTestData.gyroZro;
        this.mGyroSelfDiff = new XYZ(f4 - xyz3.x, xyz2.y - xyz3.y, xyz2.z - xyz3.z);
    }

    public int getTestResult() {
        double d = this.mVectorSum;
        if (d >= 0.800000011920929d && d <= 1.2000000476837158d) {
            return (isValidTestData(this.mSelfTestData.accSelfPlus, ACC_SELF_MIN, ACC_SELF_MAX, "ACC Self Test(+)") && isValidTestData(this.mSelfTestData.accSelfMinus, ACC_SELF_MIN, ACC_SELF_MAX, "ACC Self Test(-)") && isValidTestData(this.mSelfTestData.gyroZro, GYRO_SELF_MIN, GYRO_SELF_MAX, "GYRO ZRO AVG") && isValidTestData(this.mSelfTestData.gyroSelf, GYRO_SELF_MIN, GYRO_SELF_MAX, "GYRO Self") && isValidTestData(this.mGyroSelfDiff, GYRO_SELF_DIFF_MIN, GYRO_SELF_DIFF_MAX, "GYRO Self Diff")) ? 1 : 0;
        }
        String str = TAG;
        Log.e(str, "Vector Sum = " + this.mVectorSum);
        return 0;
    }

    private boolean isValidTestData(XYZ xyz, float f, float f2, String str) {
        float f3 = xyz.x;
        if (f3 < f || f3 > f2) {
            String str2 = TAG;
            Log.e(str2, str + " X = " + xyz.x);
            return false;
        }
        float f4 = xyz.y;
        if (f4 < f || f4 > f2) {
            String str3 = TAG;
            Log.e(str3, str + " Y = " + xyz.y);
            return false;
        }
        float f5 = xyz.z;
        if (f5 < f || f5 > f2) {
            String str4 = TAG;
            Log.e(str4, str + " Z = " + xyz.z);
            return false;
        }
        return true;
    }

    public String getSummaryString() {
        StringBuilder sb = new StringBuilder();
        if (getTestResult() == 1) {
            sb.append("PASS");
            sb.append("\n");
        } else {
            sb.append("FAIL");
            sb.append("\n");
        }
        sb.append("ACC Vector Sum: " + String.format("%7.6f", Double.valueOf(this.mVectorSum)));
        sb.append("\n");
        sb.append("ACC Self Test(+) : " + this.mSelfTestData.accSelfPlus);
        sb.append("\n");
        sb.append("ACC Self Test(-) : " + this.mSelfTestData.accSelfMinus);
        sb.append("\n");
        sb.append("GYRO ZRO AVG : " + this.mSelfTestData.gyroZro);
        sb.append("\n");
        sb.append("GYRO Self : " + this.mSelfTestData.gyroSelf);
        sb.append("\n");
        sb.append("GYRO Self Diff : " + this.mGyroSelfDiff);
        sb.append("\n");
        return sb.toString();
    }
}
