package com.samsung.remotespen.core.device.control.factory;

import android.content.Context;
import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.factory.creator.BleSpenFactoryCreator;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.HashMap;

/* loaded from: classes.dex */
public abstract class BleSpenDeviceFactory {
    private static HashMap<SpenModelName, BleSpenDeviceFactory> mFactoryMap = new HashMap<>();

    public abstract BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder(Context context);

    public abstract BleSpenDriver createBleSpenDriver();

    public abstract PenBehaviorPolicyManager createPenBehaviorPolicyManager(Context context, BleSpenDriver bleSpenDriver, BleSpenInstanceId bleSpenInstanceId, AbsPenBehaviorPolicy.Callback callback);

    public abstract BleSpenApplicationFeature getApplicationFeature();

    public abstract BleSpenDeviceFeature getDeviceFeature();

    public abstract WacomChargingDriver getWacomChargingDriver(Context context);

    public static synchronized BleSpenDeviceFactory getInstance(SpenModelName spenModelName) {
        BleSpenDeviceFactory bleSpenDeviceFactory;
        synchronized (BleSpenDeviceFactory.class) {
            Assert.notNull(spenModelName);
            bleSpenDeviceFactory = mFactoryMap.get(spenModelName);
            if (bleSpenDeviceFactory == null) {
                bleSpenDeviceFactory = BleSpenFactoryCreator.createSpenFactory(spenModelName);
                mFactoryMap.put(spenModelName, bleSpenDeviceFactory);
            }
        }
        return bleSpenDeviceFactory;
    }
}
