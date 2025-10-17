package com.samsung.remotespen.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.util.StringArray;
import com.samsung.util.debug.Assert;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BleConnectionHistoryManager {
    private static final String ITEM_SEPARATOR = "\n";
    private static final int MAX_HISTORY_COUNT = 50;
    private static final String PREFERENCE_NAME = "connHistory";
    private static final String PREF_KEY_HISTORY_DATA = "connHistoryData";
    private static final String TAG = "BleConnectionHistoryManager";
    private static BleConnectionHistoryManager sInstance;
    private ConnectionInfo mCurConnInfo;
    private StringArray mPastHistory;
    private SharedPreferences mPreference;

    public static synchronized BleConnectionHistoryManager getInstance(Context context) {
        BleConnectionHistoryManager bleConnectionHistoryManager;
        synchronized (BleConnectionHistoryManager.class) {
            if (sInstance == null) {
                sInstance = new BleConnectionHistoryManager(context);
            }
            bleConnectionHistoryManager = sInstance;
        }
        return bleConnectionHistoryManager;
    }

    public StringArray getHistory() {
        StringArray stringArray = new StringArray();
        stringArray.addAll(this.mPastHistory);
        ConnectionInfo connectionInfo = this.mCurConnInfo;
        if (connectionInfo != null) {
            stringArray.add(connectionInfo.toString());
        }
        return stringArray;
    }

    public void markConnecting() {
        if (this.mCurConnInfo != null) {
            flushCurrentConnectionInfo();
        }
        if (this.mCurConnInfo == null) {
            this.mCurConnInfo = new ConnectionInfo();
        }
        this.mCurConnInfo.markConnecting();
    }

    public void markConnected(String str, String str2, String str3) {
        ConnectionInfo connectionInfo = this.mCurConnInfo;
        if (connectionInfo != null && (connectionInfo.getConnectedTime() > 0 || this.mCurConnInfo.getDisconnectedTime() > 0)) {
            flushCurrentConnectionInfo();
        }
        if (this.mCurConnInfo == null) {
            this.mCurConnInfo = new ConnectionInfo();
        }
        this.mCurConnInfo.markConnected(str, str2, str3);
    }

    public void markDisconnected(BleSpenManager.StateChangeInfo stateChangeInfo, boolean z) {
        ConnectionInfo connectionInfo = this.mCurConnInfo;
        if (connectionInfo != null && connectionInfo.getDisconnectedTime() > 0) {
            flushCurrentConnectionInfo();
        }
        if (this.mCurConnInfo == null) {
            this.mCurConnInfo = new ConnectionInfo();
        }
        this.mCurConnInfo.markDisconnected(stateChangeInfo, z);
        flushCurrentConnectionInfo();
    }

    public void setBatteryLevel(int i) {
        ConnectionInfo connectionInfo = this.mCurConnInfo;
        if (connectionInfo != null) {
            connectionInfo.setBatteryLevel(i);
        }
    }

    public void flushCurrentConnectionInfo() {
        if (this.mCurConnInfo != null) {
            ensurePastHistoryCountNotOverflow();
            String connectionInfo = this.mCurConnInfo.toString();
            String str = TAG;
            Log.v(str, "flushCurrentConnectionInfo : " + connectionInfo);
            this.mPastHistory.add(connectionInfo);
            this.mCurConnInfo = null;
            writeHistory();
            return;
        }
        Log.e(TAG, "flushCurrentConnectionInfo : no data to flush");
    }

    private BleConnectionHistoryManager(Context context) {
        this.mPastHistory = new StringArray();
        this.mPreference = context.getApplicationContext().getSharedPreferences(PREFERENCE_NAME, 0);
        this.mPastHistory = readPastHistory();
    }

    private StringArray readPastHistory() {
        StringArray stringArray = new StringArray();
        for (String str : this.mPreference.getString(PREF_KEY_HISTORY_DATA, Constants.packageName.NONE).split(ITEM_SEPARATOR)) {
            String trim = str.trim();
            if (!TextUtils.isEmpty(trim)) {
                stringArray.add(trim);
            }
        }
        return stringArray;
    }

    private void writeHistory() {
        long currentTimeMillis = System.currentTimeMillis();
        StringArray stringArray = this.mPastHistory;
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = stringArray.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(ITEM_SEPARATOR);
        }
        ConnectionInfo connectionInfo = this.mCurConnInfo;
        if (connectionInfo != null) {
            String connectionInfo2 = connectionInfo.toString();
            Assert.e(!TextUtils.isEmpty(connectionInfo2));
            sb.append(connectionInfo2);
            sb.append(ITEM_SEPARATOR);
        }
        String sb2 = sb.toString();
        SharedPreferences.Editor edit = this.mPreference.edit();
        edit.putString(PREF_KEY_HISTORY_DATA, sb2);
        edit.apply();
        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
        String str = TAG;
        Log.v(str, "writePastHistory : cnt=" + (stringArray.size() + 1) + ", dataLen=" + sb2.length() + " elapsed=" + currentTimeMillis2);
    }

    private void ensurePastHistoryCountNotOverflow() {
        while (this.mPastHistory.size() >= 50) {
            this.mPastHistory.remove(0);
        }
    }
}
