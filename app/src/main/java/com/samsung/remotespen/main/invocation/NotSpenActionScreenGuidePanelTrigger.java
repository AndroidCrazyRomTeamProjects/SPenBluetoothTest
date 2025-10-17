package com.samsung.remotespen.main.invocation;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.samsung.util.ActivitySwitchDetector;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: GuidePanelTrigger.java */
/* loaded from: classes.dex */
public class NotSpenActionScreenGuidePanelTrigger {
    private static final String TAG = "NotSpenActionScreenGuidePanelTrigger";
    private static final int THRESHOLD = 4;
    private int mClickCount;
    private Context mContext;
    private Runnable mListener;
    private ComponentName mTopmostActivity;

    public NotSpenActionScreenGuidePanelTrigger(Context context, Runnable runnable) {
        this.mContext = context.getApplicationContext();
        this.mListener = runnable;
    }

    public boolean increaseClickCount(int i) {
        String str = TAG;
        Log.d(str, "increaseClickCount : curCount=" + this.mClickCount + ", cntToIncrease=" + i);
        ComponentName topMostActivity = ActivitySwitchDetector.getInstance(this.mContext).getTopMostActivity();
        int i2 = this.mClickCount;
        ComponentName componentName = this.mTopmostActivity;
        if (componentName == null) {
            this.mTopmostActivity = topMostActivity;
        } else if (!componentName.equals(topMostActivity)) {
            this.mTopmostActivity = topMostActivity;
            this.mClickCount = 0;
        }
        int i3 = this.mClickCount + i;
        this.mClickCount = i3;
        if (i2 >= 4 || i3 < 4) {
            return false;
        }
        Runnable runnable = this.mListener;
        if (runnable != null) {
            runnable.run();
            return true;
        }
        return true;
    }

    public void updateState() {
        Log.v(TAG, "updateState");
        ComponentName topMostActivity = ActivitySwitchDetector.getInstance(this.mContext).getTopMostActivity();
        if (topMostActivity == null || topMostActivity.equals(this.mTopmostActivity)) {
            return;
        }
        this.mTopmostActivity = topMostActivity;
        this.mClickCount = 0;
    }

    public void resetCounting() {
        Log.v(TAG, "resetCounting");
        this.mTopmostActivity = null;
        this.mClickCount = 0;
    }
}
