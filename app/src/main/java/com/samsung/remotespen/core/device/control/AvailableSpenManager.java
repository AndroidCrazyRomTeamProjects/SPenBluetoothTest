package com.samsung.remotespen.core.device.control;

import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenPairedSpenManager.java */
/* loaded from: classes.dex */
public class AvailableSpenManager {
    private ArrayList<SpenItem> mSpenTable = new ArrayList<>();

    /* compiled from: BleSpenPairedSpenManager.java */
    /* loaded from: classes.dex */
    public static class SpenItem {
        public BleSpenInstanceId mSpenInstanceId;
    }

    public synchronized boolean contains(BleSpenInstanceId bleSpenInstanceId) {
        if (bleSpenInstanceId == null) {
            return false;
        }
        Iterator<SpenItem> it = this.mSpenTable.iterator();
        while (it.hasNext()) {
            if (bleSpenInstanceId.equals(it.next().mSpenInstanceId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean addSpen(BleSpenInstanceId bleSpenInstanceId) {
        if (contains(bleSpenInstanceId)) {
            return false;
        }
        SpenItem spenItem = new SpenItem();
        spenItem.mSpenInstanceId = bleSpenInstanceId;
        this.mSpenTable.add(spenItem);
        return true;
    }

    public synchronized boolean removeSpen(BleSpenInstanceId bleSpenInstanceId) {
        boolean z;
        z = false;
        for (int size = this.mSpenTable.size() - 1; size >= 0; size--) {
            BleSpenInstanceId bleSpenInstanceId2 = this.mSpenTable.get(size).mSpenInstanceId;
            if (bleSpenInstanceId2 != null && bleSpenInstanceId2.equals(bleSpenInstanceId)) {
                this.mSpenTable.remove(size);
                z = true;
            }
        }
        return z;
    }

    public synchronized BundledSpenInstanceId getBundledSpenInstance() {
        Iterator<SpenItem> it = this.mSpenTable.iterator();
        while (it.hasNext()) {
            SpenItem next = it.next();
            BleSpenInstanceId bleSpenInstanceId = next.mSpenInstanceId;
            if (bleSpenInstanceId != null && bleSpenInstanceId.isBundledSpen()) {
                return (BundledSpenInstanceId) next.mSpenInstanceId;
            }
        }
        return null;
    }

    public synchronized ArrayList<BleSpenInstanceId> getAvailableSpenInstanceIds() {
        ArrayList<BleSpenInstanceId> arrayList;
        arrayList = new ArrayList<>();
        Iterator<SpenItem> it = this.mSpenTable.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().mSpenInstanceId);
        }
        return arrayList;
    }

    public synchronized BleSpenInstanceId getLastAddedSpenInstanceId() {
        if (this.mSpenTable.size() == 0) {
            return null;
        }
        ArrayList<SpenItem> arrayList = this.mSpenTable;
        return arrayList.get(arrayList.size() - 1).mSpenInstanceId;
    }
}
