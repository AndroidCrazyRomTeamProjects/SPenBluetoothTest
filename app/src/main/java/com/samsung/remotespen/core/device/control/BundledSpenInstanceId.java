package com.samsung.remotespen.core.device.control;

import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.util.features.SpenModelName;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SpenInstanceIdHelper.java */
/* loaded from: classes.dex */
public class BundledSpenInstanceId extends BleSpenInstanceId {
    @Override // com.samsung.remotespen.core.device.data.BleSpenInstanceId
    public boolean isBundledSpen() {
        return true;
    }

    public BundledSpenInstanceId(SpenModelName spenModelName) {
        super(spenModelName);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BundledSpenInstanceId)) {
            return false;
        }
        return equals(this.mSpenModelName, ((BundledSpenInstanceId) obj).mSpenModelName);
    }

    public String toString() {
        return this.mSpenModelName.name();
    }

    public String toUidString() {
        return this.mSpenModelName.getPermanentName();
    }
}
