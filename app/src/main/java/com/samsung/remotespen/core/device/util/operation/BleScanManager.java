package com.samsung.remotespen.core.device.util.operation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleScanner;
import com.samsung.remotespen.core.device.ble.abstraction.IScanCallback;
import com.samsung.remotespen.core.device.ble.abstraction.IScanResult;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.sep.SepWrapper;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class BleScanManager {
    private static final String TAG = "BleScanManager";
    private IBleScanner mBleScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanListener mScanListener;
    private boolean mIsScanning = false;
    private ArrayList<IBleDevice> mLeDevices = new ArrayList<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mScanTimeoutListener = new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleScanManager.1
        @Override // java.lang.Runnable
        public void run() {
            Log.v(BleScanManager.TAG, "scanTimeoutListener");
            if (!BleScanManager.this.isScanning()) {
                Log.e(BleScanManager.TAG, "ScanTimeoutListener : Not scanning");
            } else {
                BleScanManager.this.stopScan();
            }
        }
    };
    private IScanCallback mInternalLeScanCallback = new IScanCallback() { // from class: com.samsung.remotespen.core.device.util.operation.BleScanManager.2
        @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanCallback
        public void onScanResult(IScanResult iScanResult) {
            IBleDevice device = iScanResult.getDevice();
            String str = BleScanManager.TAG;
            Log.v(str, "onScanResult : " + device.getAddress() + " / " + device.getName() + " /  rssi=" + iScanResult.getRssi());
            if (!BleScanManager.this.isScanning()) {
                Log.e(BleScanManager.TAG, "onScanResult : Not scanning state");
                return;
            }
            if (!BleScanManager.this.mLeDevices.contains(iScanResult.getDevice())) {
                BleScanManager.this.mLeDevices.add(iScanResult.getDevice());
            }
            if (BleScanManager.this.mScanListener != null) {
                BleScanManager.this.mScanListener.onScanResult(iScanResult, BleScanManager.this.mLeDevices);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IScanCallback
        public void onScanFailed(int i) {
            String str = BleScanManager.TAG;
            Log.e(str, "onScanFailed : errorCode = " + i);
            BleScanManager.this.stopScan();
        }
    };

    /* loaded from: classes.dex */
    public interface ScanListener {
        void onFinishScan();

        void onScanResult(IScanResult iScanResult, ArrayList<IBleDevice> arrayList);
    }

    public boolean isScanning() {
        return this.mIsScanning;
    }

    public void stopScan() {
        if (!isScanning()) {
            Log.d(TAG, "stopScan : Not scanning state");
            return;
        }
        Log.i(TAG, "stopScan");
        this.mIsScanning = false;
        this.mLeDevices.clear();
        this.mHandler.removeCallbacks(this.mScanTimeoutListener);
        this.mBleScanner.stopScan(this.mInternalLeScanCallback);
        ScanListener scanListener = this.mScanListener;
        if (scanListener != null) {
            scanListener.onFinishScan();
            this.mScanListener = null;
        }
    }

    public void startScan(Context context, int i, ParcelUuid parcelUuid, BleSpenScanFilter bleSpenScanFilter, ScanListener scanListener) {
        startScan(context, i, parcelUuid, bleSpenScanFilter, null, scanListener);
    }

    public void startScan(Context context, int i, ParcelUuid parcelUuid, String str, ScanListener scanListener) {
        Assert.notNull(str);
        startScan(context, i, parcelUuid, null, str, scanListener);
    }

    private void startScan(Context context, int i, ParcelUuid parcelUuid, BleSpenScanFilter bleSpenScanFilter, String str, ScanListener scanListener) {
        if (!haveDeviceScanPermission(context)) {
            Log.e(TAG, "startScan : No permission to scan the BLE devices");
            if (scanListener != null) {
                scanListener.onFinishScan();
                return;
            }
            return;
        }
        BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(context);
        this.mBluetoothAdapter = bluetoothAdapter;
        if (bluetoothAdapter == null) {
            Log.d(TAG, "startScan : Bluetooth is not supported");
            if (scanListener != null) {
                scanListener.onFinishScan();
            }
        } else if (!SepWrapper.BluetoothAdapter.semIsBleEnabled(bluetoothAdapter)) {
            Log.e(TAG, "startScan : BLE is not enabled");
            if (scanListener != null) {
                scanListener.onFinishScan();
            }
        } else {
            IBleScanner bleScanner = BleEnvManager.getInstance(context).getBleScanner();
            this.mBleScanner = bleScanner;
            if (bleScanner == null) {
                Log.e(TAG, "startScan : failed to get BLE scanner");
                if (scanListener != null) {
                    scanListener.onFinishScan();
                    return;
                }
                return;
            }
            this.mHandler.removeCallbacks(this.mScanTimeoutListener);
            this.mHandler.postDelayed(this.mScanTimeoutListener, i);
            this.mLeDevices.clear();
            this.mIsScanning = true;
            this.mScanListener = scanListener;
            List<ScanFilter> scanFilters = getScanFilters(bleSpenScanFilter, str);
            ScanSettings scanSettings = getScanSettings(true, parcelUuid);
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("startScan scanDuration=");
            sb.append(i);
            sb.append(", scanFilter=");
            sb.append(scanFilters);
            sb.append(", scanSettings.getScanMode=");
            sb.append(scanSettings != null ? Integer.valueOf(scanSettings.getScanMode()) : scanSettings);
            sb.append(", deviceTypeUuid=");
            sb.append(parcelUuid);
            Log.i(str2, sb.toString());
            this.mBleScanner.startScan(scanFilters, scanSettings, this.mInternalLeScanCallback);
        }
    }

    private ScanSettings getScanSettings(boolean z, ParcelUuid parcelUuid) {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        if (z) {
            builder.setScanMode(100);
            builder.semSetCustomScanParams(4092, 4092);
            if (parcelUuid != null) {
                try {
                    ReflectionUtils.invokeMethod(builder, "setUuid", parcelUuid);
                } catch (Exception e) {
                    String str = TAG;
                    Log.e(str, "getScanSettings : failed to invoke setUuid(). e=" + e);
                }
            }
        }
        return builder.build();
    }

    private List<ScanFilter> getScanFilters(BleSpenScanFilter bleSpenScanFilter, String str) {
        boolean z;
        ScanFilter.Builder builder = new ScanFilter.Builder();
        boolean z2 = true;
        if (bleSpenScanFilter != null) {
            bleSpenScanFilter.applyFilter(builder);
            z = true;
        } else {
            z = false;
        }
        if (TextUtils.isEmpty(str)) {
            z2 = z;
        } else {
            builder.setDeviceAddress(str);
        }
        if (z2) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(builder.build());
            return arrayList;
        }
        return null;
    }

    private boolean haveDeviceScanPermission(Context context) {
        return context.checkSelfPermission("android.permission.PEERS_MAC_ADDRESS") == 0 || (context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0 && context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0);
    }
}
