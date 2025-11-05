package com.samsung.remotespen.core.device.ble;

import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IAdvertiser;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleEnv;
import com.samsung.remotespen.core.device.ble.abstraction.IBleScanner;
import com.samsung.remotespen.core.device.ble.abstraction.IScanCallback;
import com.samsung.remotespen.core.device.ble.ext1.Ext1BleEnv;
import com.samsung.remotespen.core.device.ble.stock.StockBleEnv;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class BleEnvManager {
    private static final String TAG = "BleEnvManager";
    private static BleEnvManager sInstance;
    private final ArrayList<IBleEnv> mBleEnvList = new ArrayList<>();
    private final BleScanner mScanner = new BleScanner(this, null);
    private final StockBleEnv mStockBleEnv;

    /* loaded from: classes.dex */
    public enum BleDeviceType {
        STOCK,
        EXT1
    }

    /* loaded from: classes.dex */
    public class BleScanner implements IBleScanner {
        private BleScanner() {
        }

        public /* synthetic */ BleScanner(BleEnvManager bleEnvManager, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleScanner
        public void startScan(List<ScanFilter> list, ScanSettings scanSettings, IScanCallback iScanCallback) {
            Iterator it = BleEnvManager.this.mBleEnvList.iterator();
            while (it.hasNext()) {
                IAdvertiser advertiser = ((IBleEnv) it.next()).getAdvertiser();
                if (advertiser != null) {
                    advertiser.onStartScan(list, scanSettings, iScanCallback);
                }
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleScanner
        public void stopScan(IScanCallback iScanCallback) {
            Iterator it = BleEnvManager.this.mBleEnvList.iterator();
            while (it.hasNext()) {
                IAdvertiser advertiser = ((IBleEnv) it.next()).getAdvertiser();
                if (advertiser != null) {
                    advertiser.onStopScan(iScanCallback);
                }
            }
        }
    }

    public static synchronized BleEnvManager getInstance(Context context) {
        BleEnvManager bleEnvManager;
        synchronized (BleEnvManager.class) {
            if (sInstance == null) {
                sInstance = new BleEnvManager(context);
            }
            bleEnvManager = sInstance;
        }
        return bleEnvManager;
    }

    private BleEnvManager(Context context) {
        this.mStockBleEnv = new StockBleEnv(context);
        attachStocklBleEnv();
    }

    public synchronized IBleDevice getBluetoothDevice(Context context, String str) {
        IBleEnv bleEnv = getBleEnv(str);
        if (bleEnv == null) {
            return null;
        }
        return bleEnv.getBluetoothDevice(context, str);
    }

    public synchronized IBleScanner getBleScanner() {
        return this.mScanner;
    }

    public synchronized boolean attachStocklBleEnv() {
        if (this.mBleEnvList.contains(this.mStockBleEnv)) {
            Log.w(TAG, "attachStockBleEnv : Stock env is already attached");
            return false;
        }
        addBleEnv(this.mStockBleEnv);
        this.mStockBleEnv.onAttached();
        return true;
    }

    public synchronized boolean detachStockBleEnv() {
        if (!this.mBleEnvList.contains(this.mStockBleEnv)) {
            Log.w(TAG, "detachStockBleEnv : Stock BLE env not attached");
            return false;
        }
        this.mBleEnvList.remove(this.mStockBleEnv);
        this.mStockBleEnv.onDetached();
        return true;
    }

    public synchronized boolean attachBleDevice(Context context, BleDeviceType bleDeviceType, String str) {
        if (bleDeviceType == BleDeviceType.STOCK) {
            Log.e(TAG, "attachBleDevice : Stock env cannot be a BLE device");
            return false;
        } else if (str == null) {
            Assert.fail("BD address is missing");
            return false;
        } else {
            IBleEnv bleEnv = getBleEnv(str);
            if (bleEnv != null && bleEnv != this.mStockBleEnv) {
                String str2 = TAG;
                Log.e(str2, "attachBleDevice : already attached device : " + bleDeviceType + ", " + str);
                return false;
            }
            Ext1BleEnv ext1BleEnv = null;
            if (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$ble$BleEnvManager$BleDeviceType[bleDeviceType.ordinal()] == 1) {
                ext1BleEnv = new Ext1BleEnv(context, str);
            } else {
                String str3 = TAG;
                Log.e(str3, "attachBleDevice : unexpected device : " + bleDeviceType + ", " + str);
            }
            if (ext1BleEnv != null) {
                addBleEnv(ext1BleEnv);
                ext1BleEnv.onAttached();
                return true;
            }
            return false;
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.ble.BleEnvManager$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$ble$BleEnvManager$BleDeviceType;

        static {
            int[] iArr = new int[BleDeviceType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$ble$BleEnvManager$BleDeviceType = iArr;
            try {
                iArr[BleDeviceType.EXT1.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    public synchronized boolean detachBleDevice(String str) {
        if (str == null) {
            Log.e(TAG, "detachBleDevice : address is missing");
            return false;
        }
        IBleEnv bleEnv = getBleEnv(str);
        if (bleEnv == this.mStockBleEnv) {
            String str2 = TAG;
            Log.e(str2, "detachBleDevice : cannot detach stock BLE env : " + str);
            return false;
        } else if (bleEnv == null) {
            String str3 = TAG;
            Log.e(str3, "detachBleDevice : failed to find matched BLE env : " + str);
            return false;
        } else {
            this.mBleEnvList.remove(bleEnv);
            bleEnv.onDetached();
            return true;
        }
    }

    private synchronized boolean addBleEnv(IBleEnv iBleEnv) {
        if (this.mBleEnvList.contains(iBleEnv)) {
            Log.e(TAG, "addBleEnv : BLE env already exists");
            return false;
        } else if (this.mBleEnvList.size() == 0) {
            this.mBleEnvList.add(iBleEnv);
            return true;
        } else {
            int size = this.mBleEnvList.size() - 1;
            if (this.mBleEnvList.get(size) == this.mStockBleEnv) {
                if (iBleEnv instanceof StockBleEnv) {
                    Log.e(TAG, "addBleEnv : Stock env cannot be added more than one");
                    return false;
                }
                this.mBleEnvList.add(size, iBleEnv);
            } else {
                this.mBleEnvList.add(iBleEnv);
            }
            return true;
        }
    }

    private IBleEnv getBleEnv(String str) {
        Iterator<IBleEnv> it = this.mBleEnvList.iterator();
        while (it.hasNext()) {
            IBleEnv next = it.next();
            if (next.canHandleAddress(str)) {
                return next;
            }
        }
        return null;
    }
}
