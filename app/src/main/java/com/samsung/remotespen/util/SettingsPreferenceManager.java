package com.samsung.remotespen.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.text.TextUtils;
import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.RemoteSpenTileService;
import com.samsung.util.SpenGestureManagerWrapper;
import com.samsung.util.constants.SPenSettingsConstants;
import com.samsung.util.debug.Assert;
import com.samsung.util.debug.Log;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.usage.SAUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/* loaded from: classes.dex */
public class SettingsPreferenceManager {
    private static final String AIR_ACTION_ENABLED_BEFORE_EMERGENCY_MODE = "air_action_enabled_before_emergency_mode";
    private static final String AIR_MOUSE_FTU_COMPLETED = "air_mouse_ftu_completed";
    private static final String AIR_MOUSE_HIGHLIGHTER_SIZE = "air_mouse_highlighter_size";
    private static final String AIR_MOUSE_POINTER_COLOR = "air_mouse_pointer_color";
    private static final String AIR_MOUSE_POINTER_SIZE = "air_mouse_pointer_size";
    private static final String AIR_MOUSE_POINTER_SPEED = "air_mouse_pointer_speed";
    private static final String AIR_MOUSE_SHOW_POINTER_TRACE = "air_mouse_show_pointer_trace";
    private static final String FIRMWARE_AVAILABLE_LAST_REMIND_TIME = "firmware_available_last_remind_time";
    private static final String GLOBAL_ACTION_ENABLED = "global_action_enabled";
    private static final String MY_DEVICE_ID = "my_device_id";
    private static final String NEED_TO_REVERT_AIR_ACTION_ENABLED_BEFORE_AIRPLANE_MODE = "need_to_revert_air_action_enabled_before_airplane_mode";
    private static final String NEED_TO_REVERT_KEEP_CONNECT_ENABLED = "need_to_revert_keep_connect_enabled";
    private static final String PREF_NAME = "remotespen_pref";
    private static final String SEM_SPEN_WRITING_COLOR = "spen_writing_color";
    private static final String TAG = "SettingsPreferenceManager";
    private static final String TIPS_VIEW_PASSED_ENABLED = "tips_view_passed_enabled";
    private static final String WELCOME_VIEW_PASSED_ENABLED = "welcome_view_passed_enabled";
    private static SettingsPreferenceManager sInstance;
    private ContentResolver mContentResolver;
    private Context mContext;
    private SharedPreferences mPreference;
    private ArrayList<PreferenceChangeListener> mAirMousePrefChangeListenerArray = new ArrayList<>();
    private ArrayList<SettingsChangeListener> mSettingChangeListenerArray = new ArrayList<>();
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.samsung.remotespen.util.SettingsPreferenceManager.1
        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            SettingsPreferenceManager.this.onPreferenceChanged(str);
        }
    };

    /* loaded from: classes.dex */
    public interface PreferenceChangeListener {
        void onPreferenceChanged(SettingsPreferenceManager settingsPreferenceManager, String str);
    }

    /* loaded from: classes.dex */
    public interface SettingsChangeListener {
        void onChanged(String str);
    }

    public static SettingsPreferenceManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SettingsPreferenceManager(context);
        }
        return sInstance;
    }

    private SettingsPreferenceManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mContentResolver = applicationContext.getContentResolver();
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(PREF_NAME, 0);
        this.mPreference = sharedPreferences;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this.mSharedPreferenceChangeListener);
    }

    public void resetSettings() {
        SharedPreferences.Editor edit = this.mPreference.edit();
        edit.clear();
        edit.apply();
    }

    private SharedPreferences.Editor prepareEditor(String str) {
        SharedPreferences.Editor edit = this.mPreference.edit();
        edit.remove(str);
        return edit;
    }

    public void setAirActionEnabledBeforeEmergencyMode(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_ACTION_ENABLED_BEFORE_EMERGENCY_MODE);
        prepareEditor.putBoolean(AIR_ACTION_ENABLED_BEFORE_EMERGENCY_MODE, z);
        prepareEditor.apply();
    }

    public boolean getAirActionEnabledBeforeEmergencyMode() {
        return this.mPreference.getBoolean(AIR_ACTION_ENABLED_BEFORE_EMERGENCY_MODE, false);
    }

    public void setNeedToRevertAirActionEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(NEED_TO_REVERT_AIR_ACTION_ENABLED_BEFORE_AIRPLANE_MODE);
        prepareEditor.putBoolean(NEED_TO_REVERT_AIR_ACTION_ENABLED_BEFORE_AIRPLANE_MODE, z);
        prepareEditor.apply();
    }

    public boolean getNeedToRevertAirActionEnabled() {
        return this.mPreference.getBoolean(NEED_TO_REVERT_AIR_ACTION_ENABLED_BEFORE_AIRPLANE_MODE, false);
    }

    public void setNeedToRevertKeepConnectEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(NEED_TO_REVERT_KEEP_CONNECT_ENABLED);
        prepareEditor.putBoolean(NEED_TO_REVERT_KEEP_CONNECT_ENABLED, z);
        prepareEditor.apply();
    }

    public boolean getNeedToRevertKeepConnectEnabled() {
        return this.mPreference.getBoolean(NEED_TO_REVERT_KEEP_CONNECT_ENABLED, false);
    }

    public boolean getGlobalActionEnabled() {
        return this.mPreference.getBoolean(GLOBAL_ACTION_ENABLED, true);
    }

    public boolean setAirActionEnabled(boolean z) {
        String str = TAG;
        Log.i(str, "setAirActionEnabled : " + z);
        Settings.System.putInt(this.mContentResolver, SPenSettingsConstants.URI_PEN_AIR_ACTION, z ? 1 : 0);
        SAUtils.insertSpenRemoteStatusLog(z);
        if (getNeedToRevertAirActionEnabled()) {
            setNeedToRevertAirActionEnabled(false);
        }
        TileService.requestListeningState(this.mContext, new ComponentName(this.mContext.getPackageName(), RemoteSpenTileService.class.getName()));
        invokeSettingChangeListener(SPenSettingsConstants.URI_PEN_AIR_ACTION);
        return true;
    }

    public boolean getAirActionEnabled() {
        return ModelFeatures.canDeviceSupportBleSpen(this.mContext) && Settings.System.getInt(this.mContentResolver, SPenSettingsConstants.URI_PEN_AIR_ACTION, 1) != 0;
    }

    public void setSpenUnlockEnabled(boolean z) {
        Settings.System.putInt(this.mContentResolver, SPenSettingsConstants.URI_UNLOCK_WITH_SPEN_REMOTE, z ? 1 : 0);
        SAUtils.insertSpenUnlockStatusLog(z);
        invokeSettingChangeListener(SPenSettingsConstants.URI_UNLOCK_WITH_SPEN_REMOTE);
    }

    public boolean getSpenUnlockEnabled() {
        return Settings.System.getInt(this.mContentResolver, SPenSettingsConstants.URI_UNLOCK_WITH_SPEN_REMOTE, 0) != 0;
    }

    public boolean setPenColorCode(Context context, String str) {
        String str2 = TAG;
        Log.i(str2, "setPenColorCode : " + str);
        Settings.System.putString(this.mContentResolver, SEM_SPEN_WRITING_COLOR, str);
        new SpenGestureManagerWrapper(context).setBleSpenCmfCode(str);
        invokeSettingChangeListener(SEM_SPEN_WRITING_COLOR);
        return true;
    }

    public void setWelcomeViewPassedEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(WELCOME_VIEW_PASSED_ENABLED);
        prepareEditor.putInt(WELCOME_VIEW_PASSED_ENABLED, z ? 1 : 0);
        prepareEditor.apply();
    }

    public boolean getWelcomeViewPassedEnabled() {
        return this.mPreference.getInt(WELCOME_VIEW_PASSED_ENABLED, 0) != 0;
    }

    public void setTipsViewPassedEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(TIPS_VIEW_PASSED_ENABLED);
        prepareEditor.putInt(TIPS_VIEW_PASSED_ENABLED, z ? 1 : 0);
        prepareEditor.apply();
    }

    public boolean getTipsViewPassedEnabled() {
        return this.mPreference.getInt(TIPS_VIEW_PASSED_ENABLED, 0) != 0;
    }

    public String getMyDeviceId() {
        StringBuilder sb = new StringBuilder(this.mPreference.getString(MY_DEVICE_ID, Constants.packageName.NONE));
        if (TextUtils.isEmpty(sb.toString())) {
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", Integer.valueOf(random.nextInt(255))));
                if (i < 5) {
                    sb.append(":");
                }
            }
            SharedPreferences.Editor prepareEditor = prepareEditor(MY_DEVICE_ID);
            prepareEditor.putString(MY_DEVICE_ID, sb.toString());
            prepareEditor.apply();
            Log.i(TAG, "getMyDeviceId : my device id generated. ID = " + ((Object) sb));
        }
        return sb.toString();
    }

    public void resetMyDeviceId() {
        SharedPreferences.Editor prepareEditor = prepareEditor(MY_DEVICE_ID);
        prepareEditor.putString(MY_DEVICE_ID, Constants.packageName.NONE);
        prepareEditor.apply();
    }

    public void setAirMouseFtuCompleted(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_FTU_COMPLETED);
        prepareEditor.putInt(AIR_MOUSE_FTU_COMPLETED, z ? 1 : 0);
        prepareEditor.apply();
    }

    public boolean getAirMouseFtuCompleted() {
        return this.mPreference.getInt(AIR_MOUSE_FTU_COMPLETED, 0) != 0;
    }

    public void setAirMousePointerSpeed(float f) {
        Assert.e(0.0f <= f && f <= 1.0f);
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_POINTER_SPEED);
        prepareEditor.putFloat(AIR_MOUSE_POINTER_SPEED, f);
        prepareEditor.apply();
    }

    public float getAirMousePointerSpeed() {
        return this.mPreference.getFloat(AIR_MOUSE_POINTER_SPEED, 0.5f);
    }

    public void setAirMousePointerColor(int i) {
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_POINTER_COLOR);
        prepareEditor.putInt(AIR_MOUSE_POINTER_COLOR, i);
        prepareEditor.apply();
    }

    public int getAirMousePointerColor() {
        return this.mPreference.getInt(AIR_MOUSE_POINTER_COLOR, this.mContext.getResources().getColor(R.color.remotespen_airmouse_laser_pointer_purple, null));
    }

    public void setAirMousePointerSize(float f) {
        Assert.e(0.0f <= f && f <= 1.0f);
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_POINTER_SIZE);
        prepareEditor.putFloat(AIR_MOUSE_POINTER_SIZE, f);
        prepareEditor.apply();
    }

    public float getAirMousePointerSize() {
        return this.mPreference.getFloat(AIR_MOUSE_POINTER_SIZE, 0.5f);
    }

    public void setAirMousePointerTraceEnabled(boolean z) {
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_SHOW_POINTER_TRACE);
        prepareEditor.putBoolean(AIR_MOUSE_SHOW_POINTER_TRACE, z);
        prepareEditor.apply();
    }

    public boolean getAirMousePointerTraceEnabled() {
        return this.mPreference.getBoolean(AIR_MOUSE_SHOW_POINTER_TRACE, true);
    }

    public void setAirMouseHighlighterSize(float f) {
        Assert.e(0.0f <= f && f <= 1.0f);
        SharedPreferences.Editor prepareEditor = prepareEditor(AIR_MOUSE_HIGHLIGHTER_SIZE);
        prepareEditor.putFloat(AIR_MOUSE_HIGHLIGHTER_SIZE, f);
        prepareEditor.apply();
    }

    public float getAirMouseHighlighterSize() {
        return this.mPreference.getFloat(AIR_MOUSE_HIGHLIGHTER_SIZE, 0.5f);
    }

    public void registerAirMousePreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        if (this.mAirMousePrefChangeListenerArray.contains(preferenceChangeListener)) {
            return;
        }
        this.mAirMousePrefChangeListenerArray.add(preferenceChangeListener);
    }

    public void unregisterAirMousePreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
        this.mAirMousePrefChangeListenerArray.remove(preferenceChangeListener);
    }

    private void invokeAirMousePreferenceChangeListener(String str) {
        Iterator<PreferenceChangeListener> it = this.mAirMousePrefChangeListenerArray.iterator();
        while (it.hasNext()) {
            it.next().onPreferenceChanged(this, str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPreferenceChanged(String str) {
        String str2 = TAG;
        Log.i(str2, "onPreferenceChanged : " + str);
        if (str == null) {
            return;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case -2061871568:
                if (str.equals(AIR_MOUSE_SHOW_POINTER_TRACE)) {
                    c = 0;
                    break;
                }
                break;
            case -384598222:
                if (str.equals(AIR_MOUSE_POINTER_SIZE)) {
                    c = 1;
                    break;
                }
                break;
            case -112990194:
                if (str.equals(AIR_MOUSE_HIGHLIGHTER_SIZE)) {
                    c = 2;
                    break;
                }
                break;
            case 947746386:
                if (str.equals(AIR_MOUSE_POINTER_COLOR)) {
                    c = 3;
                    break;
                }
                break;
            case 962545462:
                if (str.equals(AIR_MOUSE_POINTER_SPEED)) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                invokeAirMousePreferenceChangeListener(str);
                return;
            default:
                return;
        }
    }

    public void registerSettingChangeListener(SettingsChangeListener settingsChangeListener) {
        if (this.mSettingChangeListenerArray.contains(settingsChangeListener)) {
            return;
        }
        this.mSettingChangeListenerArray.add(settingsChangeListener);
    }

    public void unregisterSettingChangeListener(SettingsChangeListener settingsChangeListener) {
        this.mSettingChangeListenerArray.remove(settingsChangeListener);
    }

    private void invokeSettingChangeListener(String str) {
        Iterator<SettingsChangeListener> it = this.mSettingChangeListenerArray.iterator();
        while (it.hasNext()) {
            it.next().onChanged(str);
        }
    }
}
