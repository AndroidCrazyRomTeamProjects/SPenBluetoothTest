package com.samsung.remotespen.core.device.control.detector;

import com.samsung.aboutpage.Constants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: GestureHistoryManager.java */
/* loaded from: classes.dex */
public class GestureHistoryInfo {
    private static final int MAX_VERTEX_ARRAY_SIZE = 200;
    private int mActualVertexCount;
    private String mBleSpenGestureType;
    private long mElapsedTime;
    private String mEndDate;
    private ArrayList<Vertex> mRawVertexArray;
    private String mStartDate;
    private long mStartTime;
    private String mState;
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");

    /* compiled from: GestureHistoryManager.java */
    /* loaded from: classes.dex */
    public static class Vertex {
        public int deltaX;
        public int deltaY;

        public Vertex(int i, int i2) {
            this.deltaX = i;
            this.deltaY = i2;
        }

        public String toString() {
            return Constants.packageName.NONE + this.deltaX + ", " + this.deltaY;
        }
    }

    public GestureHistoryInfo() {
        ArrayList<Vertex> arrayList = new ArrayList<>();
        this.mRawVertexArray = arrayList;
        arrayList.ensureCapacity(MAX_VERTEX_ARRAY_SIZE);
    }

    public synchronized void addRawVertex(Vertex vertex) {
        if (this.mRawVertexArray.size() < MAX_VERTEX_ARRAY_SIZE) {
            this.mRawVertexArray.add(vertex);
        }
        this.mActualVertexCount++;
    }

    public synchronized void setStartDate() {
        this.mStartTime = System.currentTimeMillis();
        this.mStartDate = this.mTimeFormat.format(new Date(this.mStartTime));
    }

    public synchronized void setEndDate() {
        long currentTimeMillis = System.currentTimeMillis();
        this.mElapsedTime = currentTimeMillis - this.mStartTime;
        this.mEndDate = this.mTimeFormat.format(new Date(currentTimeMillis));
    }

    public synchronized void setGestureType(String str) {
        this.mBleSpenGestureType = str;
    }

    public synchronized void setState(String str) {
        this.mState = str;
    }

    public synchronized void init() {
        this.mRawVertexArray.clear();
        this.mActualVertexCount = 0;
        this.mBleSpenGestureType = null;
        this.mStartDate = null;
        this.mEndDate = null;
        this.mState = null;
    }

    public synchronized String toString() {
        StringBuilder sb;
        sb = new StringBuilder();
        int size = this.mRawVertexArray.size();
        int i = this.mActualVertexCount;
        String format = size == i ? String.format("%3d", Integer.valueOf(i)) : String.format("%d / %d", Integer.valueOf(this.mRawVertexArray.size()), Integer.valueOf(this.mActualVertexCount));
        sb.append(getFormattedNameAndValueString(Constants.packageName.NONE, String.format("%-18s", this.mStartDate) + " ~ " + String.format("%-18s", this.mEndDate), 39));
        sb.append(" ");
        sb.append(getFormattedNameAndValueString(Constants.packageName.NONE, "[" + String.format("%4d", Long.valueOf(this.mElapsedTime)) + "]", 6));
        sb.append(" ");
        sb.append(getFormattedNameAndValueString(Constants.packageName.NONE, this.mBleSpenGestureType, 19));
        sb.append(" ");
        sb.append(getFormattedNameAndValueString(Constants.packageName.NONE, this.mState, 13));
        sb.append(" ");
        sb.append(getFormattedNameAndValueString(Constants.packageName.NONE, "[" + format + "]", 5));
        sb.append(" ::");
        Iterator<Vertex> it = this.mRawVertexArray.iterator();
        while (it.hasNext()) {
            Vertex next = it.next();
            sb.append("{" + next.deltaX + ", " + next.deltaY + "}");
        }
        sb.append(";");
        return sb.toString();
    }

    private String getFormattedNameAndValueString(String str, Object obj, int i) {
        String obj2 = obj != null ? obj.toString() : "null";
        return String.format("%s%-" + i + "s", str, obj2);
    }
}
