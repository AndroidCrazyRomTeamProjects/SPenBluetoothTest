package com.samsung.remotespen.core.device.control;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class PairingRequestAcceptor {
    private static final String TAG = "PairingRequestAcceptor";
    private Context mContext;
    private final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.PairingRequestAcceptor.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (PairingRequestAcceptor.this.onPairingRequest(intent)) {
                abortBroadcast();
            }
        }
    };
    private String mSpenAddressToAccept;

    public PairingRequestAcceptor(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void start() {
        registerPairingRequestReceiver();
    }

    public void stop() {
        unregisterPairingRequestReceiver();
    }

    public void setSpenAddressToAccept(String str) {
        String str2 = TAG;
        Log.v(str2, "setSpenAddressToAccept : " + str);
        this.mSpenAddressToAccept = str;
    }

    private void registerPairingRequestReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        intentFilter.setPriority(999);
        this.mContext.registerReceiver(this.mPairingRequestReceiver, intentFilter);
    }

    private void unregisterPairingRequestReceiver() {
        try {
            this.mContext.unregisterReceiver(this.mPairingRequestReceiver);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "unregisterPairingRequestReceiver : e=" + e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean onPairingRequest(Intent intent) {
        String action = intent.getAction();
        if (!"android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
            String str = TAG;
            Log.e(str, "onPairingRequest : unexpected action : " + action);
            return false;
        }
        BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (bluetoothDevice == null) {
            Log.e(TAG, "onPairingRequest : no device information");
            return false;
        }
        ArrayList arrayList = new ArrayList();
        BleSpenInstanceId bundledSpenInstanceId = BleSpenPairedSpenManager.getInstance(this.mContext).getBundledSpenInstanceId();
        String spenAddress = bundledSpenInstanceId != null ? SpenInstanceIdHelper.from(this.mContext, bundledSpenInstanceId).getSpenAddress() : null;
        if (spenAddress != null) {
            arrayList.add(spenAddress);
        }
        String str2 = this.mSpenAddressToAccept;
        if (str2 != null && !arrayList.contains(str2)) {
            arrayList.add(this.mSpenAddressToAccept);
        }
        if (arrayList.size() == 0) {
            Log.e(TAG, "onPairingRequest : empty acceptable BD address");
            return false;
        }
        String address = bluetoothDevice.getAddress();
        if (!arrayList.contains(address)) {
            String str3 = TAG;
            Log.e(str3, "onPairingRequest : Not my device - incoming=" + address + " acceptableTargetAddrCnt=" + arrayList.size());
            return false;
        }
        int intExtra = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", RecyclerView.UNDEFINED_DURATION);
        String str4 = TAG;
        Log.i(str4, "onPairingRequest : " + address + " type=" + intExtra);
        if (intExtra == 3) {
            bluetoothDevice.setPairingConfirmation(true);
        } else {
            Log.e(str4, "onPairingRequest : unexpected type");
        }
        return true;
    }
}
