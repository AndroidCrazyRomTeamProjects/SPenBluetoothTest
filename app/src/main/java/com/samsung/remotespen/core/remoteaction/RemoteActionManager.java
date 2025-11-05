package com.samsung.remotespen.core.remoteaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.samsung.remotespen.core.remoteaction.PackageChangeMonitor;
import com.samsung.remotespen.core.remoteaction.RemoteActionCollector;
import com.samsung.util.constants.CommonIntent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class RemoteActionManager {
    private static final String TAG = "RemoteActionManager";
    private static RemoteActionManager sInstance;
    private RemoteActionCollector.CollectCompleteListener mCollectCompleteListener;
    private PackageChangeMonitor mPackageChangeMonitor;
    private ArrayList<RemoteActionChangeListener> mRemoteActionChangeListenerTable;
    private RemoteActionCollectCompleteListener mRemoteActionCollectCompleteListener;
    private RemoteActionCollector mRemoteActionCollector;
    private RemoteActionTable mRemoteActionTable;

    /* loaded from: classes.dex */
    public interface RemoteActionChangeListener {
        void onRemoteActionChanged(String str, boolean z);

        void onRemoteActionReloaded();
    }

    /* loaded from: classes.dex */
    public interface RemoteActionCollectCompleteListener {
        void onCollectComplete();
    }

    public static synchronized RemoteActionManager getInstance(Context context) {
        RemoteActionManager remoteActionManager;
        synchronized (RemoteActionManager.class) {
            if (sInstance == null) {
                sInstance = new RemoteActionManager(context);
            }
            remoteActionManager = sInstance;
        }
        return remoteActionManager;
    }

    private RemoteActionManager(Context context) {
        this(context, context.getPackageManager());
    }

    public RemoteActionManager(Context context, PackageManager packageManager) {
        this.mRemoteActionChangeListenerTable = new ArrayList<>();
        this.mCollectCompleteListener = new RemoteActionCollector.CollectCompleteListener() { // from class: com.samsung.remotespen.core.remoteaction.RemoteActionManager.1
            @Override // com.samsung.remotespen.core.remoteaction.RemoteActionCollector.CollectCompleteListener
            public void onComplete(RemoteActionTable remoteActionTable) {
                Log.d(RemoteActionManager.TAG, "onComplete : collection finished");
                boolean z = RemoteActionManager.this.mRemoteActionTable != null;
                RemoteActionManager.this.mRemoteActionTable = remoteActionTable;
                RemoteActionManager.this.invokeCollectCompleteCallback();
                if (z) {
                    RemoteActionManager.this.invokeRemoteActionReloadedCallback();
                }
            }
        };
        Context applicationContext = context.getApplicationContext();
        this.mPackageChangeMonitor = new PackageChangeMonitor(applicationContext);
        startPackageUpdateMonitoring();
        RemoteActionCollector remoteActionCollector = new RemoteActionCollector(applicationContext, packageManager);
        this.mRemoteActionCollector = remoteActionCollector;
        remoteActionCollector.setCollectCompleteListener(this.mCollectCompleteListener);
        this.mRemoteActionCollector.collect();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED);
        applicationContext.registerReceiver(new BroadcastReceiver() { // from class: com.samsung.remotespen.core.remoteaction.RemoteActionManager.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                RemoteActionManager.this.onReceiveBroadcast(intent);
            }
        }, intentFilter);
    }

    public void registerRemoteActionChangeListener(RemoteActionChangeListener remoteActionChangeListener) {
        this.mRemoteActionChangeListenerTable.add(remoteActionChangeListener);
    }

    public void unregisterRemoteActionChangeListener(RemoteActionChangeListener remoteActionChangeListener) {
        this.mRemoteActionChangeListenerTable.remove(remoteActionChangeListener);
    }

    public void collectRemoteActions() {
        this.mRemoteActionCollector.collect();
    }

    public boolean isCollected() {
        return this.mRemoteActionTable != null;
    }

    public List<String> getPackageList() {
        if (!isCollected()) {
            Log.d(TAG, "getPackageList : remote action is not collected");
            return null;
        }
        return this.mRemoteActionTable.getPackageList();
    }

    public List<String> getActivityList(String str) {
        if (!isCollected()) {
            Log.d(TAG, "getActivityList : remote action is not collected");
            return null;
        }
        return this.mRemoteActionTable.getActivityList(str);
    }

    public RemoteAction getRemoteAction(String str, String str2) {
        if (!isCollected()) {
            Log.d(TAG, "getRemoteAction : remote action is not collected");
            return null;
        }
        return this.mRemoteActionTable.getRemoteAction(str, str2);
    }

    public Action getAction(String str, String str2, String str3) {
        if (str3 == null) {
            Log.e(TAG, "getAction : action id is null", new Exception());
            return null;
        }
        RemoteAction remoteAction = getRemoteAction(str, str2);
        if (remoteAction == null) {
            Log.e(TAG, "getAction : remote action is null", new Exception());
            return null;
        }
        int actionCount = remoteAction.getActionCount();
        for (int i = 0; i < actionCount; i++) {
            Action action = remoteAction.getAction(i);
            if (action != null && str3.equals(action.getId())) {
                return action;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPackageChangeDetected(String str, boolean z) {
        String str2 = TAG;
        Log.d(str2, "onPackageChangeDetected : " + str);
        RemoteActionTable remoteActionTable = this.mRemoteActionTable;
        if (remoteActionTable != null) {
            this.mRemoteActionCollector.updateMata(remoteActionTable, str);
            Iterator<RemoteActionChangeListener> it = this.mRemoteActionChangeListenerTable.iterator();
            while (it.hasNext()) {
                it.next().onRemoteActionChanged(str, z);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onReceiveBroadcast(Intent intent) {
        String action = intent.getAction();
        String str = TAG;
        Log.d(str, "onReceiveBroadcast : " + action);
        if (CommonIntent.INTENT_ACTION_EMERGENCY_STATE_CHANGED.equals(action)) {
            int intExtra = intent.getIntExtra(CommonIntent.INTENT_EXTRA_REASON, 0);
            Log.d(str, "onReceiveBroadcast : emergencyReason = " + intExtra);
            if (intExtra == 5) {
                this.mRemoteActionCollector.collect();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeCollectCompleteCallback() {
        RemoteActionCollectCompleteListener remoteActionCollectCompleteListener = this.mRemoteActionCollectCompleteListener;
        if (remoteActionCollectCompleteListener != null) {
            remoteActionCollectCompleteListener.onCollectComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeRemoteActionReloadedCallback() {
        Iterator<RemoteActionChangeListener> it = this.mRemoteActionChangeListenerTable.iterator();
        while (it.hasNext()) {
            it.next().onRemoteActionReloaded();
        }
    }

    private void startPackageUpdateMonitoring() {
        PackageChangeMonitor packageChangeMonitor = this.mPackageChangeMonitor;
        if (packageChangeMonitor != null) {
            packageChangeMonitor.registerChangeListener(new PackageChangeMonitor.PackageChangeListener() { // from class: com.samsung.remotespen.core.remoteaction.RemoteActionManager.3
                @Override // com.samsung.remotespen.core.remoteaction.PackageChangeMonitor.PackageChangeListener
                public void onChange(String str, int i, boolean z) {
                    RemoteActionManager.this.onPackageChangeDetected(str, z);
                }
            });
        }
    }

    private void stopPackageUpdateMonitoring() {
        PackageChangeMonitor packageChangeMonitor = this.mPackageChangeMonitor;
        if (packageChangeMonitor != null) {
            packageChangeMonitor.unregisterChangeListener();
        }
    }
}
