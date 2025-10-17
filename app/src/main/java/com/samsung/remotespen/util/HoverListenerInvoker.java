package com.samsung.remotespen.util;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.samsung.remotespen.util.SpenInputDetector;
import com.samsung.util.debug.Log;
import java.util.ArrayList;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SpenInputDetector.java */
/* loaded from: classes.dex */
public class HoverListenerInvoker {
    private static final String TAG = "HoverListenerInvoker";
    private HoverSession mCurHoverSession;
    private ArrayList<SpenInputDetector.Listener> mHoverEventListeners = new ArrayList<>();
    private Handler mHoverHandler = new Handler() { // from class: com.samsung.remotespen.util.HoverListenerInvoker.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            synchronized (HoverListenerInvoker.this) {
                for (int size = HoverListenerInvoker.this.mHoverEventListeners.size() - 1; size >= 0; size--) {
                    ((SpenInputDetector.Listener) HoverListenerInvoker.this.mHoverEventListeners.get(size)).onHoverEvent(message.what, message.arg1, message.arg2);
                }
            }
            super.handleMessage(message);
        }
    };

    /* compiled from: SpenInputDetector.java */
    /* loaded from: classes.dex */
    public static class HoverSession {
        public long mHoverDuration;
        public long mHoverEnterTime;
        public long mHoverExitTime;
        public Runnable mHoverExitTimeoutHandler;
        public long mLastHoverMoveTime;

        private HoverSession() {
        }

        public void setHoverExitTimerHandler(Runnable runnable) {
            this.mHoverExitTimeoutHandler = runnable;
        }
    }

    public synchronized void registerHoverListener(SpenInputDetector.Listener listener) {
        if (listener != null) {
            this.mHoverEventListeners.add(listener);
        }
    }

    public synchronized boolean isHoverSessionActivated() {
        boolean z;
        HoverSession hoverSession = this.mCurHoverSession;
        if (hoverSession != null) {
            z = hoverSession.mHoverExitTime == 0;
        }
        return z;
    }

    public synchronized void unregisterHoverListener(SpenInputDetector.Listener listener) {
        if (listener != null) {
            this.mHoverEventListeners.remove(listener);
        }
    }

    public synchronized void notifyHoverEnter(int i, int i2, int i3) {
        boolean z = true;
        if (!isHoverSessionActivated()) {
            final HoverSession hoverSession = new HoverSession();
            hoverSession.mHoverEnterTime = SystemClock.elapsedRealtime();
            hoverSession.setHoverExitTimerHandler(new Runnable() { // from class: com.samsung.remotespen.util.HoverListenerInvoker.2
                @Override // java.lang.Runnable
                public void run() {
                    if (hoverSession != HoverListenerInvoker.this.mCurHoverSession) {
                        Log.e(HoverListenerInvoker.TAG, "hoverExitTimer : my session is not current session");
                    } else if (hoverSession.mHoverExitTime > 0) {
                        Log.d(HoverListenerInvoker.TAG, "hoverExitTimer : already closed session");
                    } else {
                        long elapsedRealtime = SystemClock.elapsedRealtime();
                        HoverSession hoverSession2 = hoverSession;
                        long max = elapsedRealtime - Math.max(hoverSession2.mHoverEnterTime, hoverSession2.mLastHoverMoveTime);
                        long j = hoverSession.mHoverDuration;
                        if (j <= 0 || max < j) {
                            HoverListenerInvoker.this.mHoverHandler.postDelayed(hoverSession.mHoverExitTimeoutHandler, j - max);
                            return;
                        }
                        Log.d(HoverListenerInvoker.TAG, "hoverExitTimer : timeout");
                        HoverListenerInvoker.this.notifyHoverExit(hoverSession);
                    }
                }
            });
            this.mCurHoverSession = hoverSession;
        } else {
            Log.d(TAG, "notifyHoverEnter : already hovered state");
            z = false;
        }
        HoverSession hoverSession2 = this.mCurHoverSession;
        hoverSession2.mHoverDuration = i3;
        Runnable runnable = hoverSession2.mHoverExitTimeoutHandler;
        if (runnable != null) {
            this.mHoverHandler.removeCallbacks(runnable);
        }
        if (z) {
            sendHoverMessage(9, i, i2, this.mCurHoverSession);
        }
        HoverSession hoverSession3 = this.mCurHoverSession;
        long j = hoverSession3.mHoverDuration;
        if (j > 0) {
            this.mHoverHandler.postDelayed(hoverSession3.mHoverExitTimeoutHandler, j);
        }
    }

    public synchronized void notifyHoverMove(int i, int i2) {
        if (!isHoverSessionActivated()) {
            Log.e(TAG, "notifyHoverMove : active session not available");
            return;
        }
        this.mCurHoverSession.mLastHoverMoveTime = SystemClock.elapsedRealtime();
        sendHoverMessage(7, i, i2, this.mCurHoverSession);
    }

    public synchronized void notifyHoverExit() {
        notifyHoverExit(this.mCurHoverSession);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyHoverExit(HoverSession hoverSession) {
        if (hoverSession == null) {
            Log.e(TAG, "notifyHoverExit : session is null");
        } else if (hoverSession.mHoverExitTime > 0) {
            Log.e(TAG, "notifyHoverExit : session is already closed");
        } else {
            hoverSession.mHoverExitTime = SystemClock.elapsedRealtime();
            Runnable runnable = hoverSession.mHoverExitTimeoutHandler;
            if (runnable != null) {
                this.mHoverHandler.removeCallbacks(runnable);
            }
            sendHoverMessage(10, 0, 0, hoverSession);
        }
    }

    private void sendHoverMessage(int i, int i2, int i3, HoverSession hoverSession) {
        Message message = new Message();
        message.what = i;
        message.arg1 = i2;
        message.arg2 = i3;
        message.obj = hoverSession;
        this.mHoverHandler.sendMessage(message);
    }
}
