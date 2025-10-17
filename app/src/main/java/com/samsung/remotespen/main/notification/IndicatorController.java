package com.samsung.remotespen.main.notification;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.util.StatusBarManagerHelper;
import com.samsung.util.CommonUtils;

/* loaded from: classes.dex */
public class IndicatorController {
    private static final int ICON_BLINK_INTERVAL = 700;
    private static final String ICON_SLOT_NAME = "remote_spen";
    private static final String TAG = "IndicatorController";
    private final Context mContext;
    private final Handler mHandler = new Handler();
    private Runnable mIconBlinkHandler;

    private int getBatteryLevelResId(int i) {
        return i >= 80 ? R.drawable.stat_sys_spen_normal : i >= 60 ? R.drawable.stat_sys_spen_normal_04 : i >= 40 ? R.drawable.stat_sys_spen_normal_03 : i >= 20 ? R.drawable.stat_sys_spen_normal_02 : R.drawable.stat_sys_spen_normal_01;
    }

    public IndicatorController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public synchronized void showConnectionStateIcon(BleSpenManager.ConnectionState connectionState, int i) {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.d(TAG, "showIcon : Not focused user");
            return;
        }
        Log.d(TAG, "showIcon");
        cancelBlinking();
        showIcon(getIconResId(connectionState, i));
    }

    public synchronized void showChargingIcon() {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.d(TAG, "showChargingIcon : Not focused user");
            return;
        }
        Log.d(TAG, "showChargingIcon");
        cancelBlinking();
        showIcon(R.drawable.pen_air_command_charge_indicator_anim);
    }

    public synchronized void showConnectingIcon() {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.d(TAG, "showConnectingIcon : Not focused user");
            return;
        }
        Log.d(TAG, "showConnectingIcon");
        cancelBlinking();
        Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.main.notification.IndicatorController.1
            private boolean mShouldShow = false;

            @Override // java.lang.Runnable
            public void run() {
                if (!CommonUtils.isMyUserFocused(IndicatorController.this.mContext)) {
                    Log.e(IndicatorController.TAG, "showConnectingIcon : callback : Not focused user");
                    IndicatorController.this.cancelBlinking();
                    return;
                }
                if (this.mShouldShow) {
                    IndicatorController.this.showIcon(R.drawable.stat_sys_spen_connecting);
                } else {
                    IndicatorController.this.showIcon(R.drawable.stat_sys_spen_disconnected);
                }
                this.mShouldShow = !this.mShouldShow;
                IndicatorController.this.mHandler.postDelayed(this, 700L);
            }
        };
        this.mIconBlinkHandler = runnable;
        this.mHandler.post(runnable);
    }

    public synchronized void hideIcon() {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.d(TAG, "hideIcon : Not focused user");
            return;
        }
        Log.d(TAG, "hideIcon");
        cancelBlinking();
        StatusBarManagerHelper.setIconVisibility(this.mContext, ICON_SLOT_NAME, false);
        StatusBarManagerHelper.removeIcon(this.mContext, ICON_SLOT_NAME);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showIcon(int i) {
        Context context = this.mContext;
        StatusBarManagerHelper.setIcon(context, ICON_SLOT_NAME, i, 0, context.getString(R.string.air_action_title));
        StatusBarManagerHelper.setIconVisibility(this.mContext, ICON_SLOT_NAME, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelBlinking() {
        Runnable runnable = this.mIconBlinkHandler;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mIconBlinkHandler = null;
        }
    }

    /* renamed from: com.samsung.remotespen.main.notification.IndicatorController$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionState;

        static {
            int[] iArr = new int[BleSpenManager.ConnectionState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionState = iArr;
            try {
                iArr[BleSpenManager.ConnectionState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionState[BleSpenManager.ConnectionState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionState[BleSpenManager.ConnectionState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private int getIconResId(BleSpenManager.ConnectionState connectionState, int i) {
        int i2 = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionState[connectionState.ordinal()];
        if (i2 != 1) {
            return i2 != 2 ? R.drawable.stat_sys_spen_disconnected : R.drawable.stat_sys_spen_normal;
        }
        return getBatteryLevelResId(i);
    }
}
