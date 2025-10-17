package com.samsung.remotespen.external;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import com.samsung.util.debug.Assert;
import java.util.HashMap;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: RemoteSpenInternalBindingService.java */
/* loaded from: classes.dex */
public class AliveRemoteCallbackList<E extends IInterface> extends RemoteCallbackList<E> {
    private static final String TAG = RemoteSpenInternalBindingService.TAG;
    private String mListName;
    private HashMap<IBinder, String> mPackageMap = new HashMap<>();

    public AliveRemoteCallbackList(String str) {
        this.mListName = str;
    }

    @Override // android.os.RemoteCallbackList
    public void onCallbackDied(E e, Object obj) {
        super.onCallbackDied(e, obj);
        String str = TAG;
        Log.i(str, "onCallbackDied : " + this.mListName + " remote callback died. callback = " + e);
        unregister(e);
    }

    @Override // android.os.RemoteCallbackList
    public boolean register(E e) {
        Assert.fail("register : Should not invoked directly.");
        return super.register(e);
    }

    public synchronized boolean register(E e, String str) {
        boolean register;
        register = super.register(e);
        if (register) {
            this.mPackageMap.put(e.asBinder(), str);
        } else {
            String str2 = TAG;
            Log.e(str2, "register : " + this.mListName + " : callback registration failed. callback=" + e);
        }
        return register;
    }

    @Override // android.os.RemoteCallbackList
    public synchronized boolean unregister(E e) {
        boolean unregister;
        unregister = super.unregister(e);
        if (unregister) {
            this.mPackageMap.remove(e.asBinder());
        } else {
            String str = TAG;
            Log.e(str, "unregister : " + this.mListName + " : callback is not in list. callback=" + e);
        }
        return unregister;
    }

    public synchronized boolean contains(String str) {
        if (this.mPackageMap.size() == 0) {
            return false;
        }
        return this.mPackageMap.containsValue(str);
    }

    public boolean isEmpty() {
        return getRegisteredCallbackCount() == 0;
    }
}
