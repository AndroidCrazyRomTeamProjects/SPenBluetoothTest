package com.samsung.remotespen.util;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.util.SpenInputDetector;
import com.samsung.util.OsVersion;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.SafeHandlerThread;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class SpenInputDetector {
    private static int KEYCODE_DOUBLE_TAP = 1078;
    private static final int MSG_PEN_BUTTON_PRESS_STATE_CHANGED_ON_HOVER = 1000;
    private static final String TAG = "SpenInputDetector";
    private static SpenInputDetector sInstance;
    private Context mContext;
    private SafeHandlerThread mEventReceivingThread;
    private InputEventReceiver mInputEventReceiver;
    private boolean mPenButtonPressedOnHover = false;
    private Object mSyncObj = new Object();
    private ArrayList<Listener> mInputEventListeners = new ArrayList<>();
    private Handler mHandler = new Handler() { // from class: com.samsung.remotespen.util.SpenInputDetector.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 1000) {
                boolean z = message.arg1 == 1;
                if (SpenInputDetector.this.mPenButtonPressedOnHover != z) {
                    SpenInputDetector.this.mPenButtonPressedOnHover = z;
                    Log.d(SpenInputDetector.TAG, "mPenButtonPressedOnHoverHandler : " + SpenInputDetector.this.mPenButtonPressedOnHover);
                    synchronized (SpenInputDetector.this.mSyncObj) {
                        for (int size = SpenInputDetector.this.mInputEventListeners.size() - 1; size >= 0; size--) {
                            ((Listener) SpenInputDetector.this.mInputEventListeners.get(size)).onPenButtonPressStateChangedOnHover(SpenInputDetector.this.mPenButtonPressedOnHover);
                        }
                    }
                }
            }
        }
    };
    private HoverListenerInvoker mHoverListenerInvoker = new HoverListenerInvoker();

    /* loaded from: classes.dex */
    public interface Listener {
        void onHoverEvent(int i, int i2, int i3);

        void onPenButtonPressStateChangedOnHover(boolean z);

        void onSpenMotionEvent(MotionEvent motionEvent);

        void onTouchEvent(int i);
    }

    static {
        try {
            KEYCODE_DOUBLE_TAP = ((Integer) ReflectionUtils.getStaticObjectField(KeyEvent.class, "KEYCODE_DOUBLE_TAP")).intValue();
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "static : e=" + e);
        }
    }

    public static synchronized SpenInputDetector getInstance(Context context) {
        SpenInputDetector spenInputDetector;
        synchronized (SpenInputDetector.class) {
            if (sInstance == null) {
                sInstance = new SpenInputDetector(context);
            }
            spenInputDetector = sInstance;
        }
        return spenInputDetector;
    }

    private SpenInputDetector(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerListener(Listener listener) {
        if (listener != null) {
            synchronized (this.mSyncObj) {
                String str = TAG;
                Log.d(str, "registerListener: listener " + listener);
                this.mInputEventListeners.add(listener);
                this.mHoverListenerInvoker.registerHoverListener(listener);
            }
        }
        if (this.mInputEventReceiver == null) {
            String str2 = TAG;
            Log.d(str2, "registerListener : start HoverInputEventReceiver");
            InputManager inputManager = (InputManager) this.mContext.getSystemService("input");
            Object obj = null;
            try {
                obj = ReflectionUtils.invokeMethod(inputManager, "monitorInput", str2, 0, Integer.valueOf(((Integer) ReflectionUtils.getStaticObjectField(InputManager.class, "MONITOR_FILTER_SPEN")).intValue()));
            } catch (Exception e) {
                if (OsVersion.isSupportOneUi4_1()) {
                    String str3 = TAG;
                    Log.e(str3, "startMonitor : failed to register filtered input monitor. e=" + e + ", " + e.getCause(), e);
                }
                try {
                    String str4 = TAG;
                    Log.d(str4, "startMonitor : uses unfiltered input monitor");
                    obj = ReflectionUtils.invokeMethod(inputManager, "monitorInput", str4, 0);
                } catch (Exception e2) {
                    String str5 = TAG;
                    Log.e(str5, "startMonitor : e=" + e2 + ", " + e2.getCause(), e2);
                }
            }
            if (obj != null) {
                SafeHandlerThread safeHandlerThread = this.mEventReceivingThread;
                if (safeHandlerThread == null || !safeHandlerThread.isAlive()) {
                    SafeHandlerThread safeHandlerThread2 = new SafeHandlerThread(TAG);
                    this.mEventReceivingThread = safeHandlerThread2;
                    safeHandlerThread2.start();
                }
                Looper looper = this.mEventReceivingThread.getLooper();
                Assert.notNull(looper);
                this.mInputEventReceiver = new InputEventReceiver((InputChannel) obj, looper);
            }
        }
    }

    public void unregisterListener(Listener listener) {
        synchronized (this.mSyncObj) {
            if (listener != null) {
                String str = TAG;
                Log.d(str, "unregisterListener : listener" + listener);
                this.mInputEventListeners.remove(listener);
                this.mHoverListenerInvoker.unregisterHoverListener(listener);
            }
            if (this.mInputEventListeners.isEmpty()) {
                Log.d(TAG, "unregisterListener : stop input detecting");
                InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
                if (inputEventReceiver != null) {
                    inputEventReceiver.dispose();
                    this.mInputEventReceiver = null;
                }
                SafeHandlerThread safeHandlerThread = this.mEventReceivingThread;
                if (safeHandlerThread != null) {
                    safeHandlerThread.quit();
                    this.mEventReceivingThread = null;
                }
            }
        }
    }

    /* loaded from: classes.dex */
    public class InputEventReceiver extends android.view.InputEventReceiver {
        public InputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent, int i) {
            onInputEvent(inputEvent);
        }

        public void onInputEvent(InputEvent inputEvent) {
            long elapsedRealtime;
            long elapsedRealtime2;
            int i;
            long elapsedRealtime3 = SystemClock.elapsedRealtime();
            try {
                boolean z = true;
                if (inputEvent instanceof MotionEvent) {
                    MotionEvent motionEvent = (MotionEvent) inputEvent;
                    int toolType = motionEvent.getToolType(0);
                    if (toolType == 2 || toolType == 4) {
                        notifySpenMotionEvent(motionEvent);
                    }
                    int action = motionEvent.getAction();
                    if (toolType != 2) {
                        if (action == 9) {
                            Log.d(SpenInputDetector.TAG, "onInputEvent : Not Spen hover event. tooltype=" + toolType);
                        }
                        if (i > 0) {
                            return;
                        }
                        return;
                    }
                    processPenButtonPressedOnHover(motionEvent);
                    if (action == 0 || action == 1 || action == 3) {
                        inputEvent.getDevice().getName();
                        notifyTouchEvent(action);
                    } else if (action == 7) {
                        if (!SpenInputDetector.this.mHoverListenerInvoker.isHoverSessionActivated()) {
                            Log.i(SpenInputDetector.TAG, "Hover Enter is missed. force to send hoverEnter.");
                            SpenInputDetector.this.mHoverListenerInvoker.notifyHoverEnter((int) motionEvent.getX(), (int) motionEvent.getY(), Action.Repeat.REPEATABLE_INTERVAL_MEDIUM_VALUE);
                        }
                        SpenInputDetector.this.mHoverListenerInvoker.notifyHoverMove((int) motionEvent.getX(), (int) motionEvent.getY());
                    } else if (action == 9) {
                        SpenInputDetector.this.mHoverListenerInvoker.notifyHoverEnter((int) motionEvent.getX(), (int) motionEvent.getY(), Action.Repeat.REPEATABLE_INTERVAL_MEDIUM_VALUE);
                    } else if (action == 10) {
                        SpenInputDetector.this.mHoverListenerInvoker.notifyHoverExit();
                    }
                } else if (inputEvent instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) inputEvent;
                    if (keyEvent.getKeyCode() == SpenInputDetector.KEYCODE_DOUBLE_TAP) {
                        if (keyEvent.getAction() != 0) {
                            z = false;
                        }
                        Log.d(SpenInputDetector.TAG, "onInputEvent : KEYCODE_DOUBLE_TAP, isPressed : " + z);
                        if (z) {
                            SpenInputDetector.this.mHoverListenerInvoker.notifyHoverEnter(0, 0, 300);
                        }
                    }
                }
                long elapsedRealtime4 = SystemClock.elapsedRealtime();
                finishInputEvent(inputEvent, false);
                long elapsedRealtime5 = SystemClock.elapsedRealtime() - elapsedRealtime3;
                if (elapsedRealtime5 > 100) {
                    Log.e(SpenInputDetector.TAG, "onInputEvent : sum=" + elapsedRealtime5 + ", durBody=" + (elapsedRealtime4 - elapsedRealtime3) + ", durFinishInputEvent=" + (elapsedRealtime2 - elapsedRealtime4));
                }
            } finally {
                long elapsedRealtime6 = SystemClock.elapsedRealtime();
                finishInputEvent(inputEvent, false);
                long elapsedRealtime7 = SystemClock.elapsedRealtime() - elapsedRealtime3;
                if (elapsedRealtime7 > 100) {
                    Log.e(SpenInputDetector.TAG, "onInputEvent : sum=" + elapsedRealtime7 + ", durBody=" + (elapsedRealtime6 - elapsedRealtime3) + ", durFinishInputEvent=" + (elapsedRealtime - elapsedRealtime6));
                }
            }
        }

        private void processPenButtonPressedOnHover(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            boolean z = (motionEvent.getButtonState() & 32) != 0;
            if (action != 7) {
                if (action == 9) {
                    sendPenButtonPressedOnHoverEvent(z, 0L, false);
                } else if (action != 10) {
                } else {
                    sendPenButtonPressedOnHoverEvent(z, 1500L, false);
                }
            } else if (SpenInputDetector.this.mPenButtonPressedOnHover != z) {
                String str = SpenInputDetector.TAG;
                Log.v(str, "processPenButtonPressedOnHover action : ACTION_HOVER_MOVE, isPenButtonPressed : " + z);
                if (z) {
                    sendPenButtonPressedOnHoverEvent(z, 0L, false);
                } else {
                    sendPenButtonPressedOnHoverEvent(z, 10L, true);
                }
            }
        }

        private void sendPenButtonPressedOnHoverEvent(boolean z, long j, boolean z2) {
            if (SpenInputDetector.this.mHandler.hasMessages(1000)) {
                if (z2) {
                    return;
                }
                SpenInputDetector.this.mHandler.removeMessages(1000);
            }
            Message message = new Message();
            message.what = 1000;
            message.arg1 = z ? 1 : 0;
            if (j > 0) {
                SpenInputDetector.this.mHandler.sendMessageDelayed(message, j);
            } else {
                SpenInputDetector.this.mHandler.sendMessage(message);
            }
        }

        private void notifyTouchEvent(final int i) {
            SpenInputDetector.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.util.SpenInputDetector.InputEventReceiver.1
                @Override // java.lang.Runnable
                public void run() {
                    synchronized (SpenInputDetector.this.mSyncObj) {
                        for (int size = SpenInputDetector.this.mInputEventListeners.size() - 1; size >= 0; size--) {
                            ((Listener) SpenInputDetector.this.mInputEventListeners.get(size)).onTouchEvent(i);
                        }
                    }
                }
            });
        }

        private void notifySpenMotionEvent(final MotionEvent motionEvent) {
            SpenInputDetector.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.util.SpenInputDetector$InputEventReceiver$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    SpenInputDetector.InputEventReceiver.this.lambda$notifySpenMotionEvent$0(motionEvent);
                }
            });
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$notifySpenMotionEvent$0(MotionEvent motionEvent) {
            synchronized (SpenInputDetector.this.mSyncObj) {
                MotionEvent obtain = MotionEvent.obtain(motionEvent);
                for (int size = SpenInputDetector.this.mInputEventListeners.size() - 1; size >= 0; size--) {
                    ((Listener) SpenInputDetector.this.mInputEventListeners.get(size)).onSpenMotionEvent(obtain);
                }
                obtain.recycle();
            }
        }
    }
}
