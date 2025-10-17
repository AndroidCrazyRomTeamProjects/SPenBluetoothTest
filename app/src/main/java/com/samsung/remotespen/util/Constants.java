package com.samsung.remotespen.util;

/* loaded from: classes.dex */
public class Constants {
    public static final long BLE_APPEAR_ANIMATOR_DURATION = 250;
    public static final long BLE_APPEAR_ANIMATOR_STAY_DURATION = 1000;
    public static final long BLE_APP_SWITCH_ANIMATOR_DELAY = 250;
    public static final long BLE_GESTURE_FAIL_GUIDE_PANEL_TOUCH_TIMEOUT = 3000;
    public static final long BLE_GUIDE_PANEL_ANIMATOR_DURATION = 3000;
    public static final long BLE_GUIDE_PANEL_DURATION_WITHOUT_ANIMATION = 1000;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_RIGHT = 2;
    public static final String FMM_BROADCAST_RECEIVER_NAME = "com.samsung.android.fmm.application.receiver.FmmEventReceiver";
    public static final String FMM_PACKAGE_NAME = "com.samsung.android.fmm";
    public static final String GOOGLE_GRANT_PERMISSION_ACTIVITY_NAME = "com.android.packageinstaller.permission.ui.GrantPermissionsActivity";
    public static final String GOOGLE_PACKAGE_INSTALLER_PACKAGE_NAME = "com.google.android.packageinstaller";
    public static final String INTENT_ACTION_FMM_CHANGED = "com.samsung.android.fmm.WD_CHANGED";
    public static final String INTENT_ACTION_FMM_REQUEST = "com.samsung.android.fmm.OPERATION_REQUEST";
    public static final String INTENT_ACTION_FMM_RESPONSE = "com.samsung.android.fmm.OPERATION_RESPONSE";
    public static final String INTENT_ACTION_REMOTESPEN_COMMON_BROADCAST = "com.samsung.remotespen.action.COMMON_BROADCAST";
    public static final String INTENT_ACTION_THEME_APPLY_START = "com.samsung.android.theme.themecenter.THEME_APPLY_START";
    public static final String SAMSUNG_CAMERA_ACTIVITY_NAME = "com.sec.android.app.camera.Camera";
    public static final String SAMSUNG_CAMERA_PACKAGE_NAME = "com.sec.android.app.camera";

    /* loaded from: classes.dex */
    public enum ErrorCode {
        NO_ERROR,
        KNOX_AIRCOMMAND_OFF,
        CARMODE_ON,
        KIDSMODE_ON,
        HMT_ON,
        KNOXSTATE_ON,
        PINMODE_ON,
        DAYDREAM_ON,
        MIRRORLINK_ON,
        SETTING_RESUMED,
        RETAILMODE_ON,
        MISSING_PHONE_LOCK,
        DOMESTIC_OTA_START,
        SECURE_KEYGUARD_ON,
        DEX_MODE_ON,
        HW_TEST_MODE_ON,
        EMERGENCY_MODE_ON,
        DEX_MODE_TOUCHPAD_ON
    }

    /* loaded from: classes.dex */
    public enum ReasonUnpair {
        REASON_UNPAIR_ONLY,
        REASON_UNPAIR_FOR_ADD_NEW
    }
}
