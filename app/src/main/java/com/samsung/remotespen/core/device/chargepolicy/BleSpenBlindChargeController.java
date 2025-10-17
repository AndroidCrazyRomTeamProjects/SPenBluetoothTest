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
import com.samsung.remotespen.core.device.util.AlarmTimer;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.SafeHandlerThread;
import com.samsung.util.debug.Assert;

/* loaded from: classes.dex */
public class BleSpenBlindChargeController extends BleSpenChargeController {
    private static final long MAX_CONTINUOUS_CHARGE_DURATION = 600000;
    private static final long PERFORM_CHARGE_DELAY = 1000;
    private static final String TAG = "BleSpenBlindChargeController";
    private BroadcastReceiver mBroadcastReceiver;
    private Runnable mChargeCallback;
    private BleSpenChargeController.ChargeEnableStateListener mChargeEnableStateListener;
    private AlarmTimer mChargeStopTimer;
    private Handler mHandler;
    private SafeHandlerThread mHandlerThread;
    private boolean mIsChargeBlockedDueToBatteryTemperature;
    private boolean mIsRunning;
    private boolean mIsSupportAdvancedCharge;
    private SpenInsertionEventDetector.Listener mPenInsertionListener;
    private int mPhoneBatteryTemperature;
    private BleSpenChargeLockStateMonitor.StateChangedListener mSpenChargeLockListener;
    private BleSpenChargeLockStateMonitor mSpenChargeLockStateMonitor;
    private SpenInsertionEventDetector mSpenInsertionDetector;
    private StateProvider mStateProvider;
    private Object mSyncObj;
    private WacomChargingDriver mWacomChargeController;
    private PowerManager.WakeLock mWakeLock;

    /* loaded from: classes.dex */
    public interface StateProvider {
        boolean isBundledSpenDisconnected();
    }

