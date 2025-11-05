package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.control.connection.BleSpenAdvertisementFindOperation;
import com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.device.util.operation.BleSpenRemoveBondOperation;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.ui.tutorial.AirActionTutorialConstant;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;

/* loaded from: classes.dex */
public class BleSpenConnector implements BleCancellableOperation {
    private static final int CONNECTION_TIMEOUT_AFTER_TICTOC = 20000;
    private static final int DEFAULT_WAIT_DURATION_AFTER_SERVICE_DISCOVERY = 500;
    private static final int RESET_INTERVAL = 200;
    private static final int RETRY_INTERVAL = 3000;
    private static final int SCAN_DURATION_ON_FIRST_TRY = 3000;
    private static final String TAG = "BleSpenConnector";
    private static final int WAIT_DURATION_FOR_ENSURE_SPEN_REBOOT_IS_STARTED = 6700;
    private final boolean SHOULD_AUTO_RECONNECT;
    private String mBdAddress;
    private BleSpenAdvertisementFindOperation mBleSpenAdvertisementFindOperation;
    private BleSpenDriver mBleSpenDriver;
    private BleSpenRemoveBondOperation mBleSpenRemoveBondOperation;
    private BleCancellableOperation.FinishListener mCancelFinishListener;
    private ConnectionListener mConnectionFinishListener;
    private int mConnectionTimeout;
    private Context mContext;
    private BleCancellableOperation mCurWorkingOp;
    private BleSpenDriver.EventListener mDriverEventListener;
    private Runnable mFindDeviceAdvertisement;
    private IBleGattCallback mGattCallback;
    private boolean mIsAfterTictoc;
    private SpenInsertionEventDetector.Listener mPenInsertionEventListener;
    private BleSpenDeviceFeature mSpenDeviceFeature;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    private SpenModelName mSpenModelName;
    private long mStartTime;
    private long mTimeout;
    private WacomChargingDriver mWacomChargingDriver;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsCancelled = false;
    private Runnable mReconnectionHandler = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.1
        @Override // java.lang.Runnable
        public void run() {
            BleSpenConnector bleSpenConnector = BleSpenConnector.this;
            bleSpenConnector.markOperationFinish(bleSpenConnector.mWaitForReconnectionIntervalOperation);
            Log.d(BleSpenConnector.TAG, "Reconnection : Retrying to connect...");
            BleSpenConnector.this.tryToConnect();
        }
    };
    private Runnable mResetSpen = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.2
        @Override // java.lang.Runnable
        public void run() {
            BleSpenConnector.this.resetSpenNoSleep();
        }
    };
    private BleCancellableOperation mWaitForReconnectionIntervalOperation = new BleCancellableOperation() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.3
        @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
        public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
            BleSpenConnector.this.mHandler.removeCallbacks(BleSpenConnector.this.mReconnectionHandler);
            Log.i(BleSpenConnector.TAG, "WaitForReconnectionIntervalOperation : operation cancelled");
            BleSpenConnector.this.invokeFinishListener(BleOpResultCode.CANCELLED);
        }
    };
    private int mConnTriedCount = 0;
    private boolean mShouldSendResetCmdWhenFirstTryFail = false;

    /* loaded from: classes.dex */
    public interface ConnectionListener {
        void onConnectionResult(BleOpResultData bleOpResultData, long j);

        void onFinish(BleOpResultData bleOpResultData, long j);
    }

    public BleSpenConnector(Context context, SpenModelName spenModelName, BleSpenDriver bleSpenDriver, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        BleSpenDeviceFactory bleSpenDeviceFactory = BleSpenDeviceFactory.getInstance(spenModelName);
        this.mSpenDeviceFeature = bleSpenDeviceFactory.getDeviceFeature();
        this.SHOULD_AUTO_RECONNECT = canAutoReconnect();
        this.mContext = context.getApplicationContext();
        this.mSpenModelName = spenModelName;
        this.mBleSpenDriver = bleSpenDriver;
        this.mDriverEventListener = eventListener;
        this.mGattCallback = iBleGattCallback;
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(context);
        this.mBleSpenAdvertisementFindOperation = new BleSpenAdvertisementFindOperation(context);
        this.mBleSpenRemoveBondOperation = new BleSpenRemoveBondOperation(context);
        this.mWacomChargingDriver = bleSpenDeviceFactory.getWacomChargingDriver(this.mContext);
        if (this.mSpenDeviceFeature.isSupportWacomCharger()) {
            WacomChargingDriver wacomChargingDriver = this.mWacomChargingDriver;
            Assert.notNull(wacomChargingDriver, "WACOM charge driver is null! even the Spen supports the WACOM Charger. model=" + spenModelName);
        }
    }

    public void startConnection(String str, long j, boolean z, ConnectionListener connectionListener) {
        String str2 = TAG;
        Log.i(str2, "startConnection timeout=" + j);
        this.mConnTriedCount = 0;
        this.mBdAddress = str;
        this.mConnectionFinishListener = connectionListener;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mTimeout = j;
        this.mIsAfterTictoc = j == 20000;
        this.mShouldSendResetCmdWhenFirstTryFail = z;
        Assert.e(this.mPenInsertionEventListener == null);
        SpenInsertionEventDetector.Listener listener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.4
            @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
            public void onInsertEvent(boolean z2) {
                if (z2) {
                    return;
                }
                Log.d(BleSpenConnector.TAG, "startConnection : pen detached");
                BleSpenConnector.this.cancelOperation(null);
            }
        };
        this.mPenInsertionEventListener = listener;
        this.mSpenInsertionEventDetector.registerListener(listener);
        this.mBleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.5
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j2) {
                BleSpenConnector.this.tryToConnect();
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        if (isInProgress()) {
            this.mIsCancelled = true;
            boolean z = this.mCancelFinishListener == null;
            Assert.e(z, "Cancel listener is already registered : " + this.mCancelFinishListener);
            this.mCancelFinishListener = finishListener;
            if (this.mCurWorkingOp != null) {
                String str = TAG;
                Log.d(str, "cancelOperation : cancelling " + this.mCurWorkingOp.getClass().getSimpleName());
                this.mCurWorkingOp.cancelOperation(null);
                if (this.mCurWorkingOp == this.mBleSpenAdvertisementFindOperation) {
                    cancelAdvertiseFindOperation();
                    return;
                }
                return;
            }
            Log.d(TAG, "cancelOperation");
            return;
        }
        Log.e(TAG, "cancelOperation : connection is not started");
        if (finishListener != null) {
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
        }
    }

    private boolean canAutoReconnect() {
        if (!this.mSpenDeviceFeature.isSupportAutoConnect()) {
            Log.d(TAG, "canAutoReconnect: not support auto connect.");
            return false;
        } else if (!this.mSpenDeviceFeature.isSupportStandbyMode()) {
            Log.d(TAG, "canAutoReconnect : not support standby mode");
            return true;
        } else if (!SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled()) {
            Log.d(TAG, "canAutoReconnect : standby mode is enabled");
            return false;
        } else {
            Log.d(TAG, "canAutoReconnect");
            return true;
        }
    }

    private boolean isInProgress() {
        return this.mStartTime > 0;
    }

    private void reserveReconnection() {
        Log.d(TAG, "reserveReconnection");
        cancelReconnection(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.6
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                Log.d(BleSpenConnector.TAG, "reserveReconnection : reconnection cancel finished");
                BleSpenConnector bleSpenConnector = BleSpenConnector.this;
                bleSpenConnector.markOperationStart(bleSpenConnector.mWaitForReconnectionIntervalOperation);
                BleSpenConnector.this.mHandler.postDelayed(BleSpenConnector.this.mReconnectionHandler, 3000L);
            }
        });
    }

    private void cancelAdvertiseFindOperation() {
        Log.d(TAG, "cancelAdvertiseFindOperation");
        this.mHandler.removeCallbacks(this.mFindDeviceAdvertisement);
        markOperationFinish(this.mBleSpenAdvertisementFindOperation);
        invokeFinishListener(BleOpResultCode.CANCELLED);
    }

    private void cancelReconnection(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.7
            @Override // java.lang.Runnable
            public void run() {
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), SystemClock.elapsedRealtime() - elapsedRealtime);
                }
            }
        };
        this.mHandler.removeCallbacks(this.mReconnectionHandler);
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver != null) {
            bleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.8
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tryToConnect() {
        int i;
        this.mConnectionTimeout = getConnectionTimeoutDuration();
        boolean isInserted = this.mSpenInsertionEventDetector.isInserted();
        boolean isKeepConnectedEnabled = SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled();
        String str = TAG;
        Log.i(str, "tryToConnect : mSpenModelName=" + this.mSpenModelName + " timeout=" + this.mConnectionTimeout + " try count=" + (this.mConnTriedCount + 1) + " penInsertion=" + isInserted + " isKeepConnectedEnabled=" + isKeepConnectedEnabled);
        if (isCancelled()) {
            Log.e(str, "tryToConnect: connection cancelled");
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else if (this.mConnectionTimeout < 0) {
            Log.e(str, "tryToConnect: out of retry count");
            invokeFinishListener(BleOpResultCode.OUT_OF_RETRY_COUNT);
        } else {
            this.mConnTriedCount++;
            markOperationStart(this.mBleSpenAdvertisementFindOperation);
            if (this.mSpenDeviceFeature.isSupportWacomCharger()) {
                if (this.mSpenDeviceFeature.isSupportStandbyMode() && !isKeepConnectedEnabled && isInserted) {
                    if (!this.mIsAfterTictoc) {
                        Log.d(str, "tryToConnect : immediately reset because of standby mode");
                        this.mHandler.postDelayed(this.mResetSpen, 200L);
                        i = 6900;
                    }
                } else {
                    int i2 = this.mConnTriedCount;
                    if (i2 == 1) {
                        Log.d(str, "tryToConnect : first try");
                        this.mConnectionTimeout = Action.Repeat.REPEATABLE_INTERVAL_MAX_VALUE;
                        if (isInserted) {
                            this.mWacomChargingDriver.stopCharge();
                            this.mWacomChargingDriver.startCharge();
                        }
                    } else if (i2 == 2 && this.mShouldSendResetCmdWhenFirstTryFail && isInserted) {
                        Log.d(str, "tryToConnect : second try");
                        this.mHandler.postDelayed(this.mResetSpen, 0L);
                        i = WAIT_DURATION_FOR_ENSURE_SPEN_REBOOT_IS_STARTED;
                    }
                }
                Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.9
                    @Override // java.lang.Runnable
                    public void run() {
                        BleSpenConnector.this.mBleSpenAdvertisementFindOperation.findDeviceAdvertisement(BleSpenConnector.this.mSpenModelName, BleSpenConnector.this.mBdAddress, BleSpenConnector.this.mConnectionTimeout, new BleSpenAdvertisementFindOperation.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.9.1
                            @Override // com.samsung.remotespen.core.device.control.connection.BleSpenAdvertisementFindOperation.FinishListener
                            public void onFinish(BleOpResultData bleOpResultData) {
                                BleSpenConnector bleSpenConnector = BleSpenConnector.this;
                                bleSpenConnector.markOperationFinish(bleSpenConnector.mBleSpenAdvertisementFindOperation);
                                BleSpenConnector bleSpenConnector2 = BleSpenConnector.this;
                                bleSpenConnector2.onFinishDeviceScan(bleOpResultData, bleSpenConnector2.mBleSpenAdvertisementFindOperation.getElapsedTime());
                            }
                        });
                    }
                };
                this.mFindDeviceAdvertisement = runnable;
                this.mHandler.postDelayed(runnable, i);
            }
            i = 0;
            Runnable runnable2 = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.9
                @Override // java.lang.Runnable
                public void run() {
                    BleSpenConnector.this.mBleSpenAdvertisementFindOperation.findDeviceAdvertisement(BleSpenConnector.this.mSpenModelName, BleSpenConnector.this.mBdAddress, BleSpenConnector.this.mConnectionTimeout, new BleSpenAdvertisementFindOperation.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.9.1
                        @Override // com.samsung.remotespen.core.device.control.connection.BleSpenAdvertisementFindOperation.FinishListener
                        public void onFinish(BleOpResultData bleOpResultData) {
                            BleSpenConnector bleSpenConnector = BleSpenConnector.this;
                            bleSpenConnector.markOperationFinish(bleSpenConnector.mBleSpenAdvertisementFindOperation);
                            BleSpenConnector bleSpenConnector2 = BleSpenConnector.this;
                            bleSpenConnector2.onFinishDeviceScan(bleOpResultData, bleSpenConnector2.mBleSpenAdvertisementFindOperation.getElapsedTime());
                        }
                    });
                }
            };
            this.mFindDeviceAdvertisement = runnable2;
            this.mHandler.postDelayed(runnable2, i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetSpenNoSleep() {
        this.mWacomChargingDriver.resetSpen();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishDeviceScan(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishDeviceScan : " + resultCode.name());
        if (isCancelled()) {
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else if (resultCode == BleOpResultCode.SUCCESS) {
            IBleDevice bluetoothDevice = BleEnvManager.getInstance(this.mContext).getBluetoothDevice(this.mContext, this.mBdAddress);
            if (bluetoothDevice == null) {
                Log.e(str, "onFinishDeviceScan : failed to get device");
                invokeFinishListener(BleOpResultCode.API_CALL_FAIL);
            } else if (this.mSpenDeviceFeature.isSupportPenColorCode() && bluetoothDevice.getBondState() == 12 && bluetoothDevice.getName() == null) {
                Log.e(str, "onFinishDeviceScan : the bonding data does not contains the device name. removing the bonding data");
                markOperationStart(this.mBleSpenRemoveBondOperation);
                this.mBleSpenRemoveBondOperation.removeBond(this.mBdAddress, new BleSpenRemoveBondOperation.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.10
                    @Override // com.samsung.remotespen.core.device.util.operation.BleSpenRemoveBondOperation.FinishListener
                    public void onFinish(BleOpResultData bleOpResultData2) {
                        BleSpenConnector bleSpenConnector = BleSpenConnector.this;
                        bleSpenConnector.markOperationFinish(bleSpenConnector.mBleSpenRemoveBondOperation);
                        BleSpenConnector bleSpenConnector2 = BleSpenConnector.this;
                        bleSpenConnector2.onFinishRemoveBond(bleOpResultData2, bleSpenConnector2.mBleSpenRemoveBondOperation.getElapsedTime());
                    }
                });
            } else {
                doDriverOpen();
            }
        } else {
            if (resultCode == BleOpResultCode.SCANNING_TIMEOUT && this.mSpenDeviceFeature.isSupportWacomCharger()) {
                boolean isInserted = this.mSpenInsertionEventDetector.isInserted();
                Log.i(str, "onFinishDeviceScan : send charging signal again due to scan timeout. insertion=" + isInserted);
                if (isInserted) {
                    this.mWacomChargingDriver.stopCharge();
                    this.mWacomChargingDriver.startCharge();
                }
            }
            invokeConnectionResultListener(bleOpResultData, j);
            reserveReconnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishRemoveBond(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.i(str, "onFinishRemoveBond : " + resultCode.name() + ", elapsed=" + j);
        if (isCancelled()) {
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else if (resultCode == BleOpResultCode.SUCCESS) {
            doDriverOpen();
        } else {
            invokeConnectionResultListener(bleOpResultData, j);
            reserveReconnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishDriverOpen(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.i(str, "onFinishDriverOpen : " + resultCode.name() + " totalElapsed=" + getElapsedTime() + " data=" + bleOpResultData);
        if (isCancelled()) {
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else if (resultCode == BleOpResultCode.SUCCESS) {
            if (this.mBleSpenDriver == null) {
                Log.e(str, "onFinishDriverOpen : driver is null!");
                invokeConnectionResultListener(bleOpResultData, j);
                reserveReconnection();
                return;
            }
            final BleSpenConnSetupSeqHelper bleSpenConnSetupSeqHelper = new BleSpenConnSetupSeqHelper(this.mContext, this.mBleSpenDriver);
            markOperationStart(bleSpenConnSetupSeqHelper);
            bleSpenConnSetupSeqHelper.doFullConnectionSetupSequence(j > AirActionTutorialConstant.DESCRIPTION_SHOW_DELAY ? 0L : 500L, this.mSpenDeviceFeature, new BleSpenConnSetupSeqHelper.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.11
                @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnSetupSeqHelper.FinishListener
                public void onFinish(BleOpResultData bleOpResultData2, long j2) {
                    BleSpenConnector.this.markOperationFinish(bleSpenConnSetupSeqHelper);
                    BleSpenConnector.this.onFinishConnSetupSeq(bleOpResultData2, j2);
                }
            });
        } else {
            invokeConnectionResultListener(bleOpResultData, j);
            reserveReconnection();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishConnSetupSeq(BleOpResultData bleOpResultData, long j) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str = TAG;
        Log.d(str, "onFinishConnSetupSeq : " + resultCode.name() + " " + bleOpResultData);
        invokeConnectionResultListener(bleOpResultData, j);
        if (resultCode == BleOpResultCode.SUCCESS) {
            invokeFinishListener(bleOpResultData);
        } else if (isCancelled()) {
            Log.i(str, "onFinishConnSetupSeq : Conn seq failed - CANCELLED");
            invokeFinishListener(BleOpResultCode.CANCELLED);
        } else {
            BleOpResultCode bleOpResultCode = BleOpResultCode.DISCONNECTED;
            if (resultCode == bleOpResultCode && bleOpResultData.getGattStatusCode(0).equals(8)) {
                Log.i(str, "onFinishConnSetupSeq : Conn seq failed - DISCONNECTED by LINK LOSS");
                invokeFinishListener(bleOpResultCode);
                return;
            }
            Log.i(str, "onFinishConnSetupSeq : Conn seq failed - " + resultCode.name() + " retrying..");
            reserveReconnection();
        }
    }

    private void doDriverOpen() {
        markOperationStart(this.mBleSpenDriver);
        this.mBleSpenDriver.open(this.mBdAddress, this.SHOULD_AUTO_RECONNECT, this.mSpenDeviceFeature.isSupportWacomCharger(), new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.12
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleSpenConnector bleSpenConnector = BleSpenConnector.this;
                bleSpenConnector.markOperationFinish(bleSpenConnector.mBleSpenDriver);
                BleSpenConnector.this.onFinishDriverOpen(bleOpResultData, j);
            }
        }, this.mDriverEventListener, this.mGattCallback);
    }

    private int getConnectionTimeoutDuration() {
        return getElapsedTime() >= this.mTimeout ? -1 : 10000;
    }

    private long getElapsedTime() {
        return SystemClock.elapsedRealtime() - this.mStartTime;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeFinishListener(BleOpResultCode bleOpResultCode) {
        invokeFinishListener(new BleOpResultData(bleOpResultCode));
    }

    private void invokeFinishListener(final BleOpResultData bleOpResultData) {
        final long elapsedTime = getElapsedTime();
        SpenInsertionEventDetector.Listener listener = this.mPenInsertionEventListener;
        if (listener != null) {
            this.mSpenInsertionEventDetector.unregisterListener(listener);
            this.mPenInsertionEventListener = null;
        }
        this.mIsCancelled = false;
        this.mStartTime = 0L;
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.13
            @Override // java.lang.Runnable
            public void run() {
                if (BleSpenConnector.this.mConnectionFinishListener != null) {
                    BleSpenConnector.this.mConnectionFinishListener.onFinish(bleOpResultData, elapsedTime);
                }
                if (BleSpenConnector.this.mCancelFinishListener != null) {
                    BleSpenConnector.this.mCancelFinishListener.onFinish(bleOpResultData);
                    BleSpenConnector.this.mCancelFinishListener = null;
                }
            }
        };
        if (bleOpResultData.getResultCode() != BleOpResultCode.SUCCESS) {
            cancelReconnection(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.14
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData2, long j) {
                    BleSpenConnector.this.mBleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnector.14.1
                        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                        public void onFinish(BleOpResultData bleOpResultData3, long j2) {
                            String str = BleSpenConnector.TAG;
                            Log.d(str, "invokeFinishListener : driver close finished. result=" + bleOpResultData3.getResultCode() + ", " + j2 + "ms");
                            runnable.run();
                        }
                    });
                }
            });
        } else {
            runnable.run();
        }
    }

    private void invokeConnectionResultListener(BleOpResultData bleOpResultData, long j) {
        ConnectionListener connectionListener = this.mConnectionFinishListener;
        if (connectionListener != null) {
            connectionListener.onConnectionResult(bleOpResultData, j);
        }
    }

    private boolean isCancelled() {
        boolean z = true;
        boolean z2 = !SpenAdvertiseMonitor.canSpenAdvertise(this.mContext, this.mSpenModelName);
        boolean z3 = !BleUtils.isBleEnabled(this.mContext);
        if (!this.mIsCancelled && !z2 && !z3) {
            z = false;
        }
        if (z) {
            Log.e(TAG, "isCancelled : cancelled.  cancelFlag:" + this.mIsCancelled + "  penCannotAdv:" + z2 + "  bleDisabled:" + z3);
        }
        return z;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void markOperationStart(BleCancellableOperation bleCancellableOperation) {
        if (this.mCurWorkingOp != null) {
            String str = TAG;
            Log.e(str, "markOperationStart : prev op(" + this.mCurWorkingOp.getClass().getSimpleName() + ") is still working! newOp=" + bleCancellableOperation.getClass().getSimpleName(), new Exception());
            return;
        }
        this.mCurWorkingOp = bleCancellableOperation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void markOperationFinish(BleCancellableOperation bleCancellableOperation) {
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
