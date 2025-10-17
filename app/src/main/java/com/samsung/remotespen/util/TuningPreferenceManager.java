package com.samsung.remotespen.util;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class TuningPreferenceManager {
    private static final String KEY_CLICK_BUTTON_DOWN_POSITION = "click_button_down_position";
    private static final String KEY_CPU_BOOSTER_ON_MOVE = "cpu_booster_on_movement";
    private static final String KEY_MAX_POINTER_PREDICTION_TIME = "max_pointer_prediction_time";
    private static final String KEY_ORIENTATION_ADAPTIVE_POINTER_MOVEMENT = "orientation_adaptive_pointer_movement";
    private static final String KEY_POINTER_MOVEMENT_DELAY = "pointer_movement_delay";
    private static final String KEY_QUICK_MOUSE = "enable_quick_mouse";
    private static final String KEY_SHOW_CLICKABLE_AREA = "show_clickable_area";
    private static final String KEY_SMOOTH_POINTER_MOVEMENT = "smooth_pointer_movement";
    private static final String PREF_NAME = "remotespen_tuning_pref";
    private static final String TAG = "TuningPreferenceManager";
    private static TuningPreferenceManager sInstance;
    private Context mContext;
    private SharedPreferences mPreference;

    public static TuningPreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TuningPreferenceManager(context);
        }
        return sInstance;
    }

    private TuningPreferenceManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mPreference = applicationContext.getSharedPreferences(PREF_NAME, 0);
    }

    public void setQuickMouseEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_QUICK_MOUSE);
        prepareEditor.putBoolean(KEY_QUICK_MOUSE, z);
        prepareEditor.apply();
    }

    public boolean getQuickMouseEnabled() {
        return this.mPreference.getBoolean(KEY_QUICK_MOUSE, false);
    }

    public void setOrientationAdaptivePointerMovementEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_ORIENTATION_ADAPTIVE_POINTER_MOVEMENT);
        prepareEditor.putBoolean(KEY_ORIENTATION_ADAPTIVE_POINTER_MOVEMENT, z);
        prepareEditor.apply();
    }

    public boolean getOrientationAdaptivePointerMovementEnabled() {
        return this.mPreference.getBoolean(KEY_ORIENTATION_ADAPTIVE_POINTER_MOVEMENT, false);
    }

    public void setSmoothPointerMovementEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_SMOOTH_POINTER_MOVEMENT);
        prepareEditor.putBoolean(KEY_SMOOTH_POINTER_MOVEMENT, z);
        prepareEditor.apply();
    }

    public boolean getSmoothPointerMovementEnabled() {
        return this.mPreference.getBoolean(KEY_SMOOTH_POINTER_MOVEMENT, true);
    }

    public void setPointerMovementDelay(int i) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_POINTER_MOVEMENT_DELAY);
        prepareEditor.putInt(KEY_POINTER_MOVEMENT_DELAY, i);
        prepareEditor.apply();
    }

    public int getPointerMovementDelay() {
        return this.mPreference.getInt(KEY_POINTER_MOVEMENT_DELAY, 22);
    }

    public void setMaxPointerPredictionTime(int i) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_MAX_POINTER_PREDICTION_TIME);
        prepareEditor.putInt(KEY_MAX_POINTER_PREDICTION_TIME, i);
        prepareEditor.apply();
    }

    public int getMaxPointerPredictionTime() {
        return this.mPreference.getInt(KEY_MAX_POINTER_PREDICTION_TIME, 60);
    }

    public void setShowClickableAreaEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_SHOW_CLICKABLE_AREA);
        prepareEditor.putBoolean(KEY_SHOW_CLICKABLE_AREA, z);
        prepareEditor.apply();
    }

    public boolean getShowClickableAreaEnabled() {
        return this.mPreference.getBoolean(KEY_SHOW_CLICKABLE_AREA, false);
    }

    public void setClickButtonDownPositionEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_CLICK_BUTTON_DOWN_POSITION);
        prepareEditor.putBoolean(KEY_CLICK_BUTTON_DOWN_POSITION, z);
        prepareEditor.apply();
    }

    public boolean getClickButtonDownPositionEnabled() {
        return this.mPreference.getBoolean(KEY_CLICK_BUTTON_DOWN_POSITION, false);
    }

    private SharedPreferences.Editor prepareEditor(String str) {
        SharedPreferences.Editor edit = this.mPreference.edit();
        edit.remove(str);
        return edit;
    }

    public void setCpuBoosterOnMovementEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(KEY_CPU_BOOSTER_ON_MOVE);
        prepareEditor.putBoolean(KEY_CPU_BOOSTER_ON_MOVE, z);
        prepareEditor.apply();
    }

    public boolean getCpuBoosterOnMoveEnabled() {
        return this.mPreference.getBoolean(KEY_CPU_BOOSTER_ON_MOVE, false);
    }
}
