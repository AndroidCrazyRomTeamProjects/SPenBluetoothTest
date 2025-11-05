package com.samsung.remotespen.core.device.data;

import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public abstract class BleSpenInstanceId {
    public final SpenModelName mSpenModelName;

    public abstract boolean isBundledSpen();

    public BleSpenInstanceId(SpenModelName spenModelName) {
        Assert.notNull(spenModelName);
        this.mSpenModelName = spenModelName;
    }

    public SpenModelName getSpenModelName() {
        return this.mSpenModelName;
    }

    public boolean equals(Object obj, Object obj2) {
        if (obj == obj2) {
            return true;
        }
        return obj != null && obj.equals(obj2);
    }
}
