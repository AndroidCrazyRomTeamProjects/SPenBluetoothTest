package com.samsung.remotespen.util;

import android.content.ComponentName;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.samsung.android.lib.episode.EternalContract;
import com.samsung.util.ActivitySwitchDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.debug.Log;
import com.samsung.android.view.SemWindowManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class CameraAvailabilityMonitor {
    private static final int MSG_SEND_CAMERA_AVAILABLE_CALLBACK = 1001;
    private static final int MSG_SEND_CAMERA_UNAVAILABLE_CALLBACK = 1002;
    public static final String TAG = "CameraAvailabilityMonitor";
    private static CameraAvailabilityMonitor sInstance;
    private CameraManager mCameraManager;
    private Context mContext;
    private boolean mIsCameraPreviewOn;
    private ArrayList<CameraAvailableListener> mCameraAvailableListenerArray = new ArrayList<>();
    private Handler mHandler = new Handler() { // from class: com.samsung.remotespen.util.CameraAvailabilityMonitor.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == CameraAvailabilityMonitor.MSG_SEND_CAMERA_AVAILABLE_CALLBACK) {
                CameraAvailabilityMonitor.this.callCameraAvailableListener();
            } else if (i != CameraAvailabilityMonitor.MSG_SEND_CAMERA_UNAVAILABLE_CALLBACK) {
            } else {
                CameraAvailabilityMonitor.this.callCameraUnavailableListener();
            }
        }
    };
    private CameraManager.AvailabilityCallback mCameraAvailabilityCallback = new CameraManager.AvailabilityCallback() { // from class: com.samsung.remotespen.util.CameraAvailabilityMonitor.2
        @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
        public void onCameraAvailable(String str) {
            String str2 = CameraAvailabilityMonitor.TAG;
            Log.d(str2, "onCameraAvailable : " + str);
            CameraAvailabilityMonitor.this.onCameraAvailable();
            CameraAvailabilityMonitor.this.mIsCameraPreviewOn = false;
        }

        @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
        public void onCameraUnavailable(String str) {
            String str2 = CameraAvailabilityMonitor.TAG;
            Log.d(str2, "onCameraUnavailable : " + str);
            if (!CameraAvailabilityMonitor.this.isTopMostPackageIncludedInCameraControlBlockList()) {
                CameraAvailabilityMonitor.this.onCameraUnavailable();
                CameraAvailabilityMonitor.this.mIsCameraPreviewOn = true;
                return;
            }
            Log.d(str2, "onCameraUnavailable : isIncludedCameraControlBlockList");
        }
    };

    /* loaded from: classes.dex */
    public interface CameraAvailableListener {
        void onCameraAvailable(boolean z);
    }

    public void registerListener(CameraAvailableListener cameraAvailableListener) {
        this.mCameraAvailableListenerArray.add(cameraAvailableListener);
    }

    public void unregisterListener(CameraAvailableListener cameraAvailableListener) {
        this.mCameraAvailableListenerArray.remove(cameraAvailableListener);
    }

    public static synchronized CameraAvailabilityMonitor getInstance(Context context) {
        CameraAvailabilityMonitor cameraAvailabilityMonitor;
        synchronized (CameraAvailabilityMonitor.class) {
            if (sInstance == null) {
                sInstance = new CameraAvailabilityMonitor(context);
            }
            cameraAvailabilityMonitor = sInstance;
        }
        return cameraAvailabilityMonitor;
    }

    private CameraAvailabilityMonitor(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mCameraManager = (CameraManager) applicationContext.getSystemService("camera");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callCameraAvailableListener() {
        Iterator<CameraAvailableListener> it = this.mCameraAvailableListenerArray.iterator();
        while (it.hasNext()) {
            it.next().onCameraAvailable(true);
            Log.v(TAG, "callCameraAvailableListener");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callCameraUnavailableListener() {
        Iterator<CameraAvailableListener> it = this.mCameraAvailableListenerArray.iterator();
        while (it.hasNext()) {
            it.next().onCameraAvailable(false);
            Log.v(TAG, "callCameraUnavailableListener");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCameraAvailable() {
        if (this.mHandler.hasMessages(MSG_SEND_CAMERA_AVAILABLE_CALLBACK)) {
            this.mHandler.removeMessages(MSG_SEND_CAMERA_AVAILABLE_CALLBACK);
        }
        Message message = new Message();
        message.what = MSG_SEND_CAMERA_AVAILABLE_CALLBACK;
        this.mHandler.sendMessageDelayed(message, 100L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCameraUnavailable() {
        if (this.mHandler.hasMessages(MSG_SEND_CAMERA_UNAVAILABLE_CALLBACK)) {
            this.mHandler.removeMessages(MSG_SEND_CAMERA_UNAVAILABLE_CALLBACK);
        }
        Message message = new Message();
        message.what = MSG_SEND_CAMERA_UNAVAILABLE_CALLBACK;
        this.mHandler.sendMessageDelayed(message, 100L);
    }

    public void registerCameraAvailabilityMonitor() {
        this.mCameraManager.registerAvailabilityCallback(this.mCameraAvailabilityCallback, (Handler) null);
    }

    public void unregisterCameraAvailabilityMonitor() {
        this.mCameraManager.unregisterAvailabilityCallback(this.mCameraAvailabilityCallback);
    }

    public boolean isCameraPreviewOn() {
        String str = TAG;
        Log.d(str, "isCameraPreviewOn : mIsCameraPreviewOn is " + this.mIsCameraPreviewOn);
        return (!this.mIsCameraPreviewOn || SystemProperties.get("service.camera.sfs.running", "0").equals("1") || SystemProperties.get("service.bioface.authenticating", "0").equals("1") || isVtCallOnGoing() || isRecordingScreenWithPip()) ? false : true;
    }

    private boolean isVtCallOnGoing() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(EternalContract.DEVICE_TYPE_PHONE);
        if (telephonyManager != null) {
            try {
                return telephonyManager.semIsVideoCall();
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "isVtCallOnGoing : e=" + e + ", " + e.getCause(), e);
                return false;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isTopMostPackageIncludedInCameraControlBlockList() {
        String[] remoteSpenCameraControlExceptionPkgList = CommonUtils.getRemoteSpenCameraControlExceptionPkgList(this.mContext);
        ComponentName topMostActivity = ActivitySwitchDetector.getInstance(this.mContext).getTopMostActivity();
        if (topMostActivity == null) {
            Log.e(TAG, "isTopMostPackageIncludedInCameraControlBlockList : component is null");
            return false;
        }
        String packageName = topMostActivity.getPackageName();
        for (String str : remoteSpenCameraControlExceptionPkgList) {
            if (packageName.equals(str)) {
                Log.d(TAG, "isTopMostPackageIncludedInCameraControlBlockList : Top most package is included in camera control block list. topMostPackageName is " + packageName);
                return true;
            }
        }
        return false;
    }

    private boolean isRecordingScreenWithPip() {
        List<SemWindowManager.VisibleWindowInfo> visibleWindowInfoList = SemWindowManager.getInstance().getVisibleWindowInfoList();
        if (visibleWindowInfoList == null) {
            Log.e(TAG, "isRecordingScreenWithPip : failed to get window list");
            return false;
        }
        for (SemWindowManager.VisibleWindowInfo visibleWindowInfo : visibleWindowInfoList) {
            if ("com.samsung.android.app.smartcapture".equals(visibleWindowInfo.packageName) && "Screen_Recording_PIP_Preview".equals(visibleWindowInfo.name)) {
                Log.d(TAG, "isRecordingScreenWithPip : Screen record with PIP enabled");
                return true;
            }
        }
        return false;
    }
}
