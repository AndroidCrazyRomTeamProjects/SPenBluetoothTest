package com.samsung.remotespen.core.device;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.util.ActivitySwitchDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.debug.Assert;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class SpenEventDispatchChecker {
    private static final String TAG = "SpenEventDispatchChecker";
    private SdkStateListener mSdkStateListener;
    private ArrayList<String> mTagArray = new ArrayList<>();

    /* loaded from: classes.dex */
    public interface SdkStateListener {
        boolean isReceivesSpenEventViaSdk(String str);
    }

    public synchronized void setSdkStateListener(SdkStateListener sdkStateListener) {
        this.mSdkStateListener = sdkStateListener;
    }

    public synchronized void blockSpenEventDispatch(String str) {
        Assert.e(!TextUtils.isEmpty(str), "tag should not be empty");
        if (!this.mTagArray.contains(str)) {
            this.mTagArray.add(str);
        }
        String str2 = TAG;
        Log.i(str2, "blockSpenEventDispatch : tag=" + str + ", count=" + this.mTagArray.size());
    }

    public synchronized void unblockSpenEventDispatch(String str) {
        Assert.e(!TextUtils.isEmpty(str), "tag should not be empty");
        this.mTagArray.remove(str);
        String str2 = TAG;
        Log.i(str2, "unblockSpenEventDispatch : tag=" + str + ", count=" + this.mTagArray.size());
    }

    public synchronized boolean isForegroundAppReceivesSpenEvent(Context context) {
        if (isLcdOn(context)) {
            if (isKeyguardLocked(context)) {
                return false;
            }
            if (this.mSdkStateListener == null) {
                return false;
            }
            ComponentName topMostActivity = ActivitySwitchDetector.getInstance(context).getTopMostActivity();
            if (topMostActivity == null) {
                Log.e(TAG, "isForegroundAppReceivesSpenEvent : failed to get topmost activity");
                return false;
            }
            String packageName = topMostActivity.getPackageName();
            if (this.mSdkStateListener.isReceivesSpenEventViaSdk(packageName)) {
                String str = TAG;
                Log.i(str, "isForegroundAppReceivesSpenEvent : " + packageName + " is connected with SDK");
                return true;
            }
            return false;
        }
        return false;
    }

    public synchronized boolean isSpenEventDispatchBlocked(Context context, String str) {
        if (!isLcdOn(context)) {
            Log.d(TAG, "isSpenEventDispatchBlocked : screen off case");
            return false;
        } else if (isKeyguardLocked(context)) {
            Log.d(TAG, "isSpenEventDispatchBlocked : keyguard enable case");
            return false;
        } else {
            boolean z = true;
            if (CommonUtils.isKidsMode(context)) {
                Log.i(TAG, "isSpenEventDispatchBlocked : isKidsMode");
                return true;
            } else if (this.mSdkStateListener != null && !TextUtils.isEmpty(str) && this.mSdkStateListener.isReceivesSpenEventViaSdk(str)) {
                Log.i(TAG, "isSpenEventDispatchBlocked : " + str + " is connected with SDK");
                return true;
            } else {
                if (this.mTagArray.size() <= 0) {
                    z = false;
                }
                if (z) {
                    Log.i(TAG, "isSpenEventDispatchBlocked : blocked by tag. count= " + this.mTagArray.size() + ", [0]=" + this.mTagArray.get(0));
                    Iterator<String> it = this.mTagArray.iterator();
                    while (it.hasNext()) {
                        Log.d(TAG, "isSpenEventDispatchBlocked : tag = " + it.next());
                    }
                }
                return z;
            }
        }
    }

    private boolean isLcdOn(Context context) {
        return CommonUtils.isScreenOn(context);
    }

    private boolean isKeyguardLocked(Context context) {
        return ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked();
    }
}
