package com.samsung.remotespen.core.device.control.behavior;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.NormalBehaviorPolicy;
import com.samsung.remotespen.core.device.control.behavior.policy.StandbyBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.util.constants.SPenSettingsConstants;
import com.samsung.util.settings.SettingsValueManager;
import com.samsung.util.settings.SpenSettingObserver;

/* loaded from: classes.dex */
public class RainbowPenBehaviorPolicyManager extends PenBehaviorPolicyManager {
    private static final String TAG = "RainbowPenBehaviorPolicyManager";
    private SpenSettingObserver mAllowMultiplePensSettingObserver;
    private AbsPenBehaviorPolicy mBehaviorPolicy;
    private AbsPenBehaviorPolicy.Callback mCallback;
    private Context mContext;
    private BleSpenDriver mDriver;
    private BleSpenInstanceId mInstanceId;
    private SpenSettingObserver mStandbyModeSettingObserver;
    private SpenSettingObserver.Listener mStandbyModeSettingChangedListener = new SpenSettingObserver.Listener() { // from class: com.samsung.remotespen.core.device.control.behavior.RainbowPenBehaviorPolicyManager.1
        @Override // com.samsung.util.settings.SpenSettingObserver.Listener
        public void onSettingChanged() {
            SettingsValueManager settingsValueManager = SettingsValueManager.getInstance(RainbowPenBehaviorPolicyManager.this.mContext);
            boolean z = !settingsValueManager.isKeepConnectedEnabled();
            if (z) {
                Log.i(RainbowPenBehaviorPolicyManager.TAG, "mStandbyModeSettingChangedListener : Turn off allow multiple pens setting");
                settingsValueManager.setAllowMultiplePensEnabled(false);
            }
            RainbowPenBehaviorPolicyManager.this.updateBehaviorPolicy(z);
        }
    };
    private SpenSettingObserver.Listener mAllowMultiplePensSettingChangedListener = new SpenSettingObserver.Listener() { // from class: com.samsung.remotespen.core.device.control.behavior.RainbowPenBehaviorPolicyManager.2
        @Override // com.samsung.util.settings.SpenSettingObserver.Listener
        public void onSettingChanged() {
            SettingsPreferenceManager settingsPreferenceManager = SettingsPreferenceManager.getInstance(RainbowPenBehaviorPolicyManager.this.mContext);
            SettingsValueManager settingsValueManager = SettingsValueManager.getInstance(RainbowPenBehaviorPolicyManager.this.mContext);
            boolean isAllowMultiplePensEnabled = settingsValueManager.isAllowMultiplePensEnabled();
            boolean z = !settingsValueManager.isKeepConnectedEnabled();
            if (isAllowMultiplePensEnabled && z) {
                Log.i(RainbowPenBehaviorPolicyManager.TAG, "mAllowMultiplePensSettingChangedListener : Turn on keep connected setting");
                settingsPreferenceManager.setNeedToRevertKeepConnectEnabled(true);
                settingsValueManager.setKeepConnectedEnabled(true);
            } else if (isAllowMultiplePensEnabled || !settingsPreferenceManager.getNeedToRevertKeepConnectEnabled()) {
            } else {
                settingsPreferenceManager.setNeedToRevertKeepConnectEnabled(false);
                settingsValueManager.setKeepConnectedEnabled(false);
            }
        }
    };

    public RainbowPenBehaviorPolicyManager(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback) {
        this.mContext = context;
        this.mDriver = bleSpenDriver;
        this.mInstanceId = bleSpenInstanceId;
        this.mCallback = callback;
        this.mStandbyModeSettingObserver = new SpenSettingObserver(this.mContext);
        this.mAllowMultiplePensSettingObserver = new SpenSettingObserver(this.mContext);
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager
    public void start() {
        this.mStandbyModeSettingObserver.registerObserver(SPenSettingsConstants.URI_KEEP_CONNECTED, this.mStandbyModeSettingChangedListener);
        this.mAllowMultiplePensSettingObserver.registerObserver(SPenSettingsConstants.URI_POWER_SAVER, this.mAllowMultiplePensSettingChangedListener);
        if (SettingsValueManager.getInstance(this.mContext).isAllowMultiplePensEnabled() && !SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled()) {
            Log.i(TAG, "RainbowPenBehaviorPolicyManager : Turn on keep connected setting");
            SettingsPreferenceManager.getInstance(this.mContext).setNeedToRevertKeepConnectEnabled(true);
            SettingsValueManager.getInstance(this.mContext).setKeepConnectedEnabled(true);
        }
        updateBehaviorPolicy(!SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled());
    }

    @Override // com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager
    public void stop() {
        this.mAllowMultiplePensSettingObserver.unregisterObserver();
        this.mStandbyModeSettingObserver.unregisterObserver();
        this.mAllowMultiplePensSettingObserver = null;
        this.mStandbyModeSettingObserver = null;
        this.mBehaviorPolicy.stop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBehaviorPolicy(boolean z) {
        String str = TAG;
        Log.d(str, "updateBehaviorPolicy : isStandbyModeEnabled =" + z);
        AbsPenBehaviorPolicy absPenBehaviorPolicy = this.mBehaviorPolicy;
        if (absPenBehaviorPolicy != null) {
            absPenBehaviorPolicy.stop();
            this.mBehaviorPolicy = null;
        }
        if (z) {
            this.mBehaviorPolicy = new StandbyBehaviorPolicy(this.mContext, this.mDriver, this.mInstanceId, this.mCallback);
        } else {
            this.mBehaviorPolicy = new NormalBehaviorPolicy(this.mContext, this.mDriver, this.mInstanceId, this.mCallback);
        }
        this.mBehaviorPolicy.start();
    }
}
