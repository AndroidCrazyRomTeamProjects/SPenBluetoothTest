package com.samsung.remotespen.core.device.control.factory.canvas;

import android.content.Context;
import com.samsung.remotespen.core.device.control.factory.davinci.DavinciWacomChargingDriver;

/* loaded from: classes.dex */
public class CanvasWacomChargingDriver extends DavinciWacomChargingDriver {
    static {
        DavinciWacomChargingDriver.TAG = CanvasWacomChargingDriver.class.getSimpleName();
    }

    public CanvasWacomChargingDriver(Context context) {
        super(context, 200L, 20L);
    }
}
