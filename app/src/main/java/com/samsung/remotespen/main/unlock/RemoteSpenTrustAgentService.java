package com.samsung.remotespen.main.unlock;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.service.trust.TrustAgentService;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import com.android.internal.widget.LockPatternUtils;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/* loaded from: classes.dex */
public class RemoteSpenTrustAgentService extends TrustAgentService {
    private static final String ACTION_GRANT_TRUST = "action.sample_trust_agent.grant_trust";
    private static final String ACTION_MANAGE_TRUST = "action.sample_trust_agent.manage_trust";
    private static final String ACTION_REVOKE_TRUST = "action.sample_trust_agent.revoke_trust";
    private static final String EXTRA_DISMISS_KEYGUARD = "extra.dismiss_keyguard";
    private static final String EXTRA_DURATION = "extra.duration";
    private static final String EXTRA_INITIATED_BY_USER = "extra.init_by_user";
    private static final String EXTRA_MANAGE = "extra.manage";
    private static final String EXTRA_MESSAGE = "extra.message";
    private static final String TAG = RemoteSpenTrustAgentService.class.getSimpleName();
    public LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.main.unlock.RemoteSpenTrustAgentService.1
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (RemoteSpenTrustAgentService.ACTION_GRANT_TRUST.equals(action)) {
                boolean booleanExtra = intent.getBooleanExtra(RemoteSpenTrustAgentService.EXTRA_INITIATED_BY_USER, false);
                int i = booleanExtra;
                if (intent.getBooleanExtra(RemoteSpenTrustAgentService.EXTRA_DISMISS_KEYGUARD, false)) {
                    i = (booleanExtra ? 1 : 0) | true;
                }
                try {
                    long longExtra = intent.getLongExtra(RemoteSpenTrustAgentService.EXTRA_DURATION, 0L);
                    RemoteSpenTrustAgentService.this.grantTrust(intent.getStringExtra(RemoteSpenTrustAgentService.EXTRA_MESSAGE), longExtra, i);
                    String str = RemoteSpenTrustAgentService.TAG;
                    Log.i(str, "grantTrust:duration-" + longExtra);
                } catch (IllegalStateException e) {
                    String str2 = RemoteSpenTrustAgentService.TAG;
                    Log.i(str2, "grantTrust:failed: " + e.toString());
                }
            } else if (RemoteSpenTrustAgentService.ACTION_REVOKE_TRUST.equals(action)) {
                RemoteSpenTrustAgentService.this.revokeTrust();
            } else if (RemoteSpenTrustAgentService.ACTION_MANAGE_TRUST.equals(action)) {
                RemoteSpenTrustAgentService.this.setManageTrust(intent.getBooleanExtra(RemoteSpenTrustAgentService.EXTRA_MANAGE, false));
            }
        }
    };

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GRANT_TRUST);
        intentFilter.addAction(ACTION_REVOKE_TRUST);
        intentFilter.addAction(ACTION_MANAGE_TRUST);
        this.mLocalBroadcastManager.registerReceiver(this.mReceiver, intentFilter);
        setManageTrust(SettingsPreferenceManager.getInstance(this).getSpenUnlockEnabled());
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        this.mLocalBroadcastManager.unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setManageTrust(boolean z) {
        setManagingTrust(z);
        String str = TAG;
        Log.i(str, "setManagingTrust:managingTrust-" + z);
    }

    public static void sendGrantTrust(Context context, long j) {
        Intent intent = new Intent(ACTION_GRANT_TRUST);
        intent.putExtra(EXTRA_MESSAGE, "RemoteSpenTrustAgentService");
        intent.putExtra(EXTRA_DURATION, j);
        intent.putExtra(EXTRA_INITIATED_BY_USER, false);
        String str = TAG;
        Log.d(str, "sendGrantTrust:Action-action.sample_trust_agent.grant_trust, duration-" + j);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendRevokeTrust(Context context) {
        Intent intent = new Intent(ACTION_REVOKE_TRUST);
        Log.d(TAG, "sendRevokeTrust:Action-action.sample_trust_agent.revoke_trust");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendManageTrust(Context context, boolean z) {
        Intent intent = new Intent(ACTION_MANAGE_TRUST);
        intent.putExtra(EXTRA_MANAGE, z);
        String str = TAG;
        Log.d(str, "sendManageTrust:Action-action.sample_trust_agent.manage_trust, managingTrust-" + z);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static boolean isTrustedAgent(Context context) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Preference.DEFAULT_ORDER)) {
            if (RemoteSpenTrustAgentService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void addToTrustedAgent(Context context) {
        Log.i(TAG, "addToTrustedAgent");
        LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
        Collection enabledTrustAgents = lockPatternUtils.getEnabledTrustAgents(UserHandle.semGetMyUserId());
        if (enabledTrustAgents == null) {
            enabledTrustAgents = new ArrayList();
        }
        ComponentName componentName = new ComponentName(context, RemoteSpenTrustAgentService.class);
        Iterator it = enabledTrustAgents.iterator();
        while (it.hasNext()) {
            if (((ComponentName) it.next()).equals(componentName)) {
                it.remove();
            }
        }
        enabledTrustAgents.add(componentName);
        lockPatternUtils.setEnabledTrustAgents(enabledTrustAgents, UserHandle.semGetMyUserId());
    }
}
