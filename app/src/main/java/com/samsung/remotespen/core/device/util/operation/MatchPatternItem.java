package com.samsung.remotespen.core.device.util.operation;

/* compiled from: BleSpenAttachedPenAddrFinder.java */
/* loaded from: classes.dex */
class MatchPatternItem {
    public int mMatchedCount = 0;
    public int mUnmatchedCount = 0;
    public StringBuilder mBuilder = new StringBuilder();

    public String getMatchPattern() {
        return this.mBuilder.toString();
    }
}
