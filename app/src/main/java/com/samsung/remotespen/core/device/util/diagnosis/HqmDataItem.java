package com.samsung.remotespen.core.device.util.diagnosis;

import com.samsung.aboutpage.Constants;
import com.crazyromteam.spenbletest.utils.Assert;

/* compiled from: HqmManager.java */
/* loaded from: classes.dex */
class HqmDataItem {
    public String mKey;
    public String mValue;

    public HqmDataItem(String str, String str2) {
        Assert.notNull(str);
        str2 = str2 == null ? Constants.packageName.NONE : str2;
        this.mKey = str;
        this.mValue = str2;
    }

    public HqmDataItem(String str, int i) {
        this(str, String.valueOf(i));
    }
}
