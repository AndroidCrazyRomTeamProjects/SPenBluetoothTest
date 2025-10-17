package com.samsung.remotespen.external;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.samsung.android.sdk.spenremote.BindInfo;

/* loaded from: classes.dex */
public class RemoteSpenBindingService extends RemoteSpenInternalBindingService {
    public static final String TAG = RemoteSpenBindingService.class.getSimpleName();

    @Override // com.samsung.remotespen.external.RemoteSpenInternalBindingService, android.app.Service
    public IBinder onBind(Intent intent) {
        int intExtra = intent.getIntExtra(BindInfo.EXTRA_BINDER_TYPE, 0);
        String str = TAG;
        Log.i(str, "onBind : binderType = " + intExtra);
        if (intExtra == 1) {
            return this.mExternalPenBinder;
        }
        if (intExtra == 2) {
            return this.m3rdPartyBinder;
        }
        return this.mMessenger.getBinder();
    }
}
