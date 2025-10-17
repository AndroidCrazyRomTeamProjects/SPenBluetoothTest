package com.samsung.remotespen.core.device.control.factory.davinci;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.FirmwareUpgradeDriver;
import com.samsung.remotespen.core.device.control.factory.FmmDriver;
import com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenAirMotionEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSelfTestResultEvent;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenDavinciDriver extends BleSpenGenericDriver {
    private static final int HID_CONNECT_TIMEOUT = 3000;
    public static String TAG = "BleSpenDavinciDriver";
    public static final UUID UUID_SPEN_SERVICE = UUID.fromString("DC6BB0A8-202E-487C-8F1B-53B37CC837C6");
    private BleSpenOperationMode mCurrentOperatingMode;

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void readChargingState(BleSpenDriver.OperationFinishListener operationFinishListener) {
    }

    public BleSpenDavinciDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
        this.mCurrentOperatingMode = BleSpenOperationMode.DEFAULT;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized boolean open(String str, boolean z, boolean z2, BleSpenDriver.OperationFinishListener operationFinishListener, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        BleUtils.writeBundledBleSpenAddressToEfs(this.mContext, str);
        return super.open(str, z, z2, operationFinishListener, eventListener, iBleGattCallback);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void disconnect(BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.disconnect(operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void close(BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.close(operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onConnectionStateChanged(IBleGatt iBleGatt, int i, int i2) {
        super.onConnectionStateChanged(iBleGatt, i, i2);
        if (i2 == 0) {
            this.mCurrentOperatingMode = BleSpenOperationMode.DEFAULT;
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        UUID uuid = bluetoothGattCharacteristic.getUuid();
        if (BleSpenUuid.BUTTON_EVENT.equals(uuid)) {
            dispatchButtonData(iBleGatt, bluetoothGattCharacteristic);
        } else if (BleSpenUuid.RAW_SENSOR_DATA.equals(uuid)) {
            dispatchRawSensorData(iBleGatt, bluetoothGattCharacteristic);
        } else if (BleSpenUuid.SELF_TEST.equals(uuid)) {
            dispatchSelfTestResultData(iBleGatt, bluetoothGattCharacteristic);
        } else {
            super.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSlaveLatency(int i, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "setSlaveLatency : " + i);
        byte[] bArr = {16};
        if (i == 9) {
            bArr[0] = 16;
        } else if (1 > i || i > 8) {
            String str2 = TAG;
            Log.e(str2, "setSlaveLatency : Unsupported slave latency value : " + i);
        } else {
            bArr[0] = (byte) (i | 16);
        }
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str3 = BleSpenDavinciDriver.TAG;
                Log.d(str3, "setSlaveLatency : result = " + resultCode.name() + " elapsed=" + j);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void setConnectionInterval(BleSpenConnectionInterval bleSpenConnectionInterval, BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSpenOperationMode(final BleSpenOperationMode bleSpenOperationMode, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "setSpenOperationMode : " + bleSpenOperationMode);
        final byte[] bArr = {1};
        byte[] bArr2 = {2};
        byte[] bArr3 = {3};
        byte[] bArr4 = {4};
        byte[] bArr5 = {-4};
        byte[] bArr6 = {-10};
        byte[] bArr7 = {5};
        switch (AnonymousClass5.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[bleSpenOperationMode.ordinal()]) {
            case 1:
            case 2:
                break;
            case 3:
                bArr = bArr2;
                break;
            case 4:
                bArr = bArr7;
                break;
            case 5:
                bArr = bArr3;
                break;
            case 6:
                bArr = bArr4;
                break;
            case 7:
                bArr = bArr5;
                break;
            case 8:
                bArr = bArr6;
                break;
            default:
                String str2 = TAG;
                Log.e(str2, "setSpenOperationMode : Unsupported mode : " + bleSpenOperationMode);
                if (operationFinishListener != null) {
                    operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                }
                return;
        }
        new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver.2
            @Override // java.lang.Runnable
            public void run() {
                BleSpenDavinciDriver.this.mBleDriver.writeCharacteristic(BleSpenDavinciDriver.this.getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver.2.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        String str3 = BleSpenDavinciDriver.TAG;
                        Log.d(str3, "setSpenOperationMode : result = " + resultCode.name() + " elapsed=" + j);
                        if (bleOpResultData.isSuccess()) {
                            AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                            BleSpenDavinciDriver.this.mCurrentOperatingMode = bleSpenOperationMode;
                        }
                        BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                        if (operationFinishListener2 != null) {
                            operationFinishListener2.onFinish(bleOpResultData, j);
                        }
                    }
                });
            }
        }.run();
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver$5  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass5 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode;

        static {
            int[] iArr = new int[BleSpenOperationMode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode = iArr;
            try {
                iArr[BleSpenOperationMode.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.AIR_MOUSE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.AIR_MOUSE_POWER_SAVING.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.POSTURE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_ON.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_ON.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_OFF.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void sendFastDischargeCommand(int i, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (i < 0 || i > 15) {
            String str = TAG;
            Log.e(str, "sendFastDischargeCommand : incorrect command : " + i);
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            }
            return;
        }
        String str2 = TAG;
        Log.d(str2, "sendFastDischargeCommand : cmd=" + i);
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, new byte[]{(byte) (i + 80)}, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver.3
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str3 = BleSpenDavinciDriver.TAG;
                Log.d(str3, "sendFastDischargeCommand : result = " + resultCode.name() + " elapsed=" + j);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized BleSpenOperationMode getCurrentOperationMode() {
        return this.mCurrentOperatingMode;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public UUID getSpenServiceUuid() {
        return UUID_SPEN_SERVICE;
    }

    private void dispatchButtonData(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        IBleDevice device = iBleGatt.getDevice();
        String address = device != null ? device.getAddress() : "NULL";
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value == null || value.length == 0) {
            Log.e(TAG, "dispatchButtonData : no characteristic data!");
            return;
        }
        int i = value[0] & 255;
        int i2 = value.length >= 6 ? value[5] & 255 : -1;
        if (i == 0) {
            String str = TAG;
            Log.i(str, "dispatchButtonData : Button Up(" + i + "), seq=" + i2);
            if (this.mEventListener != null) {
                this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_UP, elapsedRealtime, address));
            }
        } else if (i == 3) {
            String str2 = TAG;
            Log.i(str2, "dispatchButtonData : Button Down(" + i + "), seq=" + i2);
            if (this.mEventListener != null) {
                this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_DOWN, elapsedRealtime, address));
            }
        } else if (i != 143 && i != 14 && i != 15) {
            if (i == 254) {
                Log.e(TAG, "dispatchButtonData : Motion sensor calibration failed");
                invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.CALIBRATION_FAIL));
            } else if (i == 255) {
                Log.i(TAG, "dispatchButtonData : Motion sensor calibration success");
                invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.SUCCESS));
            } else {
                String str3 = TAG;
                Log.e(str3, "dispatchButtonData : Unrecognized(" + i + "), seq=" + i2);
            }
        } else {
            short extractShortValue = extractShortValue(value, 1);
            short extractShortValue2 = extractShortValue(value, 3);
            String str4 = TAG;
            Log.v(str4, "dispatchButtonData : Gesture(" + i + ") dx=" + ((int) extractShortValue) + ", dy=" + ((int) extractShortValue2) + ", seq=" + i2);
            if (this.mEventListener != null) {
                BleSpenAirMotionEvent createAirGestureEvent = createAirGestureEvent(BleSpenAirMotionEvent.Action.MOVE, extractShortValue, extractShortValue2, elapsedRealtime, address);
                if (i == 143) {
                    Log.i(TAG, "dispatchButtonData : Impurity motion data");
                    createAirGestureEvent.markAsImpurity(true);
                }
                this.mEventListener.onSensorEvent(createAirGestureEvent);
            }
        }
    }

    private void dispatchRawSensorData(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value != null) {
            String str = TAG;
            Log.d(str, "dispatchRawSensorData : Accelerometer & Gyroscope. len=" + value.length);
            if (value.length <= 0 || this.mEventListener == null) {
                return;
            }
            IBleDevice device = iBleGatt.getDevice();
            String address = device != null ? device.getAddress() : "NULL";
            short extractShortValue = extractShortValue(value, 1);
            short extractShortValue2 = extractShortValue(value, 3);
            short extractShortValue3 = extractShortValue(value, 5);
            short extractShortValue4 = extractShortValue(value, 7);
            short extractShortValue5 = extractShortValue(value, 9);
            short extractShortValue6 = extractShortValue(value, 11);
            String str2 = TAG;
            Log.d(str2, "dispatchRawSensorData : accX=" + ((int) extractShortValue) + ", accY=" + ((int) extractShortValue2) + ", accZ=" + ((int) extractShortValue3) + ", gyroX=" + ((int) extractShortValue4) + ", gyroY=" + ((int) extractShortValue5) + ", gyroZ=" + ((int) extractShortValue6));
            this.mEventListener.onSensorEvent(createAccGyroEvent(extractShortValue, extractShortValue2, extractShortValue3, extractShortValue4, extractShortValue5, extractShortValue6, elapsedRealtime, address));
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readRawBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL_RAW, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.davinci.BleSpenDavinciDriver.4
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                int valueOf;
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length >= 2) {
                    Integer valueOf2 = Integer.valueOf((byteData[0] & 255) | ((byteData[1] << 8) & 65535));
                    if (valueOf2.intValue() >= 739) {
                        valueOf = 100;
                    } else if (valueOf2.intValue() < 654) {
                        valueOf = 0;
                    } else {
                        valueOf = Integer.valueOf((int) (((valueOf2.intValue() - 654) / 85.0f) * 100.0f));
                    }
                    bleOpResultData.setRawBatteryLevel(valueOf);
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    private void dispatchSelfTestResultData(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value != null) {
            String str = TAG;
            Log.d(str, "dispatchSelfTestResultData : Sensor self test. len=" + value.length);
            if (value.length <= 0 || this.mEventListener == null) {
                return;
            }
            IBleDevice device = iBleGatt.getDevice();
            String address = device != null ? device.getAddress() : "NULL";
            boolean z = value.length >= 36;
            Assert.e(z, "Unexpected data length. len=" + value.length);
            BleSpenSelfTestResultEvent.SelfTestData selfTestData = new BleSpenSelfTestResultEvent.SelfTestData();
            selfTestData.accRaw = new BleSpenSelfTestResultEvent.XYZ((float) extractShortValue(value, 0), (float) extractShortValue(value, 2), (float) extractShortValue(value, 4));
            selfTestData.accSelfPlus = new BleSpenSelfTestResultEvent.XYZ(((float) extractShortValue(value, 6)) * 0.12207031f, ((float) extractShortValue(value, 8)) * 0.12207031f, ((float) extractShortValue(value, 10)) * 0.12207031f);
            selfTestData.accSelfMinus = new BleSpenSelfTestResultEvent.XYZ(extractShortValue(value, 24) * 0.12207031f, extractShortValue(value, 26) * 0.12207031f, extractShortValue(value, 28) * 0.12207031f);
            selfTestData.gyroZro = new BleSpenSelfTestResultEvent.XYZ(extractShortValue(value, 30) * 0.07f, extractShortValue(value, 32) * 0.07f, extractShortValue(value, 34) * 0.07f);
            selfTestData.gyroSelf = new BleSpenSelfTestResultEvent.XYZ(extractShortValue(value, 12) * 0.07f, extractShortValue(value, 14) * 0.07f, extractShortValue(value, 16) * 0.07f);
            selfTestData.gyroSelfBias = new BleSpenSelfTestResultEvent.XYZ(extractShortValue(value, 18) * 0.07f, extractShortValue(value, 20) * 0.07f, extractShortValue(value, 22) * 0.07f);
            String str2 = TAG;
            Log.d(str2, "dispatchSelfTestResultData : \n" + selfTestData);
            this.mEventListener.onSensorEvent(createSelfTestResultEvent(selfTestData, elapsedRealtime, address));
        }
    }

    private short extractShortValue(byte[] bArr, int i) {
        return (short) (((bArr[i + 1] & 255) << 8) | (bArr[i] & 255));
    }
}
