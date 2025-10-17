package com.samsung.remotespen.core.device.control.connection;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.control.PairingRequestAcceptor;
import com.samsung.remotespen.core.device.control.connection.BleSpenConnector;
import com.samsung.remotespen.core.device.control.connection.BleSpenHardResetter;
import com.samsung.remotespen.core.device.control.connection.BleSpenReconnector;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder;
import com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BleSpenConnectionFlowController implements BleCancellableOperation {
    private static final int CONNECTION_TIMEOUT_AFTER_TICTOC = 20000;
    private static final int CONNECTION_TIMEOUT_FOR_STANDBY_MODE = 5000;
    private static final int CONNECTION_TIMEOUT_MARGIN = 10000;
    private static final int MAX_WAKE_LOCK_DURATION_FOR_CONNECTION = 600000;
    private static final int RECONNECTION_TIMEOUT_DEFAULT = 10000;
    private static final String TAG = "BleSpenConnectionFlowController";
    private static final int TICTOC_DURATION = 30000;
    private BleSpenDriver mBleSpenDriver;
    private BleSpenHardResetter mBleSpenHardResetter;
    private Context mContext;
    private BleCancellableOperation mCurWorkingOp;
    private FinishListener mFinishListener;
    private PairingRequestAcceptor mPairingRequestAcceptor;
    private SpenModelName mTargetSpenModelName;
    private PowerManager.WakeLock mWakeLock;
    private boolean mIsInProgress = false;
    private BleStandAloneModeEnabler mBleStandAloneModeEnabler = new BleStandAloneModeEnabler();
    private ArrayList<BleCancellableOperation.FinishListener> mCancelFinishListenerList = new ArrayList<>();

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(BleOpResultData bleOpResultData, String str);
    }

    public BleSpenConnectionFlowController(Context context, BleSpenDriver bleSpenDriver, PairingRequestAcceptor pairingRequestAcceptor) {
        this.mContext = context.getApplicationContext();
        this.mBleSpenDriver = bleSpenDriver;
        this.mPairingRequestAcceptor = pairingRequestAcceptor;
        this.mBleSpenHardResetter = new BleSpenHardResetter(context);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "BLE SPen connection");
    }

    public void connectToSpen(final SpenModelName spenModelName, final String str, boolean z, int i, final boolean z2, FinishListener finishListener, final BleSpenDriver.EventListener eventListener, final IBleGattCallback iBleGattCallback) {
        String str2 = TAG;
        Log.i(str2, "connectToSpen : " + spenModelName.name() + ", " + str + ", reset=" + z + ", batteryLevel=" + i + ", isBleConnTriggerCodePenDetach=" + z2);
        if (this.mFinishListener != null) {
            Log.e(str2, "connectToSpen : Finish listener is already available");
        }
        this.mFinishListener = finishListener;
        if (this.mWakeLock.isHeld()) {
            Log.e(str2, "connectToSpen : wake lock is already acquired");
        }
        this.mWakeLock.acquire(600000L);
        DiagnosisManager.getInstance(this.mContext).notifyConnectionStarted(spenModelName);
        if (isConnectionInProgress()) {
            Log.e(str2, "connectToSpen : Connection is already in progress");
            BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.ALREADY_RUNNING);
            bleOpResultData.setMessage("connectToSpen");
            invokeFinishListener(bleOpResultData, null);
        } else if (!this.mBleSpenDriver.isDisconnected()) {
            Log.e(str2, "connectToSpen : Already connected");
            BleOpResultData bleOpResultData2 = new BleOpResultData(BleOpResultCode.ALREADY_OPENED);
            bleOpResultData2.setMessage("connectToSpen");
            invokeFinishListener(bleOpResultData2, null);
        } else {
            setConnectionInProgress(true);
            this.mTargetSpenModelName = spenModelName;
            if (z) {
                markOperationStart(this.mBleSpenHardResetter);
                WacomChargingDriver wacomChargingDriver = BleSpenDeviceFactory.getInstance(spenModelName).getWacomChargingDriver(this.mContext);
                if (wacomChargingDriver == null) {
                    Log.e(str2, "connectToSpen : WACOM reset not supported");
                    setConnectionInProgress(false);
                    invokeFinishListener(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), null);
                    return;
                }
                this.mBleSpenHardResetter.performReset(wacomChargingDriver, i, new BleSpenHardResetter.FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.1
                    @Override // com.samsung.remotespen.core.device.control.connection.BleSpenHardResetter.FinishListener
                    public void onFinish(BleOpResultData bleOpResultData3) {
                        BleSpenConnectionFlowController bleSpenConnectionFlowController = BleSpenConnectionFlowController.this;
                        bleSpenConnectionFlowController.markOperationFinish(bleSpenConnectionFlowController.mBleSpenHardResetter);
                        if (bleOpResultData3.getResultCode() == BleOpResultCode.SUCCESS) {
                            BleSpenConnectionFlowController.this.connectToSpen(spenModelName, str, z2, eventListener, iBleGattCallback);
                        } else {
                            BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData3, null);
                        }
                    }
                });
                return;
            }
            connectToSpen(spenModelName, str, z2, eventListener, iBleGattCallback);
        }
    }

    public void performReconnectionSetupSequence(SpenModelName spenModelName, FinishListener finishListener) {
        String str = TAG;
        Log.i(str, "performReconnectionSetupSequence");
        if (this.mFinishListener != null) {
            Log.e(str, "performReconnectionSetupSequence : Finish listener is already available");
        }
        this.mFinishListener = finishListener;
        if (this.mWakeLock.isHeld()) {
            Log.e(str, "performReconnectionSetupSequence : wake lock is already acquired");
        }
        this.mWakeLock.acquire(600000L);
        DiagnosisManager.getInstance(this.mContext).notifyConnectionStarted(spenModelName);
        if (isConnectionInProgress()) {
            Log.e(str, "performReconnectionSetupSequence : Connection is already in progress");
            BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.ALREADY_RUNNING);
            bleOpResultData.setMessage("connectToSpen");
            invokeFinishListener(bleOpResultData, null);
        } else if (this.mBleSpenDriver.isDisconnected()) {
            Log.e(str, "performReconnectionSetupSequence : disconnected state.");
            BleOpResultData bleOpResultData2 = new BleOpResultData(BleOpResultCode.DISCONNECTED);
            bleOpResultData2.setMessage("performReconnectionSetupSequence");
            invokeFinishListener(bleOpResultData2, null);
        } else {
            setConnectionInProgress(true);
            this.mTargetSpenModelName = spenModelName;
            tryToReconnectViaBluetooth(10000L, new FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.2
                @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                public void onFinish(BleOpResultData bleOpResultData3, String str2) {
                    if (bleOpResultData3.getResultCode() == BleOpResultCode.SUCCESS) {
                        BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData3, str2);
                    } else {
                        BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData3, null);
                    }
                }
            });
        }
    }

    public boolean isConnectionInProgress() {
        return this.mIsInProgress;
    }

    public void setConnectionInProgress(boolean z) {
        String str = TAG;
        Log.d(str, "setConnectionInProgress :" + z);
        this.mIsInProgress = z;
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        String str = TAG;
        Log.d(str, "cancelOperation");
        if (!isConnectionInProgress()) {
            if (finishListener != null) {
                finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
                return;
            }
            return;
        }
        synchronized (this.mCancelFinishListenerList) {
            if (this.mCancelFinishListenerList.size() > 0) {
                Log.i(str, "cancelOperation : cancel listener is already exists. count=" + this.mCancelFinishListenerList.size());
            }
            if (finishListener != null) {
                this.mCancelFinishListenerList.add(finishListener);
            }
        }
        BleCancellableOperation bleCancellableOperation = this.mCurWorkingOp;
        if (bleCancellableOperation != null) {
            bleCancellableOperation.cancelOperation(null);
        } else {
            Log.e(str, "cancelOperation : No working operation");
        }
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

    /* JADX INFO: Access modifiers changed from: private */
    public void connectToSpen(final SpenModelName spenModelName, final String str, final boolean z, final BleSpenDriver.EventListener eventListener, final IBleGattCallback iBleGattCallback) {
        final BleSpenAttachedPenAddrFinder createAttachedPenAddrFinder = BleSpenDeviceFactory.getInstance(spenModelName).createAttachedPenAddrFinder(this.mContext);
        final BleSpenAttachedPenAddrFinder.ResultListener resultListener = new BleSpenAttachedPenAddrFinder.ResultListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.3
            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.ResultListener
            public void onFinishOnePhase(BleSpenAttachedPenAddrFinder.Phase phase) {
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.ResultListener
            public void onFinished(BleSpenAttachedPenAddrFinder.ResultData resultData, long j) {
                BleSpenConnectionFlowController.this.markOperationFinish(createAttachedPenAddrFinder);
                DiagnosisManager.getInstance(BleSpenConnectionFlowController.this.mContext).notifyTicTocPerformedDuringConnection(spenModelName);
                BleSpenAttachedPenAddrFinder.ResultCode resultCode = resultData.getResultCode();
                if (resultCode != BleSpenAttachedPenAddrFinder.ResultCode.SUCCESS) {
                    String str2 = BleSpenConnectionFlowController.TAG;
                    Log.e(str2, "connectToSpen : " + resultCode.name());
                    BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.TICTOC_FAIL);
                    bleOpResultData.setMessage(resultCode.name());
                    BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData, null);
                    return;
                }
                String foundPenAddress = resultData.getFoundPenAddress();
                Assert.notNull(foundPenAddress);
                BleSpenConnectionFlowController.this.tryToConnectViaBluetooth(spenModelName, foundPenAddress, 20000L, false, new FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.3.1
                    @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                    public void onFinish(BleOpResultData bleOpResultData2, String str3) {
                        BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData2, str3);
                    }
                }, eventListener, iBleGattCallback);
            }
        };
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.4
            @Override // java.lang.Runnable
            public void run() {
                BleSpenDeviceFeature deviceFeature = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature();
                final boolean isSupportWacomCharger = deviceFeature.isSupportWacomCharger();
                final BleSpenScanFilter scanFilter = deviceFeature.getScanFilter();
                if (TextUtils.isEmpty(str)) {
                    if (isSupportWacomCharger) {
                        BleSpenConnectionFlowController.this.markOperationStart(createAttachedPenAddrFinder);
                        createAttachedPenAddrFinder.startTicToc(scanFilter, BleSpenConnectionFlowController.TICTOC_DURATION, resultListener);
                        return;
                    }
                    Assert.fail("Target Spen address is empty! model=" + spenModelName.name());
                    return;
                }
                boolean z2 = !SettingsValueManager.getInstance(BleSpenConnectionFlowController.this.mContext).isAllowMultiplePensEnabled();
                long minChargeDurationForSpenBoot = deviceFeature.getMinChargeDurationForSpenBoot(z2);
                if (z) {
                    minChargeDurationForSpenBoot = 5000;
                }
                long j = minChargeDurationForSpenBoot + 10000;
                Log.i(BleSpenConnectionFlowController.TAG, "connectionCallback : timeout=" + j + ", powerSaving=" + z2 + ", isBleConnTriggerCodePenDetach=" + z);
                BleSpenConnectionFlowController.this.tryToConnectViaBluetooth(spenModelName, str, j, true, new FinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.4.1
                    @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                    public void onFinish(BleOpResultData bleOpResultData, String str2) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        if (resultCode == BleOpResultCode.SUCCESS) {
                            BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData, str2);
                        } else if (resultCode == BleOpResultCode.CANCELLED) {
                            BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData, null);
                        } else if (!isSupportWacomCharger) {
                            BleSpenConnectionFlowController.this.invokeFinishListener(bleOpResultData, null);
                        } else {
                            AnonymousClass4 anonymousClass4 = AnonymousClass4.this;
                            BleSpenConnectionFlowController.this.markOperationStart(createAttachedPenAddrFinder);
                            AnonymousClass4 anonymousClass42 = AnonymousClass4.this;
                            createAttachedPenAddrFinder.startTicToc(scanFilter, BleSpenConnectionFlowController.TICTOC_DURATION, resultListener);
                        }
                    }
                }, eventListener, iBleGattCallback);
            }
        };
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver != null) {
            bleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.5
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    Log.d(BleSpenConnectionFlowController.TAG, "connectToSpen : close finished");
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void tryToConnectViaBluetooth(SpenModelName spenModelName, String str, long j, boolean z, FinishListener finishListener, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        String str2 = TAG;
        Log.d(str2, "tryToConnectViaBluetooth");
        Assert.notNull(finishListener);
        if (this.mBleSpenDriver == null) {
            Log.e(str2, "tryToConnectViaBluetooth : driver is null");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.GATT_NULL), null);
            return;
        }
        final AnonymousClass6 anonymousClass6 = new AnonymousClass6(str, spenModelName, eventListener, iBleGattCallback, j, z, finishListener);
        this.mBleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.7
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j2) {
                anonymousClass6.run();
            }
        });
    }

    /* renamed from: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController$6  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass6 implements Runnable {
        public final /* synthetic */ String val$bdAddress;
        public final /* synthetic */ BleSpenDriver.EventListener val$driverEventListener;
        public final /* synthetic */ FinishListener val$finishListener;
        public final /* synthetic */ IBleGattCallback val$gattCallback;
        public final /* synthetic */ boolean val$sendResetCmd;
        public final /* synthetic */ SpenModelName val$spenModelName;
        public final /* synthetic */ long val$timeout;

        public AnonymousClass6(String str, SpenModelName spenModelName, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback, long j, boolean z, FinishListener finishListener) {
            this.val$bdAddress = str;
            this.val$spenModelName = spenModelName;
            this.val$driverEventListener = eventListener;
            this.val$gattCallback = iBleGattCallback;
            this.val$timeout = j;
            this.val$sendResetCmd = z;
            this.val$finishListener = finishListener;
        }

        @Override // java.lang.Runnable
        public void run() {
            BleSpenConnectionFlowController bleSpenConnectionFlowController = BleSpenConnectionFlowController.this;
            bleSpenConnectionFlowController.markOperationStart(bleSpenConnectionFlowController.mBleStandAloneModeEnabler);
            BleSpenConnectionFlowController.this.mBleStandAloneModeEnabler.enableBleStandAloneMode(BleSpenConnectionFlowController.this.mContext, new BleStandAloneModeEnabler.Listener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.6.1
                @Override // com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler.Listener
                public void onFinish(BleOpResultData bleOpResultData) {
                    BleSpenConnectionFlowController bleSpenConnectionFlowController2 = BleSpenConnectionFlowController.this;
                    bleSpenConnectionFlowController2.markOperationFinish(bleSpenConnectionFlowController2.mBleStandAloneModeEnabler);
                    if (bleOpResultData.getResultCode() == BleOpResultCode.SUCCESS) {
                        BleSpenConnectionFlowController.this.mPairingRequestAcceptor.setSpenAddressToAccept(AnonymousClass6.this.val$bdAddress);
                        Context context = BleSpenConnectionFlowController.this.mContext;
                        AnonymousClass6 anonymousClass6 = AnonymousClass6.this;
                        SpenModelName spenModelName = anonymousClass6.val$spenModelName;
                        BleSpenDriver bleSpenDriver = BleSpenConnectionFlowController.this.mBleSpenDriver;
                        AnonymousClass6 anonymousClass62 = AnonymousClass6.this;
                        final BleSpenConnector bleSpenConnector = new BleSpenConnector(context, spenModelName, bleSpenDriver, anonymousClass62.val$driverEventListener, anonymousClass62.val$gattCallback);
                        BleSpenConnectionFlowController.this.markOperationStart(bleSpenConnector);
                        AnonymousClass6 anonymousClass63 = AnonymousClass6.this;
                        bleSpenConnector.startConnection(anonymousClass63.val$bdAddress, anonymousClass63.val$timeout, anonymousClass63.val$sendResetCmd, new BleSpenConnector.ConnectionListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.6.1.1
                            @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnector.ConnectionListener
                            public void onFinish(BleOpResultData bleOpResultData2, long j) {
                                BleSpenConnectionFlowController.this.markOperationFinish(bleSpenConnector);
                                BleOpResultCode resultCode = bleOpResultData2.getResultCode();
                                String str = BleSpenConnectionFlowController.TAG;
                                Log.d(str, "tryToConnectViaBluetooth : finished. result=" + resultCode.name() + ", " + BleSpenConnectionFlowController.this.millisToSecondStr(j));
                                AnonymousClass6.this.val$finishListener.onFinish(bleOpResultData2, resultCode == BleOpResultCode.SUCCESS ? AnonymousClass6.this.val$bdAddress : null);
                            }

                            @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnector.ConnectionListener
                            public void onConnectionResult(BleOpResultData bleOpResultData2, long j) {
                                BleOpResultCode resultCode = bleOpResultData2.getResultCode();
                                String str = BleSpenConnectionFlowController.TAG;
                                Log.d(str, "tryToConnectViaBluetooth : connection result=" + resultCode.name() + ", elapsed=" + j);
                            }
                        });
                        return;
                    }
                    Log.e(BleSpenConnectionFlowController.TAG, "tryToConnectViaBluetooth : Failed to enable BLE stand alone mode");
                    AnonymousClass6.this.val$finishListener.onFinish(bleOpResultData, null);
                }
            });
        }
    }

    private void tryToReconnectViaBluetooth(long j, final FinishListener finishListener) {
        String str = TAG;
        Log.d(str, "tryToReconnectViaBluetooth");
        Assert.notNull(finishListener);
        if (this.mBleSpenDriver == null) {
            Log.e(str, "tryToReconnectViaBluetooth : driver is null");
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.GATT_NULL), null);
            return;
        }
        final BleSpenReconnector bleSpenReconnector = new BleSpenReconnector(this.mContext, this.mBleSpenDriver);
        markOperationStart(bleSpenReconnector);
        bleSpenReconnector.startReconnection(j, new BleSpenReconnector.ConnectionListener() { // from class: com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.8
            @Override // com.samsung.remotespen.core.device.control.connection.BleSpenReconnector.ConnectionListener
            public void onFinish(BleOpResultData bleOpResultData, long j2) {
                BleSpenConnectionFlowController.this.markOperationFinish(bleSpenReconnector);
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str2 = BleSpenConnectionFlowController.TAG;
                Log.d(str2, "tryToReconnectViaBluetooth : finished. result=" + resultCode.name() + ", " + BleSpenConnectionFlowController.this.millisToSecondStr(j2));
                finishListener.onFinish(bleOpResultData, BleSpenConnectionFlowController.this.mBleSpenDriver.getConnectedDeviceAddress());
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeFinishListener(BleOpResultData bleOpResultData, String str) {
        String str2 = TAG;
        Log.d(str2, "invokeFinishListener");
        FinishListener finishListener = this.mFinishListener;
        if (finishListener != null) {
            finishListener.onFinish(bleOpResultData, str);
            this.mFinishListener = null;
        } else {
            Log.e(str2, "invokeFinishListener : finish listener is null");
        }
        synchronized (this.mCancelFinishListenerList) {
            Iterator<BleCancellableOperation.FinishListener> it = this.mCancelFinishListenerList.iterator();
            while (it.hasNext()) {
                BleCancellableOperation.FinishListener next = it.next();
                Assert.notNull(next);
                next.onFinish(bleOpResultData);
            }
            this.mCancelFinishListenerList.clear();
        }
        setConnectionInProgress(false);
        DiagnosisManager.getInstance(this.mContext).notifyConnectionFinished(this.mTargetSpenModelName, bleOpResultData);
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String millisToSecondStr(long j) {
        return j < 0 ? "N/A" : String.format("%d.%03ds", Long.valueOf(j / 1000), Long.valueOf(j % 1000));
    }
}
