package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IScanResult;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.device.util.operation.BleScanManager;
import com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class BleSpenAdvertisementFindOperation extends BleSpenAsyncOperation {
    private static final String TAG = "BleSpenAdvertisementFindOperation";
    private String mAddressToFind;
    private FinishListener mFinishListener;
    private boolean mIsSupportWacomCharger;
    private BleScanManager mScanManager;

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData);
    }

    public BleSpenAdvertisementFindOperation(Context context) {
        super(context, TAG);
        this.mScanManager = new BleScanManager();
    }

    public void findDeviceAdvertisement(SpenModelName spenModelName, String str, int i, FinishListener finishListener) {
        String str2 = TAG;
        Log.d(str2, "findDeviceAdvertisement : target = " + str);
        Assert.notNull(str);
        Context context = getContext();
        BleSpenDeviceFeature deviceFeature = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature();
        this.mFinishListener = finishListener;
        this.mAddressToFind = str;
        this.mIsSupportWacomCharger = deviceFeature.isSupportWacomCharger();
        if (startOperation()) {
            boolean canSpenAdvertise = SpenAdvertiseMonitor.canSpenAdvertise(context, spenModelName);
            if (this.mIsSupportWacomCharger && !canSpenAdvertise) {
                Log.e(str2, "findDeviceAdvertisement : Spen is detached");
                finishOperation(BleOpResultCode.CANCELLED);
                return;
            }
            startTimer(i);
            this.mScanManager.startScan(context, i, BleUtils.getSpenDeviceTypeUuid(this.mIsSupportWacomCharger), this.mAddressToFind, new BleScanManager.ScanListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenAdvertisementFindOperation.1
                @Override // com.samsung.remotespen.core.device.util.operation.BleScanManager.ScanListener
                public void onFinishScan() {
                }

                @Override // com.samsung.remotespen.core.device.util.operation.BleScanManager.ScanListener
                public void onScanResult(IScanResult iScanResult, ArrayList<IBleDevice> arrayList) {
                    BleSpenAdvertisementFindOperation.this.onDeviceScanned(iScanResult);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceScanned(IScanResult iScanResult) {
        String address = iScanResult.getDevice().getAddress();
        String str = TAG;
        Log.v(str, "onDeviceScanned : " + address);
        if (this.mAddressToFind.equals(address)) {
            Log.i(str, "onDeviceScanned : target device found : " + address);
            finishOperation(BleOpResultCode.SUCCESS);
        }
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onFinishOperation(BleOpResultData bleOpResultData) {
        Assert.notNull(bleOpResultData);
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        if (resultCode != BleOpResultCode.SUCCESS) {
            String str = TAG;
            Log.i(str, "onFinishOperation : resultCode = " + resultCode);
        }
        cancelTimer();
        this.mScanManager.stopScan();
        this.mAddressToFind = null;
        FinishListener finishListener = this.mFinishListener;
        if (finishListener != null) {
            finishListener.onFinish(bleOpResultData);
            this.mFinishListener = null;
        }
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onSpenInsertionEvent(boolean z) {
        if (!this.mIsSupportWacomCharger || z) {
            return;
        }
        Log.e(TAG, "onSpenInsertionEvent : Spen detached");
        finishOperation(BleOpResultCode.CANCELLED);
    }

    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAsyncOperation
    public void onTimerExpired() {
        Log.v(TAG, "onTimerExpired");
        finishOperation(BleOpResultCode.SCANNING_TIMEOUT);
    }
}
