package com.samsung.remotespen.core.device.chargepolicy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController;
import com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeLockStateMonitor;
import com.samsung.remotespen.core.device.control.BleSpenDeviceMainController;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.WacomChargingDriver;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.core.device.util.AlarmTimer;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.SafeHandlerThread;
import com.samsung.util.constants.SPenSettingsConstants;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;
import com.samsung.util.settings.SpenSettingObserver;

/* loaded from: classes.dex */
public class BleSpenGenericChargeController extends BleSpenChargeController {
    private static final long CHARGE_STATE_CHECK_DELAY = 1500;
    private static final long DISCONNECT_CHARGE_DURATION = 90000;
    private static final long DISCONNECT_CHARGE_PERIOD = 32400000;
    private static final long MAX_CONTINUOUS_CHARGE_DURATION = 600000;
    private static final long PERFORM_CHARGE_DELAY = 1000;
    private static final String TAG = "BleSpenGenericChargeController";
    private BleSpenDeviceMainController mBleSpenMainController;
    private BleSpenDeviceMainController.BleSpenStateListener mBleStateListener;
    private BroadcastReceiver mBroadcastReceiver;
    private BleSpenChargeController.ChargeEnableStateListener mChargeEnableStateListener;
    private Handler mHandler;
    private SafeHandlerThread mHandlerThread;
    private boolean mIsChargeBlockedDueToBatteryTemperature;
    private boolean mIsRunning;
    private boolean mIsShortTimeChargeEnabled;
    private boolean mIsSupportAdvancedCharge;
    private Runnable mLongTimeChargeCallback;
    private AlarmTimer mLongTimeChargeStopTimer;
    private Object mLongtimeChargeSyncObj;
    private BleSpenMotionSensorCalibrationController mMotionSensorCalibrationController;
    private SpenInsertionEventDetector.Listener mPenInsertionListener;
    private int mPhoneBatteryTemperature;
    private SpenSettingObserver.Listener mSettingChangedListener;
    private SpenSettingObserver mSettingObserver;
    private Object mShortTimeChargeSyncObj;
    private AlarmTimer mShortTimeChargeTimer;
    private BleSpenChargeLockStateMonitor.StateChangedListener mSpenChargeLockListener;
    private BleSpenChargeLockStateMonitor mSpenChargeLockStateMonitor;
    private SpenInsertionEventDetector mSpenInsertionDetector;
    private WacomChargingDriver mWacomChargeController;
    private PowerManager.WakeLock mWakeLock;

