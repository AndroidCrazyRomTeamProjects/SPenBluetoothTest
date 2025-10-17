package com.samsung.remotespen.core.device;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController;
import com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController;
import com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeControllerCreator;
import com.samsung.remotespen.core.device.control.BleSpenDeviceMainController;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.control.SpenInstanceIdHelper;
import com.samsung.remotespen.core.device.control.detector.SensorActionDetectorParams;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleDisconnReqData;
import com.samsung.remotespen.core.device.data.BleDisconnTriggerCode;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenApproachState;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonId;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.core.device.data.FmmConfig;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.fota.FirmwareInfo;
import com.samsung.remotespen.core.fota.FirmwareTransferManager;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class BleSpenManager {
    private static final long CONNECTION_FAIL_COUNT_FOR_REPORT = 5;
    private static long RESET_SPEN_TIMEOUT = 90000;
    private static final String TAG = "BleSpenManager";
    private static BleSpenManager sInstance;
    private int mConnectionFailCountForReport;
    private Context mContext;
    private Handler mHandler;
    private BleSpenPairedSpenManager mPairedSpenManager;
    private SpenEventDispatchChecker mSpenEventDispatchChecker;
    private ArrayList<InstanceItem> mSpenInstanceArray = new ArrayList<>();
    private ArrayList<StateListener> mStateListenerArray = new ArrayList<>();
    private ArrayList<EventListener> mEventListenerArray = new ArrayList<>();
    private ArrayList<SensorEventListener> mSensorEventListenerArray = new ArrayList<>();
    private ArrayList<ChargeStateChangeListener> mChargeStateChangeListener = new ArrayList<>();
    private ArrayList<FirmwareUpgradeStateListener> mFirmwareUpgradeStateListenerArray = new ArrayList<>();
    private ArrayList<LedStateChangeListener> mLedStateChangeListenerArray = new ArrayList<>();
    private ArrayList<PenFrequencyChangeListener> mPenFrequencyChangeListenerArray = new ArrayList<>();
    private ArrayList<ResetStateListener> mResetStateListenerArray = new ArrayList<>();
    private boolean mIsFirmwareUpgradeInProgress = false;
    private boolean mIsManualPairingInProgress = false;
    private boolean mIsResetBundledSpenInProgress = false;
    private final Runnable rollbackResetBundledSpenInProgress = new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.1
        @Override // java.lang.Runnable
        public void run() {
            BleSpenManager.this.setResetBundledSpenInProgress(false, ResetReason.TIMEOUT);
        }
    };
    private final Runnable canceledResetBundledSpenInProgress = new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.2
        @Override // java.lang.Runnable
        public void run() {
            BleSpenManager.this.setResetBundledSpenInProgress(false, ResetReason.CANCELED);
        }
    };
    private BleSpenDeviceMainController.BleSpenStateListener mSpenStateListener = new BleSpenDeviceMainController.BleSpenStateListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4
        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onConnectionStateChanged(final BleSpenInstanceId bleSpenInstanceId, final BleSpenDeviceMainController.ConnectionState connectionState, final BleSpenDeviceMainController.ConnectionState connectionState2, final BleStateChangeInfo bleStateChangeInfo) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.1
                @Override // java.lang.Runnable
                public void run() {
                    ConnectionState convertConnectionState = BleSpenManager.this.convertConnectionState(connectionState);
                    ConnectionState convertConnectionState2 = BleSpenManager.this.convertConnectionState(connectionState2);
                    for (int size = BleSpenManager.this.mStateListenerArray.size() - 1; size >= 0; size--) {
                        ((StateListener) BleSpenManager.this.mStateListenerArray.get(size)).onConnectionStateChanged(bleSpenInstanceId, convertConnectionState, convertConnectionState2, new StateChangeInfo(bleStateChangeInfo));
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onBatteryLevelChanged(final BleSpenInstanceId bleSpenInstanceId, final int i, final int i2) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.2
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mStateListenerArray.size() - 1; size >= 0; size--) {
                        ((StateListener) BleSpenManager.this.mStateListenerArray.get(size)).onBatteryLevelChanged(bleSpenInstanceId, i, i2);
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onButtonEvent(final BleSpenInstanceId bleSpenInstanceId, final BleSpenButtonEvent bleSpenButtonEvent) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.3
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mEventListenerArray.size() - 1; size >= 0; size--) {
                        ((EventListener) BleSpenManager.this.mEventListenerArray.get(size)).onButtonEvent(bleSpenInstanceId, bleSpenButtonEvent);
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onAirGestureActionEvent(final BleSpenInstanceId bleSpenInstanceId, final BleSpenGestureEvent bleSpenGestureEvent) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.4
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mEventListenerArray.size() - 1; size >= 0; size--) {
                        ((EventListener) BleSpenManager.this.mEventListenerArray.get(size)).onAirGestureActionEvent(bleSpenInstanceId, bleSpenGestureEvent);
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent) {
            for (int size = BleSpenManager.this.mSensorEventListenerArray.size() - 1; size >= 0; size--) {
                ((SensorEventListener) BleSpenManager.this.mSensorEventListenerArray.get(size)).onSpenSensorEvent(bleSpenInstanceId, bleSpenSensorEvent);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onChargeStateChanged(final BleSpenInstanceId bleSpenInstanceId, final BleSpenChargeState bleSpenChargeState) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.5
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mChargeStateChangeListener.size() - 1; size >= 0; size--) {
                        ((ChargeStateChangeListener) BleSpenManager.this.mChargeStateChangeListener.get(size)).onChargeStateChanged(bleSpenInstanceId, bleSpenChargeState);
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onLedStateChanged(final BleSpenInstanceId bleSpenInstanceId, final BleSpenLedState bleSpenLedState) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.6
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mLedStateChangeListenerArray.size() - 1; size >= 0; size--) {
                        ((LedStateChangeListener) BleSpenManager.this.mLedStateChangeListenerArray.get(size)).onLedStateChanged(bleSpenInstanceId, bleSpenLedState);
                    }
                }
            });
        }

        @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
        public void onPenFrequencyChanged(final BleSpenInstanceId bleSpenInstanceId, final BleSpenFrequency bleSpenFrequency) {
            BleSpenManager.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.4.7
                @Override // java.lang.Runnable
                public void run() {
                    for (int size = BleSpenManager.this.mPenFrequencyChangeListenerArray.size() - 1; size >= 0; size--) {
                        ((PenFrequencyChangeListener) BleSpenManager.this.mPenFrequencyChangeListenerArray.get(size)).onPenFrequencyChanged(bleSpenInstanceId, bleSpenFrequency);
                    }
                }
            });
        }
    };

    /* loaded from: classes.dex */
    public interface ButtonAndGestureDetector {
        void enableDoubleClickDetection(BleSpenButtonId bleSpenButtonId, boolean z);

        void enableDoubleClickHoldDetection(BleSpenButtonId bleSpenButtonId, boolean z);

        SensorActionDetectorParams getDetectorParams(BleSpenButtonId bleSpenButtonId);

        void setClickIntervalSetting(int i);

        void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams);

        void setMotionStayRangeThresholdScale(float f);
    }

    /* loaded from: classes.dex */
    public interface ChargeStateChangeListener {
        void onChargeStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenChargeState bleSpenChargeState);
    }

    /* loaded from: classes.dex */
    public enum ConnectionFailCountMode {
        CONNECTION_FAIL_COUNT_RESET,
        CONNECTION_FAIL_COUNT_INCREASE
    }

    /* loaded from: classes.dex */
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /* loaded from: classes.dex */
    public interface EventListener {
        void onAirGestureActionEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenGestureEvent bleSpenGestureEvent);

        void onButtonEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenButtonEvent bleSpenButtonEvent);
    }

    /* loaded from: classes.dex */
    public interface FirmwareUpgradeStateListener {
        void onFinish(BleSpenInstanceId bleSpenInstanceId, BleOpResultCode bleOpResultCode);

        void onPrepared(BleSpenInstanceId bleSpenInstanceId);

        void onProgress(BleSpenInstanceId bleSpenInstanceId, int i);
    }

    /* loaded from: classes.dex */
    public interface LedStateChangeListener {
        void onLedStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenLedState bleSpenLedState);
    }

    /* loaded from: classes.dex */
    public interface OpFinishListener {
        void onFinish(OpResultData opResultData);
    }

    /* loaded from: classes.dex */
    public interface PenFrequencyChangeListener {
        void onPenFrequencyChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenFrequency bleSpenFrequency);
    }

    /* loaded from: classes.dex */
    public enum ReasonCode {
        GATT_SUCCESS,
        LOCAL_DISCONNECT,
        REMOTE_DISCONNECT,
        LINK_LOSS,
        UNDEFINED_GATT_STATUS,
        TEXT_RESULT,
        LMP_RESPONSE_TIMEOUT,
        UNDEFINED
    }

    /* loaded from: classes.dex */
    public enum ResetReason {
        RESET_MENU,
        RETRY_RESET,
        CONNECT_BUTTON,
        INSERT_SPEN,
        MANAGER_INIT,
        CANCELED,
        FAILED,
        AIRACTIONS_OFF,
        SETTING_DESTROY,
        CONNECTED,
        TIMEOUT
    }

    /* loaded from: classes.dex */
    public interface ResetStateListener {
        void onResetStateChanged(boolean z, ResetReason resetReason);
    }

    /* loaded from: classes.dex */
    public interface SensorEventListener {
        void onSpenSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent);
    }

    /* loaded from: classes.dex */
    public interface StateListener {
        void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2);

        void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, ConnectionState connectionState, ConnectionState connectionState2, StateChangeInfo stateChangeInfo);
    }

    public int getRegisterDate() {
        return 0;
    }

    public static /* synthetic */ int access$108(BleSpenManager bleSpenManager) {
        int i = bleSpenManager.mConnectionFailCountForReport;
        bleSpenManager.mConnectionFailCountForReport = i + 1;
        return i;
    }

    /* loaded from: classes.dex */
    public static class OpResultData {
        private FmmConfig mFmmConfig;
        private BleSpenLedState mLedState;
        private BleSpenFrequency mPenFrequency;
        private byte[] mPenLog;
        private Integer mRawBattery;
        private ResultCode mResultCode;
        private Integer mRssi;

        /* loaded from: classes.dex */
        public enum ResultCode {
            SUCCESS,
            FAIL
        }

        public OpResultData() {
        }

        public OpResultData(ResultCode resultCode) {
            setResultCode(resultCode);
        }

        public boolean isSuccess() {
            return this.mResultCode == ResultCode.SUCCESS;
        }

        public void setResultCode(ResultCode resultCode) {
            this.mResultCode = resultCode;
        }

        public ResultCode getResultCode() {
            return this.mResultCode;
        }

        public void setRawBatteryLevel(Integer num) {
            this.mRawBattery = num;
        }

        public Integer getRawBatteryLevel() {
            return this.mRawBattery;
        }

        public void setRssi(Integer num) {
            this.mRssi = num;
        }

        public Integer getRssi() {
            return this.mRssi;
        }

        public void setFmmConfig(FmmConfig fmmConfig) {
            this.mFmmConfig = fmmConfig;
        }

        public FmmConfig getFmmConfig() {
            return this.mFmmConfig;
        }

        public void setLedState(BleSpenLedState bleSpenLedState) {
            this.mLedState = bleSpenLedState;
        }

        public BleSpenLedState getLedState() {
            return this.mLedState;
        }

        public void setPenLog(byte[] bArr) {
            this.mPenLog = bArr;
        }

        public byte[] getPenLog() {
            return this.mPenLog;
        }

        public BleSpenFrequency getPenFrequency() {
            return this.mPenFrequency;
        }

        public void setPenFrequency(BleSpenFrequency bleSpenFrequency) {
            this.mPenFrequency = bleSpenFrequency;
        }
    }

    /* loaded from: classes.dex */
    public static class StateChangeInfo extends BleStateChangeInfo {
        private ReasonCode mReasonCode;

        private StateChangeInfo(BleStateChangeInfo bleStateChangeInfo) {
            this.mReasonCode = ReasonCode.UNDEFINED;
            this.mGattStatusCode = bleStateChangeInfo.getGattStatusCode();
            this.mOpResult = bleStateChangeInfo.getBleOpResultData();
            this.mConnReqData = bleStateChangeInfo.getConnReqData();
            this.mDisconnReqData = bleStateChangeInfo.getDisconnReqData();
            this.mReasonCode = gattStatusToStateChangeCode(this.mGattStatusCode);
        }

        @Override // com.samsung.remotespen.core.device.data.BleStateChangeInfo
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.mReasonCode);
            if (this.mConnReqData != null) {
                sb.append("[CONN_TRIG:");
                sb.append(this.mConnReqData.getTriggerCode());
                sb.append("]");
            }
            if (this.mOpResult != null) {
                sb.append("[RC:");
                sb.append(this.mOpResult.toString());
                sb.append("]");
            }
            if (this.mGattStatusCode != null) {
                sb.append("[GATT:");
                sb.append(this.mGattStatusCode);
                sb.append("]");
            }
            if (this.mDisconnReqData != null) {
                sb.append("[DISCONN_TRIG:");
                sb.append(this.mDisconnReqData.getTriggerCode());
                sb.append("]");
            }
            return sb.toString();
        }

        public ReasonCode getStateChangeCode() {
            return this.mReasonCode;
        }

        public String getTextResult() {
            BleOpResultData bleOpResultData = this.mOpResult;
            if (bleOpResultData != null) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                return resultCode != null ? resultCode.name() : "null";
            }
            return null;
        }

        @Override // com.samsung.remotespen.core.device.data.BleStateChangeInfo
        public BleDisconnTriggerCode getDisconnectionTriggerCode() {
            return convert(this.mReasonCode);
        }

        private static ReasonCode gattStatusToStateChangeCode(Integer num) {
            if (num == null) {
                return ReasonCode.TEXT_RESULT;
            }
            int intValue = num.intValue();
            if (intValue != 0) {
                if (intValue != 8) {
                    if (intValue != 19) {
                        if (intValue != 22) {
                            if (intValue == 34) {
                                return ReasonCode.LMP_RESPONSE_TIMEOUT;
                            }
                            return ReasonCode.UNDEFINED_GATT_STATUS;
                        }
                        return ReasonCode.LOCAL_DISCONNECT;
                    }
                    return ReasonCode.REMOTE_DISCONNECT;
                }
                return ReasonCode.LINK_LOSS;
            }
            return ReasonCode.GATT_SUCCESS;
        }

        private static BleDisconnTriggerCode convert(ReasonCode reasonCode) {
            if (reasonCode == null) {
                return BleDisconnTriggerCode.UNDEFINED;
            }
            switch (AnonymousClass29.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[reasonCode.ordinal()]) {
                case 1:
                    return BleDisconnTriggerCode.GATT_SUCCESS;
                case 2:
                    return BleDisconnTriggerCode.LOCAL_DISCONNECT;
                case 3:
                    return BleDisconnTriggerCode.REMOTE_DISCONNECT;
                case 4:
                    return BleDisconnTriggerCode.LINK_LOSS;
                case 5:
                    return BleDisconnTriggerCode.UNDEFINED_GATT_STATUS;
                case 6:
                    return BleDisconnTriggerCode.TEXT_RESULT;
                case 7:
                    return BleDisconnTriggerCode.LMP_RESPONSE_TIMEOUT;
                default:
                    return BleDisconnTriggerCode.UNDEFINED;
            }
        }
    }

    /* loaded from: classes.dex */
    public static class InstanceItem {
        public BleSpenBlindChargeController mBlindChargeController;
        public BleSpenChargeController mBundledSpenChargeController;
        public FirmwareTransferManager mFirmwareTransferManager;
        public BleSpenDeviceMainController mMainController;

        private InstanceItem() {
        }
    }

    public String getTextResetReason(ResetReason resetReason) {
        return resetReason != null ? resetReason.name() : "null";
    }

    public boolean isConnectionFailForReport() {
        String str = TAG;
        Log.i(str, "mConnectionFailCountForReport=" + this.mConnectionFailCountForReport);
        return ((long) this.mConnectionFailCountForReport) >= CONNECTION_FAIL_COUNT_FOR_REPORT;
    }

    public void setConnectionFailCountModeForReport(final ConnectionFailCountMode connectionFailCountMode, final int i) {
        this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.BleSpenManager.3
            @Override // java.lang.Runnable
            public void run() {
                String str = BleSpenManager.TAG;
                Log.d(str, "setConnectionFailCountModeForReport mode=" + connectionFailCountMode + ", delay=" + i);
                int i2 = AnonymousClass29.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionFailCountMode[connectionFailCountMode.ordinal()];
                if (i2 == 1) {
                    BleSpenManager.this.mConnectionFailCountForReport = 0;
                } else if (i2 == 2) {
                    BleSpenManager.access$108(BleSpenManager.this);
                } else {
                    throw new IllegalStateException("Unexpected value: " + connectionFailCountMode);
                }
            }
        }, i);
    }

    public boolean isResetBundledSpenInProgress() {
        return this.mIsResetBundledSpenInProgress;
    }

    public void setResetBundledSpenInProgress(int i) {
        this.mHandler.removeCallbacks(this.canceledResetBundledSpenInProgress);
        this.mHandler.postDelayed(this.canceledResetBundledSpenInProgress, i);
    }

    public void setResetBundledSpenInProgress(boolean z, ResetReason resetReason) {
        Log.i(TAG, "setResetBundledSpenInProgress : spen is resetting isInProgress=" + z + ", reason=" + getTextResetReason(resetReason));
        this.mIsResetBundledSpenInProgress = z;
        for (int size = this.mResetStateListenerArray.size() + (-1); size >= 0; size--) {
            this.mResetStateListenerArray.get(size).onResetStateChanged(z, resetReason);
        }
        if (z) {
            this.mHandler.removeCallbacks(this.rollbackResetBundledSpenInProgress);
            this.mHandler.postDelayed(this.rollbackResetBundledSpenInProgress, RESET_SPEN_TIMEOUT);
            return;
        }
        this.mHandler.removeCallbacks(this.rollbackResetBundledSpenInProgress);
        if (SettingsPreferenceManager.getInstance(this.mContext).getAirActionEnabled()) {
            startAutoConnection(false);
        }
    }

    public void registerResetStateListener(ResetStateListener resetStateListener) {
        if (this.mResetStateListenerArray.contains(resetStateListener)) {
            return;
        }
        this.mResetStateListenerArray.add(resetStateListener);
    }

    public void unregisterResetStateListener(ResetStateListener resetStateListener) {
        this.mResetStateListenerArray.remove(resetStateListener);
    }

    public static synchronized BleSpenManager getInstance(Context context) {
        BleSpenManager bleSpenManager;
        synchronized (BleSpenManager.class) {
            if (sInstance == null && ModelFeatures.canDeviceSupportBleSpen(context)) {
                sInstance = new BleSpenManager(context);
            }
            bleSpenManager = sInstance;
        }
        return bleSpenManager;
    }

    private BleSpenManager(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        BleSpenPairedSpenManager bleSpenPairedSpenManager = BleSpenPairedSpenManager.getInstance(applicationContext);
        this.mPairedSpenManager = bleSpenPairedSpenManager;
        bleSpenPairedSpenManager.registerChangeListener(new BleSpenPairedSpenManager.ChangeListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.5
            @Override // com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager.ChangeListener
            public void onAdded(BleSpenInstanceId bleSpenInstanceId) {
                String str = BleSpenManager.TAG;
                Log.d(str, "init : onAdded : " + bleSpenInstanceId);
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager.ChangeListener
            public void onRemoved(final BleSpenInstanceId bleSpenInstanceId, boolean z) {
                final InstanceItem instanceItem = BleSpenManager.this.getInstanceItem(bleSpenInstanceId);
                if (instanceItem == null) {
                    String str = BleSpenManager.TAG;
                    Log.e(str, "init : onRemoved : not registered instance : " + bleSpenInstanceId);
                    return;
                }
                BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
                bleOpResultData.setMessage("Device unpaired");
                instanceItem.mMainController.closeBleSpenDriver(bleOpResultData, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.5.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData2, long j) {
                        String spenAddress = SpenInstanceIdHelper.from(BleSpenManager.this.mContext, bleSpenInstanceId).getSpenAddress();
                        if (spenAddress != null) {
                            BleUtils.removeBond(BleSpenManager.this.mContext, spenAddress);
                        }
                        instanceItem.mMainController.unregisterStateListener(BleSpenManager.this.mSpenStateListener);
                        instanceItem.mMainController.release(null);
                        BleSpenManager.this.mSpenInstanceArray.remove(instanceItem);
                        String str2 = BleSpenManager.TAG;
                        Log.d(str2, "init : onRemoved : " + bleSpenInstanceId + ", availableCount=" + BleSpenManager.this.mSpenInstanceArray.size());
                    }
                });
            }
        });
        this.mSpenEventDispatchChecker = new SpenEventDispatchChecker();
        this.mHandler = new Handler(Looper.getMainLooper());
        Iterator<BleSpenInstanceId> it = this.mPairedSpenManager.getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            this.mSpenInstanceArray.add(createSpenInstanceItem(it.next()));
        }
        setResetBundledSpenInProgress(false, ResetReason.MANAGER_INIT);
    }

    public void registerListener(StateListener stateListener, EventListener eventListener) {
        if (stateListener != null) {
            this.mStateListenerArray.add(stateListener);
        }
        if (eventListener != null) {
            this.mEventListenerArray.add(eventListener);
        }
    }

    public void unregisterListener(StateListener stateListener, EventListener eventListener) {
        if (stateListener != null) {
            this.mStateListenerArray.remove(stateListener);
        }
        if (eventListener != null) {
            this.mEventListenerArray.remove(eventListener);
        }
    }

    public void registerSensorEventListener(SensorEventListener sensorEventListener) {
        if (sensorEventListener != null) {
            this.mSensorEventListenerArray.add(sensorEventListener);
        }
    }

    public void unregisterSensorEventListener(SensorEventListener sensorEventListener) {
        if (sensorEventListener != null) {
            this.mSensorEventListenerArray.remove(sensorEventListener);
        }
    }

    public void registerChargeStateChangeListener(ChargeStateChangeListener chargeStateChangeListener) {
        if (chargeStateChangeListener != null) {
            this.mChargeStateChangeListener.add(chargeStateChangeListener);
        }
    }

    public void unregisterChargeStateChangeListener(ChargeStateChangeListener chargeStateChangeListener) {
        if (chargeStateChangeListener != null) {
            this.mChargeStateChangeListener.remove(chargeStateChangeListener);
        }
    }

    public void registerLedStateChangeListener(LedStateChangeListener ledStateChangeListener) {
        if (ledStateChangeListener != null) {
            this.mLedStateChangeListenerArray.add(ledStateChangeListener);
        }
    }

    public void unregisterLedStateChangeListener(LedStateChangeListener ledStateChangeListener) {
        if (ledStateChangeListener != null) {
            this.mLedStateChangeListenerArray.remove(ledStateChangeListener);
        }
    }

    public void registerPenFrequencyChangeListener(PenFrequencyChangeListener penFrequencyChangeListener) {
        if (penFrequencyChangeListener != null) {
            this.mPenFrequencyChangeListenerArray.add(penFrequencyChangeListener);
        }
    }

    public void unregisterPenFrequencyChangeListener(PenFrequencyChangeListener penFrequencyChangeListener) {
        if (penFrequencyChangeListener != null) {
            this.mPenFrequencyChangeListenerArray.remove(penFrequencyChangeListener);
        }
    }

    public BleDisconnTriggerCode getDisconnTriggerCode(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getConnectionState : main controller is null");
            return null;
        }
        return mainController.getDisconnTriggerCode();
    }

    public ConnectionState getConnectionState(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getConnectionState : main controller is null");
            return ConnectionState.DISCONNECTED;
        }
        return convertConnectionState(mainController.getConnectionState());
    }

    public boolean isAnySpenConnected() {
        Iterator<InstanceItem> it = this.mSpenInstanceArray.iterator();
        while (it.hasNext()) {
            if (it.next().mMainController.getConnectionState() == BleSpenDeviceMainController.ConnectionState.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public int getBatteryLevel(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getBatteryLevel : main controller is null");
            return -1;
        }
        return mainController.getBatteryLevel();
    }

    public int getLastBatteryLevel(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getLastBatteryLevel : main controller is null");
            return -1;
        }
        return mainController.getLastBatteryLevel();
    }

    public String getPenColorCode(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getPenColorCode : main controller is null");
            return null;
        }
        return mainController.getPenColorCode();
    }

    public String getFirmwareVersion(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getFirmwareVersion : main controller is null");
            return Constants.packageName.NONE;
        }
        return mainController.getFirmwareVersion();
    }

    public BleSpenChargeState getChargeState(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getChargeState : main controller is null");
            return BleSpenChargeState.UNKNOWN;
        }
        return mainController.getChargeState();
    }

    public BleSpenOperationMode getCurrentOperationMode(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getCurrentOperationMode : main controller is null");
            return BleSpenOperationMode.DEFAULT;
        }
        return mainController.getCurrentOperationMode();
    }

    public BleSpenChargeController getBundledSpenChargeController() {
        InstanceItem instanceItem = getInstanceItem(this.mPairedSpenManager.getBundledSpenInstanceId());
        if (instanceItem == null) {
            Log.e(TAG, "getBundledSpenChargeController : no matched SPen");
            return null;
        }
        return instanceItem.mBundledSpenChargeController;
    }

    public BleSpenBlindChargeController getBundledSpenBlindChargeController() {
        InstanceItem instanceItem = getInstanceItem(this.mPairedSpenManager.getBundledSpenInstanceId());
        if (instanceItem == null) {
            Log.e(TAG, "getBundledSpenBlindChargeController : no matched SPen");
            return null;
        }
        return instanceItem.mBlindChargeController;
    }

    public BleSpenDeviceMainController getBundledSpenDeviceMainController() {
        InstanceItem instanceItem = getInstanceItem(this.mPairedSpenManager.getBundledSpenInstanceId());
        if (instanceItem == null) {
            Log.e(TAG, "getBundledSpenDeviceMainController : no matched SPen");
            return null;
        }
        return instanceItem.mMainController;
    }

    public SpenEventDispatchChecker getSpenEventDispatchChecker() {
        return this.mSpenEventDispatchChecker;
    }

    public String getConnectedDeviceAddress(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getDeviceAddress : main controller is null");
            return null;
        }
        return mainController.getConnectedDeviceAddress();
    }

    public void setSlaveLatency(BleSpenInstanceId bleSpenInstanceId, int i, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "setSlaveLatency : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.setSlaveLatency(i, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.6
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void setConnectionInterval(BleSpenInstanceId bleSpenInstanceId, BleSpenConnectionInterval bleSpenConnectionInterval, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "setConnectionInterval : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.setConnectionInterval(bleSpenConnectionInterval, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.7
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void setSpenOperationMode(BleSpenInstanceId bleSpenInstanceId, BleSpenOperationMode bleSpenOperationMode, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "setSpenOperationMode : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.setSpenOperationMode(bleSpenOperationMode, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.8
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void sendFastDischargeCommand(BleSpenInstanceId bleSpenInstanceId, int i, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "sendFastDischargeCommand : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.sendFastDischargeCommand(i, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.9
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void performCalibration(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "performCalibration : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.performCalibration(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.10
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void requestRemoteRssi(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "requestRemoteRssi : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.readRemoteRssi(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.11
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void requestRawBatteryLevel(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "requestRawBatteryLevel : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.readRawBatteryLevel(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.12
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void enableRawSensorDataNotification(BleSpenInstanceId bleSpenInstanceId, boolean z, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "enableRawSensorDataNotification : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.enableRawSensorDataNotification(z, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.13
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void enableSelfTestNotification(BleSpenInstanceId bleSpenInstanceId, boolean z, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "enableSelfTestNotification : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.enableSelfTestNotification(z, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.14
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void enablePenLogNotification(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "enablePenLogNotification : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.enablePenLogNotification(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.15
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public BleSpenApproachState getSpenApproachState(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getSpenApproachState : main controller is null");
            return BleSpenApproachState.UNKNOWN;
        }
        return mainController.getSpenApproachState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public OpResultData convertToOpResultData(BleOpResultData bleOpResultData) {
        OpResultData opResultData = new OpResultData();
        if (bleOpResultData.getResultCode() == BleOpResultCode.SUCCESS) {
            opResultData.setResultCode(OpResultData.ResultCode.SUCCESS);
            opResultData.setRawBatteryLevel(bleOpResultData.getRawBatteryLevel());
            opResultData.setRssi(bleOpResultData.getRssi(null));
            opResultData.setFmmConfig(bleOpResultData.getFmmConfig());
            opResultData.setLedState(bleOpResultData.getLedState());
            opResultData.setPenLog(bleOpResultData.getByteData());
        } else {
            opResultData.setResultCode(OpResultData.ResultCode.FAIL);
        }
        return opResultData;
    }

    public void startAutoConnection(boolean z) {
        String str = TAG;
        Log.i(str, "startAutoConnection tryConnect=" + z);
        BleSpenDeviceMainController mainController = getMainController(this.mPairedSpenManager.getBundledSpenInstanceId());
        if (mainController == null) {
            Log.e(str, "startAutoConnection : main controller is null");
        } else {
            mainController.startBleSpenAutoConnection(z);
        }
    }

    public void stopAutoConnection(boolean z, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(this.mPairedSpenManager.getBundledSpenInstanceId());
        if (mainController == null) {
            Log.e(TAG, "stopAutoConnection : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        mainController.stopBleSpenAutoConnection(z, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.16
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "stopAutoConnection : auto connection stopped. result=" + bleOpResultData.getResultCode() + ", elapsed=" + (SystemClock.elapsedRealtime() - elapsedRealtime));
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public boolean connectToSpen(BleConnReqData bleConnReqData) {
        Assert.ensureMainThread();
        SpenModelName targetSpenModelName = bleConnReqData.getTargetSpenModelName();
        Assert.notNull(targetSpenModelName);
        BleSpenInstanceId createInstanceId = SpenInstanceIdHelper.createInstanceId(this.mContext, targetSpenModelName, bleConnReqData.getTargetAddress());
        InstanceItem instanceItem = getInstanceItem(createInstanceId);
        if (instanceItem == null) {
            instanceItem = createSpenInstanceItem(createInstanceId);
            this.mSpenInstanceArray.add(instanceItem);
        }
        return instanceItem.mMainController.connectToAttachedPen(bleConnReqData);
    }

    public void disconnect(BleSpenInstanceId bleSpenInstanceId, BleDisconnReqData bleDisconnReqData, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "disconnect : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        mainController.disconnect(bleDisconnReqData, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.17
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "disconnect : disconnect result=" + bleOpResultData.getResultCode() + ", elapsed=" + (SystemClock.elapsedRealtime() - elapsedRealtime));
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void closeConnection(BleSpenInstanceId bleSpenInstanceId, BleDisconnReqData bleDisconnReqData, final OpFinishListener opFinishListener) {
        final BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "closeConnection : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        disconnect(bleSpenInstanceId, bleDisconnReqData, new OpFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.18
            @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
            public void onFinish(OpResultData opResultData2) {
                BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
                bleOpResultData.setMessage("Force connection close due to application request");
                mainController.closeBleSpenDriver(bleOpResultData, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.18.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData2, long j) {
                        String str = BleSpenManager.TAG;
                        Log.d(str, "closeConnection : close result=" + bleOpResultData2.getResultCode() + ", elapsed=" + j);
                        AnonymousClass18 anonymousClass18 = AnonymousClass18.this;
                        if (opFinishListener != null) {
                            opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData2));
                        }
                    }
                });
            }
        });
    }

    public void closeAllConnections(final OpFinishListener opFinishListener) {
        final ArrayList<BleSpenInstanceId> availableSpenInstanceIds = this.mPairedSpenManager.getAvailableSpenInstanceIds();
        OpFinishListener opFinishListener2 = new OpFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.19
            @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
            public void onFinish(OpResultData opResultData) {
                if (availableSpenInstanceIds.size() == 0) {
                    Log.i(BleSpenManager.TAG, "closeAllConnections : all connection closed");
                    OpFinishListener opFinishListener3 = opFinishListener;
                    if (opFinishListener3 != null) {
                        opFinishListener3.onFinish(new OpResultData(OpResultData.ResultCode.SUCCESS));
                        return;
                    }
                    return;
                }
                BleSpenManager.this.closeConnection((BleSpenInstanceId) availableSpenInstanceIds.remove(0), null, this);
            }
        };
        if (availableSpenInstanceIds.size() > 0) {
            closeConnection(availableSpenInstanceIds.remove(0), null, opFinishListener2);
        } else if (opFinishListener != null) {
            opFinishListener.onFinish(new OpResultData(OpResultData.ResultCode.SUCCESS));
        }
    }

    public ButtonAndGestureDetector getButtonAndGestureDetector() {
        return new ButtonAndGestureDetector() { // from class: com.samsung.remotespen.core.device.BleSpenManager.20
            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public void setClickIntervalSetting(int i) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "setClickIntervalSetting : Main controller is null!");
                } else {
                    mainController.getSensorActionDetector().setDoubleClickWaitInterval(i);
                }
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public void enableDoubleClickDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "enableDoubleClickDetection : Main controller is null!");
                } else {
                    mainController.getSensorActionDetector().enableDoubleClickDetection(bleSpenButtonId, z);
                }
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public void enableDoubleClickHoldDetection(BleSpenButtonId bleSpenButtonId, boolean z) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "enableDoubleClickHoldDetection : Main controller is null!");
                } else {
                    mainController.getSensorActionDetector().enableDoubleClickHoldDetection(bleSpenButtonId, z);
                }
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public void setMotionStayRangeThresholdScale(float f) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "setMotionStayRangeThresholdScale : Main controller is null!");
                } else {
                    mainController.getSensorActionDetector().setMotionStayRangeThresholdScale(f);
                }
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public void setDetectorParams(SensorActionDetectorParams sensorActionDetectorParams) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "setDetectorParams : Main controller is null!");
                } else {
                    mainController.getSensorActionDetector().setDetectorParams(sensorActionDetectorParams);
                }
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.ButtonAndGestureDetector
            public SensorActionDetectorParams getDetectorParams(BleSpenButtonId bleSpenButtonId) {
                BleSpenManager bleSpenManager = BleSpenManager.this;
                BleSpenDeviceMainController mainController = bleSpenManager.getMainController(bleSpenManager.mPairedSpenManager.getLastPairedSpenInstanceId());
                if (mainController == null) {
                    Log.e(BleSpenManager.TAG, "getDetectorParams : Main controller is null!");
                    return null;
                }
                return mainController.getSensorActionDetector().getDetectorParams(bleSpenButtonId);
            }
        };
    }

    public void pauseGestureDetection(List<BleSpenGestureType> list) {
        BleSpenDeviceMainController mainController = getMainController(this.mPairedSpenManager.getLastPairedSpenInstanceId());
        if (mainController == null) {
            Log.e(TAG, "pauseGestureDetection : Main controller is null!");
        } else {
            mainController.pauseGestureDetection(list);
        }
    }

    public void resumeAllGestureDetection() {
        BleSpenDeviceMainController mainController = getMainController(this.mPairedSpenManager.getLastPairedSpenInstanceId());
        if (mainController == null) {
            Log.e(TAG, "resumeAllGestureDetection : Main controller is null!");
        } else {
            mainController.resumeAllGestureDetection();
        }
    }

    public void requestGetFmmConfig(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "requestGetFmmConfig : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.requestGetFmmConfig(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.21
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "requestGetFmmConfig : result=" + bleOpResultData.getResultCode() + ", elapsed=" + j);
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void requestSetFmmConfig(FmmConfig fmmConfig, BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "requestSetFmmConfig : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.requestSetFmmConfig(fmmConfig, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.22
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "requestSetFmmConfig : result=" + bleOpResultData.getResultCode() + ", elapsed=" + j);
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void enablePenClockNotification(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "enablePenClockNotification : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.enablePenClockNotification(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.23
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "enablePenClockNotification : result=" + bleOpResultData.getResultCode() + ", elapsed=" + j);
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void getLedState(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getLedState : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.getLedState(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.24
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "getLedState : result=" + bleOpResultData.getResultCode() + ", elapsed=" + j);
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public void setLedState(BleSpenInstanceId bleSpenInstanceId, BleSpenLedState bleSpenLedState, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "setLedState : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.setLedState(bleSpenLedState, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.25
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenManager.TAG;
                Log.d(str, "setLedState : result=" + bleOpResultData.getResultCode() + ", elapsed=" + j);
                if (opFinishListener != null) {
                    opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
                }
            }
        });
    }

    public BleSpenFrequency getPenFrequency(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "getPenFrequency : main controller is null");
            return BleSpenFrequency.UNKNOWN;
        }
        return mainController.getPenFrequency();
    }

    public void readPenLog(BleSpenInstanceId bleSpenInstanceId, final OpFinishListener opFinishListener) {
        BleSpenDeviceMainController mainController = getMainController(bleSpenInstanceId);
        if (mainController == null) {
            Log.e(TAG, "readPenLog : main controller is null");
            OpResultData opResultData = new OpResultData(OpResultData.ResultCode.FAIL);
            if (opFinishListener != null) {
                opFinishListener.onFinish(opResultData);
                return;
            }
            return;
        }
        mainController.readPenLog(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.26
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                opFinishListener.onFinish(BleSpenManager.this.convertToOpResultData(bleOpResultData));
            }
        });
    }

    public void registerFirmwareUpgradeStateListener(FirmwareUpgradeStateListener firmwareUpgradeStateListener) {
        if (this.mFirmwareUpgradeStateListenerArray.contains(firmwareUpgradeStateListener)) {
            return;
        }
        this.mFirmwareUpgradeStateListenerArray.add(firmwareUpgradeStateListener);
    }

    public void unregisterFirmwareUpgradeStateListener(FirmwareUpgradeStateListener firmwareUpgradeStateListener) {
        this.mFirmwareUpgradeStateListenerArray.remove(firmwareUpgradeStateListener);
    }

    public void upgradeFirmware(BleSpenInstanceId bleSpenInstanceId, FirmwareInfo firmwareInfo) {
        final InstanceItem instanceItem;
        if (bleSpenInstanceId == null || (instanceItem = getInstanceItem(bleSpenInstanceId)) == null) {
            return;
        }
        FirmwareTransferManager firmwareTransferManager = instanceItem.mFirmwareTransferManager;
        if (firmwareTransferManager == null) {
            Log.e(TAG, "upgradeFirmware : FirmwareTransferManager is null");
            return;
        }
        this.mIsFirmwareUpgradeInProgress = true;
        firmwareTransferManager.startUpgrade(firmwareInfo, new FirmwareTransferManager.StateListener() { // from class: com.samsung.remotespen.core.device.BleSpenManager.27
            @Override // com.samsung.remotespen.core.fota.FirmwareTransferManager.StateListener
            public void onPrepared() {
                Iterator it = BleSpenManager.this.mFirmwareUpgradeStateListenerArray.iterator();
                while (it.hasNext()) {
                    ((FirmwareUpgradeStateListener) it.next()).onPrepared(instanceItem.mMainController.getSpenInstanceId());
                }
            }

            @Override // com.samsung.remotespen.core.fota.FirmwareTransferManager.StateListener
            public void onProgress(int i) {
                Iterator it = BleSpenManager.this.mFirmwareUpgradeStateListenerArray.iterator();
                while (it.hasNext()) {
                    ((FirmwareUpgradeStateListener) it.next()).onProgress(instanceItem.mMainController.getSpenInstanceId(), i);
                }
            }

            @Override // com.samsung.remotespen.core.fota.FirmwareTransferManager.StateListener
            public void onFinish(BleOpResultCode bleOpResultCode) {
                String str = BleSpenManager.TAG;
                Log.d(str, "onFinish: result = " + bleOpResultCode);
                BleSpenManager.this.mIsFirmwareUpgradeInProgress = false;
                Iterator it = BleSpenManager.this.mFirmwareUpgradeStateListenerArray.iterator();
                while (it.hasNext()) {
                    ((FirmwareUpgradeStateListener) it.next()).onFinish(instanceItem.mMainController.getSpenInstanceId(), bleOpResultCode);
                }
            }
        });
    }

    public boolean isFirmwareUpgradeInProgress() {
        return this.mIsFirmwareUpgradeInProgress;
    }

    /* renamed from: com.samsung.remotespen.core.device.BleSpenManager$29  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass29 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionFailCountMode;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState;

        static {
            int[] iArr = new int[BleSpenDeviceMainController.ConnectionState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState = iArr;
            try {
                iArr[BleSpenDeviceMainController.ConnectionState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState[BleSpenDeviceMainController.ConnectionState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState[BleSpenDeviceMainController.ConnectionState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState[BleSpenDeviceMainController.ConnectionState.BLE_OFF.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            int[] iArr2 = new int[ConnectionFailCountMode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionFailCountMode = iArr2;
            try {
                iArr2[ConnectionFailCountMode.CONNECTION_FAIL_COUNT_RESET.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ConnectionFailCountMode[ConnectionFailCountMode.CONNECTION_FAIL_COUNT_INCREASE.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            int[] iArr3 = new int[ReasonCode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode = iArr3;
            try {
                iArr3[ReasonCode.GATT_SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.LOCAL_DISCONNECT.ordinal()] = 2;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.REMOTE_DISCONNECT.ordinal()] = 3;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.LINK_LOSS.ordinal()] = 4;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.UNDEFINED_GATT_STATUS.ordinal()] = 5;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.TEXT_RESULT.ordinal()] = 6;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.LMP_RESPONSE_TIMEOUT.ordinal()] = 7;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$BleSpenManager$ReasonCode[ReasonCode.UNDEFINED.ordinal()] = 8;
            } catch (NoSuchFieldError unused14) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public ConnectionState convertConnectionState(BleSpenDeviceMainController.ConnectionState connectionState) {
        int i = AnonymousClass29.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$BleSpenDeviceMainController$ConnectionState[connectionState.ordinal()];
        if (i != 1) {
            if (i == 2) {
                return ConnectionState.CONNECTING;
            }
            return ConnectionState.DISCONNECTED;
        }
        return ConnectionState.CONNECTED;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public InstanceItem getInstanceItem(BleSpenInstanceId bleSpenInstanceId) {
        if (bleSpenInstanceId == null) {
            return null;
        }
        Iterator<InstanceItem> it = this.mSpenInstanceArray.iterator();
        while (it.hasNext()) {
            InstanceItem next = it.next();
            if (bleSpenInstanceId.equals(next.mMainController.getSpenInstanceId())) {
                return next;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public BleSpenDeviceMainController getMainController(BleSpenInstanceId bleSpenInstanceId) {
        InstanceItem instanceItem;
        if (bleSpenInstanceId == null || (instanceItem = getInstanceItem(bleSpenInstanceId)) == null) {
            return null;
        }
        return instanceItem.mMainController;
    }

    private InstanceItem createSpenInstanceItem(BleSpenInstanceId bleSpenInstanceId) {
        Assert.notNull(bleSpenInstanceId);
        InstanceItem instanceItem = new InstanceItem();
        BleSpenDeviceMainController bleSpenDeviceMainController = new BleSpenDeviceMainController();
        bleSpenDeviceMainController.initialize(this.mContext, bleSpenInstanceId);
        bleSpenDeviceMainController.registerStateListener(this.mSpenStateListener);
        BleSpenDeviceFactory bleSpenDeviceFactory = BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName());
        if (bleSpenDeviceFactory.getDeviceFeature().isSupportWacomCharger()) {
            instanceItem.mBundledSpenChargeController = BleSpenChargeControllerCreator.create(this.mContext, bleSpenDeviceMainController);
        }
        if (bleSpenDeviceFactory.getApplicationFeature().isSupportBlindCharge()) {
            instanceItem.mBlindChargeController = new BleSpenBlindChargeController(this.mContext, bleSpenDeviceMainController, new BleSpenBlindChargeController.StateProvider() { // from class: com.samsung.remotespen.core.device.BleSpenManager.28
                @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.StateProvider
                public boolean isBundledSpenDisconnected() {
                    ConnectionState connectionState;
                    BleSpenInstanceId bundledSpenInstanceId = BleSpenManager.this.mPairedSpenManager.getBundledSpenInstanceId();
                    if (bundledSpenInstanceId == null || (connectionState = BleSpenManager.this.getConnectionState(bundledSpenInstanceId)) == ConnectionState.DISCONNECTED) {
                        return true;
                    }
                    String str = BleSpenManager.TAG;
                    Log.i(str, "isBundledSpenDisconnected : not disconnected. instanceId = " + bundledSpenInstanceId + ", state : " + connectionState);
                    return false;
                }
            });
        }
        if (bleSpenDeviceFactory.getApplicationFeature().isSupportFirmwareUpgrade()) {
            instanceItem.mFirmwareTransferManager = new FirmwareTransferManager(this.mContext, bleSpenDeviceMainController);
        }
        instanceItem.mMainController = bleSpenDeviceMainController;
        return instanceItem;
    }

    public void setManualPairingInProgress(boolean z) {
        this.mIsManualPairingInProgress = z;
    }

    public boolean isManualPairingInProgress() {
        return this.mIsManualPairingInProgress;
    }

    public boolean isDisconnectByStandbyMode(BleSpenInstanceId bleSpenInstanceId) {
        return bleSpenInstanceId.isBundledSpen() && getDisconnTriggerCode(bleSpenInstanceId) == BleDisconnTriggerCode.STANDBY && getConnectionState(bleSpenInstanceId) == ConnectionState.DISCONNECTED;
    }
}
