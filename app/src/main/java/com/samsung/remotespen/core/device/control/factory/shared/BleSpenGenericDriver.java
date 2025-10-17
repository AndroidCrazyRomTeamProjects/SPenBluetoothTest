package com.samsung.remotespen.core.device.control.factory.shared;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.FirmwareUpgradeDriver;
import com.samsung.remotespen.core.device.control.factory.FmmDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenAccGyroEvent;
import com.samsung.remotespen.core.device.data.BleSpenAirMotionEvent;
import com.samsung.remotespen.core.device.data.BleSpenApproachState;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenSelfTestResultEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorId;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.UUID;

/* loaded from: classes.dex */
public abstract class BleSpenGenericDriver extends BleSpenDriver {
    private static final String SPEN_ID_PREFIX = "EMB_";
    private static final String TAG = "BleSpenGenericDriver";
    private int mBatteryLevel;
    private long mCalibrationStartTime;
    public BleSpenChargeState mChargeState;
    public String mFirmwareVersion;
    private LogComposer mLogComposer;
    private String mPenColorCode;
    private BleSpenFrequency mPenFrequency;
    private BleSpenApproachState mPenTipApproachState;
    public BleSpenDriver.OperationFinishListener mPerformCalibrationListener;

    public BleSpenGenericDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
        this.mBatteryLevel = -1;
        this.mFirmwareVersion = Constants.packageName.NONE;
        this.mChargeState = BleSpenChargeState.UNKNOWN;
        this.mPenColorCode = Constants.packageName.NONE;
        this.mCalibrationStartTime = 0L;
        this.mPenTipApproachState = BleSpenApproachState.UNKNOWN;
        this.mLogComposer = null;
        this.mPenFrequency = BleSpenFrequency.UNKNOWN;
        this.mPerformCalibrationListener = null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void close(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleSpenGenericDriver.this.mBatteryLevel = -1;
                BleSpenGenericDriver.this.mFirmwareVersion = Constants.packageName.NONE;
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void discoverService(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.discoverService(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.isSuccess() && BleSpenGenericDriver.this.mSpenDeviceFeature.isSupportPenColorCode()) {
                    BleSpenGenericDriver bleSpenGenericDriver = BleSpenGenericDriver.this;
                    bleSpenGenericDriver.mPenColorCode = bleSpenGenericDriver.getPenColorCodeFromDeviceName();
                    if (BleSpenGenericDriver.this.mPenColorCode == null) {
                        Log.e(BleSpenGenericDriver.TAG, "discoverService : failed to get color code");
                        BleSpenGenericDriver.this.mPenColorCode = Constants.packageName.NONE;
                    }
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onConnectionStateChanged(IBleGatt iBleGatt, int i, int i2) {
        super.onConnectionStateChanged(iBleGatt, i, i2);
        if (i2 != 0) {
            if (i2 != 2) {
                return;
            }
            this.mChargeState = BleSpenChargeState.UNKNOWN;
            this.mPenTipApproachState = BleSpenApproachState.UNKNOWN;
            this.mPenFrequency = BleSpenFrequency.UNKNOWN;
            return;
        }
        BleSpenChargeState bleSpenChargeState = BleSpenChargeState.UNKNOWN;
        this.mChargeState = bleSpenChargeState;
        this.mPenTipApproachState = BleSpenApproachState.UNKNOWN;
        this.mPenFrequency = BleSpenFrequency.UNKNOWN;
        notifyChargeStateChanged(bleSpenChargeState);
        if (this.mPerformCalibrationListener != null) {
            invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.CALIBRATION_FAIL));
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        byte[] value;
        byte[] value2;
        UUID uuid = bluetoothGattCharacteristic.getUuid();
        if (BleSpenUuid.BUTTON_EVENT.equals(uuid) && (value2 = bluetoothGattCharacteristic.getValue()) != null && value2.length > 0 && (value2[0] & 255) == 224) {
            String str = TAG;
            Log.d(str, "onCharacteristicChanged : BUTTON_EVENT data size :" + value2.length + " / " + ((int) value2[0]));
            finishLogComposer();
        }
        if (BleSpenUuid.BATTERY_LEVEL.equals(uuid) && (value = bluetoothGattCharacteristic.getValue()) != null && value.length > 0) {
            int i = value[0] & 255;
            String str2 = TAG;
            Log.i(str2, "onCharacteristicChanged : Battery:" + i);
            if (i > 100) {
                Log.d(str2, "onCharacteristicChanged : ignore");
                return;
            }
            notifyBatteryLevelChanged(i);
        }
        if (BleSpenUuid.CHARGE_STATUS.equals(uuid)) {
            int i2 = bluetoothGattCharacteristic.getValue()[0] & 255;
            BleSpenChargeState bleSpenChargeState = BleSpenChargeState.UNKNOWN;
            if (i2 == 0) {
                bleSpenChargeState = BleSpenChargeState.DISCHARGING;
            } else if (i2 == 1) {
                bleSpenChargeState = BleSpenChargeState.CHARGING;
            } else {
                String str3 = TAG;
                Log.e(str3, "onCharacteristicChanged : Unexpected charge state : " + i2);
            }
            this.mChargeState = bleSpenChargeState;
            String str4 = TAG;
            Log.i(str4, "onCharacteristicChanged : Charge:" + this.mChargeState);
            notifyChargeStateChanged(bleSpenChargeState);
        }
        if (BleSpenUuid.PEN_TIP_APPROACH.equals(uuid)) {
            this.mPenTipApproachState = BleSpenApproachState.UNKNOWN;
            byte[] value3 = bluetoothGattCharacteristic.getValue();
            if (value3.length > 0) {
                if (value3[0] == 1) {
                    this.mPenTipApproachState = BleSpenApproachState.APPROACHED;
                } else if (value3[0] == 0) {
                    this.mPenTipApproachState = BleSpenApproachState.LEFT;
                }
            }
            String str5 = TAG;
            Log.d(str5, "onCharacteristicChanged : Approach state = " + this.mPenTipApproachState);
        }
        if (BleSpenUuid.PEN_LOG.equals(uuid) && this.mLogComposer != null) {
            byte[] value4 = bluetoothGattCharacteristic.getValue();
            if (value4.length > 0) {
                this.mLogComposer.appendData(value4);
            }
        }
        if (BleSpenUuid.PEN_FREQUENCY.equals(uuid)) {
            dispatchPenFrequencyData(bluetoothGattCharacteristic);
        }
    }

    private void dispatchPenFrequencyData(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        this.mPenFrequency = convertToBleSpenFrequency(bluetoothGattCharacteristic.getValue());
        String str = TAG;
        Log.d(str, "dispatchPenFrequencyData : mPenFrequency = " + this.mPenFrequency);
        this.mEventListener.onPenFrequencyChanged(this.mPenFrequency);
    }

    private BleSpenFrequency convertToBleSpenFrequency(byte[] bArr) {
        BleSpenFrequency bleSpenFrequency = BleSpenFrequency.UNKNOWN;
        if (bArr == null || bArr.length <= 0) {
            return bleSpenFrequency;
        }
        byte b = bArr[0];
        if (b != 0) {
            if (b == 1) {
                return BleSpenFrequency.FOLD;
            }
            String str = TAG;
            Log.e(str, "convertToBleSpenFrequency: value[0] = " + ((int) bArr[0]));
            return bleSpenFrequency;
        }
        return BleSpenFrequency.DEFAULT;
    }

    private void finishLogComposer() {
        LogComposer logComposer = this.mLogComposer;
        if (logComposer != null) {
            logComposer.finishComposer();
            this.mLogComposer = null;
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readFirmwareVersion(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.FW_VER, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.3
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length > 0) {
                    BleSpenGenericDriver.this.mFirmwareVersion = Integer.toString(byteData[0] & 255);
                    String str = BleSpenGenericDriver.TAG;
                    Log.i(str, "readFirmwareVersion : Firmware = " + BleSpenGenericDriver.this.mFirmwareVersion);
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.4
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length > 0) {
                    byte b = byteData[0];
                    String str = BleSpenGenericDriver.TAG;
                    Log.i(str, "readBatteryLevel : Battery:" + ((int) b));
                    BleSpenGenericDriver.this.notifyBatteryLevelChanged(b);
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void enableConnectionIntervalAutoChange(BleSpenDriver.OperationFinishListener operationFinishListener) {
        Log.d(TAG, "enableConnectionIntervalAutoChange");
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, new byte[]{-4}, operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void enablePenTipApproachIndication(BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enablePenTipApproachIndication");
        if (!this.mSpenDeviceFeature.isSupportPenTipApproachDetection()) {
            Log.d(str, "enablePenTipApproachIndication : not supported");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                return;
            }
            return;
        }
        this.mBleDriver.enableIndication(getSpenServiceUuid(), BleSpenUuid.PEN_TIP_APPROACH, operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void enableLedIndication(BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enableLedIndication");
        if (!this.mSpenDeviceFeature.isSupportLed()) {
            Log.d(str, "enableLedIndication : not supported");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                return;
            }
            return;
        }
        this.mBleDriver.enableIndication(getSpenServiceUuid(), BleSpenUuid.LED_STATE, operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void setLedState(BleSpenLedState bleSpenLedState, BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void getLedState(BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void performCalibration(BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (!this.mSpenDeviceFeature.isSupportMotionSensor()) {
            Log.d(TAG, "performCalibration : not supported");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            }
            return;
        }
        this.mPerformCalibrationListener = operationFinishListener;
        this.mCalibrationStartTime = SystemClock.elapsedRealtime();
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, new byte[]{-2}, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.5
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.isSuccess()) {
                    Log.d(BleSpenGenericDriver.TAG, "performCalibration : request success");
                    return;
                }
                String str = BleSpenGenericDriver.TAG;
                Log.d(str, "performCalibration : request failed " + bleOpResultData.getResultCode());
                BleSpenGenericDriver.this.invokeCalibrationFinishListener(bleOpResultData);
            }
        });
    }

    public void invokeCalibrationFinishListener(BleOpResultData bleOpResultData) {
        BleSpenDriver.OperationFinishListener operationFinishListener = this.mPerformCalibrationListener;
        if (operationFinishListener == null) {
            Log.e(TAG, "invokeCalibrationFinishListener : mPerformCalibrationListener is null");
            return;
        }
        operationFinishListener.onFinish(bleOpResultData, SystemClock.elapsedRealtime() - this.mCalibrationStartTime);
        this.mPerformCalibrationListener = null;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized String getPenColorCode() {
        if (this.mSpenDeviceFeature.isSupportPenColorCode() && TextUtils.isEmpty(this.mPenColorCode)) {
            String penColorCodeFromDeviceName = getPenColorCodeFromDeviceName();
            if (penColorCodeFromDeviceName != null) {
                this.mPenColorCode = penColorCodeFromDeviceName;
            }
            String str = TAG;
            Log.i(str, "getPenColorCode : color code was empty. refreshed color code = " + this.mPenColorCode);
        }
        return this.mPenColorCode;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setBatteryLevel(int i) {
        if (isConnected()) {
            notifyBatteryLevelChanged(i);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized int getBatteryLevel() {
        if (isConnected()) {
            return this.mBatteryLevel;
        }
        return -1;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized String getFirmwareVersion() {
        if (isConnected()) {
            return this.mFirmwareVersion;
        }
        return Constants.packageName.NONE;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public BleSpenApproachState getSpenApproachState() {
        if (!isConnected()) {
            return BleSpenApproachState.UNKNOWN;
        }
        return this.mPenTipApproachState;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized BleSpenChargeState getChargeState() {
        if (!this.mSpenDeviceFeature.isSupportSpenChargeNotification()) {
            return BleSpenChargeState.UNSUPPORTED;
        } else if (!isConnected()) {
            return BleSpenChargeState.DISCONNECTED;
        } else {
            return this.mChargeState;
        }
    }

    public BleSpenButtonEvent createButtonEvent(BleSpenButtonEvent.Action action, long j, String str) {
        return createButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, action, j, str);
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver$7  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass7 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorId;

        static {
            int[] iArr = new int[BleSpenSensorId.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorId = iArr;
            try {
                iArr[BleSpenSensorId.PRIMARY_BUTTON.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorId[BleSpenSensorId.SECONDARY_BUTTON.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public BleSpenButtonEvent createButtonEvent(BleSpenSensorId bleSpenSensorId, BleSpenButtonEvent.Action action, long j, String str) {
        int i = AnonymousClass7.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorId[bleSpenSensorId.ordinal()];
        if (i == 1 || i == 2) {
            return new BleSpenButtonEvent(action, j, BleSpenSensorType.BUTTON, bleSpenSensorId, getSpenId(str));
        }
        Assert.fail("Unexpected button sensor ID : " + bleSpenSensorId);
        return null;
    }

    public BleSpenAirMotionEvent createAirGestureEvent(BleSpenAirMotionEvent.Action action, int i, int i2, long j, String str) {
        return new BleSpenAirMotionEvent(action, i, i2, j, BleSpenSensorType.AIR_GESTURE, BleSpenSensorId.AIR_GESTURE, getSpenId(str));
    }

    public BleSpenAccGyroEvent createAccGyroEvent(int i, int i2, int i3, int i4, int i5, int i6, long j, String str) {
        return new BleSpenAccGyroEvent(i, i2, i3, i4, i5, i6, j, BleSpenSensorType.ACCELEROMETER_AND_GYROSCOPE, BleSpenSensorId.AIR_GESTURE, getSpenId(str));
    }

    public BleSpenSelfTestResultEvent createSelfTestResultEvent(BleSpenSelfTestResultEvent.SelfTestData selfTestData, long j, String str) {
        return new BleSpenSelfTestResultEvent(selfTestData, j, BleSpenSensorType.ACCELEROMETER_AND_GYROSCOPE, BleSpenSensorId.AIR_GESTURE, getSpenId(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyBatteryLevelChanged(int i) {
        if (i <= 0) {
            i = 1;
        }
        int i2 = this.mBatteryLevel;
        this.mBatteryLevel = i;
        DiagnosisManager.getInstance(this.mContext).notifyBatteryLevelChanged(this.mSpenModelName, i2, this.mBatteryLevel);
        BleSpenDriver.EventListenerWrapper eventListenerWrapper = this.mEventListener;
        if (eventListenerWrapper != null) {
            eventListenerWrapper.onBatteryLevelChanged(i, i2);
        }
    }

    private void notifyChargeStateChanged(BleSpenChargeState bleSpenChargeState) {
        BleSpenDriver.EventListenerWrapper eventListenerWrapper = this.mEventListener;
        if (eventListenerWrapper != null) {
            eventListenerWrapper.onChargeStateChanged(bleSpenChargeState);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getPenColorCodeFromDeviceName() {
        String connectedDeviceName = this.mBleDriver.getConnectedDeviceName();
        if (connectedDeviceName == null) {
            Log.w(TAG, "getPenColorCodeFromDeviceName : DeviceName is null");
            return null;
        }
        String str = TAG;
        Log.v(str, "getPenColorCodeFromDeviceName : device name = " + connectedDeviceName);
        if (TextUtils.isEmpty(connectedDeviceName)) {
            Log.w(str, "getPenColorCodeFromDeviceName : DeviceName is empty");
            return null;
        } else if (!connectedDeviceName.startsWith("SPEN ") && !connectedDeviceName.startsWith("APEN ")) {
            Log.w(str, "getPenColorCodeFromDeviceName : Unrecognized device name : " + connectedDeviceName);
            return null;
        } else {
            String substring = connectedDeviceName.substring(connectedDeviceName.lastIndexOf(" ") + 1);
            if (TextUtils.isEmpty(substring)) {
                Log.w(str, "getPenColorCodeFromDeviceName : cmfCode not exist' : " + connectedDeviceName);
                return null;
            } else if (substring.length() > 2) {
                Log.w(str, "getPenColorCodeFromDeviceName : cmfCode exceed length : " + substring);
                return null;
            } else {
                Log.i(str, "getPenColorCodeFromDeviceName : cmf=" + substring);
                return substring;
            }
        }
    }

    private String getSpenId(String str) {
        return SPEN_ID_PREFIX + str;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void readPenLog(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (!this.mSpenDeviceFeature.isSupportLogExtraction() && operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
        if (this.mLogComposer != null) {
            this.mLogComposer = null;
        }
        this.mLogComposer = new LogComposer(operationFinishListener);
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.PEN_LOG, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver.6
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                String str = BleSpenGenericDriver.TAG;
                Log.d(str, "readPenLog : onFinish : " + bleOpResultData.getResultCode());
                if (bleOpResultData.isSuccess()) {
                    BleSpenGenericDriver.this.mLogComposer.setInitByte(bleOpResultData.getByteData());
                    return;
                }
                operationFinishListener.onFinish(bleOpResultData, 0L);
                BleSpenGenericDriver.this.mLogComposer = null;
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void enablePenFrequencyIndication(BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (!this.mSpenDeviceFeature.isSupportPenFrequencyChange()) {
            Log.d(TAG, "enablePenFrequencyIndication : Not supported");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                return;
            }
            return;
        }
        this.mBleDriver.enableIndication(getSpenServiceUuid(), BleSpenUuid.PEN_FREQUENCY, operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public BleSpenFrequency getPenFrequency() {
        if (!this.mSpenDeviceFeature.isSupportPenFrequencyChange()) {
            Log.d(TAG, "getPenFrequency : Not supported");
            return BleSpenFrequency.UNKNOWN;
        }
        return this.mPenFrequency;
    }
}
