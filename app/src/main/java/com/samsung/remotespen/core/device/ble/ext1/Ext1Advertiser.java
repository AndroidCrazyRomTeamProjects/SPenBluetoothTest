package com.samsung.remotespen.core.device.ble.ext1;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IScanCallback;
import com.samsung.remotespen.ui.tutorial.AirActionTutorialConstant;
import java.util.List;

/* loaded from: classes.dex */
public class Ext1Advertiser implements IAdvertiser {
    private IBleDevice mDevice;
    private boolean mIsScanning = false;

    public Ext1Advertiser(IBleDevice iBleDevice) {
        this.mDevice = iBleDevice;
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser
    public void onStartScan(List<ScanFilter> list, ScanSettings scanSettings, final IScanCallback iScanCallback) {
        this.mIsScanning = true;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.ble.ext1.Ext1Advertiser.1
            @Override // java.lang.Runnable
            public void run() {
                if (Ext1Advertiser.this.mIsScanning) {
                    iScanCallback.onScanResult(new Ext1ScanResult(Ext1Advertiser.this.mDevice));
                }
            }
        }, AirActionTutorialConstant.DESCRIPTION_SHOW_DELAY);
    }

    @Override // com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser
    public void onStopScan(IScanCallback iScanCallback) {
        this.mIsScanning = false;
    }
}
