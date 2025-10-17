package com.samsung.remotespen.core.device.chargepolicy;

import android.content.Context;
import com.samsung.remotespen.core.device.control.BleSpenDeviceMainController;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenChargeControllerCreator {
    private static final String TAG = "BleSpenChargeControllerCreator";

    public static BleSpenChargeController create(Context context, BleSpenDeviceMainController bleSpenDeviceMainController) {
        SpenModelName targetSpenModelName = bleSpenDeviceMainController.getTargetSpenModelName();
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[targetSpenModelName.ordinal()];
        if (i == 1 || i == 2) {
            return new BleSpenDummyChargeController(context);
        }
        if (i == 3 || i == 4 || i == 5) {
            return new BleSpenGenericChargeController(context, bleSpenDeviceMainController);
        }
        Assert.fail("Unexpected SPen model : " + targetSpenModelName);
        return null;
    }

    /* renamed from: com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeControllerCreator$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName;

        static {
            int[] iArr = new int[SpenModelName.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName = iArr;
            try {
                iArr[SpenModelName.GENERIC_BUNDLED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.CROWN.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.DAVINCI.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.CANVAS.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.RAINBOW.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }
}
