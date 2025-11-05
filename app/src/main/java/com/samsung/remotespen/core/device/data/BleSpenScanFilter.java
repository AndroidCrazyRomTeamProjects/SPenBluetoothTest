package com.samsung.remotespen.core.device.data;

import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;
import com.crazyromteam.spenbletest.utils.Assert;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenScanFilter {
    private static final String TAG = "BleSpenScanFilter";
    private FilterBuilder mFilterBuilder;

    /* loaded from: classes.dex */
    public interface FilterBuilder {
        void applyFilter(ScanFilter.Builder builder);
    }

    public BleSpenScanFilter(final UUID uuid) {
        this.mFilterBuilder = new FilterBuilder() { // from class: com.samsung.remotespen.core.device.data.BleSpenScanFilter.1
            @Override // com.samsung.remotespen.core.device.data.BleSpenScanFilter.FilterBuilder
            public void applyFilter(ScanFilter.Builder builder) {
                builder.setServiceUuid(ParcelUuid.fromString(uuid.toString()));
            }
        };
    }

    public BleSpenScanFilter(FilterBuilder filterBuilder) {
        Assert.notNull(filterBuilder);
        this.mFilterBuilder = filterBuilder;
    }

    public void applyFilter(ScanFilter.Builder builder) {
        this.mFilterBuilder.applyFilter(builder);
    }
}
