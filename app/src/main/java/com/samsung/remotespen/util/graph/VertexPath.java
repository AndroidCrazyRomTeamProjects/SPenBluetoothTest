package com.samsung.remotespen.util.graph;

import com.samsung.remotespen.util.graph.GraphUtils;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class VertexPath {
    private static final String TAG = "VertexPath";
    private Vertex mLastAbsVertex;
    private ArrayList<LineSegment> mLineArray;
    private Float mTotalLength;

    /* loaded from: classes.dex */
    public static class Vertex {
        public final float x;
        public final float y;

        private Vertex(float f, float f2) {
            this.x = f;
            this.y = f2;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Vertex)) {
                return false;
            }
            Vertex vertex = (Vertex) obj;
            return this.x == vertex.x && this.y == vertex.y;
        }
    }

    /* loaded from: classes.dex */
    public static class LineSegment {
        public final Vertex mFrom;
        private final float mLength;
        private final GraphUtils.Line mLine;
        public final Vertex mTo;

        public LineSegment(Vertex vertex, Vertex vertex2) {
            this.mFrom = vertex;
            this.mTo = vertex2;
            this.mLength = GraphUtils.getDistance(vertex.x, vertex.y, vertex2.x, vertex2.y);
            this.mLine = GraphUtils.Line.createLine(vertex.x, vertex.y, vertex2.x, vertex2.y);
        }
    }

    public VertexPath() {
        init();
    }

    public VertexPath(VertexPath vertexPath) {
        init();
        this.mLineArray.addAll(vertexPath.mLineArray);
        this.mLastAbsVertex = vertexPath.mLastAbsVertex;
        this.mTotalLength = vertexPath.mTotalLength;
    }

    public void appendRelativeVertex(float f, float f2) {
        Vertex vertex;
        Vertex vertex2 = this.mLastAbsVertex;
        if (vertex2 == null) {
            vertex = new Vertex(f, f2);
        } else {
            vertex = new Vertex(vertex2.x + f, vertex2.y + f2);
        }
        appendAbsVertex(vertex);
    }

    public void appendAbsVertex(Vertex vertex) {
        Vertex vertex2 = this.mLastAbsVertex;
        if (vertex2 == null) {
            vertex2 = vertex;
        }
        LineSegment lineSegment = new LineSegment(vertex2, vertex);
        this.mLineArray.add(lineSegment);
        this.mLastAbsVertex = vertex;
        Float f = this.mTotalLength;
        if (f != null) {
            this.mTotalLength = Float.valueOf(f.floatValue() + lineSegment.mLength);
        }
    }

    public void clear() {
        init();
    }

    public Vertex getAbsVertex(int i) {
        return this.mLineArray.get(i).mTo;
    }

    public Vertex getRelativeVertex(int i) {
        LineSegment lineSegment = this.mLineArray.get(i);
        Vertex vertex = lineSegment.mTo;
        float f = vertex.x;
        Vertex vertex2 = lineSegment.mFrom;
        return new Vertex(f - vertex2.x, vertex.y - vertex2.y);
    }

    public float getLengthFromPrevVertex(int i) {
        return this.mLineArray.get(i).mLength;
    }

    public float getTotalLength() {
        if (this.mTotalLength == null) {
            this.mTotalLength = Float.valueOf(getLength(0, getVertexCount() - 1));
        }
        return this.mTotalLength.floatValue();
    }

    public float getLength(int i, int i2) {
        long j = 0;
        for (int i3 = i + 1; i3 <= i2; i3++) {
            j = ((float) j) + this.mLineArray.get(i3).mLength;
        }
        return (float) j;
    }

    public int getVertexCount() {
        return this.mLineArray.size();
    }

    public int getVertexAngle(int i) {
        int size = this.mLineArray.size();
        if (i == 0 || i >= size - 1) {
            return 0;
        }
        Vertex relativeVertex = getRelativeVertex(i);
        Vertex relativeVertex2 = getRelativeVertex(i + 1);
        int angle3p = GraphUtils.getAngle3p(-relativeVertex.x, -relativeVertex.y, relativeVertex2.x, relativeVertex2.y);
        if (angle3p > 180) {
            angle3p -= 180;
        }
        return angle3p < 0 ? angle3p + 180 : angle3p;
    }

    public int getIndex(Vertex vertex) {
        for (int i = 0; i < this.mLineArray.size(); i++) {
            Vertex vertex2 = this.mLineArray.get(i).mTo;
            if (vertex2.x == vertex.x && vertex2.y == vertex.y) {
                return i;
            }
        }
        return 0;
    }

    private void init() {
        this.mLineArray = new ArrayList<>();
        this.mLastAbsVertex = null;
        this.mTotalLength = Float.valueOf(0.0f);
    }
}
