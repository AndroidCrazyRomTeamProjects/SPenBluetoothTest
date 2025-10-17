package com.samsung.remotespen.util.graph;

import android.graphics.PointF;

/* loaded from: classes.dex */
public class GraphUtils {
    private static final float MAX_GRADIENT = 1000000.0f;
    private static final String TAG = "GraphUtils";

    /* loaded from: classes.dex */
    public static class Line {
        public float mGradient;
        public float mOffset;

        public Line(float f, float f2) {
            this.mGradient = f;
            this.mOffset = f2;
        }

        public static Line createLine(float f, float f2, float f3, float f4) {
            if (f == f3 && f2 == f4) {
                return null;
            }
            return new Line(f, f2, f3, f4);
        }

        public float getAngle() {
            return (float) Math.toDegrees(Math.atan(this.mGradient));
        }

        private Line(float f, float f2, float f3, float f4) {
            int i = (f > f3 ? 1 : (f == f3 ? 0 : -1));
            if (i == 0 && f2 != f4) {
                this.mGradient = GraphUtils.MAX_GRADIENT;
                if (f4 < f2) {
                    this.mGradient = GraphUtils.MAX_GRADIENT * (-1.0f);
                }
            } else if (i != 0 && f2 == f4) {
                this.mGradient = 0.0f;
            } else {
                this.mGradient = (f4 - f2) / (f3 - f);
            }
            this.mOffset = f2 - (this.mGradient * f);
        }

        public String toString() {
            return String.format("Gradient=%.2f, Offset=%.2f, Angle=%.1f", Float.valueOf(this.mGradient), Float.valueOf(this.mOffset), Float.valueOf(getAngle()));
        }

        public float getY(float f) {
            return (this.mGradient * f) + this.mOffset;
        }

        public float getX(float f) {
            return (f - this.mOffset) / this.mGradient;
        }
    }

    public static PointF getCrossedPosition(Line line, Line line2) {
        float f = line.mGradient;
        float f2 = line2.mGradient;
        if (f == f2) {
            return null;
        }
        float f3 = (line2.mOffset - line.mOffset) / (f - f2);
        return new PointF(f3, line.getY(f3));
    }

    public static float getMinDistance(Line line, float f, float f2) {
        PointF minDistanceCrossPosition = getMinDistanceCrossPosition(line, f, f2);
        if (minDistanceCrossPosition == null) {
            return 0.0f;
        }
        float f3 = f - minDistanceCrossPosition.x;
        float f4 = f2 - minDistanceCrossPosition.y;
        return (float) Math.sqrt((f3 * f3) + (f4 * f4));
    }

    public static PointF getMinDistanceCrossPosition(Line line, float f, float f2) {
        float f3 = line.mGradient;
        float f4 = f3 == 0.0f ? -1000000.0f : (-1.0f) / f3;
        return getCrossedPosition(line, new Line(f4, ((-f4) * f) + f2));
    }

    public static float getDistance(float f, float f2, float f3, float f4) {
        float f5 = f3 - f;
        float f6 = f4 - f2;
        return (float) Math.sqrt((f5 * f5) + (f6 * f6));
    }

    public static int getDistance(int i, int i2, int i3, int i4) {
        float f = i3 - i;
        float f2 = i4 - i2;
        return (int) Math.sqrt((f * f) + (f2 * f2));
    }

    public static int getAngle3p(float f, float f2, float f3, float f4) {
        return (int) ((Math.acos(((f * f3) + (f2 * f4)) / (((float) Math.sqrt((f * f) + (f2 * f2))) * ((float) Math.sqrt((f3 * f3) + (f4 * f4))))) * 180.0d) / 3.141592d);
    }
}
