package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.settings.SettingsValueManager;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class BleSpenConnSetupSeqHelper implements BleCancellableOperation {
    private static final String TAG = "BleSpenConnSetupSeqHelper";
    private BleSpenDriver mBleSpenDriver;
    private BleCancellableOperation.FinishListener mCancelFinishListener;
    private Context mContext;
    private BleCancellableOperation mCurWorkingOp;
    private FinishListener mFinishListener;
    private long mStartTime;
    private long mWaitDurationAfterServiceDiscovery;
    private boolean mIsRunning = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ArrayList<Task> mTaskQueue = new ArrayList<>();
    private boolean mIsCancelRequested = false;

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData, long j);
    }

    /* loaded from: classes.dex */
    public enum Task {
        SERVICE_DISCOVERY,
        ENABLE_BATTERY_NOTIFICATION,
        ENABLE_BUTTON_NOTIFICATION,
        ENABLE_CHARGE_NOTIFICATION,
        READ_FIRMWARE_VERSION,
        READ_BATTERY_LEVEL,
        READ_CHARGE_STATE,
        ENABLE_CONNECTION_INTERVAL_AUTO_CHANGE,
        WRITE_EASY_CONNECT_ID,
        ENABLE_LED_INDICATION,
        ENABLE_PEN_FREQUENCY_INDICATION,
        WRITE_OPERATION_MODE
    }

    public BleSpenConnSetupSeqHelper(Context context, BleSpenDriver bleSpenDriver) {
        if (bleSpenDriver == null) {
            Log.e(TAG, "BleSpenConnSetupSeqHelper : BleSpenDriver should not be null");
        }
        this.mContext = context;
        this.mBleSpenDriver = bleSpenDriver;
    }

    public boolean doFullConnectionSetupSequence(long j, BleSpenDeviceFeature bleSpenDeviceFeature, FinishListener finishListener) {
        if (this.mBleSpenDriver == null) {
            Log.e(TAG, "doFullConnectionSetupSequence : driver is null");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.GATT_NULL), 0L);
            return false;
        } else if (this.mIsRunning) {
            Log.e(TAG, "doFullConnectionSetupSequence : connection sequence is running");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.ALREADY_RUNNING), 0L);
            return false;
        } else {
            this.mStartTime = System.currentTimeMillis();
            this.mFinishListener = finishListener;
            this.mWaitDurationAfterServiceDiscovery = j;
            this.mIsRunning = true;
            this.mTaskQueue.clear();
            this.mTaskQueue.add(Task.SERVICE_DISCOVERY);
            this.mTaskQueue.add(Task.ENABLE_BATTERY_NOTIFICATION);
            this.mTaskQueue.add(Task.ENABLE_BUTTON_NOTIFICATION);
            this.mTaskQueue.add(Task.READ_FIRMWARE_VERSION);
            this.mTaskQueue.add(Task.READ_BATTERY_LEVEL);
            this.mTaskQueue.add(Task.ENABLE_CONNECTION_INTERVAL_AUTO_CHANGE);
            if (bleSpenDeviceFeature.isSupportSpenChargeNotification()) {
                this.mTaskQueue.add(Task.ENABLE_CHARGE_NOTIFICATION);
            }
            if (bleSpenDeviceFeature.isSupportEasyConnect()) {
                this.mTaskQueue.add(Task.WRITE_EASY_CONNECT_ID);
            }
            if (bleSpenDeviceFeature.isSupportLed()) {
                this.mTaskQueue.add(Task.ENABLE_LED_INDICATION);
            }
            if (bleSpenDeviceFeature.isSupportPenFrequencyChange()) {
                this.mTaskQueue.add(Task.ENABLE_PEN_FREQUENCY_INDICATION);
            }
            if (bleSpenDeviceFeature.isSupportChargeState()) {
                this.mTaskQueue.add(Task.READ_CHARGE_STATE);
            }
            if (bleSpenDeviceFeature.isSupportStandbyMode()) {
                this.mTaskQueue.add(Task.WRITE_OPERATION_MODE);
            }
            dispatchTaskQueue();
            return true;
        }
    }

    public boolean doReconnectionSetupSequence(FinishListener finishListener) {
        if (this.mBleSpenDriver == null) {
            Log.e(TAG, "doReconnectionSetupSequence : driver is null");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.GATT_NULL), 0L);
            return false;
        } else if (this.mIsRunning) {
            Log.e(TAG, "doReconnectionSetupSequence : connection sequence is running");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.ALREADY_RUNNING), 0L);
            return false;
        } else {
            this.mStartTime = System.currentTimeMillis();
            this.mFinishListener = finishListener;
            this.mIsRunning = true;
            this.mTaskQueue.clear();
            this.mTaskQueue.add(Task.SERVICE_DISCOVERY);
            this.mTaskQueue.add(Task.ENABLE_BATTERY_NOTIFICATION);
            this.mTaskQueue.add(Task.ENABLE_BUTTON_NOTIFICATION);
            dispatchTaskQueue();
            return true;
        }
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        String str = TAG;
        Log.d(str, "cancelOperation");
        if (!isInProgress()) {
            if (finishListener != null) {
                finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
                return;
            }
            return;
        }
        this.mIsCancelRequested = true;
        boolean z = this.mCancelFinishListener == null;
        Assert.e(z, "Cancel listener is already registered : " + this.mCancelFinishListener);
        this.mCancelFinishListener = finishListener;
        if (this.mCurWorkingOp != null) {
            Log.d(str, "cancelOperation : Cancelling " + this.mCurWorkingOp.getClass().getSimpleName());
            this.mCurWorkingOp.cancelOperation(null);
        }
    }

    private boolean isInProgress() {
        return this.mStartTime > 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchTaskQueue() {
        if (this.mTaskQueue.size() == 0) {
            Log.d(TAG, "dispatchTaskQueue : All sequence finished successfully. ");
            onFinishSpenConnectionSequence(new BleOpResultData(BleOpResultCode.SUCCESS), System.currentTimeMillis() - this.mStartTime);
        } else if (this.mIsCancelRequested) {
            String str = TAG;
            Log.e(str, "dispatchTaskQueue : Cancelled");
            long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
            BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.CANCELLED);
            bleOpResultData.setMessage("Cancelled on " + str);
            onFinishSpenConnectionSequence(bleOpResultData, currentTimeMillis);
        } else {
            final Task task = this.mTaskQueue.get(0);
            this.mTaskQueue.remove(0);
            BleSpenDriver.OperationFinishListener operationFinishListener = new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper.1
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(final BleOpResultData bleOpResultData2, final long j) {
                    String str2 = BleSpenConnSetupSeqHelper.TAG;
                    Log.d(str2, "onFinish : " + task + ", result=" + bleOpResultData2.getResultCode());
                    BleSpenConnSetupSeqHelper bleSpenConnSetupSeqHelper = BleSpenConnSetupSeqHelper.this;
                    bleSpenConnSetupSeqHelper.markOperationFinish(bleSpenConnSetupSeqHelper.mBleSpenDriver);
                    BleSpenConnSetupSeqHelper.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                            BleSpenConnSetupSeqHelper.this.onFinishOneTask(task, bleOpResultData2, j);
                        }
                    });
                }
            };
            switch (AnonymousClass3.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[task.ordinal()]) {
                case 1:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.discoverService(operationFinishListener);
                    return;
                case 2:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enableBatteryNotification(operationFinishListener);
                    return;
                case 3:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enableButtonNotification(operationFinishListener);
                    return;
                case 4:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enableChargeNotification(operationFinishListener);
                    return;
                case 5:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enableConnectionIntervalAutoChange(operationFinishListener);
                    return;
                case 6:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.readFirmwareVersion(operationFinishListener);
                    return;
                case 7:
                    if (this.mBleSpenDriver.getBatteryLevel() < 0) {
                        Log.d(TAG, "dispatchTaskQueue : need to read the battery level");
                        markOperationStart(this.mBleSpenDriver);
                        this.mBleSpenDriver.readBatteryLevel(operationFinishListener);
                        return;
                    }
                    Log.v(TAG, "dispatchTaskQueue : not need to read the battery level");
                    onFinishOneTask(task, new BleOpResultData(BleOpResultCode.SUCCESS), 0L);
                    return;
                case 8:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.readChargingState(operationFinishListener);
                    return;
                case 9:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.writeEasyConnectId(operationFinishListener);
                    return;
                case 10:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enableLedIndication(operationFinishListener);
                    return;
                case 11:
                    markOperationStart(this.mBleSpenDriver);
                    this.mBleSpenDriver.enablePenFrequencyIndication(operationFinishListener);
                    return;
                case 12:
                    markOperationStart(this.mBleSpenDriver);
                    BleSpenOperationMode bleSpenOperationMode = SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled() ^ true ? BleSpenOperationMode.STANDBY : BleSpenOperationMode.DEFAULT;
                    String str2 = TAG;
                    Log.d(str2, "dispatchTaskQueue : WRITE_OPERATION_MODE : mode = " + bleSpenOperationMode);
                    this.mBleSpenDriver.setSpenOperationMode(bleSpenOperationMode, operationFinishListener);
                    return;
                default:
                    Assert.fail("Unexpected task type - " + task);
                    return;
            }
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper$3  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass3 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task;

        static {
            int[] iArr = new int[Task.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task = iArr;
            try {
                iArr[Task.SERVICE_DISCOVERY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_BATTERY_NOTIFICATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_BUTTON_NOTIFICATION.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_CHARGE_NOTIFICATION.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_CONNECTION_INTERVAL_AUTO_CHANGE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.READ_FIRMWARE_VERSION.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.READ_BATTERY_LEVEL.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.READ_CHARGE_STATE.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.WRITE_EASY_CONNECT_ID.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_LED_INDICATION.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.ENABLE_PEN_FREQUENCY_INDICATION.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$connection$BleSpenConnSetupSeqHelper$Task[Task.WRITE_OPERATION_MODE.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishOneTask(Task task, BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishOneTask : " + task + " " + resultCode + " " + j + "ms");
        if (task == Task.ENABLE_PEN_FREQUENCY_INDICATION && resultCode == BleOpResultCode.API_CALL_FAIL) {
            Log.d(str, "onFinishOneTask : ignore fail code. connection will success.");
            resultCode = BleOpResultCode.SUCCESS;
        }
        if (resultCode == BleOpResultCode.SUCCESS) {
            Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper.2
                @Override // java.lang.Runnable
                public void run() {
                    BleSpenConnSetupSeqHelper.this.dispatchTaskQueue();
                }
            };
            long j2 = 0;
            if (task == Task.SERVICE_DISCOVERY) {
                j2 = this.mWaitDurationAfterServiceDiscovery;
                Log.d(str, "onFinishOneTask : waiting for " + j2 + "ms after service discovery");
            }
            this.mHandler.postDelayed(runnable, j2);
            return;
        }
        onFinishSpenConnectionSequence(bleOpResultData, j);
    }

    private void onFinishSpenConnectionSequence(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishSpenConnectionSequence : Finished. result = " + resultCode.name());
        long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
        this.mIsRunning = false;
        this.mIsCancelRequested = false;
        this.mTaskQueue.clear();
        this.mStartTime = 0L;
        FinishListener finishListener = this.mFinishListener;
        if (finishListener != null) {
            finishListener.onFinish(bleOpResultData, currentTimeMillis);
        }
        BleCancellableOperation.FinishListener finishListener2 = this.mCancelFinishListener;
        if (finishListener2 != null) {
            finishListener2.onFinish(bleOpResultData);
            this.mCancelFinishListener = null;
        }
    }

    private synchronized void markOperationStart(BleCancellableOperation bleCancellableOperation) {
        if (this.mCurWorkingOp != null) {
            String str = TAG;
            Log.e(str, "markOperationStart : prev op(" + this.mCurWorkingOp.getClass().getSimpleName() + ") is still working! newOp=" + bleCancellableOperation.getClass().getSimpleName(), new Exception());
            return;
        }
        this.mCurWorkingOp = bleCancellableOperation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void markOperationFinish(BleCancellableOperation bleCancellableOperation) {
        BleCancellableOperation bleCancellableOperation2 = this.mCurWorkingOp;
        if (bleCancellableOperation2 == null) {
            String str = TAG;
            Log.e(str, "markOperationFinish : cur op is null. So cannot finish the " + bleCancellableOperation.getClass().getSimpleName() + " operation", new Exception());
        } else if (bleCancellableOperation2 != bleCancellableOperation) {
            String str2 = TAG;
            Log.e(str2, "markOperationFinish : prev op(" + this.mCurWorkingOp.getClass().getSimpleName() + ") is still working! opToFinish=" + bleCancellableOperation.getClass().getSimpleName(), new Exception());
        } else {
            this.mCurWorkingOp = null;
        }
    }
}
