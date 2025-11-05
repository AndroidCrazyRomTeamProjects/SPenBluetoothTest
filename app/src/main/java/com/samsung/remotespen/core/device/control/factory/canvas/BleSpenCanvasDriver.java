package com.samsung.remotespen.core.device.control.factory.canvas;

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
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSelfTestResultEvent;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.SpenModelName;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenCanvasDriver extends BleSpenGenericDriver {
    public BleSpenOperationMode mCurrentOperationMode;
    private boolean mIsButtonPressed;
    private UUID mSpenServiceUuid;
    private static final UUID UUID_LEGACY_SPEN_SERVICE = UUID.fromString("DC6BB0A8-202E-487C-8F1B-53B37CC837C6");
    private static final UUID UUID_SPEN_SERVICE = UUID.fromString("0000FD6C-0000-1000-8000-00805F9B34FB");
    public static String TAG = BleSpenCanvasDriver.class.getSimpleName();

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void readChargingState(BleSpenDriver.OperationFinishListener operationFinishListener) {
    }

    public BleSpenCanvasDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
        this.mIsButtonPressed = false;
        this.mCurrentOperationMode = BleSpenOperationMode.DEFAULT;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized boolean open(String str, boolean z, boolean z2, BleSpenDriver.OperationFinishListener operationFinishListener, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        BleUtils.writeBundledBleSpenAddressToEfs(this.mContext, str);
        return super.open(str, z, z2, operationFinishListener, eventListener, iBleGattCallback);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void discoverService(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.discoverService(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.isSuccess()) {
                    IBleGatt gatt = BleSpenCanvasDriver.this.getGatt();
                    if (gatt.getService(BleSpenCanvasDriver.UUID_SPEN_SERVICE) == null) {
                        if (gatt.getService(BleSpenCanvasDriver.UUID_LEGACY_SPEN_SERVICE) != null) {
                            Log.i(BleSpenCanvasDriver.TAG, "discoverService : legacy SPen service UUID");
                            BleSpenCanvasDriver.this.mSpenServiceUuid = BleSpenCanvasDriver.UUID_LEGACY_SPEN_SERVICE;
                        }
                    } else {
                        BleSpenCanvasDriver.this.mSpenServiceUuid = BleSpenCanvasDriver.UUID_SPEN_SERVICE;
                    }
                    if (BleSpenCanvasDriver.this.mSpenServiceUuid == null) {
                        Log.e(BleSpenCanvasDriver.TAG, "discoverService : Failed to find the Spen service");
                        bleOpResultData.setBleResultCode(BleOpResultCode.SERVICE_DISCOVERY_FAIL);
                    }
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
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
    public synchronized void setSlaveLatency(int i, BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.e(str, "setSlaveLatency : " + i + " - not supported");
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void setConnectionInterval(BleSpenConnectionInterval bleSpenConnectionInterval, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        byte[] bArr = {34};
        byte[] bArr2 = {35};
        int i = AnonymousClass6.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval[bleSpenConnectionInterval.ordinal()];
        if (i != 1) {
            if (i != 2) {
                String str = TAG;
                Log.e(str, "setConnectionInterval : Unsupported interval : " + bleSpenConnectionInterval);
                if (operationFinishListener != null) {
                    operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                    return;
                }
                return;
            }
            bArr = bArr2;
        }
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str2 = BleSpenCanvasDriver.TAG;
                Log.d(str2, "BleSpenConnectionInterval : result = " + resultCode.name() + " elapsed=" + j);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver$6  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass6 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval;
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
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.POSTURE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_ON.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_ON.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_OFF.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            int[] iArr2 = new int[BleSpenConnectionInterval.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval = iArr2;
            try {
                iArr2[BleSpenConnectionInterval.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval[BleSpenConnectionInterval.MINIMUM.ordinal()] = 2;
            } catch (NoSuchFieldError unused8) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSpenOperationMode(final BleSpenOperationMode bleSpenOperationMode, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final byte[] bArr = {1};
        byte[] bArr2 = {3};
        byte[] bArr3 = {4};
        byte[] bArr4 = {-4};
        byte[] bArr5 = {-10};
        switch (AnonymousClass6.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[bleSpenOperationMode.ordinal()]) {
            case 1:
            case 2:
                break;
            case 3:
                bArr = bArr2;
                break;
            case 4:
                bArr = bArr3;
                break;
            case 5:
                bArr = bArr4;
                break;
            case 6:
                bArr = bArr5;
                break;
            default:
                String str = TAG;
                Log.e(str, "setSpenOperationMode : Unsupported mode : " + bleSpenOperationMode);
                if (operationFinishListener != null) {
                    operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                }
                return;
        }
        new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.3
            @Override // java.lang.Runnable
            public void run() {
                BleSpenCanvasDriver.this.mBleDriver.writeCharacteristic(BleSpenCanvasDriver.this.getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.3.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        String str2 = BleSpenCanvasDriver.TAG;
                        Log.d(str2, "setSpenOperationMode : result = " + resultCode.name() + " elapsed=" + j);
                        if (bleOpResultData.isSuccess()) {
                            AnonymousClass3 anonymousClass3 = AnonymousClass3.this;
                            BleSpenCanvasDriver.this.mCurrentOperationMode = bleSpenOperationMode;
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
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, new byte[]{(byte) (i + 80)}, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.4
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str3 = BleSpenCanvasDriver.TAG;
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
        return this.mCurrentOperationMode;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public UUID getSpenServiceUuid() {
        return this.mSpenServiceUuid;
    }

    private void dispatchButtonData(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        IBleDevice device = iBleGatt.getDevice();
        String address = device != null ? device.getAddress() : "NULL";
        byte[] value = bluetoothGattCharacteristic.getValue();
        if (value == null || value.length == 0) {
            Log.e(TAG, "dispatchButtonData : no characteristic data!");
        } else if ((value[0] & 255) == 240) {
            int i = value[1] & 255;
            String str = TAG;
            Log.d(str, "dispatchButtonData : Multiple event format. count=" + i);
            int i2 = 2;
            for (int i3 = 0; i3 < i; i3++) {
                i2 += dispatchButtonData(value, i2, elapsedRealtime, address);
                if (i2 >= value.length) {
                    return;
                }
            }
        } else {
            dispatchButtonData(value, 0, elapsedRealtime, address);
        }
    }

    private int dispatchButtonData(byte[] bArr, int i, long j, String str) {
        int i2 = bArr[i] & 255;
        if (i2 == 0) {
            int i3 = bArr.length >= 6 ? bArr[i + 5] & 255 : -1;
            String str2 = TAG;
            Log.i(str2, "dispatchButtonData : Button Up(" + i2 + "), seq=" + i3);
            if (!this.mIsButtonPressed) {
                Log.e(TAG, "dispatchButtonData : Button is already released state!");
            }
            this.mIsButtonPressed = false;
            if (this.mEventListener != null) {
                this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_UP, j, str));
            }
            return 6;
        } else if (i2 == 3) {
            String str3 = TAG;
            Log.i(str3, "dispatchButtonData : Button Down(" + i2 + ")");
            if (this.mIsButtonPressed) {
                Log.e(TAG, "dispatchButtonData : Button is already pressed state!");
                if (this.mEventListener != null) {
                    this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_UP, j, str));
                }
            }
            this.mIsButtonPressed = true;
            if (this.mEventListener != null) {
                this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_DOWN, j, str));
            }
            return 1;
        } else {
            if (i2 == 255) {
                int i4 = bArr[i + 1] & 255;
                if (i4 == 1) {
                    int i5 = bArr[i + 2] & 255;
                    if (i5 == 254) {
                        Log.e(TAG, "dispatchButtonData : Motion sensor calibration failed");
                        invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.CALIBRATION_FAIL));
                    } else if (i5 == 255) {
                        Log.i(TAG, "dispatchButtonData : Motion sensor calibration success");
                        invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.SUCCESS));
                    } else {
                        String str4 = TAG;
                        Log.i(str4, "dispatchButtonData : unknown calibration result : " + i5);
                    }
                } else if (i4 == 3) {
                    String str5 = TAG;
                    Log.d(str5, "dispatchButtonData : sensor state = " + (bArr[i + 2] & 255));
                } else if (i4 == 4) {
                    int i6 = bArr[i + 2] & 255;
                    if (i6 == 0) {
                        this.mEventListener.onNotifyBatteryState(BleSpenBatteryState.FULL);
                    } else if (i6 == 1) {
                        this.mEventListener.onNotifyBatteryState(BleSpenBatteryState.NOT_FULL);
                    }
                } else {
                    String str6 = TAG;
                    Log.d(str6, "dispatchButtonData : unknown debug msg(0xFF) type : " + i4);
                }
            } else if (i2 == 14 || i2 == 15 || i2 == 142 || i2 == 143) {
                if (this.mCurrentOperationMode == BleSpenOperationMode.SENSOR_ON) {
                    boolean z = this.mIsButtonPressed;
                    if (z && (i2 == 14 || i2 == 142)) {
                        Log.i(TAG, "dispatchButtonData : button up state detected");
                        this.mIsButtonPressed = false;
                        if (this.mEventListener != null) {
                            this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_UP, j, str));
                        }
                    } else if (!z && (i2 == 15 || i2 == 143)) {
                        Log.i(TAG, "dispatchButtonData : button down state detected");
                        this.mIsButtonPressed = true;
                        if (this.mEventListener != null) {
                            this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.BUTTON_DOWN, j, str));
                        }
                    }
                }
                short extractShortValue = extractShortValue(bArr, i + 1);
                short extractShortValue2 = extractShortValue(bArr, i + 3);
                String str7 = TAG;
                Log.v(str7, "dispatchButtonData : Gesture(" + i2 + ") dx=" + ((int) extractShortValue) + ", dy=" + ((int) extractShortValue2) + ", seq=" + (bArr[i + 5] & 255));
                if (this.mEventListener != null) {
                    BleSpenAirMotionEvent createAirGestureEvent = createAirGestureEvent(BleSpenAirMotionEvent.Action.MOVE, extractShortValue, extractShortValue2, j, str);
                    if (i2 == 142 || i2 == 143) {
                        Log.i(TAG, "dispatchButtonData : Impurity motion data");
                        createAirGestureEvent.markAsImpurity(true);
                    }
                    this.mEventListener.onSensorEvent(createAirGestureEvent);
                }
                return 6;
            } else {
                String str8 = TAG;
                Log.e(str8, "dispatchButtonData : Unrecognized button header(0x" + Integer.toHexString(i2).toUpperCase() + ")");
            }
            return bArr.length - i;
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

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readRawBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL_RAW, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver.5
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

    private short extractShortValue(byte[] bArr, int i) {
        return (short) (((bArr[i + 1] & 255) << 8) | (bArr[i] & 255));
    }
}
