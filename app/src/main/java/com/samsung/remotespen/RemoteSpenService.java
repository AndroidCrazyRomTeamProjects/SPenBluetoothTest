package com.samsung.remotespen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.samsung.remotespen.top.RemoteSpenMainController;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.util.CommonUtils;
import com.samsung.util.ProcessStateManager;
import com.samsung.util.features.ModelFeatures;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class RemoteSpenService extends Service {
    public static final String INTENT_KEY_STOP_SERVICE = "stop_service";
    private static RemoteSpenService sInstance;
    private RemoteSpenMainController mRemoteSpenMainController;
    private static final String TAG = RemoteSpenService.class.getSimpleName();
    private static boolean sIsReleasingMainController = false;
    private static boolean sCheckPairedSpenCount = true;
    private Handler mHandler = new Handler();
    private Runnable mRemoteSpenStarter = new Runnable() { // from class: com.samsung.remotespen.RemoteSpenService.1
        @Override // java.lang.Runnable
        public void run() {
            boolean canStartMainController = RemoteSpenMainController.canStartMainController(RemoteSpenService.this, RemoteSpenService.sCheckPairedSpenCount);
            boolean unused = RemoteSpenService.sCheckPairedSpenCount = true;
            if (!canStartMainController) {
                RemoteSpenService.this.onRequestStopService();
                return;
            }
            RemoteSpenService.this.mRemoteSpenMainController = new RemoteSpenMainController(RemoteSpenService.this, new RemoteSpenMainController.ServiceInterface() { // from class: com.samsung.remotespen.RemoteSpenService.1.1
                @Override // com.samsung.remotespen.top.RemoteSpenMainController.ServiceInterface
                public void requestStopService() {
                    RemoteSpenService.this.onRequestStopService();
                }

                @Override // com.samsung.remotespen.top.RemoteSpenMainController.ServiceInterface
                public void onReleaseFinished() {
                    boolean unused2 = RemoteSpenService.sIsReleasingMainController = false;
                    if (RemoteSpenService.sInstance != null) {
                        Log.i(RemoteSpenService.TAG, "onReleaseFinished : New service started. creates new main controller");
                        RemoteSpenService.sInstance.mHandler.post(RemoteSpenService.sInstance.mRemoteSpenStarter);
                        return;
                    }
                    Log.i(RemoteSpenService.TAG, "onReleaseFinished");
                }
            });
            RemoteSpenService.this.mRemoteSpenMainController.init();
        }
    };

    @Override // android.app.Service
    public void onCreate() {
        String str = TAG;
        Log.i(str, "onCreate");
        super.onCreate();
        sInstance = this;
        if (sIsReleasingMainController) {
            Log.i(str, "onCreate : Previous RemoteSpenMainController is not released yet.");
        } else {
            this.mHandler.post(this.mRemoteSpenStarter);
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        String str = TAG;
        Log.i(str, "onStartCommand : intent=" + intent);
        if (intent == null) {
            return 2;
        }
        super.onStartCommand(intent, i, i2);
        setForegroundProcess(true);
        if (intent.getBooleanExtra(INTENT_KEY_STOP_SERVICE, false)) {
            Log.d(str, "onStartCommand : stop service request arrived");
            onRequestStopService();
            return 2;
        }
        return 1;
    }

    private void setForegroundProcess(boolean z) {
        ProcessStateManager.getInstance().setForegroundProcess(this, "RemoteSpenService", z);
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        sInstance = null;
        sCheckPairedSpenCount = true;
        RemoteSpenMainController remoteSpenMainController = this.mRemoteSpenMainController;
        if (remoteSpenMainController != null) {
            sIsReleasingMainController = true;
            remoteSpenMainController.release();
        }
        setForegroundProcess(false);
        super.onDestroy();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind");
        return null;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override // android.app.Service
    public void onRebind(Intent intent) {
        Log.v(TAG, "onRebind");
        super.onRebind(intent);
    }

    public static boolean isServiceRunning() {
        return sInstance != null;
    }

    private static boolean canStartService(Context context, boolean z) {
        if (!ModelFeatures.canDeviceSupportBleSpen(context)) {
            Log.i(TAG, "canStartService : BLE SPen not supported");
            return false;
        } else if (CommonUtils.isKnoxContainerEnabled(context)) {
            Log.i(TAG, "canStartService : remote Spen is disabled on knox container enabled mode");
            return false;
        } else if (!SettingsPreferenceManager.getInstance(context).getAirActionEnabled()) {
            Log.i(TAG, "canStartService : remote Spen is disabled");
            return false;
        } else if (RemoteSpenMainController.canStartMainController(context, z)) {
            return true;
        } else {
            Log.i(TAG, "canStartService : Not main controller launchable condition");
            return false;
        }
    }

    public static boolean startService(Context context) {
        return startService(context, true);
    }

    public static boolean startService(Context context, boolean z) {
        if (isServiceRunning()) {
            Log.i(TAG, "startService : RemoteSpenService is already running");
            return false;
        }
        sCheckPairedSpenCount = z;
        if (canStartService(context, z)) {
            String str = TAG;
            Log.i(str, "startService : Starting RemoteSpenService checkPairedSpenCount : " + sCheckPairedSpenCount);
            Intent intent = new Intent();
            intent.setClass(context, RemoteSpenService.class);
            context.startService(intent);
            return true;
        }
        Log.w(TAG, "startService : RemoteSpenService not started");
        return false;
    }

    public static boolean isRemoteSpenMainControllerReady() {
        RemoteSpenService remoteSpenService = sInstance;
        return (remoteSpenService == null || remoteSpenService.mRemoteSpenMainController == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onRequestStopService() {
        Log.d(TAG, "onRequestStopService");
        stopSelf();
    }

    @Override // android.app.Service, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        Log.v(TAG, "onConfigurationChanged");
        CommonUtils.resetGlobalConfiguration();
        super.onConfigurationChanged(configuration);
        RemoteSpenMainController remoteSpenMainController = this.mRemoteSpenMainController;
        if (remoteSpenMainController != null) {
            remoteSpenMainController.onConfigurationChanged(configuration);
        }
    }

    @Override // android.app.Service
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Log.v(TAG, "dump");
        RemoteSpenMainController remoteSpenMainController = this.mRemoteSpenMainController;
        if (remoteSpenMainController != null) {
            remoteSpenMainController.dumpCurrentState(fileDescriptor, printWriter);
        }
    }
}
