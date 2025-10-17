package com.samsung.remotespen.core.device.chargepolicy;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController;

/* loaded from: classes.dex */
public class BleSpenDummyChargeController extends BleSpenChargeController {
    private static final String TAG = "BleSpenDummyChargeController";

    public BleSpenDummyChargeController(Context context) {
        super(context);
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void start() {
        Log.v(TAG, "start");
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void stop() {
        Log.v(TAG, "stop");
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void setChargeEnableStateListener(BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener) {
        Log.v(TAG, "setChargeEnableStateListener");
    }
}
