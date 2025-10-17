package com.samsung.remotespen.util.graph;

import android.os.SystemClock;
import com.samsung.remotespen.util.graph.GraphUtils;
import com.samsung.remotespen.util.graph.VertexPath;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public class VertexPathNormalizer {
    private static final String TAG = "VertexPathNormalizer";

    private static int getKey(int i, int i2) {
        return (i << 16) | i2;
    }

    /* loaded from: classes.dex */
    public static class PathInfo {
        public float mErrorAreaSum;
        public float mLengthSum;
        public ArrayList<Integer> mVertexIndexTable = new ArrayList<>();

        public void append(PathInfo pathInfo) {
            this.mErrorAreaSum += pathInfo.mErrorAreaSum;
            this.mLengthSum += pathInfo.mLengthSum;
            this.mVertexIndexTable.addAll(pathInfo.mVertexIndexTable);
        }

        public int getVertexCount() {
            return this.mVertexIndexTable.size();
        }
    }

    /* loaded from: classes.dex */
    public static class LineInfo {
        public final int mEndIndex;
        public final float mErrorAreaSum;
        public final float mLength;
        public final int mStartIndex;

        public LineInfo(int i, int i2, float f, float f2) {
            this.mStartIndex = i;
            this.mEndIndex = i2;
            this.mLength = f;
            this.mErrorAreaSum = f2;
        }
    }

    public static VertexPath getAccuratelyNormalizedPath(VertexPath vertexPath, float f) {
        if (vertexPath.getVertexCount() == 0) {
            Log.v(TAG, "getAccuratelyNormalizedPath : empty vertex path");
            return new VertexPath();
        }
        long elapsedRealtime = SystemClock.elapsedRealtime();
        PathInfo pathInfo = new PathInfo();
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        pathInfo.mVertexIndexTable.add(0, 0);
        estimateAccurateMinVector(vertexPath, 0, vertexPath.getVertexCount() - 1, f, pathInfo, hashMap, hashMap2);
        String str = TAG;
        Log.v(str, "getAccuratelyNormalizedPath : vertex : " + vertexPath.getVertexCount() + " -> " + pathInfo.mVertexIndexTable.size() + ", elapsed=" + (SystemClock.elapsedRealtime() - elapsedRealtime) + ", errorAreaSum=" + pathInfo.mErrorAreaSum + ", length=" + pathInfo.mLengthSum + ", cache1 size=" + hashMap.size() + ", cache2 size=" + hashMap2.size());
        Iterator<Integer> it = pathInfo.mVertexIndexTable.iterator();
        while (it.hasNext()) {
            int intValue = it.next().intValue();
            String str2 = TAG;
            Log.v(str2, "getAccuratelyNormalizedPath : [" + intValue + "]");
        }
        VertexPath vertexPath2 = new VertexPath();
        Iterator<Integer> it2 = pathInfo.mVertexIndexTable.iterator();
        while (it2.hasNext()) {
            vertexPath2.appendAbsVertex(vertexPath.getAbsVertex(it2.next().intValue()));
        }
        return vertexPath2;
    }

    public static float getMaxDistanceBetweenVertex(VertexPath vertexPath) {
        int vertexCount = vertexPath.getVertexCount();
        float f = 0.0f;
        if (vertexCount == 0) {
            return 0.0f;
        }
        int i = 0;
        if (vertexCount == 1) {
            return vertexPath.getLengthFromPrevVertex(0);
        }
        long elapsedRealtime = SystemClock.elapsedRealtime();
        int i2 = -1;
        int i3 = -1;
        while (i < vertexCount - 1) {
            VertexPath.Vertex absVertex = vertexPath.getAbsVertex(i);
            int i4 = i + 1;
            for (int i5 = i4; i5 < vertexCount; i5++) {
                VertexPath.Vertex absVertex2 = vertexPath.getAbsVertex(i5);
                float distance = GraphUtils.getDistance(absVertex.x, absVertex.y, absVertex2.x, absVertex2.y);
                if (distance > f) {
                    i2 = i;
                    i3 = i5;
                    f = distance;
                }
            }
            i = i4;
        }
        Log.v(TAG, "getMaxDistanceBetweenVertex : vertexCnt=" + vertexCount + " distance=" + f + " index=" + i2 + "~" + i3 + " elapsed=" + (SystemClock.elapsedRealtime() - elapsedRealtime));
        return f;
    }

    private static void estimateAccurateMinVector(VertexPath vertexPath, int i, int i2, float f, PathInfo pathInfo, HashMap<Integer, PathInfo> hashMap, HashMap<Integer, ArrayList<LineInfo>> hashMap2) {
        if (i >= i2 || i >= vertexPath.getVertexCount() - 1) {
            return;
        }
        Iterator<LineInfo> it = getCandidateLineVertexIndexes(vertexPath, i, i2, f, hashMap2).iterator();
        PathInfo pathInfo2 = null;
        while (it.hasNext()) {
            LineInfo next = it.next();
            int i3 = next.mEndIndex;
            PathInfo pathInfo3 = new PathInfo();
            pathInfo3.mVertexIndexTable.add(Integer.valueOf(i3));
            pathInfo3.mLengthSum += next.mLength;
            pathInfo3.mErrorAreaSum += next.mErrorAreaSum;
            PathInfo pathInfo4 = hashMap.get(Integer.valueOf(getKey(i3, i2)));
            if (pathInfo4 != null) {
                pathInfo3.append(pathInfo4);
            } else {
                estimateAccurateMinVector(vertexPath, i3, i2, f, pathInfo3, hashMap, hashMap2);
            }
            if (pathInfo2 == null || pathInfo3.getVertexCount() < pathInfo2.getVertexCount() || (pathInfo3.getVertexCount() == pathInfo2.getVertexCount() && pathInfo3.mErrorAreaSum < pathInfo2.mErrorAreaSum)) {
                pathInfo2 = pathInfo3;
            }
        }
        if (pathInfo2 != null) {
            hashMap.put(Integer.valueOf(getKey(i, i2)), pathInfo2);
            pathInfo.append(pathInfo2);
        }
    }

    private static ArrayList<LineInfo> getCandidateLineVertexIndexes(VertexPath vertexPath, int i, int i2, float f, HashMap<Integer, ArrayList<LineInfo>> hashMap) {
        boolean z;
        Assert.e(i < i2);
        int key = getKey(i, i2);
        ArrayList<LineInfo> arrayList = hashMap.get(Integer.valueOf(key));
        if (arrayList != null) {
            return arrayList;
        }
        ArrayList<LineInfo> arrayList2 = new ArrayList<>();
        VertexPath.Vertex absVertex = vertexPath.getAbsVertex(i);
        int i3 = i + 1;
        for (int i4 = i3; i4 <= i2; i4++) {
            VertexPath.Vertex absVertex2 = vertexPath.getAbsVertex(i4);
            GraphUtils.Line createLine = GraphUtils.Line.createLine(absVertex.x, absVertex.y, absVertex2.x, absVertex2.y);
            if (createLine != null) {
                float f2 = 0.0f;
                float distance = GraphUtils.getDistance(absVertex.x, absVertex.y, absVertex2.x, absVertex2.y);
                float f3 = distance * f;
                int i5 = i3;
                while (true) {
                    if (i5 >= i4) {
                        z = true;
                        break;
                    }
                    VertexPath.Vertex absVertex3 = vertexPath.getAbsVertex(i5);
                    f2 += GraphUtils.getMinDistance(createLine, absVertex3.x, absVertex3.y) * vertexPath.getLengthFromPrevVertex(i5);
                    if (f2 > f3) {
                        z = false;
                        break;
                    }
                    i5++;
                }
                if (z && f2 / distance <= f) {
                    arrayList2.add(new LineInfo(i, i4, distance, f2));
                }
            }
        }
        hashMap.put(Integer.valueOf(key), arrayList2);
        return arrayList2;
    }
}