    public BleSpenGenericChargeController(Context context, BleSpenDeviceMainController bleSpenDeviceMainController) {
        super(context);
        this.mLongtimeChargeSyncObj = new Object();
        this.mShortTimeChargeSyncObj = new Object();
        this.mIsSupportAdvancedCharge = false;
        this.mIsRunning = false;
        this.mPhoneBatteryTemperature = 0;
        this.mIsChargeBlockedDueToBatteryTemperature = false;
        this.mIsShortTimeChargeEnabled = false;
        this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                String action = intent.getAction();
                action.hashCode();
                char c = 65535;
                switch (action.hashCode()) {
                    case -1886648615:
                        if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1538406691:
                        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 1019184907:
                        if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                            c = 2;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        BleSpenGenericChargeController.this.onPhoneChargingStateChanged(false);
                        return;
                    case 1:
                        BleSpenGenericChargeController.this.onPhoneBatteryStateChanged(intent);
                        return;
                    case 2:
                        BleSpenGenericChargeController.this.onPhoneChargingStateChanged(true);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mLongTimeChargeCallback = new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.2
            @Override // java.lang.Runnable
            public void run() {
                BleSpenGenericChargeController.this.performLongTimeCharge();
            }
        };
        this.mSpenChargeLockListener = new BleSpenChargeLockStateMonitor.StateChangedListener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.3
            @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeLockStateMonitor.StateChangedListener
            public void onChanged(boolean z) {
                String str = BleSpenGenericChargeController.TAG;
                Log.i(str, "SpenChargeLockListener : chargeLock=" + z);
                if (!BleSpenGenericChargeController.this.mIsRunning) {
                    Log.e(BleSpenGenericChargeController.TAG, "SpenChargeLockListener : not running state");
                } else if (z || !BleSpenGenericChargeController.this.isConnected()) {
                } else {
                    BleSpenGenericChargeController.this.mWacomChargeController.turnOnWacomChargingModule();
                    BleSpenGenericChargeController.this.reserveLongTimeCharge();
                }
            }
        };
        this.mBleStateListener = new BleSpenDeviceMainController.BleSpenStateListener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.4
            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onAirGestureActionEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenGestureEvent bleSpenGestureEvent) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onButtonEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenButtonEvent bleSpenButtonEvent) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onChargeStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenChargeState bleSpenChargeState) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onLedStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenLedState bleSpenLedState) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onPenFrequencyChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenFrequency bleSpenFrequency) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent) {
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onConnectionStateChanged(BleSpenInstanceId bleSpenInstanceId, BleSpenDeviceMainController.ConnectionState connectionState, BleSpenDeviceMainController.ConnectionState connectionState2, BleStateChangeInfo bleStateChangeInfo) {
                BleSpenGenericChargeController.this.onConnectionStateChanged(bleSpenInstanceId, connectionState, bleStateChangeInfo);
            }

            @Override // com.samsung.remotespen.core.device.control.BleSpenDeviceMainController.BleSpenStateListener
            public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
                BleSpenGenericChargeController.this.onBatteryLevelChanged(bleSpenInstanceId, i, i2);
            }
        };
        this.mPenInsertionListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.5
            @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
            public void onInsertEvent(boolean z) {
                BleSpenGenericChargeController.this.onSpenInsertEvent(z);
            }
        };
        this.mSettingChangedListener = new SpenSettingObserver.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.6
            @Override // com.samsung.util.settings.SpenSettingObserver.Listener
            public void onSettingChanged() {
                BleSpenGenericChargeController bleSpenGenericChargeController = BleSpenGenericChargeController.this;
                bleSpenGenericChargeController.mIsShortTimeChargeEnabled = !SettingsValueManager.getInstance(bleSpenGenericChargeController.mContext).isKeepConnectedEnabled();
            }
        };
        Assert.notNull(bleSpenDeviceMainController, "Main controller is null!");
        SpenModelName targetSpenModelName = bleSpenDeviceMainController.getTargetSpenModelName();
        this.mBleSpenMainController = bleSpenDeviceMainController;
        this.mSpenInsertionDetector = SpenInsertionEventDetector.getInstance(this.mContext);
        this.mSpenChargeLockStateMonitor = BleSpenChargeLockStateMonitor.getInstance(context);
        boolean isSupportAdvancedCharge = BleSpenDeviceFactory.getInstance(targetSpenModelName).getApplicationFeature().isSupportAdvancedCharge();
        this.mIsSupportAdvancedCharge = isSupportAdvancedCharge;
        if (!isSupportAdvancedCharge) {
            this.mLongTimeChargeStopTimer = new AlarmTimer(context, this.mLongtimeChargeSyncObj);
        }
        this.mShortTimeChargeTimer = new AlarmTimer(context, this.mShortTimeChargeSyncObj);
        WacomChargingDriver wacomChargingDriver = BleSpenDeviceFactory.getInstance(targetSpenModelName).getWacomChargingDriver(this.mContext);
        this.mWacomChargeController = wacomChargingDriver;
        Assert.notNull(wacomChargingDriver);
        PowerManager.WakeLock newWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "BLE SPen charging controller");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        this.mMotionSensorCalibrationController = new BleSpenMotionSensorCalibrationController(this.mContext, this.mBleSpenMainController);
        this.mSettingObserver = new SpenSettingObserver(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean stopLongTimeChargeTimer(boolean z) {
        AlarmTimer alarmTimer;
        if (this.mIsSupportAdvancedCharge || (alarmTimer = this.mLongTimeChargeStopTimer) == null || !alarmTimer.isTimerWorking()) {
            return false;
        }
        this.mLongTimeChargeStopTimer.stopTimer(z);
        return true;
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void start() {
        String str = TAG;
        Log.i(str, "start");
        Assert.e(!this.mIsRunning, "Already running!");
        this.mBleSpenMainController.registerStateListener(this.mBleStateListener);
        this.mSpenInsertionDetector.registerListener(this.mPenInsertionListener);
        this.mSpenChargeLockStateMonitor.registerListener(this.mSpenChargeLockListener);
        this.mIsRunning = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mPhoneBatteryTemperature = CommonUtils.getDeviceBatteryTemperature(this.mContext);
        Log.d(str, "start : current phone battery temperature = " + this.mPhoneBatteryTemperature);
        if (this.mHandlerThread != null) {
            Log.e(str, "start : Handler thread active");
            this.mHandlerThread.quit();
        }
        SafeHandlerThread safeHandlerThread = new SafeHandlerThread("SpenChargeHandleThread");
        this.mHandlerThread = safeHandlerThread;
        safeHandlerThread.start();
        this.mHandler = this.mHandlerThread.getHandler();
        this.mMotionSensorCalibrationController.start();
        this.mSettingObserver.registerObserver(SPenSettingsConstants.URI_KEEP_CONNECTED, this.mSettingChangedListener);
        if (SettingsValueManager.getInstance(this.mContext).isKeepConnectedEnabled()) {
            return;
        }
        this.mIsShortTimeChargeEnabled = true;
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void stop() {
        Log.i(TAG, "stop");
        SafeHandlerThread safeHandlerThread = this.mHandlerThread;
        if (safeHandlerThread != null) {
            safeHandlerThread.quit();
            this.mHandlerThread = null;
        }
        this.mMotionSensorCalibrationController.stop();
        this.mHandler.removeCallbacks(this.mLongTimeChargeCallback);
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        this.mBleSpenMainController.unregisterStateListener(this.mBleStateListener);
        this.mSpenInsertionDetector.unregisterListener(this.mPenInsertionListener);
        this.mSpenChargeLockStateMonitor.unregisterListener(this.mSpenChargeLockListener);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mShortTimeChargeTimer.stopTimer(false);
        stopLongTimeChargeTimer(true);
        this.mIsRunning = false;
        this.mSettingObserver.unregisterObserver();
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void setChargeEnableStateListener(BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener) {
        if (chargeEnableStateListener != null) {
            Assert.e(this.mChargeEnableStateListener == null, "Temperature listener already set");
        }
        this.mChargeEnableStateListener = chargeEnableStateListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onConnectionStateChanged(final BleSpenInstanceId bleSpenInstanceId, final BleSpenDeviceMainController.ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.7
            @Override // java.lang.Runnable
            public void run() {
                if (bleSpenInstanceId.equals(BleSpenGenericChargeController.this.mBleSpenMainController.getSpenInstanceId())) {
                    BleSpenDeviceMainController.ConnectionState connectionState2 = connectionState;
                    if (connectionState2 == BleSpenDeviceMainController.ConnectionState.CONNECTED) {
                        String str = BleSpenGenericChargeController.TAG;
                        Log.i(str, "onConnectionStateChanged (State =  " + connectionState + ") : stop short time charge & start long time charge");
                        BleSpenGenericChargeController.this.mShortTimeChargeTimer.stopTimer(false);
                        BleSpenGenericChargeController.this.reserveLongTimeCharge();
                    } else if (connectionState2 == BleSpenDeviceMainController.ConnectionState.DISCONNECTED) {
                        BleSpenGenericChargeController.this.stopLongTimeChargeTimer(true);
                        if (!BleSpenGenericChargeController.this.isPhoneCharging()) {
                            if (BleSpenGenericChargeController.this.canReserveShortTimeCharge()) {
                                BleSpenGenericChargeController.this.reserveShortTimeCharge(BleSpenGenericChargeController.DISCONNECT_CHARGE_PERIOD);
                                String str2 = BleSpenGenericChargeController.TAG;
                                Log.i(str2, "onConnectionStateChanged (State =  " + connectionState + ") : stop long time charge & start short time charge");
                                return;
                            }
                            return;
                        }
                        BleSpenGenericChargeController.this.reserveLongTimeCharge();
                        String str3 = BleSpenGenericChargeController.TAG;
                        Log.i(str3, "onConnectionStateChanged (State =  " + connectionState + ") : stop long time charge & restart long time charge(in phone charging state)");
                    }
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenInsertEvent(boolean z) {
        String str = TAG;
        Log.i(str, "onSpenInsertEvent :" + z);
        if (!z) {
            stopLongTimeChargeTimer(true);
        } else if (isConnected()) {
            reserveLongTimeCharge();
        } else if (canReserveShortTimeCharge()) {
            reserveShortTimeCharge(DISCONNECT_CHARGE_PERIOD);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBatteryLevelChanged(BleSpenInstanceId bleSpenInstanceId, int i, int i2) {
        if (this.mBleSpenMainController.getSpenInstanceId().equals(bleSpenInstanceId) && isConnected() && this.mSpenInsertionDetector.isInserted()) {
            reserveLongTimeCharge();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPhoneChargingStateChanged(boolean z) {
        int batteryLevel = this.mBleSpenMainController.getBatteryLevel();
        String str = TAG;
        Log.i(str, "onPhoneChargingStateChanged : isCharging=" + z + " Pen Battery Level=" + batteryLevel);
        if (z) {
            reserveLongTimeCharge();
            return;
        }
        if (batteryLevel >= 100 || isDisconnected()) {
            Log.i(str, "onPhoneChargingStateChanged : stops the charging due to SPen is fully charged or disconnected state");
            if (!stopLongTimeChargeTimer(true)) {
                Log.i(str, "onPhoneChargingStateChanged : charging timer is not running");
                if (isConnected()) {
                    this.mWacomChargeController.stopCharge();
                }
            }
        }
        if (canReserveShortTimeCharge()) {
            reserveShortTimeCharge(DISCONNECT_CHARGE_PERIOD);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPhoneBatteryStateChanged(Intent intent) {
        int phoneBatteryTemperatureFromIntent = getPhoneBatteryTemperatureFromIntent(intent);
        if (this.mPhoneBatteryTemperature == phoneBatteryTemperatureFromIntent) {
            return;
        }
        this.mPhoneBatteryTemperature = phoneBatteryTemperatureFromIntent;
        if (this.mBleSpenMainController.getConnectionState() != BleSpenDeviceMainController.ConnectionState.CONNECTED) {
            return;
        }
        String str = TAG;
        Log.d(str, "onPhoneBatteryStateChanged : battery temperature = " + phoneBatteryTemperatureFromIntent);
        if (isChargeBlockingBattTemperature(this.mPhoneBatteryTemperature)) {
            if (this.mIsChargeBlockedDueToBatteryTemperature) {
                return;
            }
            Log.i(str, "onPhoneBatteryStateChanged : stop the SPen charge. temperature = " + this.mPhoneBatteryTemperature);
            this.mWacomChargeController.stopCharge();
            this.mIsChargeBlockedDueToBatteryTemperature = true;
            BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener = this.mChargeEnableStateListener;
            if (chargeEnableStateListener != null) {
                chargeEnableStateListener.onTemperatureStateChanged(phoneBatteryTemperatureFromIntent, false);
            }
        } else if (isChargeableBattTemperature(this.mPhoneBatteryTemperature) && this.mIsChargeBlockedDueToBatteryTemperature) {
            Log.i(str, "onPhoneBatteryStateChanged : charge blocking unlocked. temperature = " + this.mPhoneBatteryTemperature);
            this.mIsChargeBlockedDueToBatteryTemperature = false;
            if (isConnected() || isPhoneCharging()) {
                reserveLongTimeCharge();
            }
            BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener2 = this.mChargeEnableStateListener;
            if (chargeEnableStateListener2 != null) {
                chargeEnableStateListener2.onTemperatureStateChanged(phoneBatteryTemperatureFromIntent, true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onContinuousChargeStopTimerExpired(long j) {
        synchronized (this.mLongtimeChargeSyncObj) {
            boolean isInserted = this.mSpenInsertionDetector.isInserted();
            String str = TAG;
            Log.i(str, "onChargeStopTimerExpired : charged for " + ((SystemClock.elapsedRealtime() - j) / 1000) + "s, penInserted=" + isInserted);
            if (isInserted) {
                this.mWacomChargeController.stopCharge();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean canReserveShortTimeCharge() {
        if (this.mIsShortTimeChargeEnabled && isDisconnected()) {
            if (this.mShortTimeChargeTimer.isTimerWorking()) {
                Log.i(TAG, "canReserveDisconnectCharge: Timer is already working");
                return false;
            }
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnected() {
        return this.mBleSpenMainController.getConnectionState() == BleSpenDeviceMainController.ConnectionState.CONNECTED;
    }

    private boolean isDisconnected() {
        return this.mBleSpenMainController.getConnectionState() == BleSpenDeviceMainController.ConnectionState.DISCONNECTED;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isPhoneCharging() {
        return CommonUtils.isPowerPlugged(this.mContext);
    }

    public void reserveLongTimeCharge() {
        synchronized (this.mLongtimeChargeSyncObj) {
            this.mWakeLock.acquire(60000L);
            this.mHandler.removeCallbacks(this.mLongTimeChargeCallback);
            this.mHandler.postDelayed(this.mLongTimeChargeCallback, 1000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performLongTimeCharge() {
        synchronized (this.mLongtimeChargeSyncObj) {
            stopLongTimeChargeTimer(!isConnected());
            this.mShortTimeChargeTimer.stopTimer(false);
            if (!isConnected() && !isDisconnected()) {
                Log.i(TAG, "performLongTimeCharge : Pen is in Connecting State");
            } else if (!this.mSpenInsertionDetector.isInserted()) {
                Log.i(TAG, "performLongTimeCharge : Pen is not inserted");
            } else if (isChargeBlockingBattTemperature(this.mPhoneBatteryTemperature)) {
                String str = TAG;
                Log.i(str, "performLongTimeCharge : stop the SPen charge. temperature = " + this.mPhoneBatteryTemperature);
                this.mWacomChargeController.stopCharge();
                this.mIsChargeBlockedDueToBatteryTemperature = true;
            } else {
                int batteryLevel = this.mBleSpenMainController.getBatteryLevel();
                if (isConnected() && batteryLevel < 100) {
                    String str2 = TAG;
                    Log.i(str2, "performLongTimeCharge : Batt : " + batteryLevel + ", Starting continuous charging");
                    startContinuousCharge();
                } else if (isPhoneCharging()) {
                    String str3 = TAG;
                    Log.i(str3, "performLongTimeCharge : Batt : " + batteryLevel + ", Starting continuous charging due to phone is under charging");
                    startContinuousCharge();
                } else {
                    String str4 = TAG;
                    Log.i(str4, "performLongTimeCharge : Batt : " + batteryLevel + ", Starting additional charge for 100% battery level ");
                    start90SecCharging();
                }
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveShortTimeCharge(long j) {
        String str = TAG;
        Log.d(str, "reserveShortTimeCharge : duration = " + (j / 1000) + " sec");
        this.mShortTimeChargeTimer.reserveTimer(j, new AlarmTimer.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.8
            @Override // com.samsung.remotespen.core.device.util.AlarmTimer.Listener
            public void onTimerExpired(long j2) {
                if (BleSpenGenericChargeController.this.mSpenInsertionDetector.isInserted()) {
                    BleSpenGenericChargeController.this.start90SecCharging();
                }
                Log.d(BleSpenGenericChargeController.TAG, "reserveDisconnectCharge : onTimerExpired : reserveDisconnectCharge");
                BleSpenGenericChargeController.this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.8.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (BleSpenGenericChargeController.this.canReserveShortTimeCharge()) {
                            BleSpenGenericChargeController.this.reserveShortTimeCharge(32490000L);
                        }
                    }
                });
            }
        });
    }

    private void startContinuousCharge() {
        startContinuousCharge(MAX_CONTINUOUS_CHARGE_DURATION);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void start90SecCharging() {
        Log.d(TAG, "start 90 Sec Charging");
        this.mWacomChargeController.stopCharge();
        CommonUtils.sleep(500L);
        this.mWacomChargeController.startCharge();
    }

    private void startContinuousCharge(final long j) {
        this.mWacomChargeController.stopCharge();
        CommonUtils.sleep(500L);
        this.mWacomChargeController.startContinuousCharge();
        this.mHandler.postDelayed(new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.9
            @Override // java.lang.Runnable
            public void run() {
                if (BleSpenGenericChargeController.this.mBleSpenMainController.getChargeState() == BleSpenChargeState.DISCHARGING) {
                    Log.e(BleSpenGenericChargeController.TAG, "startContinuousCharge : SPen is discharging state. so stop the charging");
                    BleSpenGenericChargeController.this.mWacomChargeController.stopCharge();
                } else if (BleSpenGenericChargeController.this.mIsSupportAdvancedCharge) {
                } else {
                    BleSpenGenericChargeController.this.mLongTimeChargeStopTimer.reserveTimer(j, new AlarmTimer.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenGenericChargeController.9.1
                        @Override // com.samsung.remotespen.core.device.util.AlarmTimer.Listener
                        public void onTimerExpired(long j2) {
                            BleSpenGenericChargeController.this.onContinuousChargeStopTimerExpired(j2);
                        }
                    });
                }
            }
        }, CHARGE_STATE_CHECK_DELAY);
    }
}
