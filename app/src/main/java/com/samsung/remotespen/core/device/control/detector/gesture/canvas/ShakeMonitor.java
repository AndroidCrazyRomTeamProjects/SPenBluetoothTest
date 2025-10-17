package com.samsung.remotespen.core.device.control.detector.gesture.canvas;

import android.graphics.RectF;
import android.util.Log;

import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.Utils;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.util.graph.VertexPath;
import com.samsung.remotespen.util.graph.VertexPathNormalizer;

/* loaded from: classes.dex */
public class ShakeMonitor extends CanvasEventMonitor {
    private static final int MAX_ANGLE = 110;
    private static final int MIN_ZIGZAG_COUNT = 3;
    private static final int SHAKE_ALLOWED_CRITICAL_COUNT = 3;
    private static final String TAG = "ShakeMonitor";
    private Event mOldEvent;
    private int mPrevShape;
    private int reverseShapeCount;
    private int reverseXCount;
    private int reverseYCount;

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getErrorFactorThreshold() {
        return 0.1f;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public float getMaxImpurityRatio() {
        return 0.7f;
    }

    public ShakeMonitor(CanvasEventMonitor canvasEventMonitor) {
        super(canvasEventMonitor);
        this.reverseYCount = 0;
        this.reverseXCount = 0;
        this.reverseShapeCount = 0;
        this.mPrevShape = 0;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public BleSpenGestureType whoAmI() {
        return BleSpenGestureType.SHAKE;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onInitialize() {
        super.onInitialize();
        this.mOldEvent = null;
        this.reverseXCount = 0;
        this.reverseYCount = 0;
        this.reverseShapeCount = 0;
        this.mPrevShape = 0;
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void onJudge(Event event, int[] iArr) {
        int i;
        super.onJudge(event, iArr);
        GestureScore gestureScore = getGestureScore();
        JudgeScore judgeScore = gestureScore.getJudgeScore();
        int angle = Utils.getAngle(iArr);
        int shape = Utils.getShape(iArr);
        String str = TAG;
        Log.v(str, "onJudge : Diff = " + event.deltaX + " ," + event.deltaY);
        if (shape == 0 || shape == 1) {
            Log.v(str, "onJudge : skip to update reverse count. shape = " + shape);
        } else {
            if (angle < 110) {
                updateReverseCount(event);
            }
            updateReverseShapeCount(shape);
        }
        this.mOldEvent = event;
        this.mPrevShape = shape;
        if (judgeScore.getCriticalCount() >= 3) {
            setJudgeState(JudgeState.NOT_APPLICABLE);
        } else {
            int i2 = this.reverseXCount;
            if (i2 > 3 || (i = this.reverseYCount) > 3 || this.reverseShapeCount > 3) {
                judgeScore.countOk();
                Log.v(str, "onJudge : [OK +" + judgeScore.getOkCount() + "]");
                setJudgeState(JudgeState.SATISFACTION);
            } else if (i2 == 3 || i == 3) {
                judgeScore.reset();
                for (int startIndex = gestureScore.getStartIndex(); startIndex < gestureScore.getEndIndex(); startIndex++) {
                    judgeScore.countOk();
                }
                String str2 = TAG;
                Log.v(str2, "onJudge : [OK +" + judgeScore.getOkCount() + "]");
                setJudgeState(JudgeState.SATISFACTION);
            } else {
                setJudgeState(JudgeState.INTERESTED);
            }
        }
        String str3 = TAG;
        Log.v(str3, "onJudge : " + judgeScore.toString());
        Log.v(str3, "onJudge : JudgeState = " + getJudgeState());
        Log.v(str3, "onJudge : ----------------------------------------------------");
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void makeNormalizedVertexPath(VertexPath vertexPath, VertexPath vertexPath2) {
        this.mNormalizedPath = VertexPathNormalizer.getAccuratelyNormalizedPath(scalingVertexPath(vertexPath), getErrorFactorThreshold());
    }

    @Override // com.samsung.remotespen.core.device.control.detector.gesture.canvas.CanvasEventMonitor
    public void updateTravelDistance(GestureScore gestureScore, Event event) {
        gestureScore.setTravelDistance(getRawEventHistoryManager().getTravelLength(0, getRawEventHistoryManager().getEventCount() - 1));
    }

    private VertexPath scalingVertexPath(VertexPath vertexPath) {
        VertexPath vertexPath2 = new VertexPath();
        RectF eventRange = getRawEventHistoryManager().getEventRange();
        String str = TAG;
        Log.v(str, "scalingVertexPath : range width x height = " + eventRange.width() + " x " + eventRange.height());
        float min = Math.min(eventRange.width(), eventRange.height());
        float f = min < 1.5f ? 1.5f / min : 1.5f;
        for (int i = 0; i < vertexPath.getVertexCount(); i++) {
            VertexPath.Vertex relativeVertex = vertexPath.getRelativeVertex(i);
            vertexPath2.appendRelativeVertex(relativeVertex.x * f, relativeVertex.y * f);
        }
        return vertexPath2;
    }

    private void updateReverseCount(Event event) {
        Event event2 = this.mOldEvent;
        if (event2 == null) {
            return;
        }
        if (event2.deltaX * event.deltaX < 0.0f) {
            this.reverseXCount++;
            Log.v(TAG, "isReverse : reverseXCount = " + this.reverseXCount);
        }
        if (this.mOldEvent.deltaY * event.deltaY < 0.0f) {
            this.reverseYCount++;
            Log.v(TAG, "isReverse : reverseYCount = " + this.reverseYCount);
        }
    }

    private void updateReverseShapeCount(int i) {
        int i2 = this.mPrevShape;
        if (i2 == 0) {
            this.reverseShapeCount++;
        } else if (i != i2) {
            this.reverseShapeCount++;
            Log.v(TAG, "updateReverseShapeCount : reverseShapeCount = " + this.reverseShapeCount);
        }
    }
}
