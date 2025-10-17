package com.samsung.remotespen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.RemoteViews;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.ui.settings.ManualPairingActivity;
import com.samsung.remotespen.ui.settings.RemoteSpenSettingsActivity;
import com.samsung.remotespen.util.SettingsPreferenceManager;
//import com.samsung.settings.SPenSettingActivity;
import com.samsung.util.CommonUtils;
import com.samsung.util.constants.SPenSettingsConstants;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.settings.SpenSettingObserver;

/* loaded from: classes.dex */
public class RemoteSpenTileService extends TileService {
    private static final String TAG = RemoteSpenTileService.class.getSimpleName();
    private SpenSettingObserver mAirActionSettingObserver;
    private Context mContext;

    public static void setTileEnabled(Context context, boolean z) {
        String str = TAG;
        Log.i(str, "setTileEnabled isEnabled : " + z);
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context.getApplicationContext().getPackageName(), RemoteSpenTileService.class.getName()), z ? 1 : 2, 1);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        this.mContext = getApplicationContext();
        SpenSettingObserver spenSettingObserver = new SpenSettingObserver(this);
        this.mAirActionSettingObserver = spenSettingObserver;
        spenSettingObserver.registerObserver(SPenSettingsConstants.URI_PEN_AIR_ACTION, new SpenSettingObserver.Listener() { // from class: com.samsung.remotespen.RemoteSpenTileService.1
            @Override // com.samsung.util.settings.SpenSettingObserver.Listener
            public void onSettingChanged() {
                RemoteSpenTileService remoteSpenTileService = RemoteSpenTileService.this;
                remoteSpenTileService.setTileState(remoteSpenTileService.isSpenRemoteEnabled());
            }
        });
    }

    @Override // android.service.quicksettings.TileService, android.app.Service
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        this.mAirActionSettingObserver.unregisterObserver();
    }

    @Override // android.service.quicksettings.TileService
    public void onStartListening() {
        Log.i(TAG, "onStartListening");
        super.onStartListening();
        setTileState(isSpenRemoteEnabled());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTileState(boolean z) {
        Tile qsTile = getQsTile();
        if (qsTile == null) {
            Log.e(TAG, "setTileState : tile is null");
            return;
        }
        String str = TAG;
        Log.i(str, "setTileState :" + z);
        qsTile.setIcon(Icon.createWithResource(this, z ? R.drawable.quick_panel_icon_remote_spen_on : R.drawable.quick_panel_icon_remote_spen_off));
        qsTile.setLabel(getResources().getText(R.string.air_action_tile_title));
        qsTile.setState(z ? 2 : 1);
        qsTile.updateTile();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSpenRemoteEnabled() {
        return SettingsPreferenceManager.getInstance(this.mContext).getAirActionEnabled();
    }

    private void setSpenRemoteEnabled(boolean z) {
        SettingsPreferenceManager.getInstance(this.mContext).setAirActionEnabled(z);
    }

    @Override // android.service.quicksettings.TileService
    public void onStopListening() {
        super.onStopListening();
    }

    @Override // android.service.quicksettings.TileService
    public void onClick() {
        super.onClick();
        String str = TAG;
        Log.i(str, "onClick");
        if (!CommonUtils.isSettingsChangesAllowed(this.mContext, true)) {
            Log.e(str, "Change setting is not allowed");
            return;
        }
        int state = getQsTile().getState();
        if (state == 0 || state == 1) {
            setSpenRemoteEnabled(true);
            onTurnOnSpenRemote();
        } else if (state == 2) {
            setSpenRemoteEnabled(false);
        }
        setTileState(isSpenRemoteEnabled());
    }

    public Intent semGetSettingsIntent() {
        if (!CommonUtils.isSettingsChangesAllowed(this.mContext, true)) {
            Log.e(TAG, "Change setting is not allowed");
            return null;
        }
        Intent intent = new Intent();
        intent.setClass(this, RemoteSpenSettingsActivity.class);
        intent.setFlags(268468224);
        return intent;
    }

    public CharSequence semGetDetailViewTitle() {
        return getResources().getText(R.string.air_action_title);
    }

    public RemoteViews semGetDetailView() {
        if (!CommonUtils.isSettingsChangesAllowed(this.mContext, true)) {
            Log.e(TAG, "Change setting is not allowed");
            return null;
        }
        RemoteViews remoteViews = new RemoteViews(this.mContext.getPackageName(), (int) R.layout.spen_remote_tile_detail_view);
        BleSpenPairedSpenManager bleSpenPairedSpenManager = BleSpenPairedSpenManager.getInstance(this.mContext);
        if (ModelFeatures.isBundledSpenSupportBle()) {
            if (bleSpenPairedSpenManager.isUnbundledSpenPaired()) {
                remoteViews.setViewVisibility(R.id.quick_settings_bundled_spen_detail_description, 8);
            }
        } else if (bleSpenPairedSpenManager.isUnbundledSpenPairingDetected()) {
            remoteViews.setViewVisibility(R.id.quick_settings_bundled_spen_description_container, 8);
            remoteViews.setViewVisibility(R.id.quick_settings_unbundled_spen_description, 0);
        }
        return remoteViews;
    }

    public boolean semIsToggleButtonChecked() {
        return isSpenRemoteEnabled();
    }

    public void semSetToggleButtonChecked(boolean z) {
        String str = TAG;
        Log.v(str, "semSetToggleButtonChecked : " + z);
        setSpenRemoteEnabled(z);
        if (z) {
            onTurnOnSpenRemote();
        }
    }

    private void onTurnOnSpenRemote() {
        CommonUtils.collapseStatusBarPanel(this);
        if (ModelFeatures.isBundledSpenSupportBle()) {
            if (CommonUtils.isNeedInsertSpen(this.mContext, true)) {
                startSPenSettingActivity();
                return;
            }
            return;
        }
        startActivity(ManualPairingActivity.getStartIntent(this, getSpenInstanceUidString(), true));
    }

    private void startSPenSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SPenSettingActivity.class);
        intent.addFlags(805306368);
        startActivity(intent);
    }

    private String getSpenInstanceUidString() {
        BleSpenInstanceId primaryUnbundledSpenInstanceId = BleSpenPairedSpenManager.getInstance(this.mContext).getPrimaryUnbundledSpenInstanceId();
        if (primaryUnbundledSpenInstanceId != null) {
            return SpenInstanceIdHelper.from(this.mContext, primaryUnbundledSpenInstanceId).getUidString();
        }
        return null;
    }
}
