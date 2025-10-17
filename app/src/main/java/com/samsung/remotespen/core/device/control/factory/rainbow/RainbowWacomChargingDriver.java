package com.samsung.remotespen.core.device.control.factory.rainbow;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver;
import com.samsung.util.CommonUtils;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RainbowWacomChargingDriver extends DavinciWacomChargingDriver {
    static {
        DavinciWacomChargingDriver.TAG = RainbowWacomChargingDriver.class.getSimpleName();
    }

    public RainbowWacomChargingDriver(Context context) {
        super(context, 500L, 500L);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver, com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public void resetSpen() {
        Log.d(DavinciWacomChargingDriver.TAG, "resetSpen");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.RainbowWacomChargingDriver$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                RainbowWacomChargingDriver.this.lambda$resetSpen$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$resetSpen$0() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("3");
        CommonUtils.sleep(700L);
        writeBleSpenCommand("2");
        markEndTransaction();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver, com.samsung.remotespen.core.device.control.factory.WacomChargingDriver
    public boolean startContinuousCharge() {
        Log.d(DavinciWacomChargingDriver.TAG, "startContinuousCharge");
        ensureNotMainThread(new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.RainbowWacomChargingDriver$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                RainbowWacomChargingDriver.this.lambda$startContinuousCharge$1();
            }
        });
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startContinuousCharge$1() {
        ensureMinTransactionInterval();
        writeBleSpenCommand("3");
        CommonUtils.sleep(450L);
        writeBleSpenCommand("8");
        markEndTransaction();
    }
}
