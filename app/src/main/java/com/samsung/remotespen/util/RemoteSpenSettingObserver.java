package com.samsung.remotespen.util;

import android.content.Context;
import com.samsung.util.constants.SPenSettingsConstants;
import com.samsung.util.settings.SpenSettingObserver;

/* loaded from: classes.dex */
public class RemoteSpenSettingObserver extends SpenSettingObserver {
    private static final String TAG = RemoteSpenSettingObserver.class.getSimpleName();
    private Context mContext;

    /* loaded from: classes.dex */
    public interface Listener {
        void onSettingChanged(boolean z);
    }

    public RemoteSpenSettingObserver(Context context) {
        super(context);
        this.mContext = context.getApplicationContext();
    }

    public void registerObserver(final Listener listener) {
        super.registerObserver(SPenSettingsConstants.URI_PEN_AIR_ACTION, new SpenSettingObserver.Listener() { // from class: com.samsung.remotespen.util.RemoteSpenSettingObserver.1
            @Override // com.samsung.util.settings.SpenSettingObserver.Listener
            public void onSettingChanged() {
                if (listener != null) {
                    listener.onSettingChanged(SettingsPreferenceManager.getInstance(RemoteSpenSettingObserver.this.mContext).getAirActionEnabled());
                }
            }
        });
    }
}
