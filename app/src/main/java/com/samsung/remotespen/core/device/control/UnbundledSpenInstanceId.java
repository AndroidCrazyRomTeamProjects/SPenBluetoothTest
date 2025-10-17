package com.samsung.remotespen.core.device.control;

import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: SpenInstanceIdHelper.java */
/* loaded from: classes.dex */
public class UnbundledSpenInstanceId extends BleSpenInstanceId {
    private final String mSpenAddress;

    @Override // com.samsung.remotespen.core.device.data.BleSpenInstanceId
    public boolean isBundledSpen() {
        return false;
    }

    public UnbundledSpenInstanceId(SpenModelName spenModelName, String str) {
        super(spenModelName);
        Assert.notNull(str);
        this.mSpenAddress = str;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UnbundledSpenInstanceId)) {
            return false;
        }
        UnbundledSpenInstanceId unbundledSpenInstanceId = (UnbundledSpenInstanceId) obj;
        return equals(this.mSpenModelName, unbundledSpenInstanceId.mSpenModelName) && equals(this.mSpenAddress, unbundledSpenInstanceId.mSpenAddress);
    }

    public String toString() {
        return this.mSpenModelName.name() + ", " + this.mSpenAddress;
    }

    public String getSpenAddress() {
        return this.mSpenAddress;
    }

    public String toUidString() {
        return this.mSpenModelName.getPermanentName() + "," + this.mSpenAddress;
    }
}
