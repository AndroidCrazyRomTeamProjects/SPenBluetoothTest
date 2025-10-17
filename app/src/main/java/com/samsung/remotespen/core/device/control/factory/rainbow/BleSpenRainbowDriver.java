package com.samsung.remotespen.core.device.control.factory.rainbow;

import android.util.Log;

import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.FirmwareUpgradeDriver;
import com.samsung.remotespen.core.device.control.factory.FmmDriver;
import com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.util.features.SpenModelName;

/* loaded from: classes.dex */
public class BleSpenRainbowDriver extends BleSpenCanvasDriver {
    static {
        BleSpenCanvasDriver.TAG = BleSpenRainbowDriver.class.getSimpleName();
    }

    public BleSpenRainbowDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        super(spenModelName, bleSpenDeviceFeature, fmmDriver, firmwareUpgradeDriver);
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowDriver$3  reason: invalid class name */
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
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_ON.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.STANDBY.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[BleSpenOperationMode.SENSOR_DEFAULT.ordinal()] = 4;
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
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.canvas.BleSpenCanvasDriver, com.samsung.remotespen.core.device.control.factory.BleSpenDriver
    public synchronized void setSpenOperationMode(final BleSpenOperationMode bleSpenOperationMode, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final byte[] bArr = {1};
        byte[] bArr2 = {4};
        byte[] bArr3 = {16};
        byte[] bArr4 = {17};
        byte[] bArr5 = {-4};
        byte[] bArr6 = {-10};
        switch (AnonymousClass3.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenOperationMode[bleSpenOperationMode.ordinal()]) {
            case 1:
                bArr = bArr3;
                break;
            case 2:
                bArr = bArr2;
                break;
            case 3:
                bArr = bArr4;
                break;
            case 4:
                break;
            case 5:
                bArr = bArr5;
                break;
            case 6:
                bArr = bArr6;
                break;
            default:
                String str = BleSpenCanvasDriver.TAG;
                Log.e(str, "setSpenOperationMode : Unsupported mode : " + bleSpenOperationMode);
                if (operationFinishListener != null) {
                    operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
                }
                return;
        }
        new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowDriver.1
            @Override // java.lang.Runnable
            public void run() {
                BleSpenRainbowDriver.this.mBleDriver.writeCharacteristic(BleSpenRainbowDriver.this.getSpenServiceUuid(), BleSpenUuid.MODE, bArr, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowDriver.1.1
                    @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                    public void onFinish(BleOpResultData bleOpResultData, long j) {
                        BleOpResultCode resultCode = bleOpResultData.getResultCode();
                        String str2 = BleSpenCanvasDriver.TAG;
                        Log.i(str2, "setSpenOperationMode : opMode = " + bleSpenOperationMode + ", result = " + resultCode.name() + " elapsed = " + j);
                        if (bleOpResultData.isSuccess()) {
                            AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                            BleSpenRainbowDriver.this.mCurrentOperationMode = bleSpenOperationMode;
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
    public synchronized void readFirmwareVersion(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        super.readFirmwareVersion(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.rainbow.BleSpenRainbowDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.isSuccess()) {
                    try {
                        int parseInt = Integer.parseInt(BleSpenRainbowDriver.this.getFirmwareVersion());
                        String str = BleSpenCanvasDriver.TAG;
                        Log.d(str, "readFirmwareVersion : intVersion=" + parseInt);
                        if (parseInt <= 48) {
                            String str2 = BleSpenCanvasDriver.TAG;
                            Log.e(str2, "readFirmwareVersion : CANVAS S-Pen detected. fwVer=" + parseInt);
                            BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                            if (operationFinishListener2 != null) {
                                operationFinishListener2.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), j);
                                return;
                            }
                            return;
                        }
                    } catch (Exception e) {
                        String str3 = BleSpenCanvasDriver.TAG;
                        Log.e(str3, "readFirmwareVersion : e=" + e);
                    }
                }
                BleSpenDriver.OperationFinishListener operationFinishListener3 = operationFinishListener;
                if (operationFinishListener3 != null) {
                    operationFinishListener3.onFinish(bleOpResultData, j);
                }
            }
        });
    }
}
