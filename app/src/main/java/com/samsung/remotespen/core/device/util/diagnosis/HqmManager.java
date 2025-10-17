package com.samsung.remotespen.core.device.util.diagnosis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SemHqmManager;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.ActToolHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class HqmManager {
    private static final String ACTION_HQM_UPDATE_REQUEST = "com.sec.android.intent.action.HQM_UPDATE_REQ";
    private static final String FEATURE_ID_DAILY_REPORT = "BPCN";
    private static final String FEATURE_ID_LINK_LOSS = "BPLL";
    private static final String FEATURE_ID_USAGE_INFO = "BPBA";
    private static final String HQM_COMPONENT_ID = "TSP";
    private static final String KEY_NAME_BATTERY_END_LEVEL = "EDLV";
    private static final String KEY_NAME_BATTERY_LEVEL_ERROR = "BLVE";
    private static final String KEY_NAME_BATTERY_LEVEL_ON_LINK_LOSS = "BTLV";
    private static final String KEY_NAME_BATTERY_START_LEVEL = "STLV";
    private static final String KEY_NAME_BLUETOOTH_STATE = "BLTH";
    private static final String KEY_NAME_BUTTON_CLICK_COUNT = "BTCN";
    private static final String KEY_NAME_CHARGE_STATE = "CGST";
    private static final String KEY_NAME_CONNECTION_FAIL_COUNT = "CNTF";
    private static final String KEY_NAME_CONNECTION_SUCCESS_COUNT = "CNTS";
    private static final String KEY_NAME_COUNT_FOR_EACH_CONN_FAIL_REASON = "FRSN";
    private static final String KEY_NAME_COUNT_FOR_EACH_CONN_SUCCESS_STEP = "CSTP";
    private static final String KEY_NAME_COUNT_FOR_EACH_DISCONNECT_REASON = "DRSN";
    private static final String KEY_NAME_DISCONNECT_COUNT = "DCNT";
    private static final String KEY_NAME_DOUBLE_CLICK_COUNT = "BTDC";
    private static final String KEY_NAME_END_CONDITION = "ECDT";
    private static final String KEY_NAME_END_TIME = "EDTM";
    private static final String KEY_NAME_FIRMWARE_VERSION_ON_LINK_LOSS = "FWVR";
    private static final String KEY_NAME_LONG_CLICK_COUNT = "BTLC";
    private static final String KEY_NAME_PEN_INSERTION_STATE = "PINS";
    private static final String KEY_NAME_RECONNECTION_COUNT = "LLRC";
    private static final String KEY_NAME_SINGLE_CLICK_COUNT = "BTSC";
    private static final String KEY_NAME_START_TIME = "STTM";
    private static final String KEY_NAME_USAGE_TIME = "UTIM";
    private static final String KEY_NAME_WIFI_STATE = "WIFI";
    private static final String TAG = "HqmManager";
    private static final String VALUE_SEPARATOR = "/";
    private BleStateCallback mBleStateCallback;
    private Context mContext;
    private SemHqmManager mHqmManager;
    private ConnectionInfo mLastConnectionInfo;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    private SpenUsageInfo mSpenUsageInfo;
    private OverallState mOverallState = new OverallState();
    private BroadcastReceiver mHqmUpdateRequestReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.util.diagnosis.HqmManager.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = HqmManager.TAG;
            Log.d(str, "onReceive : " + action);
            if (HqmManager.ACTION_HQM_UPDATE_REQUEST.equals(action)) {
                HqmManager.this.onReceiveHqmUpdateRequest();
            }
        }
    };

    /* loaded from: classes.dex */
    public static class ConnectionInfo {
        public DiagnosisManager.ConnectionFailReason mFailReason;
        public boolean mIsSuccess = false;
        public int mBatteryLevel = -1;
        public String mFirmwareVersion = Constants.packageName.NONE;
    }

    /* loaded from: classes.dex */
    public static class OverallState {
        public int mAbnormalBatteryLevelChangeCount;
        public int mConnSuccessCountAfterTicToc;
        public int mConnSuccessCountBeforeTicToc;
        public int mReconnectionCountDueToLinkLoss;
        public int mTotalConnectSuccessCount;
        public int mTotalConnectionFailCount;
        public int mTotalDisconnectCount;
        public int mMaxBatteryLevelAfterPenInsert = -1;
        public HashMap<String, Integer> mConnFailReasonMap = new HashMap<>();
        public HashMap<String, Integer> mDisconnectReasonMap = new HashMap<>();
    }

    /* loaded from: classes.dex */
    public enum UsageFinishCondition {
        UNDEFINED,
        INSERT,
        DISCONNECT
    }

    /* loaded from: classes.dex */
    public class SpenUsageInfo {
        public int mDoubleClickCnt;
        public int mEndBattLevel;
        public UsageFinishCondition mFinishCondition = UsageFinishCondition.UNDEFINED;
        private long mFinishTime;
        public int mLongClickCnt;
        public int mSingleClickCnt;
        public int mStartBattLevel;
        private long mStartSystemClock;
        private long mStartTime;
        public int mUsageDuration;

        public SpenUsageInfo() {
        }

        public void startUse() {
            this.mStartTime = System.currentTimeMillis();
            this.mStartSystemClock = SystemClock.elapsedRealtime();
            this.mStartBattLevel = HqmManager.this.getBatteryLevel();
        }

        public void finishUse(UsageFinishCondition usageFinishCondition) {
            this.mFinishTime = System.currentTimeMillis();
            if (HqmManager.this.mLastConnectionInfo != null) {
                this.mEndBattLevel = HqmManager.this.mLastConnectionInfo.mBatteryLevel;
            } else {
                this.mEndBattLevel = HqmManager.this.getBatteryLevel();
            }
            this.mUsageDuration = (int) (SystemClock.elapsedRealtime() - this.mStartSystemClock);
            this.mFinishCondition = usageFinishCondition;
        }
    }

    public HqmManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        SemHqmManager semHqmManager = (SemHqmManager) applicationContext.getSystemService("HqmManagerService");
        this.mHqmManager = semHqmManager;
        if (semHqmManager == null) {
            Log.e(TAG, "HqmManager : not supports HQM");
        }
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(this.mContext);
    }

    public void startMonitoring(BleStateCallback bleStateCallback) {
        this.mBleStateCallback = bleStateCallback;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_HQM_UPDATE_REQUEST);
        this.mContext.registerReceiver(this.mHqmUpdateRequestReceiver, intentFilter);
    }

    public void stopMonitoring() {
        this.mBleStateCallback = null;
        this.mContext.unregisterReceiver(this.mHqmUpdateRequestReceiver);
    }

    public void notifyConnectionFinished(boolean z, DiagnosisManager.ConnectionFailReason connectionFailReason, boolean z2) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        this.mLastConnectionInfo = connectionInfo;
        connectionInfo.mIsSuccess = z;
        connectionInfo.mFailReason = connectionFailReason;
        if (z) {
            connectionInfo.mFirmwareVersion = getFirmwareVersion();
            this.mLastConnectionInfo.mBatteryLevel = getBatteryLevel();
            OverallState overallState = this.mOverallState;
            overallState.mTotalConnectSuccessCount++;
            if (z2) {
                overallState.mConnSuccessCountAfterTicToc++;
                return;
            } else {
                overallState.mConnSuccessCountBeforeTicToc++;
                return;
            }
        }
        OverallState overallState2 = this.mOverallState;
        overallState2.mTotalConnectionFailCount++;
        if (connectionFailReason != null) {
            increaseMapValue(overallState2.mConnFailReasonMap, connectionFailReason.name());
        }
    }

    public void notifyDisconnect(DiagnosisManager.DisconnectReason disconnectReason) {
        ConnectionInfo connectionInfo;
        OverallState overallState = this.mOverallState;
        overallState.mTotalDisconnectCount++;
        if (disconnectReason != null) {
            increaseMapValue(overallState.mDisconnectReasonMap, disconnectReason.name());
        }
        if (disconnectReason == DiagnosisManager.DisconnectReason.LINK_LOSS) {
            String str = null;
            if (this.mSpenInsertionEventDetector.isInserted() && this.mOverallState.mMaxBatteryLevelAfterPenInsert >= 100 && (connectionInfo = this.mLastConnectionInfo) != null && connectionInfo.mBatteryLevel <= 1) {
                Log.e(TAG, "notifyDisconnect : Battery discharged even pen is attached");
                str = "no charge";
            }
            sendLinkLossReport(str);
        }
        SpenUsageInfo spenUsageInfo = this.mSpenUsageInfo;
        if (spenUsageInfo != null) {
            spenUsageInfo.finishUse(UsageFinishCondition.DISCONNECT);
            sendSpenUsageReport(this.mSpenUsageInfo);
        } else {
            Log.d(TAG, "notifyDisconnect : No usage info");
        }
        clearUsageInfo();
    }

    public void notifyStartReconnectionDueToLinkLoss() {
        this.mOverallState.mReconnectionCountDueToLinkLoss++;
    }

    public void notifyBatteryLevelChanged(int i, int i2) {
        ConnectionInfo connectionInfo = this.mLastConnectionInfo;
        if (connectionInfo != null) {
            connectionInfo.mBatteryLevel = i2;
        }
        if (this.mSpenInsertionEventDetector.isInserted()) {
            if (i > i2) {
                Log.d(TAG, "notifyBatteryLevelChanged : Battery level changed abnormally. before=" + i + ", after=" + i2);
                ActToolHelper.notifyEvent(this.mContext, ActToolHelper.EVENT_SPEN_BATT_ABNCHARGE);
                if (i > 90 && i2 <= 90) {
                    this.mOverallState.mAbnormalBatteryLevelChangeCount++;
                }
            }
            OverallState overallState = this.mOverallState;
            overallState.mMaxBatteryLevelAfterPenInsert = Math.max(overallState.mMaxBatteryLevelAfterPenInsert, i2);
        }
    }

    public void notifyButtonClicked(DiagnosisManager.ButtonClickType buttonClickType) {
        if (this.mSpenUsageInfo == null) {
            return;
        }
        int i = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$util$diagnosis$DiagnosisManager$ButtonClickType[buttonClickType.ordinal()];
        if (i == 1) {
            this.mSpenUsageInfo.mSingleClickCnt++;
        } else if (i == 2) {
            this.mSpenUsageInfo.mDoubleClickCnt++;
        } else if (i != 3) {
        } else {
            this.mSpenUsageInfo.mLongClickCnt++;
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.util.diagnosis.HqmManager$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$util$diagnosis$DiagnosisManager$ButtonClickType;

        static {
            int[] iArr = new int[DiagnosisManager.ButtonClickType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$util$diagnosis$DiagnosisManager$ButtonClickType = iArr;
            try {
                iArr[DiagnosisManager.ButtonClickType.SINGLE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$util$diagnosis$DiagnosisManager$ButtonClickType[DiagnosisManager.ButtonClickType.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$util$diagnosis$DiagnosisManager$ButtonClickType[DiagnosisManager.ButtonClickType.LONG.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public void notifySpenInsertionStateChanged(boolean z) {
        if (isBleConnected()) {
            String str = TAG;
            Log.d(str, "notifySpenInsertionStateChanged : " + z);
            if (!z) {
                this.mOverallState.mMaxBatteryLevelAfterPenInsert = -1;
                SpenUsageInfo spenUsageInfo = new SpenUsageInfo();
                spenUsageInfo.startUse();
                this.mSpenUsageInfo = spenUsageInfo;
                return;
            }
            this.mOverallState.mMaxBatteryLevelAfterPenInsert = getBatteryLevel();
            SpenUsageInfo spenUsageInfo2 = this.mSpenUsageInfo;
            if (spenUsageInfo2 != null) {
                spenUsageInfo2.finishUse(UsageFinishCondition.INSERT);
                sendSpenUsageReport(this.mSpenUsageInfo);
                Log.d(str, "onSpenInsertionStateChanged : clear usage info");
                clearUsageInfo();
                return;
            }
            Log.d(str, "notifySpenInsertionStateChanged : No usage info");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onReceiveHqmUpdateRequest() {
        Log.d(TAG, "onReceiveHqmUpdateRequest");
        sendDailyReport();
    }

    private void increaseMapValue(HashMap<String, Integer> hashMap, String str) {
        hashMap.put(str, Integer.valueOf((hashMap.containsKey(str) ? hashMap.get(str).intValue() : 0) + 1));
    }

    private boolean sendToHqm(String str, HqmDataArray hqmDataArray, boolean z) {
        if (this.mHqmManager == null) {
            Log.e(TAG, "sendToHqm : HQM manager is null");
            return false;
        }
        String str2 = z ? "ph" : "sm";
        String encodeToHqmDataFormat = encodeToHqmDataFormat(hqmDataArray);
        String str3 = TAG;
        Log.i(str3, "sendToHqm : " + str + ", " + encodeToHqmDataFormat);
        boolean sendHWParamToHQM = this.mHqmManager.sendHWParamToHQM(0, HQM_COMPONENT_ID, str, str2, "0.2", "sec", Constants.packageName.NONE, encodeToHqmDataFormat, Constants.packageName.NONE);
        if (!sendHWParamToHQM) {
            Log.e(str3, "sendToHqm : failed to send HQM");
        }
        return sendHWParamToHQM;
    }

    private String encodeToHqmDataFormat(HqmDataArray hqmDataArray) {
        JSONObject jSONObject;
        JSONException e;
        try {
            jSONObject = new JSONObject();
        } catch (JSONException e2) {
            jSONObject = null;
            e = e2;
        }
        try {
            Iterator<HqmDataItem> it = hqmDataArray.iterator();
            while (it.hasNext()) {
                HqmDataItem next = it.next();
                jSONObject.put(next.mKey, next.mValue);
            }
        } catch (JSONException e3) {
            e = e3;
            Log.e(TAG, "encodeToHqmDataFormat : e=" + e, e);
            return jSONObject.toString().replace("{", Constants.packageName.NONE).replace("}", Constants.packageName.NONE).replace("\\/", VALUE_SEPARATOR);
        }
        return jSONObject.toString().replace("{", Constants.packageName.NONE).replace("}", Constants.packageName.NONE).replace("\\/", VALUE_SEPARATOR);
    }

    private void sendDailyReport() {
        if (this.mOverallState == null) {
            Log.e(TAG, "sendDailyReport : state is null");
            return;
        }
        Log.d(TAG, "sendDailyReport");
        HqmDataArray hqmDataArray = new HqmDataArray();
        OverallState overallState = this.mOverallState;
        hqmDataArray.add(new HqmDataItem(KEY_NAME_CONNECTION_SUCCESS_COUNT, overallState.mTotalConnectSuccessCount));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_COUNT_FOR_EACH_CONN_SUCCESS_STEP, overallState.mConnSuccessCountBeforeTicToc + VALUE_SEPARATOR + overallState.mConnSuccessCountAfterTicToc));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_DISCONNECT_COUNT, overallState.mTotalDisconnectCount));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_COUNT_FOR_EACH_DISCONNECT_REASON, getDisconnectReasonDataString()));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_CONNECTION_FAIL_COUNT, overallState.mTotalConnectionFailCount));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_COUNT_FOR_EACH_CONN_FAIL_REASON, getConnectionFailReasonDataString()));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_RECONNECTION_COUNT, overallState.mReconnectionCountDueToLinkLoss));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_BATTERY_LEVEL_ERROR, overallState.mAbnormalBatteryLevelChangeCount));
        sendToHqm(FEATURE_ID_DAILY_REPORT, hqmDataArray, false);
        this.mOverallState = new OverallState();
    }

    private void sendLinkLossReport(String str) {
        String str2 = TAG;
        Log.d(str2, "sendLinkLossReport : " + str);
        HqmDataArray hqmDataArray = new HqmDataArray();
        hqmDataArray.add(new HqmDataItem(KEY_NAME_PEN_INSERTION_STATE, this.mSpenInsertionEventDetector.isInserted() ? "in" : "out"));
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            String str3 = wifiManager.isWifiEnabled() ? "on" : "off";
            String str4 = isWifiConnected() ? "connect" : "disconnect";
            hqmDataArray.add(new HqmDataItem(KEY_NAME_WIFI_STATE, str3 + VALUE_SEPARATOR + str4));
        } else {
            Log.e(str2, "sendLinkLossReport : wifi mgr is null!");
        }
        BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(this.mContext);
        if (bluetoothAdapter != null) {
            String str5 = bluetoothAdapter.isEnabled() ? "on" : "off";
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            int size = bondedDevices != null ? bondedDevices.size() : 0;
            hqmDataArray.add(new HqmDataItem(KEY_NAME_BLUETOOTH_STATE, str5 + VALUE_SEPARATOR + size));
        }
        ConnectionInfo connectionInfo = this.mLastConnectionInfo;
        if (connectionInfo != null) {
            if (connectionInfo.mIsSuccess) {
                hqmDataArray.add(new HqmDataItem(KEY_NAME_FIRMWARE_VERSION_ON_LINK_LOSS, connectionInfo.mFirmwareVersion));
                hqmDataArray.add(new HqmDataItem(KEY_NAME_BATTERY_LEVEL_ON_LINK_LOSS, this.mLastConnectionInfo.mBatteryLevel));
            } else {
                Log.e(str2, "sendLinkLossReport : Unexpected connection state. connection success = false");
            }
        }
        if (str != null) {
            hqmDataArray.add(new HqmDataItem(KEY_NAME_CHARGE_STATE, str));
        }
        sendToHqm(FEATURE_ID_LINK_LOSS, hqmDataArray, true);
    }

    private void sendSpenUsageReport(SpenUsageInfo spenUsageInfo) {
        Log.d(TAG, "sendSpenUsageReport");
        HqmDataArray hqmDataArray = new HqmDataArray();
        hqmDataArray.add(new HqmDataItem(KEY_NAME_BATTERY_START_LEVEL, spenUsageInfo.mStartBattLevel));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_BATTERY_END_LEVEL, spenUsageInfo.mEndBattLevel));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_USAGE_TIME, spenUsageInfo.mUsageDuration / 1000));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_BUTTON_CLICK_COUNT, (spenUsageInfo.mSingleClickCnt + (spenUsageInfo.mDoubleClickCnt * 2)) + VALUE_SEPARATOR + spenUsageInfo.mLongClickCnt));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_START_TIME, getTimeString(spenUsageInfo.mStartTime)));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_END_TIME, getTimeString(spenUsageInfo.mFinishTime)));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_END_CONDITION, spenUsageInfo.mFinishCondition.name()));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_SINGLE_CLICK_COUNT, spenUsageInfo.mSingleClickCnt));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_DOUBLE_CLICK_COUNT, spenUsageInfo.mDoubleClickCnt));
        hqmDataArray.add(new HqmDataItem(KEY_NAME_LONG_CLICK_COUNT, spenUsageInfo.mLongClickCnt));
        sendToHqm(FEATURE_ID_USAGE_INFO, hqmDataArray, true);
    }

    private String getTimeString(long j) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(j));
    }

    private void clearUsageInfo() {
        this.mSpenUsageInfo = null;
    }

    private String getConnectionFailReasonDataString() {
        return getCountDataStringFromMap(this.mOverallState.mConnFailReasonMap, new String[]{DiagnosisManager.ConnectionFailReason.CANCELLED.name(), DiagnosisManager.ConnectionFailReason.TICTOC_FAIL.name(), DiagnosisManager.ConnectionFailReason.OUT_OF_RETRY_COUNT.name(), DiagnosisManager.ConnectionFailReason.BLE_NOT_ENABLED.name()});
    }

    private String getDisconnectReasonDataString() {
        return getCountDataStringFromMap(this.mOverallState.mDisconnectReasonMap, new String[]{DiagnosisManager.DisconnectReason.USER.name(), DiagnosisManager.DisconnectReason.AIRPLANE.name(), DiagnosisManager.DisconnectReason.UPSM.name(), DiagnosisManager.DisconnectReason.LINK_LOSS.name()});
    }

    private String getCountDataStringFromMap(HashMap<String, Integer> hashMap, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strArr.length; i++) {
            Integer num = hashMap.get(strArr[i]);
            if (num == null) {
                num = 0;
            }
            if (i > 0) {
                sb.append(VALUE_SEPARATOR);
            }
            sb.append(num);
        }
        return sb.toString();
    }

    private boolean isWifiConnected() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 1;
    }

    private boolean isBleConnected() {
        BleStateCallback bleStateCallback = this.mBleStateCallback;
        if (bleStateCallback == null) {
            return false;
        }
        return bleStateCallback.isConnected();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getBatteryLevel() {
        BleStateCallback bleStateCallback = this.mBleStateCallback;
        if (bleStateCallback == null) {
            return -1;
        }
        return bleStateCallback.getBatteryLevel();
    }

    private String getFirmwareVersion() {
        BleStateCallback bleStateCallback = this.mBleStateCallback;
        return bleStateCallback == null ? Constants.packageName.NONE : bleStateCallback.getFirmwareVersion();
    }
}
