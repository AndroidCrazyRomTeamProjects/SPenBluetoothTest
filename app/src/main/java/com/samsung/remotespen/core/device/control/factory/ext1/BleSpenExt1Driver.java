package com.samsung.remotespen.core.device.control.factory.ext1;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.aboutpage.Constants;
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
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSelfTestResultEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorId;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenExt1Driver extends BleSpenGenericDriver {
    private static String TAG = "BleSpenExt1Driver";
    private BleSpenOperationMode mCurrentOperationMode;
    private boolean mIsPrimaryButtonPressed;
    private UUID mSpenServiceUuid;
    private int mTimeSyncMaxCount;
    private static final UUID UUID_SPEN_SERVICE = UUID.fromString("0000FD6C-0000-1000-8000-00805F9B34FB");
    private static int MAX_TIME_SYNC_COUNT = 10;

    public BleSpenExt1Driver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
        this.mIsPrimaryButtonPressed = false;
        this.mCurrentOperationMode = BleSpenOperationMode.DEFAULT;
        this.mTimeSyncMaxCount = 0;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized boolean open(String str, boolean z, boolean z2, BleSpenDriver.OperationFinishListener operationFinishListener, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        if (!ModelFeatures.isProvideBundleSpen(this.mContext)) {
            BleUtils.writeBundledBleSpenAddressToEfs(this.mContext, str);
        }
        this.mTimeSyncMaxCount = 0;
        return super.open(str, z, z2, operationFinishListener, eventListener, iBleGattCallback);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void discoverService(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.discoverService(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.isSuccess()) {
                    if (BleSpenExt1Driver.this.getGatt().getService(BleSpenExt1Driver.UUID_SPEN_SERVICE) != null) {
                        BleSpenExt1Driver.this.mSpenServiceUuid = BleSpenExt1Driver.UUID_SPEN_SERVICE;
                    }
                    if (BleSpenExt1Driver.this.mSpenServiceUuid == null) {
                        Log.e(BleSpenExt1Driver.TAG, "discoverService : Failed to find the Spen service");
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
    public void onConnectionStateChanged(IBleGatt iBleGatt, int i, int i2) {
        super.onConnectionStateChanged(iBleGatt, i, i2);
        if (i2 == 0) {
            this.mCurrentOperationMode = BleSpenOperationMode.DEFAULT;
        } else if (i2 != 2) {
        } else {
            this.mCurrentOperationMode = BleSpenOperationMode.DEFAULT;
            this.mIsPrimaryButtonPressed = false;
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        UUID uuid = bluetoothGattCharacteristic.getUuid();
        if (BleSpenUuid.BUTTON_EVENT.equals(uuid)) {
            dispatchButtonData(iBleGatt, bluetoothGattCharacteristic);
            super.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
        } else if (BleSpenUuid.RAW_SENSOR_DATA.equals(uuid)) {
            dispatchRawSensorData(iBleGatt, bluetoothGattCharacteristic);
        } else if (BleSpenUuid.SELF_TEST.equals(uuid)) {
            dispatchSelfTestResultData(iBleGatt, bluetoothGattCharacteristic);
        } else if (BleSpenUuid.LED_STATE.equals(uuid)) {
            byte[] value = bluetoothGattCharacteristic.getValue();
            if (value.length > 0) {
                String str = TAG;
                Log.i(str, "onCharacteristicChanged : led : " + ((int) value[0]));
                notifyLedChanged(convertIntegerToLedState(value[0]));
            }
        } else if (BleSpenUuid.PEN_SYNC_CLOCK.equals(uuid)) {
            requestSyncPenTimeClock(bluetoothGattCharacteristic);
        } else {
            super.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public BleSpenLedState convertIntegerToLedState(int i) {
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    return BleSpenLedState.ON;
                }
                Assert.fail(Constants.packageName.NONE + i);
                return BleSpenLedState.UNKNOWN;
            }
            return BleSpenLedState.BLINKING;
        }
        return BleSpenLedState.OFF;
    }

    private void notifyLedChanged(BleSpenLedState bleSpenLedState) {
        BleSpenDriver.EventListenerWrapper eventListenerWrapper = this.mEventListener;
        if (eventListenerWrapper != null) {
            eventListenerWrapper.onLedStateChanged(bleSpenLedState);
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
        int i = AnonymousClass11.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval[bleSpenConnectionInterval.ordinal()];
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
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str2 = BleSpenExt1Driver.TAG;
                Log.d(str2, "BleSpenConnectionInterval : result = " + resultCode.name() + " elapsed=" + j);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSpenOperationMode(final BleSpenOperationMode bleSpenOperationMode, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final byte[] bArr = {1};
        byte[] bArr2 = {3};
        byte[] bArr3 = {4};
        byte[] bArr4 = {-4};
        byte[] bArr5 = {-10};
        switch (AnonymousClass11.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[bleSpenOperationMode.ordinal()]) {
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
        new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.3
            @Override // java.lang.Runnable
            public void run() {
                BleSpenExt1Driver.this.mBleDriver.writeCharacteristic(BleSpenExt1Driver.this.getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.3.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        String str2 = BleSpenExt1Driver.TAG;
                        Log.d(str2, "setSpenOperationMode : result = " + resultCode.name() + " elapsed=" + j);
                        if (bleOpResultData.isSuccess()) {
                            AnonymousClass3 anonymousClass3 = AnonymousClass3.this;
                            BleSpenExt1Driver.this.mCurrentOperationMode = bleSpenOperationMode;
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

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void setLedState(BleSpenLedState bleSpenLedState, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.LED_STATE, new byte[]{convertLedStateToByte(bleSpenLedState)}, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.4
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                operationFinishListener.onFinish(bleOpResultData, j);
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void getLedState(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.LED_STATE, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.5
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                bleOpResultData.setLedState(BleSpenExt1Driver.this.convertIntegerToLedState(bleOpResultData.getByteData()[0]));
                operationFinishListener.onFinish(bleOpResultData, j);
            }
        });
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver$11  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass11 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenLedState;
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode;

        static {
            int[] iArr = new int[BleSpenLedState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenLedState = iArr;
            try {
                iArr[BleSpenLedState.BLINKING.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenLedState[BleSpenLedState.ON.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenLedState[BleSpenLedState.OFF.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            int[] iArr2 = new int[BleSpenOperationMode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode = iArr2;
            try {
                iArr2[BleSpenOperationMode.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_DEFAULT.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.POSTURE.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_ON.ordinal()] = 4;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_ON.ordinal()] = 5;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_LOW_POWER_OFF.ordinal()] = 6;
            } catch (NoSuchFieldError unused9) {
            }
            int[] iArr3 = new int[BleSpenConnectionInterval.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval = iArr3;
            try {
                iArr3[BleSpenConnectionInterval.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenConnectionInterval[BleSpenConnectionInterval.MINIMUM.ordinal()] = 2;
            } catch (NoSuchFieldError unused11) {
            }
        }
    }

    private byte convertLedStateToByte(BleSpenLedState bleSpenLedState) {
        int i = AnonymousClass11.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenLedState[bleSpenLedState.ordinal()];
        byte b = 1;
        if (i != 1) {
            b = 2;
            if (i != 2) {
                if (i != 3) {
                    Assert.fail(bleSpenLedState.toString());
                }
                return (byte) 0;
            }
        }
        return b;
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
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, new byte[]{(byte) (i + 80)}, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.6
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str3 = BleSpenExt1Driver.TAG;
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
        int length;
        int i2 = bArr[i] & 255;
        if (i2 == 255) {
            int i3 = bArr[i + 1] & 255;
            if (i3 == 1) {
                int i4 = bArr[i + 2] & 255;
                if (i4 == 254) {
                    Log.e(TAG, "dispatchButtonData : Motion sensor calibration failed");
                    invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.CALIBRATION_FAIL));
                } else if (i4 == 255) {
                    Log.i(TAG, "dispatchButtonData : Motion sensor calibration success");
                    invokeCalibrationFinishListener(new BleOpResultData(BleOpResultCode.SUCCESS));
                } else {
                    String str2 = TAG;
                    Log.i(str2, "dispatchButtonData : unknown calibration result : " + i4);
                }
            } else if (i3 == 3) {
                String str3 = TAG;
                Log.d(str3, "dispatchButtonData : sensor state = " + (bArr[i + 2] & 255));
            } else {
                String str4 = TAG;
                Log.d(str4, "dispatchButtonData : unknown debug msg(0xFF) type : " + i3);
            }
            length = bArr.length;
        } else {
            boolean z = (bArr[i] & 64) == 64;
            boolean z2 = (bArr[i] & 128) == 128;
            if (z) {
                Log.d(TAG, "dispatchButtonData: isApproached");
                i2 &= -65;
            }
            if (z2) {
                Log.d(TAG, "dispatchButtonData : isImpurity");
                i2 &= -129;
            }
            int i5 = i2;
            if (i5 == 0) {
                int i6 = bArr.length >= 6 ? bArr[i + 5] & 255 : -1;
                String str5 = TAG;
                Log.i(str5, "dispatchButtonData : Primary button up(" + i5 + "), seq=" + i6);
                if (!this.mIsPrimaryButtonPressed) {
                    Log.e(TAG, "dispatchButtonData : Primary button is already released state!");
                }
                this.mIsPrimaryButtonPressed = false;
                if (this.mEventListener != null) {
                    notifyButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, BleSpenButtonEvent.Action.BUTTON_UP, j, str, z);
                }
                return 6;
            } else if (i5 == 3) {
                String str6 = TAG;
                Log.i(str6, "dispatchButtonData : Primary button down(" + i5 + ")");
                if (this.mIsPrimaryButtonPressed) {
                    Log.e(TAG, "dispatchButtonData : Primary button is already pressed state!");
                    if (this.mEventListener != null) {
                        notifyButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, BleSpenButtonEvent.Action.BUTTON_UP, j, str, z);
                    }
                }
                this.mIsPrimaryButtonPressed = true;
                if (this.mEventListener != null) {
                    notifyButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, BleSpenButtonEvent.Action.BUTTON_DOWN, j, str, z);
                }
                return 1;
            } else if (i5 == 14 || i5 == 15) {
                if (this.mCurrentOperationMode == BleSpenOperationMode.SENSOR_ON) {
                    boolean z3 = this.mIsPrimaryButtonPressed;
                    if (z3 && i5 == 14) {
                        Log.i(TAG, "dispatchButtonData : primary button up state detected");
                        this.mIsPrimaryButtonPressed = false;
                        if (this.mEventListener != null) {
                            notifyButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, BleSpenButtonEvent.Action.BUTTON_UP, j, str, z);
                        }
                    } else if (!z3 && i5 == 15) {
                        Log.i(TAG, "dispatchButtonData : primary button down state detected");
                        this.mIsPrimaryButtonPressed = true;
                        if (this.mEventListener != null) {
                            notifyButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, BleSpenButtonEvent.Action.BUTTON_DOWN, j, str, z);
                        }
                    }
                }
                short extractShortValue = extractShortValue(bArr, i + 1);
                short extractShortValue2 = extractShortValue(bArr, i + 3);
                String str7 = TAG;
                Log.v(str7, "dispatchButtonData : Gesture(" + i5 + ") dx=" + ((int) extractShortValue) + ", dy=" + ((int) extractShortValue2) + ", seq=" + (bArr[i + 5] & 255));
                if (this.mEventListener != null) {
                    BleSpenAirMotionEvent createAirGestureEvent = createAirGestureEvent(BleSpenAirMotionEvent.Action.MOVE, extractShortValue, extractShortValue2, j, str);
                    if (z2) {
                        Log.i(TAG, "dispatchButtonData : Impurity motion data");
                        createAirGestureEvent.markAsImpurity(true);
                    }
                    if (z) {
                        Log.i(TAG, "dispatchButtonData : Motion Data with approached");
                        createAirGestureEvent.markAsApproached(true);
                    }
                    this.mEventListener.onSensorEvent(createAirGestureEvent);
                }
                return 6;
            } else {
                String str8 = TAG;
                Log.e(str8, "dispatchButtonData : Unrecognized button header(0x" + Integer.toHexString(i5).toUpperCase() + ")");
                length = bArr.length;
            }
        }
        return length - i;
    }

    private void notifyButtonEvent(BleSpenSensorId bleSpenSensorId, BleSpenButtonEvent.Action action, long j, String str, boolean z) {
        BleSpenButtonEvent createButtonEvent = createButtonEvent(BleSpenSensorId.PRIMARY_BUTTON, action, j, str);
        if (createButtonEvent != null) {
            createButtonEvent.markAsApproached(z);
            this.mEventListener.onSensorEvent(createButtonEvent);
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

    private void requestSyncPenTimeClock(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int currentTimeMillis = (int) (System.currentTimeMillis() / 1000);
        long bytesToLong = bytesToLong(bluetoothGattCharacteristic.getValue());
        Log.i(TAG, "requestSyncPenTimeClock deviceTime :" + currentTimeMillis + ", penTime :" + bytesToLong);
        if (Math.abs(currentTimeMillis - bytesToLong) > 1) {
            Log.i(TAG, "requestSyncPenTimeClock needs to be reset");
            if (this.mTimeSyncMaxCount == MAX_TIME_SYNC_COUNT) {
                this.mBleDriver.disableNotification(getSpenServiceUuid(), BleSpenUuid.PEN_SYNC_CLOCK, null);
                return;
            }
            this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.PEN_SYNC_CLOCK, ByteBuffer.allocate(4).putInt(currentTimeMillis).array(), new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.7
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    Log.v(BleSpenExt1Driver.TAG, "requestSyncPenTimeClock writeCharacteristic finish.");
                }
            });
            this.mTimeSyncMaxCount++;
            return;
        }
        this.mBleDriver.disableNotification(getSpenServiceUuid(), BleSpenUuid.PEN_SYNC_CLOCK, null);
    }

    private long bytesToLong(byte[] bArr) {
        return new BigInteger(bArr).intValue();
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readRawBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL_RAW, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.8
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

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readFirmwareVersion(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.FW_VER, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.9
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                StringBuilder sb = new StringBuilder();
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length > 0) {
                    for (byte b : byteData) {
                        sb.append((char) b);
                    }
                }
                BleSpenExt1Driver.this.mFirmwareVersion = sb.toString();
                Log.d(BleSpenExt1Driver.TAG, "onFinish : mFirmwareVersion = " + BleSpenExt1Driver.this.mFirmwareVersion);
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readChargingState(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.CHARGE_STATUS, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.ext1.BleSpenExt1Driver.10
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length > 0) {
                    if (byteData[0] == 1) {
                        BleSpenExt1Driver.this.mChargeState = BleSpenChargeState.CHARGING;
                    } else {
                        BleSpenExt1Driver.this.mChargeState = BleSpenChargeState.DISCHARGING;
                    }
                    String str = BleSpenExt1Driver.TAG;
                    Log.d(str, "getChargeState : mChargeState :" + BleSpenExt1Driver.this.mChargeState);
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
