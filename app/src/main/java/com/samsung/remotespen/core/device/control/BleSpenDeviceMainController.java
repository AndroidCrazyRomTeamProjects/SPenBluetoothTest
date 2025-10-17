package com.samsung.remotespen.core.device.control;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import com.samsung.BuildConfig;
import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.control.behavior.PenBehaviorPolicyManager;
import com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy;
import com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController;
import com.samsung.remotespen.core.device.control.connection.SpenAdvertiseMonitor;
import com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleConnReqData;
import com.samsung.remotespen.core.device.data.BleConnTriggerCode;
import com.samsung.remotespen.core.device.data.BleDisconnReqData;
import com.samsung.remotespen.core.device.data.BleDisconnTriggerCode;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenApproachState;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonId;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener;
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
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.remotespen.ui.tutorial.AirActionTutorialConstant;
import com.samsung.remotespen.util.MagneticSensorDetector;
import com.samsung.remotespen.util.SpenInputDetector;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class BleSpenDeviceMainController {
    private static final int MIN_WAIT_TIME_AFTER_AUTO_CONNECTION_START = 2000;
    private static final String TAG = "BleSpenDeviceMainController";
    private BleSpenConnectionFlowController mBleSpenConnectionFlowController;
    private BleSpenDriver mBleSpenDriver;
    private BleSpenDriver.EventListener mBleSpenDriverListener;
    private BleConnReqData mConnReqData;
    private ConnectionState mConnectionState;
    private Context mContext;
    private BleDisconnReqData mDisconnReqData;
    private IBleGattCallback mGattCallback;
    private SpenInputDetector mInputMonitor;
    private MagneticSensorDetector mMagneticSensorDetector;
    private BleSpenPairedSpenManager mPairedSpenManager;
    private PairingRequestAcceptor mPairingRequestAcceptor;
    private PenBehaviorPolicyManager mPenBehaviorPolicyManager;
    private BleSpenSensorActionDetector mSensorActionDetector;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    private BleSpenInstanceId mSpenInstanceId;
    private BleSpenDeviceFeature mTargetSpenDeviceFeature;
    private SpenModelName mTargetSpenModelName;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mLastBatteryLevel = -1;
    private boolean mIsConnectionRetryReserved = false;
    private boolean mIsReconnectReason = false;
    private BleSpenSensorActionDetector.Listener mSensorActionListener = new BleSpenSensorActionDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.1
        @Override // com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector.Listener
        public void onButtonEvent(BleSpenButtonEvent bleSpenButtonEvent) {
            int i = AnonymousClass28.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[bleSpenButtonEvent.getAction().ordinal()];
            if (i == 1) {
                DiagnosisManager.getInstance(BleSpenDeviceMainController.this.mContext).notifyButtonSingleClicked(BleSpenDeviceMainController.this.mTargetSpenModelName);
            } else if (i == 2) {
                DiagnosisManager.getInstance(BleSpenDeviceMainController.this.mContext).notifyButtonLongClicked(BleSpenDeviceMainController.this.mTargetSpenModelName);
            } else if (i == 3) {
                DiagnosisManager.getInstance(BleSpenDeviceMainController.this.mContext).notifyButtonDoubleClicked(BleSpenDeviceMainController.this.mTargetSpenModelName);
            }
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onButtonEvent(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenButtonEvent);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.detector.BleSpenSensorActionDetector.Listener
        public void onAirGestureActionEvent(BleSpenGestureEvent bleSpenGestureEvent) {
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onAirGestureActionEvent(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenGestureEvent);
            }
        }
    };
    private BroadcastReceiver mBleStateChangeBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED".equals(intent.getAction())) {
                if (!intent.hasExtra("android.bluetooth.adapter.extra.STATE")) {
                    Log.e(BleSpenDeviceMainController.TAG, "onReceive : EXTRA_STATE is not present!");
                }
                if (!intent.hasExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE")) {
                    Log.e(BleSpenDeviceMainController.TAG, "onReceive : EXTRA_PREVIOUS_STATE is not present!");
                }
                int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", 10);
                BleSpenDeviceMainController.this.onBleStateChanged(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 10), intExtra);
            }
        }
    };
    private ComponentCallbacks2 mTrimCallback = new ComponentCallbacks2() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.3
        @Override // android.content.ComponentCallbacks
        public void onConfigurationChanged(Configuration configuration) {
        }

        @Override // android.content.ComponentCallbacks
        public void onLowMemory() {
        }

        @Override // android.content.ComponentCallbacks2
        public void onTrimMemory(int i) {
            BleSpenDeviceMainController.this.onTrimMemory(i);
        }
    };
    private boolean mIsBleSpenAutoConnectionEnabled = false;
    private BleSpenDriver.EventListener mInternalBleSpenDriverListener = new BleSpenDriver.EventListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.4
        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public synchronized void onConnectionStateChanged(BleSpenDriver.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
            String str = BleSpenDeviceMainController.TAG;
            Log.i(str, "onConnectionStateChanged : connectionState = " + connectionState + ", mConnectionState = " + BleSpenDeviceMainController.this.mConnectionState);
            int i = AnonymousClass28.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$factory$BleSpenDriver$ConnectionState[connectionState.ordinal()];
            if (i != 1) {
                if (i == 2) {
                    ConnectionState connectionState2 = BleSpenDeviceMainController.this.getConnectionState();
                    ConnectionState connectionState3 = ConnectionState.CONNECTING;
                    if (connectionState2 != connectionState3) {
                        if (BleSpenDeviceMainController.this.isLinkLossStatus(bleStateChangeInfo.getGattStatusCode())) {
                            Log.d(BleSpenDeviceMainController.TAG, "onConnectionStateChanged : LinkLossStatus - mDisconnReqData will be null");
                            BleSpenDeviceMainController.this.mDisconnReqData = null;
                        }
                        bleStateChangeInfo.setDisconnReqData(BleSpenDeviceMainController.this.mDisconnReqData);
                        BleSpenDeviceMainController.this.setConnectionState(ConnectionState.DISCONNECTED, bleStateChangeInfo);
                        if (BleSpenDeviceMainController.this.mTargetSpenDeviceFeature.isSupportEasyConnect()) {
                            BleSpenDeviceMainController.this.closeBleSpenDriver(new BleOpResultData(BleOpResultCode.SUCCESS), null);
                        }
                    } else if (BleSpenDeviceMainController.this.getConnectionState() == connectionState3) {
                        if (BleSpenDeviceMainController.this.isRemoteDisconnect(bleStateChangeInfo.getGattStatusCode()) && BleSpenDeviceMainController.this.mSpenInstanceId.isBundledSpen()) {
                            Log.d(BleSpenDeviceMainController.TAG, "onConnectionStateChanged : RemoteDisconnected by reason 19");
                            BleUtils.resetBondedDevice(BleSpenDeviceMainController.this.mContext);
                        }
                        BleSpenDeviceMainController.this.mDisconnReqData = null;
                    } else {
                        BleSpenDeviceMainController.this.mDisconnReqData = null;
                    }
                }
            } else if (BleSpenDeviceMainController.this.getConnectionState() == ConnectionState.DISCONNECTED || BleSpenDeviceMainController.this.getConnectionState() == ConnectionState.CONNECTED) {
                Log.i(BleSpenDeviceMainController.TAG, "onConnectionStateChanged : DISCONNECTED -> CONNECTED. (Auto reconnected)");
                if (BleUtils.isBleEnabled(BleSpenDeviceMainController.this.mContext)) {
                    BleSpenDeviceMainController.this.startPenAutoReconnectionTransaction(new BleConnReqData(BleSpenDeviceMainController.this.mTargetSpenModelName, BleConnTriggerCode.AUTO_RECONNECTION, BleSpenDeviceMainController.this.mBleSpenDriver.getConnectedDeviceAddress()));
                } else {
                    Log.e(BleSpenDeviceMainController.TAG, "onConnectionStateChanged : BLE is not enabled.");
                }
            }
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onConnectionStateChanged(connectionState, bleStateChangeInfo);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onBatteryLevelChanged(int i, int i2) {
            BleSpenDeviceMainController.this.mLastBatteryLevel = i;
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onBatteryLevelChanged(i, i2);
            }
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onBatteryLevelChanged(BleSpenDeviceMainController.this.mSpenInstanceId, i, i2);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onChargeStateChanged(BleSpenChargeState bleSpenChargeState) {
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onChargeStateChanged(bleSpenChargeState);
            }
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onChargeStateChanged(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenChargeState);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onSensorEvent(bleSpenSensorEvent);
            }
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onSensorEvent(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenSensorEvent);
            }
            BleSpenDeviceMainController.this.mSensorActionDetector.sendSensorEvent(bleSpenSensorEvent);
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onLedStateChanged(BleSpenLedState bleSpenLedState) {
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onLedStateChanged(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenLedState);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onPenFrequencyChanged(BleSpenFrequency bleSpenFrequency) {
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onPenFrequencyChanged(bleSpenFrequency);
            }
            Iterator it = BleSpenDeviceMainController.this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                ((BleSpenStateListener) it.next()).onPenFrequencyChanged(BleSpenDeviceMainController.this.mSpenInstanceId, bleSpenFrequency);
            }
        }

        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.EventListener
        public void onNotifyBatteryState(BleSpenBatteryState bleSpenBatteryState) {
            if (BleSpenDeviceMainController.this.mBleSpenDriverListener != null) {
                BleSpenDeviceMainController.this.mBleSpenDriverListener.onNotifyBatteryState(bleSpenBatteryState);
            }
        }
    };
    private SpenInputDetector.Listener mSpenInputDetectorListener = new SpenInputDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.5
        @Override // com.samsung.remotespen.util.SpenInputDetector.Listener
        public void onSpenMotionEvent(MotionEvent motionEvent) {
        }

        @Override // com.samsung.remotespen.util.SpenInputDetector.Listener
        public void onHoverEvent(int i, int i2, int i3) {
            BleSpenDeviceMainController.this.onSpenHoverEvent(i, i2, i3);
        }

        @Override // com.samsung.remotespen.util.SpenInputDetector.Listener
        public void onPenButtonPressStateChangedOnHover(boolean z) {
            BleSpenDeviceMainController.this.mSensorActionDetector.setPenButtonPressedOnHover(BleSpenButtonId.PRIMARY, z);
        }

        @Override // com.samsung.remotespen.util.SpenInputDetector.Listener
        public void onTouchEvent(int i) {
            if (i == 0) {
                BleSpenDeviceMainController.this.mSensorActionDetector.setScreenTouchState(true);
            } else if (i == 1 || i == 3) {
                BleSpenDeviceMainController.this.mSensorActionDetector.setScreenTouchState(false);
            }
        }
    };
    private ArrayList<BleSpenStateListener> mBleSpenStateListenerTable = new ArrayList<>();
    private final SpenInsertionEventDetector.Listener mSpenInsertionEventListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.6
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            BleSpenDeviceMainController.this.onSpenInsertEvent(z);
        }
    };
    private final SpenInsertionEventDetector.Listener mSpenInsertionEventListenerForMagneticSensorDetector = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.7
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            if (z) {
                return;
            }
            BleSpenDeviceMainController.this.mMagneticSensorDetector.registerMagneticSensorEventListener();
        }
    };

    /* loaded from: classes.dex */
    public interface BleSpenStateListener {
        void onAirGestureActionEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenGestureEvent bleSpenGestureEvent);

        void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2);

        void onButtonEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenButtonEvent bleSpenButtonEvent);

        void onChargeStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenChargeState bleSpenChargeState);

        void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, ConnectionState connectionState, ConnectionState connectionState2, BleStateChangeInfo bleStateChangeInfo);

        void onLedStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenLedState bleSpenLedState);

        void onPenFrequencyChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenFrequency bleSpenFrequency);

        void onSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent);
    }

    /* loaded from: classes.dex */
    public enum ConnectionState {
        BLE_OFF,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /* renamed from: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController$28  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass28 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$factory$BleSpenDriver$ConnectionState;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action;

        static {
            int[] iArr = new int[BleSpenDriver.ConnectionState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$factory$BleSpenDriver$ConnectionState = iArr;
            try {
                iArr[BleSpenDriver.ConnectionState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$factory$BleSpenDriver$ConnectionState[BleSpenDriver.ConnectionState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            int[] iArr2 = new int[BleSpenButtonEvent.Action.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action = iArr2;
            try {
                iArr2[BleSpenButtonEvent.Action.SINGLE_CLICKED.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.LONG_CLICK_STARTED.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenButtonEvent$Action[BleSpenButtonEvent.Action.DOUBLE_CLICKED.ordinal()] = 3;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public void initialize(Context context, BleSpenInstanceId bleSpenInstanceId) {
        Assert.notNull(bleSpenInstanceId);
        if (this.mContext != null) {
            Log.e(TAG, "initialize : already initialized!");
        }
        SpenModelName spenModelName = bleSpenInstanceId.getSpenModelName();
        String str = TAG;
        Log.v(str, "initialize : model=" + spenModelName);
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(applicationContext);
        this.mTargetSpenModelName = spenModelName;
        this.mSpenInstanceId = bleSpenInstanceId;
        this.mTargetSpenDeviceFeature = BleSpenDeviceFactory.getInstance(spenModelName).getDeviceFeature();
        BleSpenDriver createBleSpenDriver = BleSpenDeviceFactory.getInstance(spenModelName).createBleSpenDriver();
        this.mBleSpenDriver = createBleSpenDriver;
        createBleSpenDriver.initialize(this.mContext);
        BleSpenSensorActionDetector bleSpenSensorActionDetector = new BleSpenSensorActionDetector(context, this.mTargetSpenModelName);
        this.mSensorActionDetector = bleSpenSensorActionDetector;
        bleSpenSensorActionDetector.initialize();
        this.mSensorActionDetector.registerListener(this.mSensorActionListener);
        if (CommonUtils.isSupportMagneticSensitivityFeature() && this.mSpenInstanceId.isBundledSpen()) {
            MagneticSensorDetector magneticSensorDetector = MagneticSensorDetector.getInstance(this.mContext);
            this.mMagneticSensorDetector = magneticSensorDetector;
            magneticSensorDetector.start();
            this.mSpenInsertionEventDetector.registerListener(this.mSpenInsertionEventListenerForMagneticSensorDetector);
        }
        PairingRequestAcceptor pairingRequestAcceptor = new PairingRequestAcceptor(context);
        this.mPairingRequestAcceptor = pairingRequestAcceptor;
        pairingRequestAcceptor.start();
        this.mBleSpenConnectionFlowController = new BleSpenConnectionFlowController(context, this.mBleSpenDriver, this.mPairingRequestAcceptor);
        this.mPairedSpenManager = BleSpenPairedSpenManager.getInstance(this.mContext);
        SpenInputDetector spenInputDetector = SpenInputDetector.getInstance(this.mContext);
        this.mInputMonitor = spenInputDetector;
        spenInputDetector.registerListener(this.mSpenInputDetectorListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intentFilter.setPriority(999);
        this.mContext.registerReceiver(this.mBleStateChangeBroadcastReceiver, intentFilter);
        BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
        bleOpResultData.setMessage("Initial disconnect state");
        if (!isBleEnabled()) {
            setConnectionState(ConnectionState.BLE_OFF, bleOpResultData);
        } else {
            setConnectionState(ConnectionState.DISCONNECTED, bleOpResultData);
        }
        this.mContext.registerComponentCallbacks(this.mTrimCallback);
        PenBehaviorPolicyManager createPenBehaviorPolicyManager = BleSpenDeviceFactory.getInstance(spenModelName).createPenBehaviorPolicyManager(this.mContext, this.mBleSpenDriver, this.mSpenInstanceId, new AbsPenBehaviorPolicy.Callback() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.8
            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void setDriverEventListener(BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
                BleSpenDeviceMainController.this.setDriverEventListener(eventListener, iBleGattCallback);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void setSpenOperationMode(BleSpenOperationMode bleSpenOperationMode, BleSpenDriver.OperationFinishListener operationFinishListener) {
                BleSpenDeviceMainController.this.setSpenOperationMode(bleSpenOperationMode, operationFinishListener);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public boolean startPenConnectionTransaction(BleConnReqData bleConnReqData) {
                return BleSpenDeviceMainController.this.startPenConnectionTransaction(bleConnReqData);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void cancelConnectionRetry() {
                BleSpenDeviceMainController.this.cancelConnectionRetry();
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void reserveConnectionRetry() {
                BleSpenDeviceMainController.this.reserveConnectionRetry();
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void setReconnectReason(boolean z) {
                BleSpenDeviceMainController.this.setReconnectReason(z);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void disconnect(BleDisconnReqData bleDisconnReqData, BleSpenDriver.OperationFinishListener operationFinishListener) {
                BleSpenDeviceMainController.this.disconnect(bleDisconnReqData, operationFinishListener);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public void closeBleSpenDriver(BleOpResultData bleOpResultData2, BleSpenDriver.OperationFinishListener operationFinishListener) {
                BleSpenDeviceMainController.this.closeBleSpenDriver(bleOpResultData2, operationFinishListener);
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public boolean isBleSpenAutoConnectionEnabled() {
                return BleSpenDeviceMainController.this.isBleSpenAutoConnectionEnabled();
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public boolean isConnected() {
                return BleSpenDeviceMainController.this.isConnected();
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public boolean isDisconnected() {
                return BleSpenDeviceMainController.this.isDisconnected();
            }

            @Override // com.samsung.remotespen.core.device.control.behavior.policy.AbsPenBehaviorPolicy.Callback
            public boolean isConnecting() {
                return BleSpenDeviceMainController.this.isConnecting();
            }
        });
        this.mPenBehaviorPolicyManager = createPenBehaviorPolicyManager;
        createPenBehaviorPolicyManager.start();
    }

    public void release(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        Log.i(TAG, BuildConfig.BUILD_TYPE);
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.9
            @Override // java.lang.Runnable
            public void run() {
                if (BleSpenDeviceMainController.this.mInputMonitor != null) {
                    BleSpenDeviceMainController.this.mInputMonitor.unregisterListener(BleSpenDeviceMainController.this.mSpenInputDetectorListener);
                    BleSpenDeviceMainController.this.mInputMonitor = null;
                }
                BleSpenDeviceMainController.this.mPenBehaviorPolicyManager.stop();
                BleSpenDeviceMainController.this.mPairingRequestAcceptor.stop();
                BleSpenDeviceMainController.this.mSensorActionDetector.unregisterListener(BleSpenDeviceMainController.this.mSensorActionListener);
                if (BleSpenDeviceMainController.this.mMagneticSensorDetector != null) {
                    BleSpenDeviceMainController.this.mMagneticSensorDetector.stop();
                }
                if (BleSpenDeviceMainController.this.mSpenInsertionEventDetector != null) {
                    BleSpenDeviceMainController.this.mSpenInsertionEventDetector.unregisterListener(BleSpenDeviceMainController.this.mSpenInsertionEventListenerForMagneticSensorDetector);
                }
                if (BleSpenDeviceMainController.this.mContext != null) {
                    BleSpenDeviceMainController.this.mContext.unregisterReceiver(BleSpenDeviceMainController.this.mBleStateChangeBroadcastReceiver);
                    BleSpenDeviceMainController.this.mContext.unregisterComponentCallbacks(BleSpenDeviceMainController.this.mTrimCallback);
                }
                BleSpenDeviceMainController.this.mBleSpenConnectionFlowController = null;
                BleSpenDeviceMainController.this.mSpenInsertionEventDetector = null;
                BleSpenDeviceMainController.this.mSensorActionDetector.release();
                BleSpenDeviceMainController.this.mSensorActionDetector = null;
                BleSpenDeviceMainController.this.mContext = null;
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), SystemClock.elapsedRealtime() - elapsedRealtime);
                }
            }
        };
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver != null) {
            bleSpenDriver.release(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.10
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    BleSpenDeviceMainController.this.mBleSpenDriver = null;
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    public void setDriverEventListener(BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        this.mBleSpenDriverListener = eventListener;
        this.mGattCallback = iBleGattCallback;
    }

    public void registerStateListener(BleSpenStateListener bleSpenStateListener) {
        if (!this.mBleSpenStateListenerTable.contains(bleSpenStateListener)) {
            this.mBleSpenStateListenerTable.add(bleSpenStateListener);
            return;
        }
        Log.e(TAG, "registerStateListener : already registered listener!");
        Assert.fail("already registered listener!");
    }

    public void unregisterStateListener(BleSpenStateListener bleSpenStateListener) {
        this.mBleSpenStateListenerTable.remove(bleSpenStateListener);
    }

    public BleSpenInstanceId getSpenInstanceId() {
        return this.mSpenInstanceId;
    }

    public BleSpenDriver getBleSpenDriver() {
        return this.mBleSpenDriver;
    }

    public void setBatteryLevel(int i) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            return;
        }
        bleSpenDriver.setBatteryLevel(i);
    }

    public int getBatteryLevel() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            return -1;
        }
        return bleSpenDriver.getBatteryLevel();
    }

    public int getLastBatteryLevel() {
        return this.mLastBatteryLevel;
    }

    public String getFirmwareVersion() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        return bleSpenDriver == null ? Constants.packageName.NONE : bleSpenDriver.getFirmwareVersion();
    }

    public BleSpenApproachState getSpenApproachState() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            return BleSpenApproachState.UNKNOWN;
        }
        return bleSpenDriver.getSpenApproachState();
    }

    public BleSpenChargeState getChargeState() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            return BleSpenChargeState.UNKNOWN;
        }
        return bleSpenDriver.getChargeState();
    }

    public BleSpenOperationMode getCurrentOperationMode() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            return null;
        }
        return bleSpenDriver.getCurrentOperationMode();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getBundleBleSpenAddress() {
        BleSpenInstanceId bundledSpenInstanceId = BleSpenPairedSpenManager.getInstance(this.mContext).getBundledSpenInstanceId();
        if (bundledSpenInstanceId != null) {
            return SpenInstanceIdHelper.from(this.mContext, bundledSpenInstanceId).getSpenAddress();
        }
        return null;
    }

    public String getPenColorCode() {
        return this.mBleSpenDriver.getPenColorCode();
    }

    public ConnectionState getConnectionState() {
        return this.mConnectionState;
    }

    public void readRemoteRssi(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "readRemoteRssi");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "readRemoteRssi : driver is null");
        } else {
            bleSpenDriver.readRemoteRssi(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.11
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            });
        }
    }

    public void readRawBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "readRawBatteryLevel");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "readRawBatteryLevel : driver is null");
        } else {
            bleSpenDriver.readRawBatteryLevel(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.12
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str2 = BleSpenDeviceMainController.TAG;
                    Log.d(str2, "readRawBatteryLevel : Raw battery level(adcValue) is " + bleOpResultData.getRawBatteryLevel());
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            });
        }
    }

    public void enableRawSensorDataNotification(boolean z, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enableRawSensorDataNotification");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "enableRawSensorDataNotification : driver is null");
        } else {
            bleSpenDriver.enableRawSensorDataNotification(z, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.13
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str2 = BleSpenDeviceMainController.TAG;
                    Log.d(str2, "enableRawSensorDataNotification : result=" + bleOpResultData.getResultCode() + " elapsed=" + j);
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            });
        }
    }

    public void enableSelfTestNotification(boolean z, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enableSelfTestNotification");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "enableSelfTestNotification : driver is null");
        } else {
            bleSpenDriver.enableSelfTestNotification(z, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.14
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str2 = BleSpenDeviceMainController.TAG;
                    Log.d(str2, "enableSelfTestNotification : result=" + bleOpResultData.getResultCode() + " elapsed=" + j);
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            });
        }
    }

    public void enablePenLogNotification(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enablePenLogNotification");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "enablePenLogNotification : driver is null");
        } else {
            bleSpenDriver.enablePenLogNotification(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.15
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str2 = BleSpenDeviceMainController.TAG;
                    Log.d(str2, "enablePenLogNotification : result=" + bleOpResultData.getResultCode() + " elapsed=" + j);
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            });
        }
    }

    public void setSlaveLatency(int i, BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "setSlaveLatency : driver is null");
        } else {
            bleSpenDriver.setSlaveLatency(i, operationFinishListener);
        }
    }

    public void setConnectionInterval(BleSpenConnectionInterval bleSpenConnectionInterval, BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.i(str, "setConnectionInterval : " + bleSpenConnectionInterval);
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "setConnectionInterval : driver is null");
        } else {
            bleSpenDriver.setConnectionInterval(bleSpenConnectionInterval, operationFinishListener);
        }
    }

    public void setSpenOperationMode(BleSpenOperationMode bleSpenOperationMode, BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "setSpenOperationMode : " + bleSpenOperationMode);
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "setSpenOperationMode : driver is null");
        } else {
            bleSpenDriver.setSpenOperationMode(bleSpenOperationMode, operationFinishListener);
        }
    }

    public void sendFastDischargeCommand(int i, BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "sendFastDischargeCommand : cmd=" + i);
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "sendFastDischargeCommand : driver is null");
        } else {
            bleSpenDriver.sendFastDischargeCommand(i, operationFinishListener);
        }
    }

    public void performCalibration(BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.i(str, "performCalibration");
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(str, "performCalibration : driver is null");
        } else {
            bleSpenDriver.performCalibration(operationFinishListener);
        }
    }

    public void requestGetFmmConfig(BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "requestGetFmmConfig : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        bleSpenDriver.requestGetFmmConfig(operationFinishListener);
    }

    public void readPenLog(BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "readPenLog : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        bleSpenDriver.readPenLog(operationFinishListener);
    }

    public void setLedState(BleSpenLedState bleSpenLedState, BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (this.mBleSpenDriver == null) {
            Log.e(TAG, "setLedState : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        String str = TAG;
        Log.i(str, "setLedState : state : " + bleSpenLedState.toString());
        this.mBleSpenDriver.setLedState(bleSpenLedState, operationFinishListener);
    }

    public void getLedState(BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "getLedState : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        bleSpenDriver.getLedState(operationFinishListener);
    }

    public BleSpenFrequency getPenFrequency() {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "getPenFrequency : driver is null");
            return BleSpenFrequency.UNKNOWN;
        }
        return bleSpenDriver.getPenFrequency();
    }

    public void requestSetFmmConfig(FmmConfig fmmConfig, BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "requestSetFmmConfig : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        bleSpenDriver.requestSetFmmConfig(fmmConfig, operationFinishListener);
    }

    public void enablePenClockNotification(BleSpenDriver.OperationFinishListener operationFinishListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "enablePenClockNotification : driver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.UNKNOWN_FAIL), 0L);
            return;
        }
        bleSpenDriver.enablePenClockNotification(operationFinishListener);
    }

    public void requestFirmwareUpgrade(File file, BleSpenFirmwareUpgradeListener bleSpenFirmwareUpgradeListener) {
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver == null) {
            Log.e(TAG, "requestFirmwareUpgrade : driver is null");
            bleSpenFirmwareUpgradeListener.onFinish(BleOpResultCode.FIRMWARE_UPGRADE_FAIL);
            return;
        }
        bleSpenDriver.requestFirmwareUpgrade(file, bleSpenFirmwareUpgradeListener);
    }

    public SpenModelName getTargetSpenModelName() {
        return this.mTargetSpenModelName;
    }

    public void startBleSpenAutoConnection(boolean z) {
        String str = TAG;
        Log.i(str, "startBleSpenAutoConnection");
        if (this.mIsBleSpenAutoConnectionEnabled) {
            Log.d(str, "startBleSpenAutoConnection : already started");
            return;
        }
        this.mIsBleSpenAutoConnectionEnabled = true;
        this.mSpenInsertionEventDetector.registerListener(this.mSpenInsertionEventListener);
        if (z) {
            this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.16
                @Override // java.lang.Runnable
                public void run() {
                    if (!BleSpenDeviceMainController.this.mIsBleSpenAutoConnectionEnabled) {
                        Log.i(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : auto connection mode is disabled");
                    } else if (!BleSpenDeviceMainController.this.mTargetSpenDeviceFeature.isBundleTypeSpen()) {
                        Log.d(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : Not advertise condition - not bundleTypeSpen");
                    } else if (!SpenAdvertiseMonitor.canSpenAdvertise(BleSpenDeviceMainController.this.mContext, BleSpenDeviceMainController.this.mTargetSpenModelName)) {
                        Log.d(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : Not advertise condition");
                        if (BleSpenDeviceMainController.this.mTargetSpenDeviceFeature.isBundleTypeSpen() && SettingsValueManager.getInstance(BleSpenDeviceMainController.this.mContext).isKeepConnectedEnabled() && !SpenInsertionEventDetector.getInstance(BleSpenDeviceMainController.this.mContext).isInserted()) {
                            Log.d(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : need to insert spen");
                        }
                    } else {
                        Log.i(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : run : Trying to connect attached spen");
                        if (BleSpenDeviceMainController.this.connectToAttachedPen(new BleConnReqData(BleSpenDeviceMainController.this.mTargetSpenModelName, BleConnTriggerCode.START_AUTO_CONNECTION, BleSpenDeviceMainController.this.getBundleBleSpenAddress()))) {
                            return;
                        }
                        Log.e(BleSpenDeviceMainController.TAG, "startBleSpenAutoConnection : run : not connectable condition.");
                    }
                }
            }, AirActionTutorialConstant.DESCRIPTION_SHOW_DELAY);
        }
    }

    public void stopBleSpenAutoConnection(boolean z, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.i(str, "stopBleSpenAutoConnection : forceDisconnect = " + z);
        if (!this.mIsBleSpenAutoConnectionEnabled) {
            Log.d(str, "stopBleSpenAutoConnection : already stopped");
        }
        this.mIsBleSpenAutoConnectionEnabled = false;
        this.mSpenInsertionEventDetector.unregisterListener(this.mSpenInsertionEventListener);
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.17
            @Override // java.lang.Runnable
            public void run() {
                BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
                bleOpResultData.setMessage("Force disconnection due to auto connection(SPen insertion) is stopped");
                BleSpenDeviceMainController.this.closeBleSpenDriver(bleOpResultData, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.17.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData2, long j) {
                        BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                        if (operationFinishListener2 != null) {
                            operationFinishListener2.onFinish(bleOpResultData2, j);
                        }
                    }
                });
            }
        };
        if (!z) {
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), 0L);
            }
        } else if (getConnectionState() == ConnectionState.CONNECTING) {
            Log.i(str, "stopBleSpenAutoConnection : Cancelling the connecting sequence..");
            cancelConnecting(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.18
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    public boolean isBleSpenAutoConnectionEnabled() {
        return this.mIsBleSpenAutoConnectionEnabled;
    }

    public boolean connectToAttachedPen(BleConnReqData bleConnReqData) {
        if (!SpenAdvertiseMonitor.canSpenAdvertise(this.mContext, this.mTargetSpenModelName)) {
            Log.e(TAG, "connectToAttachedPen : pen cannot advertise");
            return false;
        } else if (!isDisconnected()) {
            Log.e(TAG, "connectToAttachedPen : already connecting or connected");
            return false;
        } else {
            return startPenConnectionTransaction(bleConnReqData);
        }
    }

    public void cancelConnecting(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "cancelConnecting");
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        ConnectionState connectionState = getConnectionState();
        if (connectionState != ConnectionState.CONNECTING) {
            Log.e(str, "cancelConnecting : not connecting state. " + connectionState);
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.API_CALL_FAIL), 0L);
            }
        } else if (!this.mBleSpenConnectionFlowController.isConnectionInProgress()) {
            Log.e(str, "cancelConnecting : conn flow controller is not working");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.API_CALL_FAIL), 0L);
            }
        } else {
            this.mBleSpenConnectionFlowController.cancelOperation(new BleCancellableOperation.FinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.19
                @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation.FinishListener
                public void onFinish(BleOpResultData bleOpResultData) {
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, SystemClock.elapsedRealtime() - elapsedRealtime);
                    }
                }
            });
        }
    }

    public void disconnect(BleDisconnReqData bleDisconnReqData, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.20
            @Override // java.lang.Runnable
            public void run() {
                BleSpenDeviceMainController.this.mBleSpenDriver.disconnect(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.20.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        String str = BleSpenDeviceMainController.TAG;
                        Log.d(str, "disconnect : result=" + resultCode);
                        BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                        if (operationFinishListener2 != null) {
                            operationFinishListener2.onFinish(bleOpResultData, j);
                        }
                    }
                });
            }
        };
        this.mDisconnReqData = bleDisconnReqData;
        if (bleDisconnReqData != null) {
            String str = TAG;
            Log.d(str, "disconnect : disconnect request Trigger :" + this.mDisconnReqData.getTriggerCode());
        } else {
            Log.d(TAG, "disconnect : disconnect request Trigger : null");
        }
        if (getConnectionState() == ConnectionState.CONNECTING) {
            Log.d(TAG, "disconnect : connection is in progressing..");
            cancelConnecting(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.21
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    Log.d(BleSpenDeviceMainController.TAG, "disconnect : connection cancelled. performing disconnect");
                    runnable.run();
                }
            });
            return;
        }
        String str2 = TAG;
        Log.d(str2, "disconnect");
        if (this.mBleSpenDriver == null) {
            Log.e(str2, "disconnect : driver is null");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.API_CALL_FAIL), 0L);
                return;
            }
            return;
        }
        runnable.run();
    }

    public void closeBleSpenDriver(final BleOpResultData bleOpResultData, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.22
            @Override // java.lang.Runnable
            public void run() {
                BleSpenDeviceMainController.this.setConnectionState(ConnectionState.DISCONNECTED, bleOpResultData);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), SystemClock.elapsedRealtime() - elapsedRealtime);
                }
            }
        };
        BleSpenDriver bleSpenDriver = this.mBleSpenDriver;
        if (bleSpenDriver != null) {
            bleSpenDriver.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.23
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData2, long j) {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean startPenConnectionTransaction(BleConnReqData bleConnReqData) {
        String str;
        boolean z;
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.e(TAG, "startPenConnectionTransaction : Not focused user");
            return false;
        } else if (this.mBleSpenConnectionFlowController.isConnectionInProgress()) {
            Log.e(TAG, "startPenConnectionTransaction : Connection transaction is already in progress");
            return false;
        } else {
            final long currentTimeMillis = System.currentTimeMillis();
            boolean isSpenHardResetStepEnabled = bleConnReqData.isSpenHardResetStepEnabled();
            int batteryLevel = bleConnReqData.getBatteryLevel();
            SpenModelName targetSpenModelName = bleConnReqData.getTargetSpenModelName();
            boolean isSupportWacomCharger = this.mTargetSpenDeviceFeature.isSupportWacomCharger();
            boolean isBundleTypeSpen = this.mTargetSpenDeviceFeature.isBundleTypeSpen();
            String targetAddress = bleConnReqData.getTargetAddress();
            boolean z2 = bleConnReqData.getTriggerCode() == BleConnTriggerCode.PEN_DETACH;
            Assert.e(targetSpenModelName == this.mTargetSpenModelName);
            if (isBundleTypeSpen && targetAddress == null) {
                targetAddress = getBundleBleSpenAddress();
            }
            if (isSupportWacomCharger) {
                if (targetAddress == null) {
                    Log.d(TAG, "startPenConnectionTransaction : last paired address is null");
                    isSpenHardResetStepEnabled = true;
                }
                if (isSpenHardResetStepEnabled) {
                    z = isSpenHardResetStepEnabled;
                    str = null;
                    this.mConnReqData = bleConnReqData;
                    BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
                    bleOpResultData.setMessage("Starts connect");
                    setConnectionState(ConnectionState.CONNECTING, bleOpResultData);
                    this.mBleSpenConnectionFlowController.connectToSpen(targetSpenModelName, str, z, batteryLevel, z2, new BleSpenConnectionFlowController.FinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.24
                        @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                        public void onFinish(BleOpResultData bleOpResultData2, String str2) {
                            BleOpResultCode resultCode = bleOpResultData2.getResultCode();
                            long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                            String str3 = BleSpenDeviceMainController.TAG;
                            Log.d(str3, "startPenConnectionTransaction : finished. result=" + resultCode.name() + " addr=" + str2 + " elapsed=" + currentTimeMillis2);
                            BleSpenDeviceMainController.this.onFinishPenConnectionTransaction(bleOpResultData2, str2);
                        }
                    }, this.mInternalBleSpenDriverListener, this.mGattCallback);
                    return true;
                }
            } else if (targetAddress == null) {
                Log.e(TAG, "startPenConnectionTransaction : Cannot determine target SPen address");
                return false;
            }
            str = targetAddress;
            z = isSpenHardResetStepEnabled;
            this.mConnReqData = bleConnReqData;
            BleOpResultData bleOpResultData2 = new BleOpResultData(BleOpResultCode.SUCCESS);
            bleOpResultData2.setMessage("Starts connect");
            setConnectionState(ConnectionState.CONNECTING, bleOpResultData2);
            this.mBleSpenConnectionFlowController.connectToSpen(targetSpenModelName, str, z, batteryLevel, z2, new BleSpenConnectionFlowController.FinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.24
                @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                public void onFinish(BleOpResultData bleOpResultData22, String str2) {
                    BleOpResultCode resultCode = bleOpResultData22.getResultCode();
                    long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                    String str3 = BleSpenDeviceMainController.TAG;
                    Log.d(str3, "startPenConnectionTransaction : finished. result=" + resultCode.name() + " addr=" + str2 + " elapsed=" + currentTimeMillis2);
                    BleSpenDeviceMainController.this.onFinishPenConnectionTransaction(bleOpResultData22, str2);
                }
            }, this.mInternalBleSpenDriverListener, this.mGattCallback);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onFinishPenConnectionTransaction(BleOpResultData bleOpResultData, String str) {
        BleOpResultCode resultCode = bleOpResultData.getResultCode();
        String str2 = TAG;
        Log.i(str2, "onFinishPenConnectionTransaction : " + resultCode);
        if (resultCode == BleOpResultCode.SUCCESS) {
            Assert.notNull(str);
            this.mPairedSpenManager.addSpen(this.mTargetSpenModelName, str);
            setConnectionState(ConnectionState.CONNECTED, bleOpResultData);
            cancelConnectionRetry();
            return;
        }
        setConnectionState(ConnectionState.DISCONNECTED, bleOpResultData);
        if (isConnectionRetryReserved()) {
            cancelConnectionRetry();
            this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.25
                @Override // java.lang.Runnable
                public void run() {
                    if (!SpenAdvertiseMonitor.canSpenAdvertise(BleSpenDeviceMainController.this.mContext, BleSpenDeviceMainController.this.mTargetSpenModelName) || !BleSpenDeviceMainController.this.mIsBleSpenAutoConnectionEnabled) {
                        Log.d(BleSpenDeviceMainController.TAG, "onFinishPenConnectionTransaction : Spen is detached or auto connection is disabled");
                        return;
                    }
                    Log.i(BleSpenDeviceMainController.TAG, "onFinishPenConnectionTransaction : Trying to reconnect to attached pen");
                    BleSpenDeviceMainController.this.startPenConnectionTransaction(new BleConnReqData(BleSpenDeviceMainController.this.mTargetSpenModelName, BleConnTriggerCode.PEN_INSERTION, SpenInstanceIdHelper.from(BleSpenDeviceMainController.this.mContext, BleSpenDeviceMainController.this.mPairedSpenManager.getLastPairedSpenInstanceId()).getSpenAddress()));
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean startPenAutoReconnectionTransaction(BleConnReqData bleConnReqData) {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.e(TAG, "startPenAutoReconnectionTransaction : Not focused user");
            return false;
        } else if (this.mBleSpenConnectionFlowController.isConnectionInProgress()) {
            Log.e(TAG, "startPenAutoReconnectionTransaction : Connection transaction is already in progress");
            return false;
        } else {
            this.mConnReqData = bleConnReqData;
            BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
            bleOpResultData.setMessage("Starts auto reconnect");
            setConnectionState(ConnectionState.CONNECTING, bleOpResultData);
            final long currentTimeMillis = System.currentTimeMillis();
            this.mBleSpenConnectionFlowController.performReconnectionSetupSequence(this.mTargetSpenModelName, new BleSpenConnectionFlowController.FinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.26
                @Override // com.samsung.remotespen.core.device.control.connection.BleSpenConnectionFlowController.FinishListener
                public void onFinish(final BleOpResultData bleOpResultData2, final String str) {
                    BleOpResultCode resultCode = bleOpResultData2.getResultCode();
                    long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                    String str2 = BleSpenDeviceMainController.TAG;
                    Log.d(str2, "startPenAutoReconnectionTransaction : finished. result=" + resultCode.name() + " addr=" + str + " elapsed=" + currentTimeMillis2);
                    if (bleOpResultData2.isSuccess()) {
                        BleSpenDeviceMainController.this.onFinishPenConnectionTransaction(bleOpResultData2, str);
                        return;
                    }
                    Log.e(BleSpenDeviceMainController.TAG, "startPenAutoReconnectionTransaction : reconnection failed. disconnecting..");
                    BleSpenDeviceMainController.this.mBleSpenDriver.disconnect(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.26.1
                        @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                        public void onFinish(BleOpResultData bleOpResultData3, long j) {
                            BleSpenDeviceMainController.this.onFinishPenConnectionTransaction(bleOpResultData2, str);
                        }
                    });
                }
            });
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setConnectionState(ConnectionState connectionState, BleOpResultData bleOpResultData) {
        setConnectionState(connectionState, new BleStateChangeInfo(bleOpResultData));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setConnectionState(ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
        String str = TAG;
        Log.i(str, "setConnectionState : " + connectionState.name());
        ConnectionState connectionState2 = this.mConnectionState;
        boolean z = connectionState != connectionState2;
        this.mConnectionState = connectionState;
        if (z) {
            bleStateChangeInfo.setConnReqData(this.mConnReqData);
            Iterator<BleSpenStateListener> it = this.mBleSpenStateListenerTable.iterator();
            while (it.hasNext()) {
                it.next().onConnectionStateChanged(this.mSpenInstanceId, connectionState, connectionState2, bleStateChangeInfo);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenInsertEvent(boolean z) {
        String str = TAG;
        Log.i(str, "onSpenInsertEvent : insertion=" + z);
        DiagnosisManager.getInstance(this.mContext).notifySpenInsertionStateChanged(z);
        if (z) {
            Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.27
                @Override // java.lang.Runnable
                public void run() {
                    if (!BleSpenDeviceMainController.this.mSpenInsertionEventDetector.isInserted()) {
                        Log.v(BleSpenDeviceMainController.TAG, "onSpenInsertEvent : Spen is detached");
                    } else if (!BleSpenDeviceMainController.this.mIsBleSpenAutoConnectionEnabled) {
                        Log.d(BleSpenDeviceMainController.TAG, "onSpenInsertEvent : auto connection is disabled");
                    } else if (BleSpenDeviceMainController.this.isDisconnected()) {
                        Log.d(BleSpenDeviceMainController.TAG, "onSpenInsertEvent : Trying to connect to attached pen");
                        BleSpenDeviceMainController.this.startPenConnectionTransaction(new BleConnReqData(BleSpenDeviceMainController.this.mTargetSpenModelName, BleConnTriggerCode.PEN_INSERTION, BleSpenDeviceMainController.this.getBundleBleSpenAddress()));
                    } else if (BleSpenDeviceMainController.this.isConnecting()) {
                        BleSpenDeviceMainController.this.reserveConnectionRetry();
                    } else {
                        String str2 = BleSpenDeviceMainController.TAG;
                        Log.d(str2, "onSpenInsertEvent : not disconnected. state = " + BleSpenDeviceMainController.this.mConnectionState.name());
                    }
                }
            };
            if (this.mIsBleSpenAutoConnectionEnabled) {
                this.mHandler.postDelayed(runnable, 1000L);
                return;
            } else {
                Log.d(str, "onSpenInsertEvent : auto connection is not enabled");
                return;
            }
        }
        cancelConnectionRetry();
    }

    public BleDisconnTriggerCode getDisconnTriggerCode() {
        BleDisconnReqData bleDisconnReqData = this.mDisconnReqData;
        if (bleDisconnReqData == null) {
            return null;
        }
        return bleDisconnReqData.getTriggerCode();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenHoverEvent(int i, int i2, int i3) {
        if (i == 9) {
            this.mSensorActionDetector.setHoverEnterState(true);
            this.mSensorActionDetector.setScreenTouchState(false);
        } else if (i != 10) {
        } else {
            this.mSensorActionDetector.setHoverEnterState(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBleStateChanged(int i, int i2) {
        ConnectionState connectionState = getConnectionState();
        String str = TAG;
        Log.i(str, "onBleStateChanged :" + BleUtils.convertBtStateToString(i2) + " -> " + BleUtils.convertBtStateToString(i) + " connState=" + connectionState.name());
        if (i != 10 || connectionState == ConnectionState.DISCONNECTED) {
            return;
        }
        if (getConnectionState() == ConnectionState.CONNECTING) {
            Log.w(str, "onBleStateChanged : BT OFF, receive BT OFF during connecting, ignore!!");
            return;
        }
        Log.d(str, "onBleStateChanged : BT OFF, set to DISCONNECTED state forcefully");
        BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.SUCCESS);
        bleOpResultData.setMessage("Force disconnect due to STATE_OFF");
        closeBleSpenDriver(bleOpResultData, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setReconnectReason(boolean z) {
        this.mIsReconnectReason = z;
    }

    public boolean isReconnectReason() {
        String str = TAG;
        Log.v(str, "isReconnectReason mIsReconnectReason=" + this.mIsReconnectReason);
        return this.mIsReconnectReason;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveConnectionRetry() {
        Log.v(TAG, "reserveConnectionRetry");
        this.mIsConnectionRetryReserved = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelConnectionRetry() {
        this.mIsConnectionRetryReserved = false;
    }

    public boolean isConnectionRetryReserved() {
        return this.mIsConnectionRetryReserved;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDisconnected() {
        ConnectionState connectionState = this.mConnectionState;
        return (connectionState == ConnectionState.CONNECTED || connectionState == ConnectionState.CONNECTING) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnecting() {
        return this.mConnectionState == ConnectionState.CONNECTING;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnected() {
        return this.mConnectionState == ConnectionState.CONNECTED;
    }

    private boolean isBleEnabled() {
        return BleUtils.isBleEnabled(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onTrimMemory(int i) {
        String str = TAG;
        Log.d(str, "onTrimMemory : " + i);
        if (i == 40 || i == 60 || i == 80) {
            doTrimMemory(i);
            return;
        }
        Log.d(str, "onTrimMemory : " + i + " is skipped");
    }

    private void doTrimMemory(int i) {
        boolean isAllowMultiplePensEnabled = SettingsValueManager.getInstance(this.mContext).isAllowMultiplePensEnabled();
        SpenInsertionEventDetector spenInsertionEventDetector = this.mSpenInsertionEventDetector;
        boolean isInserted = spenInsertionEventDetector != null ? spenInsertionEventDetector.isInserted() : false;
        if (!isInserted || isAllowMultiplePensEnabled) {
            String str = TAG;
            Log.d(str, "doTrimMemory : " + i + " is skipped, isPenInserted : " + isInserted + ", isAllowMultiplePensEnabled : " + isAllowMultiplePensEnabled);
            return;
        }
        CommonUtils.trimMemory();
    }

    public BleSpenSensorActionDetector getSensorActionDetector() {
        return this.mSensorActionDetector;
    }

    public void pauseGestureDetection(List<BleSpenGestureType> list) {
        this.mSensorActionDetector.pauseGestureDetection(list);
    }

    public void resumeAllGestureDetection() {
        this.mSensorActionDetector.resumeAllGestureDetection();
    }

    public String getConnectedDeviceAddress() {
        return this.mBleSpenDriver.getConnectedDeviceAddress();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isLinkLossStatus(Integer num) {
        if (num == null) {
            return false;
        }
        return num.intValue() == 8 || num.intValue() == 34;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRemoteDisconnect(Integer num) {
        return num != null && num.intValue() == 19;
    }
}