    public BleSpenBlindChargeController(Context context, BleSpenDeviceMainController bleSpenDeviceMainController, StateProvider stateProvider) {
        super(context);
        this.mSyncObj = new Object();
        this.mIsRunning = false;
        this.mPhoneBatteryTemperature = 0;
        this.mIsChargeBlockedDueToBatteryTemperature = false;
        this.mIsSupportAdvancedCharge = false;
        this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.1
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
                        BleSpenBlindChargeController.this.onPhoneChargingStateChanged(false);
                        return;
                    case 1:
                        BleSpenBlindChargeController.this.onPhoneBatteryStateChanged(intent);
                        return;
                    case 2:
                        BleSpenBlindChargeController.this.onPhoneChargingStateChanged(true);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mChargeCallback = new Runnable() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.2
            @Override // java.lang.Runnable
            public void run() {
                BleSpenBlindChargeController.this.performCharge();
            }
        };
        this.mSpenChargeLockListener = new BleSpenChargeLockStateMonitor.StateChangedListener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.3
            @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeLockStateMonitor.StateChangedListener
            public void onChanged(boolean z) {
                String str = BleSpenBlindChargeController.TAG;
                Log.i(str, "SpenChargeLockListener : chargeLock=" + z);
                if (!BleSpenBlindChargeController.this.mIsRunning) {
                    Log.e(BleSpenBlindChargeController.TAG, "SpenChargeLockListener : not running state");
                } else if (z) {
                } else {
                    BleSpenBlindChargeController.this.reserveCharge();
                }
            }
        };
        this.mPenInsertionListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.4
            @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
            public void onInsertEvent(boolean z) {
                BleSpenBlindChargeController.this.onSpenInsertEvent(z);
            }
        };
        this.mSpenInsertionDetector = SpenInsertionEventDetector.getInstance(this.mContext);
        this.mWacomChargeController = BleSpenDeviceFactory.getInstance(bleSpenDeviceMainController.getTargetSpenModelName()).getWacomChargingDriver(this.mContext);
        this.mIsSupportAdvancedCharge = BleSpenDeviceFactory.getInstance(bleSpenDeviceMainController.getTargetSpenModelName()).getApplicationFeature().isSupportAdvancedCharge();
        this.mSpenChargeLockStateMonitor = BleSpenChargeLockStateMonitor.getInstance(context);
        if (!this.mIsSupportAdvancedCharge) {
            this.mChargeStopTimer = new AlarmTimer(context, this.mSyncObj);
        }
        this.mStateProvider = stateProvider;
        PowerManager.WakeLock newWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "BLE SPen charging controller");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void start() {
        String str = TAG;
        Log.i(str, "start");
        Assert.e(!this.mIsRunning, "Already running!");
        this.mSpenInsertionDetector.registerListener(this.mPenInsertionListener);
        this.mSpenChargeLockStateMonitor.registerListener(this.mSpenChargeLockListener);
        this.mIsRunning = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mPhoneBatteryTemperature = CommonUtils.getDeviceBatteryTemperature(this.mContext);
        Log.i(str, "start : current phone battery temperature = " + this.mPhoneBatteryTemperature);
        if (this.mHandlerThread != null) {
            Log.e(str, "start : Handler thread active");
            this.mHandlerThread.quitSafely();
        }
        SafeHandlerThread safeHandlerThread = new SafeHandlerThread("BlindSpenChargeHandleThread");
        this.mHandlerThread = safeHandlerThread;
        safeHandlerThread.start();
        this.mHandler = this.mHandlerThread.getHandler();
        reserveCharge();
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void stop() {
        Log.i(TAG, "stop");
        SafeHandlerThread safeHandlerThread = this.mHandlerThread;
        if (safeHandlerThread != null) {
            safeHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        this.mHandler.removeCallbacks(this.mChargeCallback);
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
        this.mSpenInsertionDetector.unregisterListener(this.mPenInsertionListener);
        this.mSpenChargeLockStateMonitor.unregisterListener(this.mSpenChargeLockListener);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        stopChargeStopTimer(true);
        this.mIsRunning = false;
    }

    @Override // com.samsung.remotespen.core.device.chargepolicy.BleSpenChargeController
    public void setChargeEnableStateListener(BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener) {
        if (chargeEnableStateListener != null) {
            Assert.e(this.mChargeEnableStateListener == null, "Temperature listener already set");
        }
        this.mChargeEnableStateListener = chargeEnableStateListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenInsertEvent(boolean z) {
        if (z) {
            Log.d(TAG, "onSpenInsertEvent : attached");
            reserveCharge();
            return;
        }
        Log.d(TAG, "onSpenInsertEvent : Detached");
        stopChargeStopTimer(true);
        stopCharge();
    }

    private void stopChargeStopTimer(boolean z) {
        AlarmTimer alarmTimer;
        if (this.mIsSupportAdvancedCharge || (alarmTimer = this.mChargeStopTimer) == null || !alarmTimer.isTimerWorking()) {
            return;
        }
        this.mChargeStopTimer.stopTimer(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPhoneChargingStateChanged(boolean z) {
        String str = TAG;
        Log.i(str, "onPhoneChargingStateChanged : isCharging=" + z);
        reserveCharge();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPhoneBatteryStateChanged(Intent intent) {
        int phoneBatteryTemperatureFromIntent = getPhoneBatteryTemperatureFromIntent(intent);
        if (this.mPhoneBatteryTemperature == phoneBatteryTemperatureFromIntent) {
            return;
        }
        this.mPhoneBatteryTemperature = phoneBatteryTemperatureFromIntent;
        if (!isDisconnected()) {
            Log.e(TAG, "onPhoneBatteryStateChanged : SPen is not disconnected state!");
            return;
        }
        String str = TAG;
        Log.d(str, "onPhoneBatteryStateChanged : battery temperature = " + phoneBatteryTemperatureFromIntent);
        if (isChargeBlockingBattTemperature(this.mPhoneBatteryTemperature)) {
            if (this.mIsChargeBlockedDueToBatteryTemperature) {
                return;
            }
            Log.i(str, "onPhoneBatteryStateChanged : stop the SPen charge. temperature = " + this.mPhoneBatteryTemperature);
            stopCharge();
            this.mIsChargeBlockedDueToBatteryTemperature = true;
            BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener = this.mChargeEnableStateListener;
            if (chargeEnableStateListener != null) {
                chargeEnableStateListener.onTemperatureStateChanged(phoneBatteryTemperatureFromIntent, false);
            }
        } else if (isChargeableBattTemperature(this.mPhoneBatteryTemperature) && this.mIsChargeBlockedDueToBatteryTemperature) {
            Log.i(str, "onPhoneBatteryStateChanged : charge blocking unlocked. temperature = " + this.mPhoneBatteryTemperature);
            this.mIsChargeBlockedDueToBatteryTemperature = false;
            reserveCharge();
            BleSpenChargeController.ChargeEnableStateListener chargeEnableStateListener2 = this.mChargeEnableStateListener;
            if (chargeEnableStateListener2 != null) {
                chargeEnableStateListener2.onTemperatureStateChanged(phoneBatteryTemperatureFromIntent, true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onChargeStopTimerExpired(long j) {
        synchronized (this.mSyncObj) {
            boolean isInserted = this.mSpenInsertionDetector.isInserted();
            String str = TAG;
            Log.i(str, "onChargeStopTimerExpired : charged for " + ((SystemClock.elapsedRealtime() - j) / 1000) + "s, penInserted=" + isInserted);
            if (isInserted) {
                stopCharge();
            }
        }
    }

    private boolean isDisconnected() {
        StateProvider stateProvider = this.mStateProvider;
        if (stateProvider == null) {
            return true;
        }
        return stateProvider.isBundledSpenDisconnected();
    }

    private boolean isPhoneCharging() {
        return CommonUtils.isPowerPlugged(this.mContext);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reserveCharge() {
        synchronized (this.mSyncObj) {
            this.mWakeLock.acquire(60000L);
            this.mHandler.removeCallbacks(this.mChargeCallback);
            this.mHandler.postDelayed(this.mChargeCallback, 1000L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void performCharge() {
        synchronized (this.mSyncObj) {
            stopChargeStopTimer(false);
            if (!isDisconnected()) {
                Log.i(TAG, "performCharge : Not disconnected state");
            } else if (!this.mSpenInsertionDetector.isInserted()) {
                Log.i(TAG, "performCharge : Pen is not inserted");
            } else if (isChargeBlockingBattTemperature(this.mPhoneBatteryTemperature)) {
                String str = TAG;
                Log.i(str, "performCharge : stop the SPen charge. temperature = " + this.mPhoneBatteryTemperature);
                stopCharge();
                this.mIsChargeBlockedDueToBatteryTemperature = true;
            } else if (isPhoneCharging()) {
                Log.i(TAG, "performCharge : Starting continuous charging due to phone is under charging");
                startContinuousCharge();
            } else {
                Log.i(TAG, "performCharge : Stop SPen charging because phone is not charging state");
                stopCharge();
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
    }

    private void startContinuousCharge() {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.i(TAG, "startContinuousCharge : user not focused");
            return;
        }
        this.mWacomChargeController.turnOnWacomChargingModule();
        this.mWacomChargeController.startContinuousCharge();
        if (this.mIsSupportAdvancedCharge) {
            return;
        }
        this.mChargeStopTimer.reserveTimer(MAX_CONTINUOUS_CHARGE_DURATION, new AlarmTimer.Listener() { // from class: com.samsung.remotespen.core.device.chargepolicy.BleSpenBlindChargeController.5
            @Override // com.samsung.remotespen.core.device.util.AlarmTimer.Listener
            public void onTimerExpired(long j) {
                BleSpenBlindChargeController.this.onChargeStopTimerExpired(j);
            }
        });
    }

    private void stopCharge() {
        if (!CommonUtils.isMyUserFocused(this.mContext)) {
            Log.i(TAG, "stopCharge : user not focused");
            return;
        }
        this.mWacomChargeController.stopCharge();
        if (SettingsPreferenceManager.getInstance(this.mContext).getAirActionEnabled()) {
            return;
        }
        this.mWacomChargeController.turnOffWacomChargingModule();
    }
}
