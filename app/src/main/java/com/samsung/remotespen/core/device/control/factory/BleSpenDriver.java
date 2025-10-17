package com.samsung.remotespen.core.device.control.factory;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenApproachState;
import com.samsung.remotespen.core.device.data.BleSpenBatteryState;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenConnectionInterval;
import com.samsung.remotespen.core.device.data.BleSpenFirmwareUpgradeListener;
import com.samsung.remotespen.core.device.data.BleSpenFrequency;
import com.samsung.remotespen.core.device.data.BleSpenLedState;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleStateChangeInfo;
import com.samsung.remotespen.core.device.data.FmmConfig;
import com.samsung.remotespen.core.device.util.SecurityUtils;
import com.samsung.remotespen.core.device.util.diagnosis.DiagnosisManager;
import com.samsung.util.debug.Assert;
import com.samsung.util.features.SpenModelName;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/* loaded from: classes.dex */
public abstract class BleSpenDriver implements BleCancellableOperation {
    private static final String TAG = "BleSpenDriver";
    public BleSpenBleDriver mBleDriver;
    public Context mContext;
    private IBleGattCallback mExternalGattCallback;
    private FirmwareUpgradeDriver mFirmwareUpgradeDriver;
    private FmmDriver mFmmDriver;
    public BleSpenDeviceFeature mSpenDeviceFeature;
    public SpenModelName mSpenModelName;
    private Object mSyncObject = new Object();
    public EventListenerWrapper mEventListener = new EventListenerWrapper();
    private IBleGattCallback mInternalGattCallback = new IBleGattCallback() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenDriver.1
        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onConnectionStateChange(IBleGatt iBleGatt, int i, int i2) {
            if (i2 == 0) {
                DiagnosisManager.getInstance(BleSpenDriver.this.mContext).notifyDisconnect(BleSpenDriver.this.mSpenModelName, i);
            }
            BleSpenDriver.this.onConnectionStateChanged(iBleGatt, i, i2);
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onConnectionStateChange(iBleGatt, i, i2);
            }
            if (BleSpenDriver.this.mEventListener != null) {
                BleStateChangeInfo bleStateChangeInfo = new BleStateChangeInfo(i);
                BleSpenDriver bleSpenDriver = BleSpenDriver.this;
                bleSpenDriver.mEventListener.onConnectionStateChanged(ConnectionState.valueOf(bleSpenDriver.mBleDriver.getConnectionState().name()), bleStateChangeInfo);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onServicesDiscovered(IBleGatt iBleGatt, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onServicesDiscovered(iBleGatt, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            BleSpenDriver.this.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onDescriptorWrite(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onDescriptorWrite(iBleGatt, bluetoothGattDescriptor, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onDescriptorRead(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onDescriptorRead(iBleGatt, bluetoothGattDescriptor, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicRead(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onCharacteristicRead(iBleGatt, bluetoothGattCharacteristic, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicWrite(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onCharacteristicWrite(iBleGatt, bluetoothGattCharacteristic, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onMtuChanged(IBleGatt iBleGatt, int i, int i2) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onMtuChanged(iBleGatt, i, i2);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onReadRemoteRssi(IBleGatt iBleGatt, int i, int i2) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onReadRemoteRssi(iBleGatt, i, i2);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onReliableWriteCompleted(IBleGatt iBleGatt, int i) {
            if (BleSpenDriver.this.mExternalGattCallback != null) {
                BleSpenDriver.this.mExternalGattCallback.onReliableWriteCompleted(iBleGatt, i);
            }
        }
    };

    /* loaded from: classes.dex */
    public enum ConnectionState {
        NO_CONNECTION,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    /* loaded from: classes.dex */
    public interface EventListener {
        void onBatteryLevelChanged(int i, int i2);

        void onChargeStateChanged(BleSpenChargeState bleSpenChargeState);

        void onConnectionStateChanged(ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo);

        void onLedStateChanged(BleSpenLedState bleSpenLedState);

        void onNotifyBatteryState(BleSpenBatteryState bleSpenBatteryState);

        void onPenFrequencyChanged(BleSpenFrequency bleSpenFrequency);

        void onSensorEvent(BleSpenSensorEvent bleSpenSensorEvent);
    }

    /* loaded from: classes.dex */
    public interface OperationFinishListener {
        void onFinish(BleOpResultData bleOpResultData, long j);
    }

    public abstract void enableConnectionIntervalAutoChange(OperationFinishListener operationFinishListener);

    public abstract void enableLedIndication(OperationFinishListener operationFinishListener);

    public abstract void enablePenFrequencyIndication(OperationFinishListener operationFinishListener);

    public abstract void enablePenTipApproachIndication(OperationFinishListener operationFinishListener);

    public abstract int getBatteryLevel();

    public abstract BleSpenChargeState getChargeState();

    public abstract BleSpenOperationMode getCurrentOperationMode();

    public abstract String getFirmwareVersion();

    public abstract void getLedState(OperationFinishListener operationFinishListener);

    public abstract String getPenColorCode();

    public abstract BleSpenFrequency getPenFrequency();

    public abstract BleSpenApproachState getSpenApproachState();

    public abstract UUID getSpenServiceUuid();

    public abstract void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic);

    public void onConnectionStateChanged(IBleGatt iBleGatt, int i, int i2) {
    }

    public abstract void performCalibration(OperationFinishListener operationFinishListener);

    public abstract void readBatteryLevel(OperationFinishListener operationFinishListener);

    public abstract void readChargingState(OperationFinishListener operationFinishListener);

    public abstract void readFirmwareVersion(OperationFinishListener operationFinishListener);

    public abstract void readPenLog(OperationFinishListener operationFinishListener);

    public abstract void readRawBatteryLevel(OperationFinishListener operationFinishListener);

    public abstract void sendFastDischargeCommand(int i, OperationFinishListener operationFinishListener);

    public abstract void setBatteryLevel(int i);

    public abstract void setConnectionInterval(BleSpenConnectionInterval bleSpenConnectionInterval, OperationFinishListener operationFinishListener);

    public abstract void setLedState(BleSpenLedState bleSpenLedState, OperationFinishListener operationFinishListener);

    public abstract void setSlaveLatency(int i, OperationFinishListener operationFinishListener);

    public abstract void setSpenOperationMode(BleSpenOperationMode bleSpenOperationMode, OperationFinishListener operationFinishListener);

    /* loaded from: classes.dex */
    public static class EventListenerWrapper {
        private ArrayList<EventListener> mEventListenerArray = new ArrayList<>();
        private Object mListenerSyncObj = new Object();

        public void registerEventListener(EventListener eventListener) {
            if (eventListener == null) {
                return;
            }
            synchronized (this.mListenerSyncObj) {
                if (this.mEventListenerArray.contains(eventListener)) {
                    return;
                }
                this.mEventListenerArray.add(eventListener);
            }
        }

        public void unregisterEventListener(EventListener eventListener) {
            synchronized (this.mListenerSyncObj) {
                this.mEventListenerArray.remove(eventListener);
            }
        }

        public void clear() {
            synchronized (this.mListenerSyncObj) {
                this.mEventListenerArray.clear();
            }
        }

        public void onConnectionStateChanged(ConnectionState connectionState, BleStateChangeInfo bleStateChangeInfo) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onConnectionStateChanged(connectionState, bleStateChangeInfo);
            }
        }

        public void onBatteryLevelChanged(int i, int i2) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onBatteryLevelChanged(i, i2);
            }
        }

        public void onChargeStateChanged(BleSpenChargeState bleSpenChargeState) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onChargeStateChanged(bleSpenChargeState);
            }
        }

        public void onSensorEvent(BleSpenSensorEvent bleSpenSensorEvent) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onSensorEvent(bleSpenSensorEvent);
            }
        }

        public void onLedStateChanged(BleSpenLedState bleSpenLedState) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onLedStateChanged(bleSpenLedState);
            }
        }

        public void onPenFrequencyChanged(BleSpenFrequency bleSpenFrequency) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onPenFrequencyChanged(bleSpenFrequency);
            }
        }

        public void onNotifyBatteryState(BleSpenBatteryState bleSpenBatteryState) {
            Iterator<EventListener> it = getListenersCopy().iterator();
            while (it.hasNext()) {
                it.next().onNotifyBatteryState(bleSpenBatteryState);
            }
        }

        private ArrayList<EventListener> getListenersCopy() {
            ArrayList<EventListener> arrayList;
            synchronized (this.mListenerSyncObj) {
                arrayList = new ArrayList<>(this.mEventListenerArray);
            }
            return arrayList;
        }
    }

    public BleSpenDriver(SpenModelName spenModelName, BleSpenDeviceFeature bleSpenDeviceFeature, FmmDriver fmmDriver, FirmwareUpgradeDriver firmwareUpgradeDriver) {
        this.mSpenModelName = spenModelName;
        this.mSpenDeviceFeature = bleSpenDeviceFeature;
        this.mFmmDriver = fmmDriver;
        this.mFirmwareUpgradeDriver = firmwareUpgradeDriver;
    }

    public void initialize(Context context) {
        this.mContext = context.getApplicationContext();
        BleSpenBleDriver bleSpenBleDriver = new BleSpenBleDriver();
        this.mBleDriver = bleSpenBleDriver;
        bleSpenBleDriver.initialize(context);
    }

    public void release(OperationFinishListener operationFinishListener) {
        this.mBleDriver.release(operationFinishListener);
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public synchronized void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        this.mBleDriver.cancelOperation(finishListener);
    }

    public synchronized boolean open(String str, boolean z, boolean z2, final OperationFinishListener operationFinishListener, final EventListener eventListener, IBleGattCallback iBleGattCallback) {
        this.mExternalGattCallback = iBleGattCallback;
        this.mEventListener.registerEventListener(eventListener);
        return this.mBleDriver.open(str, z, z2, new OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.getResultCode() != BleOpResultCode.SUCCESS) {
                    BleSpenDriver.this.mExternalGattCallback = null;
                    BleSpenDriver.this.mEventListener.unregisterEventListener(eventListener);
                }
                OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        }, this.mInternalGattCallback);
    }

    public synchronized void close(final OperationFinishListener operationFinishListener) {
        this.mBleDriver.close(new OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenDriver.3
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                BleSpenDriver.this.mEventListener.clear();
                BleSpenDriver.this.mExternalGattCallback = null;
                OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    public synchronized void connect(OperationFinishListener operationFinishListener) {
        this.mBleDriver.connect(operationFinishListener);
    }

    public synchronized void disconnect(OperationFinishListener operationFinishListener) {
        this.mBleDriver.disconnect(operationFinishListener);
    }

    public synchronized void discoverService(OperationFinishListener operationFinishListener) {
        this.mBleDriver.discoverService(operationFinishListener);
    }

    public synchronized void enableBatteryNotification(OperationFinishListener operationFinishListener) {
        this.mBleDriver.enableNotification(getSpenServiceUuid(), BleSpenUuid.BATTERY_LEVEL, operationFinishListener);
    }

    public synchronized void enableButtonNotification(OperationFinishListener operationFinishListener) {
        this.mBleDriver.enableNotification(getSpenServiceUuid(), BleSpenUuid.BUTTON_EVENT, operationFinishListener);
    }

    public synchronized void enableChargeNotification(OperationFinishListener operationFinishListener) {
        this.mBleDriver.enableNotification(getSpenServiceUuid(), BleSpenUuid.CHARGE_STATUS, operationFinishListener);
    }

    public synchronized void writeEasyConnectId(OperationFinishListener operationFinishListener) {
        this.mBleDriver.writeCharacteristic(getSpenServiceUuid(), BleSpenUuid.EASY_CONNECT_ID, SecurityUtils.createLastConnectIdentifier(this.mContext), operationFinishListener);
    }

    public synchronized void enableRawSensorDataNotification(boolean z, OperationFinishListener operationFinishListener) {
        UUID spenServiceUuid = getSpenServiceUuid();
        if (z) {
            this.mBleDriver.enableNotification(spenServiceUuid, BleSpenUuid.RAW_SENSOR_DATA, operationFinishListener);
        } else {
            this.mBleDriver.disableNotification(spenServiceUuid, BleSpenUuid.RAW_SENSOR_DATA, operationFinishListener);
        }
    }

    public synchronized void enableSelfTestNotification(boolean z, OperationFinishListener operationFinishListener) {
        UUID spenServiceUuid = getSpenServiceUuid();
        if (z) {
            this.mBleDriver.enableNotification(spenServiceUuid, BleSpenUuid.SELF_TEST, operationFinishListener);
        } else {
            this.mBleDriver.disableNotification(spenServiceUuid, BleSpenUuid.SELF_TEST, operationFinishListener);
        }
    }

    public synchronized void readRemoteRssi(OperationFinishListener operationFinishListener) {
        this.mBleDriver.readRemoteRssi(operationFinishListener);
    }

    public synchronized ConnectionState getConnectionState() {
        return ConnectionState.valueOf(this.mBleDriver.getConnectionState().name());
    }

    public synchronized int getGattConnectionState() {
        return this.mBleDriver.getGattConnectionState();
    }

    public synchronized long getConnectionIntervalInNanos() {
        return this.mBleDriver.getConnectionIntervalInNanos();
    }

    public synchronized boolean isDisconnected() {
        return this.mBleDriver.isDisconnected();
    }

    public synchronized boolean isConnected() {
        return getConnectionState() == ConnectionState.CONNECTED;
    }

    public synchronized IBleGatt getGatt() {
        return this.mBleDriver.getGatt();
    }

    public BleSpenBleDriver getBleDriver() {
        return this.mBleDriver;
    }

    public synchronized String getConnectedDeviceAddress() {
        return this.mBleDriver.getConnectedDeviceAddress();
    }

    public synchronized void setExternalGattCallback(IBleGattCallback iBleGattCallback) {
        if (iBleGattCallback != null) {
            Assert.e(this.mExternalGattCallback == null);
        }
        this.mExternalGattCallback = iBleGattCallback;
    }

    public void requestGetFmmConfig(OperationFinishListener operationFinishListener) {
        FmmDriver fmmDriver = this.mFmmDriver;
        if (fmmDriver == null) {
            Log.e(TAG, "requestGetFmmConfig fail : fmmDriver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            return;
        }
        fmmDriver.requestGetFmmConfig(getSpenServiceUuid(), this.mBleDriver, operationFinishListener);
    }

    public void requestSetFmmConfig(FmmConfig fmmConfig, OperationFinishListener operationFinishListener) {
        FmmDriver fmmDriver = this.mFmmDriver;
        if (fmmDriver == null) {
            Log.e(TAG, "requestGetFmmConfig : fmmDriver is null");
            operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            return;
        }
        fmmDriver.requestSetFmmConfig(getSpenServiceUuid(), this.mBleDriver, fmmConfig, operationFinishListener);
    }

    public void enablePenClockNotification(OperationFinishListener operationFinishListener) {
        if (this.mFmmDriver.isSupportPolicy) {
            this.mBleDriver.enableNotification(getSpenServiceUuid(), BleSpenUuid.PEN_SYNC_CLOCK, null);
        } else {
            Log.d(TAG, "enablePenClockNotification is called but isSupportPolicy is false.");
        }
    }

    public void requestFirmwareUpgrade(File file, BleSpenFirmwareUpgradeListener bleSpenFirmwareUpgradeListener) {
        FirmwareUpgradeDriver firmwareUpgradeDriver = this.mFirmwareUpgradeDriver;
        if (firmwareUpgradeDriver == null) {
            Log.e(TAG, "requestFirmwareUpgrade : Firmware update driver is null");
            bleSpenFirmwareUpgradeListener.onFinish(BleOpResultCode.NOT_SUPPORTED);
            return;
        }
        firmwareUpgradeDriver.startFirmwareUpgrade(this, file, bleSpenFirmwareUpgradeListener);
    }

    public synchronized void enablePenLogNotification(OperationFinishListener operationFinishListener) {
        String str = TAG;
        Log.d(str, "enablePenLogNotification");
        if (!this.mSpenDeviceFeature.isSupportLogExtraction()) {
            Log.d(str, "enablePenLogNotification : not supported");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.NOT_SUPPORTED), 0L);
            }
            return;
        }
        this.mBleDriver.enableNotification(getSpenServiceUuid(), BleSpenUuid.PEN_LOG, operationFinishListener);
    }

    public void registerEventListener(EventListener eventListener) {
        synchronized (this.mSyncObject) {
            this.mEventListener.registerEventListener(eventListener);
        }
    }

    public void unregisterEventListener(EventListener eventListener) {
        synchronized (this.mSyncObject) {
            this.mEventListener.unregisterEventListener(eventListener);
        }
    }
}
