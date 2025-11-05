package com.samsung.remotespen.core.device.control.factory.creator;

import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasFactory;
import com.samsung.remotespen.core.device.control.factory.crown.BleSpenCrownFactory;
import com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciFactory;
import com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Factory;
import com.samsung.remotespen.core.device.control.factory.great.BleSpenGreatFactory;
import com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowFactory;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenFactoryCreator {
    private static final String TAG = "BleSpenFactoryCreator";

    public static BleSpenDeviceFactory createSpenFactory(SpenModelName spenModelName) {
        Assert.notNull(spenModelName);
        BleSpenDeviceFactory bleSpenGreatFactory = new BleSpenGreatFactory();
        if (spenModelName != null) {
            int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[spenModelName.ordinal()];
            if (i == 1) {
                bleSpenGreatFactory = new BleSpenCrownFactory();
            } else if (i == 2) {
                bleSpenGreatFactory = new BleSpenDavinciFactory();
            } else if (i == 3) {
                bleSpenGreatFactory = new BleSpenCanvasFactory();
            } else if (i == 4) {
                bleSpenGreatFactory = new BleSpenExt1Factory();
            } else if (i == 5) {
                bleSpenGreatFactory = new BleSpenRainbowFactory();
            } else {
                String str = TAG;
                Log.e(str, "create : Unsupported SPen type : " + spenModelName);
            }
        }
        String str2 = TAG;
        Log.i(str2, "create : device=" + spenModelName + " / " + bleSpenGreatFactory.getClass().getSimpleName());
        return bleSpenGreatFactory;
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.creator.BleSpenFactoryCreator$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName;

        static {
            int[] iArr = new int[SpenModelName.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName = iArr;
            try {
                iArr[SpenModelName.CROWN.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.DAVINCI.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.CANVAS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.EXT1.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$util$features$SpenModelName[SpenModelName.RAINBOW.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }
}
