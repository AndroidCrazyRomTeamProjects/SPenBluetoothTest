package com.samsung.remotespen.core.fota;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.samsung.remotespen.core.device.control.BleSpenDeviceMainController;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener;
import com.samsung.remotespen.core.fota.FirmwareFileWebDownloader;
import java.io.File;

/* loaded from: classes.dex */
public class FirmwareTransferManager {
    public static final int ERROR_CAUSE_NOT_SUPPORTED = -1;
    public static final int ERROR_CAUSE_NO_UPDATE = 0;
    public static final int ERROR_CAUSE_UNKNOWN = -100;
    public static final int ERROR_TEMPORARY_ERROR = -2;
    private static final String FIRMWARE_FILE_NAME = "firmware";
    private static final String TAG = "FirmwareTransferManager";
    private Context mContext;
    private FirmwareFileWebDownloader mFirmwareFileWebDownloader;
    private Handler mHandler = new Handler();
    private BleSpenDeviceMainController mMainController;
    private StateListener mStateListener;

    /* loaded from: classes.dex */
    public interface StateListener {
        void onFinish(BleOpResultCode bleOpResultCode);

        void onPrepared();

        void onProgress(int i);
    }

    public FirmwareTransferManager(Context context, BleSpenDeviceMainController bleSpenDeviceMainController) {
        this.mContext = context;
        this.mMainController = bleSpenDeviceMainController;
        this.mFirmwareFileWebDownloader = new FirmwareFileWebDownloader(context);
    }

    public void startUpgrade(FirmwareInfo firmwareInfo, StateListener stateListener) {
        if (stateListener == null) {
            Log.e(TAG, "startUpgrade: listener is null");
            return;
        }
        Log.i(TAG, "startUpgrade");
        this.mStateListener = stateListener;
        this.mFirmwareFileWebDownloader.download(firmwareInfo, getFirmwareFile(), new FirmwareFileWebDownloader.FinishListener() { // from class: com.samsung.remotespen.core.fota.FirmwareTransferManager.1
            @Override // com.samsung.remotespen.core.fota.FirmwareFileWebDownloader.FinishListener
            public void onFinish(final boolean z) {
                String str = FirmwareTransferManager.TAG;
                Log.d(str, "onFinish : isSuccess = " + z);
                if (!z) {
                    FirmwareTransferManager.this.mStateListener.onFinish(BleOpResultCode.FIRMWARE_UPGRADE_FAIL);
                } else {
                    FirmwareTransferManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.fota.FirmwareTransferManager.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (z) {
                                FirmwareTransferManager.this.startFirmwareTransfer();
                            }
                        }
                    });
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startFirmwareTransfer() {
        this.mMainController.requestFirmwareUpgrade(getFirmwareFile(), new BleSpenFirmwareUpgradeListener() { // from class: com.samsung.remotespen.core.fota.FirmwareTransferManager.2
            @Override // com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener
            public void onPrepared() {
                FirmwareTransferManager.this.mStateListener.onPrepared();
            }

            @Override // com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener
            public void onProgress(int i) {
                FirmwareTransferManager.this.mStateListener.onProgress(i);
            }

            @Override // com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener
            public void onFinish(BleOpResultCode bleOpResultCode) {
                String str = FirmwareTransferManager.TAG;
                Log.d(str, "onFinish: result = " + bleOpResultCode);
                FirmwareTransferManager.this.mStateListener.onFinish(bleOpResultCode);
                FirmwareTransferManager.this.deleteFirmwareFile();
            }
        });
    }

    private File getFirmwareFile() {
        return new File(this.mContext.getFilesDir(), FIRMWARE_FILE_NAME);
    }

    private String toHexString(int i) {
        return "0x" + Integer.toHexString(i).toUpperCase();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteFirmwareFile() {
        File firmwareFile = getFirmwareFile();
        if (!firmwareFile.exists() || firmwareFile.delete()) {
            return;
        }
        Log.d(TAG, "deleteFirmwareFile : Failed to delete the file");
    }
}
