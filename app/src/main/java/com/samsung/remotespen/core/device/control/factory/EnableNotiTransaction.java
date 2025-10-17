package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.data.BleOpResultCode;
import java.util.UUID;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class EnableNotiTransaction extends Transaction {
    private UUID mCharacteristicUuid;
    private boolean mIsEnable;
    private boolean mIsIndicationType;
    private UUID mServiceUuid;

    public EnableNotiTransaction(UUID uuid, UUID uuid2, boolean z, boolean z2) {
        String str = this.TAG;
        Log.d(str, getName() + " : enable=" + z);
        this.mServiceUuid = uuid;
        this.mCharacteristicUuid = uuid2;
        this.mIsEnable = z;
        this.mIsIndicationType = z2;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.Transaction
    public void doWork(BleSpenBleDriver bleSpenBleDriver) {
        if (bleSpenBleDriver.isDisconnected()) {
            finish(BleOpResultCode.DISCONNECTED);
        }
        if (bleSpenBleDriver.requestCharacteristicNotificationEnable(this.mServiceUuid, this.mCharacteristicUuid, this.mIsEnable, this.mIsIndicationType)) {
            return;
        }
        String str = this.TAG;
        Log.e(str, getName() + " : Failed to enable notification");
        finish(BleOpResultCode.API_CALL_FAIL);
    }
}
