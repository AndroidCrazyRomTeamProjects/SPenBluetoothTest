package com.samsung.remotespen.main.invocation;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.data.BleSpenGestureHoverState;
import com.samsung.remotespen.core.device.data.BleSpenGestureImpurity;
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.ui.view.GuidePanelController;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: GuidePanelTrigger.java */
/* loaded from: classes.dex */
public class NotRecognizedGestureGuidePanelTrigger {
    private static final String TAG = "NotRecognizedGestureGuidePanelTrigger";
    private static final int THRESHOLD = 3;
    private Context mContext;
    private int mGestureCount = 0;
    private Listener mListener;

    /* compiled from: GuidePanelTrigger.java */
    /* loaded from: classes.dex */
    public interface Listener {
        boolean showGestureFailAnimTextGuide(GuidePanelController.BadGestureReason badGestureReason);

        boolean showGestureFailTextGuide(GuidePanelController.BadGestureReason badGestureReason);

        boolean showGestureFailWithGotoHelpGuide();
    }

    public NotRecognizedGestureGuidePanelTrigger(Context context, Listener listener) {
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
    }

    public boolean increaseNotRecognizedGestureCount(int i, BleSpenGestureQuality bleSpenGestureQuality) {
        boolean showGestureFailAnimTextGuide;
        int i2;
        String str = TAG;
        Log.d(str, "increaseNotRecognizedGestureCount : curCount=" + this.mGestureCount + ", cntToIncrease=" + i);
        GuidePanelController.BadGestureReason determineBadGestureReason = determineBadGestureReason(bleSpenGestureQuality);
        if (this.mListener == null) {
            return false;
        }
        if (bleSpenGestureQuality.getGestureImpurity() == BleSpenGestureImpurity.GOOD) {
            return true;
        }
        GuidePanelController.BadGestureReason badGestureReason = GuidePanelController.BadGestureReason.NONE;
        if (determineBadGestureReason == badGestureReason && this.mGestureCount == 2) {
            showGestureFailAnimTextGuide = this.mListener.showGestureFailWithGotoHelpGuide();
        } else if (determineBadGestureReason == GuidePanelController.BadGestureReason.HIGH_IMPURITY) {
            showGestureFailAnimTextGuide = this.mListener.showGestureFailTextGuide(determineBadGestureReason);
        } else {
            showGestureFailAnimTextGuide = (determineBadGestureReason == GuidePanelController.BadGestureReason.HOVER_STATE || determineBadGestureReason == badGestureReason) ? this.mListener.showGestureFailAnimTextGuide(determineBadGestureReason) : true;
        }
        if (showGestureFailAnimTextGuide && (i2 = this.mGestureCount) < 3 && determineBadGestureReason == badGestureReason) {
            this.mGestureCount = i2 + i;
        }
        return true;
    }

    public void updateState() {
        Log.v(TAG, "updateState");
    }

    public void resetCounting() {
        Log.v(TAG, "resetCounting");
        this.mGestureCount = 0;
    }

    private GuidePanelController.BadGestureReason determineBadGestureReason(BleSpenGestureQuality bleSpenGestureQuality) {
        if (bleSpenGestureQuality.getGestureHoverState() == BleSpenGestureHoverState.HOVERED) {
            return GuidePanelController.BadGestureReason.HOVER_STATE;
        }
        if (bleSpenGestureQuality.getGestureImpurity() == BleSpenGestureImpurity.HIGH_IMPURITY) {
            return GuidePanelController.BadGestureReason.HIGH_IMPURITY;
        }
        return GuidePanelController.BadGestureReason.NONE;
    }
}
