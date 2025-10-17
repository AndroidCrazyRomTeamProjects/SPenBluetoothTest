package com.samsung.remotespen.core.device.control.factory.crown;

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
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.util.features.SpenModelName;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenCrownDriver extends BleSpenGenericDriver {
    private static final String TAG = "BleSpenCrownDriver";
    public static final UUID UUID_SPEN_SERVICE = UUID.fromString("edfec62e-9910-0bac-5241-d8bda6932a2f");

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void readChargingState(BleSpenDriver.OperationFinishListener operationFinishListener) {
    }

    public BleSpenCrownDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized boolean open(String str, boolean z, boolean z2, BleSpenDriver.OperationFinishListener operationFinishListener, BleSpenDriver.EventListener eventListener, IBleGattCallback iBleGattCallback) {
        CrownWacomChargingDriver.getInstance(this.mContext).startCharge();
        BleUtils.writeBundledBleSpenAddressToEfs(this.mContext, str);
        return super.open(str, z, z2, operationFinishListener, eventListener, iBleGattCallback);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.shared.BleSpenGenericDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (BleSpenUuid.BUTTON_EVENT.equals(bluetoothGattCharacteristic.getUuid())) {
            IBleDevice device = iBleGatt.getDevice();
            String address = device != null ? device.getAddress() : "NULL";
            byte[] value = bluetoothGattCharacteristic.getValue();
            if (value == null || value.length <= 0) {
                return;
            }
            byte b = value[0];
            String str = TAG;
            Log.i(str, "onCharacteristicChanged : Button:" + ((int) b));
            if (b == 1) {
                if (this.mEventListener != null) {
                    this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.SINGLE_CLICKED, elapsedRealtime, address));
                    return;
                }
                return;
            } else if (b == 2) {
                if (this.mEventListener != null) {
                    this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.LONG_CLICK_STARTED, elapsedRealtime, address));
                    this.mEventListener.onSensorEvent(createButtonEvent(BleSpenButtonEvent.Action.LONG_CLICK_FINISHED, elapsedRealtime, address));
                    return;
                }
                return;
            } else {
                Log.e(str, "onCharacteristicChanged : Unrecognized button state : " + ((int) b));
                return;
            }
        }
        super.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSlaveLatency(int i, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "setSlaveLatency : " + i);
        byte[] bArr = {64};
        byte[] bArr2 = {67};
        byte[] bArr3 = {66};
        byte[] bArr4 = {65};
        if (i == 1) {
            bArr = bArr4;
        } else if (i == 3) {
            bArr = bArr3;
        } else if (i == 6) {
            bArr = bArr2;
        } else if (i != 9) {
            Log.e(str, "setSlaveLatency : Unsupported slave latency value : " + i);
        }
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.crown.BleSpenCrownDriver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleOpResultCode resultCode = bleOpResultData.getResultCode();
                String str2 = BleSpenCrownDriver.TAG;
                Log.d(str2, "setSlaveLatency : result = " + resultCode.name() + " elapsed=" + j);
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

    /* renamed from: com.samsung.remotespen.core.device.control.factory.crown.BleSpenCrownDriver$3  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass3 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode;

        static {
            int[] iArr = new int[BleSpenOperationMode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode = iArr;
            try {
                iArr[BleSpenOperationMode.DEFAULT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSpenOperationMode(BleSpenOperationMode bleSpenOperationMode, BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (AnonymousClass3.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[bleSpenOperationMode.ordinal()] != 1) {
            String str = TAG;
            Log.e(str, "setSpenOperationMode : not supported mode=" + bleSpenOperationMode);
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            }
        } else if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void readRawBatteryLevel(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mBleDriver.readCharacteristic(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL_RAW, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.crown.BleSpenCrownDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                byte[] byteData = bleOpResultData.getByteData();
                if (byteData != null && byteData.length >= 2) {
                    bleOpResultData.setRawBatteryLevel(Integer.valueOf((int) (((Integer.valueOf((byteData[0] & 255) | ((byteData[1] << 8) & 65535)).intValue() - 341.0f) / 384.0f) * 100.0f)));
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void sendFastDischargeCommand(int i, BleSpenDriver.OperationFinishListener operationFinishListener) {
        if (operationFinishListener != null) {
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized BleSpenOperationMode getCurrentOperationMode() {
        return BleSpenOperationMode.DEFAULT;
    }

    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public UUID getSpenServiceUuid() {
        return UUID_SPEN_SERVICE;
    }
}
