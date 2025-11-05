package com.samsung.remotespen.core.remoteaction;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.internal.content.PackageMonitor;

/* loaded from: classes.dex */
public class PackageChangeMonitor extends PackageMonitor {
    private static final String TAG = PackageChangeMonitor.class.getSimpleName();
    private Context mContext;
    private PackageChangeListener mPackageChangeListener;
    private Handler mHandler = new Handler();
    private boolean mRegistered = false;

    /* loaded from: classes.dex */
    public interface PackageChangeListener {
        void onChange(String str, int i, boolean z);
    }

    public PackageChangeMonitor(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerChangeListener(PackageChangeListener packageChangeListener) {
        this.mPackageChangeListener = packageChangeListener;
        register(this.mContext, (Looper) null, true);
        this.mRegistered = true;
    }

    public void unregisterChangeListener() {
        this.mPackageChangeListener = null;
        if (this.mRegistered) {
            try {
                try {
                    unregister();
                } catch (IllegalStateException unused) {
                    Log.i(TAG, "not registered yet, do nothing");
                }
            } finally {
                this.mRegistered = false;
            }
        }
    }

    public void onPackageAdded(String str, int i) {
        super.onPackageAdded(str, i);
        notifyChange(str, i, false);
    }

    public void onPackageRemoved(String str, int i) {
        super.onPackageRemoved(str, i);
        Log.d(TAG, "onPackageRemoved");
        notifyChange(str, i, true);
    }

    public void onPackageUpdateFinished(String str, int i) {
        super.onPackageUpdateFinished(str, i);
        Log.d(TAG, "onPackageUpdateFinished");
        notifyChange(str, i, false);
    }

    private void notifyChange(final String str, final int i, final boolean z) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.remoteaction.PackageChangeMonitor.1
            @Override // java.lang.Runnable
            public void run() {
                if (PackageChangeMonitor.this.mPackageChangeListener != null) {
                    PackageChangeMonitor.this.mPackageChangeListener.onChange(str, i, z);
                }
            }
        });
    }
}
