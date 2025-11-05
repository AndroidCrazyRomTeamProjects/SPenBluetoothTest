package com.samsung.remotespen.core.device.ble.stock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser;
import com.samsung.remotespen.core.device.ble.abstraction.IScanCallback;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.HashMap;
import java.util.List;

/* loaded from: classes.dex */
public class StockBleAdvertiser implements IAdvertiser {
    private static final String TAG = "StockBleAdvertiser";
    private static HashMap<IScanCallback, ScanCallback> mCallbackHash = new HashMap<>();
    private Context mContext;

    public StockBleAdvertiser(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser
    public synchronized void onStartScan(List<ScanFilter> list, ScanSettings scanSettings, final IScanCallback iScanCallback) {
        ScanCallback scanCallback = new ScanCallback() { // from class: com.samsung.remotespen.core.device.ble.stock.StockBleAdvertiser.1
            @Override // android.bluetooth.le.ScanCallback
            public void onScanResult(int i, ScanResult scanResult) {
                iScanCallback.onScanResult(new StockScanResult(scanResult));
            }

            @Override // android.bluetooth.le.ScanCallback
            public void onScanFailed(int i) {
                iScanCallback.onScanFailed(i);
            }
        };
        mCallbackHash.put(iScanCallback, scanCallback);
        BluetoothLeScanner leScanner = getLeScanner();
        if (leScanner != null) {
            leScanner.startScan(list, scanSettings, scanCallback);
        } else {
            Log.e(TAG, "onStartScan : failed to get scanner");
        }
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser
    public synchronized void onStopScan(IScanCallback iScanCallback) {
        ScanCallback scanCallback = mCallbackHash.get(iScanCallback);
        if (scanCallback != null) {
            mCallbackHash.remove(iScanCallback);
            BluetoothLeScanner leScanner = getLeScanner();
            if (leScanner != null) {
                leScanner.stopScan(scanCallback);
            } else {
                Log.e(TAG, "onStopScan : failed to get scanner");
            }
        } else {
            Log.e(TAG, "stopScan : Failed to find matched scan callback");
            Assert.fail("Failed to find matched scan callback");
        }
    }

    private BluetoothLeScanner getLeScanner() {
        BluetoothAdapter bluetoothAdapter = StockUtils.getBluetoothAdapter(this.mContext);
        if (bluetoothAdapter == null) {
            return null;
        }
        return bluetoothAdapter.getBluetoothLeScanner();
    }
}
