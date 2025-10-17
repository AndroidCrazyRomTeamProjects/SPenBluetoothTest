package com.samsung.remotespen.core.device.control.factory;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGatt;
import com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.util.sep.SepWrapper;
import java.util.UUID;

/* loaded from: classes.dex */
public class BleSpenBleDriver {
    private static final String ACTION_BLE_ACL_DISCONNECTED = "android.bluetooth.adapter.action.BLE_ACL_DISCONNECTED";
    private static final int BLEGATT_OPEN_FAIL_STATUS = 133;
    private static final int DEFAULT_TRANSACTION_TIMEOUT = 10000;
    private static final int DEVICE_OPEN_TRANSACTION_TIMEOUT = 15000;
    private static final int DISCONNECT_TRANSACTION_TIMEOUT = 500;
    private static final int READ_RSSI_TRANSACTION_TIMEOUT = 500;
    private static final String TAG = "BleSpenBleDriver";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    public Context mContext;
    private IBleGattCallback mExternalGattCallback;
    private IBleGatt mGatt;
    private TransactionManager mTransactionManager;
    private ConnectionState mConnectionState = ConnectionState.NO_CONNECTION;
    private long mConnectionIntervalInNanos = 0;
    private BroadcastReceiver mConnectionStateChangeReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            IBleGatt iBleGatt;
            if (BleSpenBleDriver.ACTION_BLE_ACL_DISCONNECTED.equals(intent.getAction()) || "android.bluetooth.device.action.ACL_DISCONNECTED".equals(intent.getAction())) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (bluetoothDevice == null) {
                    String str = BleSpenBleDriver.TAG;
                    Log.e(str, "onConnectionStateChanged : device is null. action=" + intent.getAction());
                    return;
                }
                synchronized (BleSpenBleDriver.this) {
                    iBleGatt = BleSpenBleDriver.this.mGatt;
                }
                if (iBleGatt == null) {
                    String str2 = BleSpenBleDriver.TAG;
                    Log.d(str2, "onConnectionStateChanged : gatt is null. action=" + intent.getAction());
                    return;
                }
                String address = iBleGatt.getDevice().getAddress();
                String address2 = bluetoothDevice.getAddress();
                if (!address2.equals(address)) {
                    String str3 = BleSpenBleDriver.TAG;
                    Log.i(str3, "onConnectionStateChanged : not my device : target=" + address2 + ", me=" + address + ", action=" + intent.getAction());
                    return;
                }
                int intExtra = intent.getIntExtra("com.samsung.bluetooth.device.extra.DISCONNECTION_REASON", -1);
                String str4 = BleSpenBleDriver.TAG;
                Log.i(str4, "ConnectionStateChangeReceiver :onConnectionStateChanged : " + iBleGatt.getDevice() + " state=" + intent.getAction() + " reason=" + intExtra);
                BleSpenBleDriver.this.onConnectionStateChanged(iBleGatt, intExtra, 0);
            }
        }
    };
    private IBleGattCallback mInternalGattCallback = new IBleGattCallback() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.2
        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onConnectionStateChange(IBleGatt iBleGatt, int i, int i2) {
            String str = BleSpenBleDriver.TAG;
            Log.i(str, "GattCallback : onConnectionStateChanged : " + iBleGatt.getDevice() + " state=" + i2 + " reason=" + i);
            if (i2 == 0 && i == 133) {
                BleSpenBleDriver.this.onConnectionStateChanged(iBleGatt, i, i2);
            } else if (i2 != 2) {
            } else {
                BleSpenBleDriver.this.onConnectionStateChanged(iBleGatt, 0, 2);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onServicesDiscovered(IBleGatt iBleGatt, int i) {
            BleOpResultCode bleOpResultCode;
            String str = BleSpenBleDriver.TAG;
            Log.i(str, "GattCallback : onServicesDiscovered : " + iBleGatt.getDevice() + " / status=" + i);
            if (i != 0) {
                String str2 = BleSpenBleDriver.TAG;
                Log.e(str2, "onServicesDiscovered: " + i);
                bleOpResultCode = BleOpResultCode.UNKNOWN_FAIL;
            } else {
                bleOpResultCode = BleOpResultCode.SUCCESS;
            }
            if (BleSpenBleDriver.this.isTransactionRunning(ServiceDiscoveryTransaction.class)) {
                BleSpenBleDriver.this.finishTransaction(bleOpResultCode, i);
            } else {
                Log.e(BleSpenBleDriver.TAG, "GattCallback : onServicesDiscovered : No running service discovering transaction");
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onServicesDiscovered(iBleGatt, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicChanged(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (!BleSpenUuid.PEN_LOG.equals(bluetoothGattCharacteristic.getUuid())) {
                String str = BleSpenBleDriver.TAG;
                Log.d(str, "GattCallback : onCharacteristicChanged : " + iBleGatt.getDevice() + " / " + BleUtils.getCharactersticInfoStr(bluetoothGattCharacteristic, false));
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onCharacteristicChanged(iBleGatt, bluetoothGattCharacteristic);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onDescriptorWrite(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            BleOpResultCode bleOpResultCode;
            BluetoothGattCharacteristic characteristic = bluetoothGattDescriptor.getCharacteristic();
            String str = BleSpenBleDriver.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("GattCallback : onDescriptorWrite : ");
            sb.append(iBleGatt.getDevice().getDeviceInfoStr());
            sb.append(" ");
            sb.append(characteristic != null ? BleSpenUuid.getName(characteristic.getUuid()) : "No Characteristic");
            sb.append(" ");
            sb.append(BleUtils.getDescriptorInfoStr(bluetoothGattDescriptor));
            sb.append(" / ");
            sb.append(i);
            Log.d(str, sb.toString());
            if (i != 0) {
                String str2 = BleSpenBleDriver.TAG;
                Log.e(str2, "GattCallback : onDescriptorWrite : Unknown status : " + i);
                bleOpResultCode = BleOpResultCode.UNKNOWN_FAIL;
            } else {
                bleOpResultCode = BleOpResultCode.SUCCESS;
            }
            if (BleSpenBleDriver.this.isTransactionRunning(EnableNotiTransaction.class)) {
                BleSpenBleDriver.this.finishTransaction(bleOpResultCode, i);
            } else {
                Log.e(BleSpenBleDriver.TAG, "GattCallback : onDescriptorWrite : No running transaction");
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onDescriptorWrite(iBleGatt, bluetoothGattDescriptor, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onDescriptorRead(IBleGatt iBleGatt, BluetoothGattDescriptor bluetoothGattDescriptor, int i) {
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onDescriptorRead : " + iBleGatt.getDevice() + " / " + BleUtils.getDescriptorInfoStr(bluetoothGattDescriptor) + " / " + i);
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onDescriptorRead(iBleGatt, bluetoothGattDescriptor, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicRead(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onCharacteristicRead : " + iBleGatt.getDevice() + " / " + BleUtils.getCharactersticInfoStr(bluetoothGattCharacteristic, false) + " / status=" + i);
            if (BleSpenBleDriver.this.isTransactionRunning(ReadCharacteristicTransaction.class)) {
                BleOpResultData bleOpResultData = new BleOpResultData();
                bleOpResultData.setGattStatusCode(i);
                if (i == 0) {
                    bleOpResultData.setBleResultCode(BleOpResultCode.SUCCESS);
                    bleOpResultData.setByteData(bluetoothGattCharacteristic.getValue());
                } else {
                    bleOpResultData.setBleResultCode(BleOpResultCode.UNKNOWN_FAIL);
                    String str2 = BleSpenBleDriver.TAG;
                    Log.e(str2, "GattCallback : onCharacteristicRead : Unknown status : " + i);
                }
                BleSpenBleDriver.this.finishTransaction(bleOpResultData);
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onCharacteristicRead(iBleGatt, bluetoothGattCharacteristic, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onCharacteristicWrite(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            BleOpResultCode bleOpResultCode;
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onCharacteristicWrite : " + iBleGatt.getDevice() + " / " + BleUtils.getCharactersticInfoStr(bluetoothGattCharacteristic, false) + " / status=" + i);
            if (i != 0) {
                String str2 = BleSpenBleDriver.TAG;
                Log.e(str2, "GattCallback : onCharacteristicWrite : Unknown status : " + i);
                bleOpResultCode = BleOpResultCode.UNKNOWN_FAIL;
            } else {
                bleOpResultCode = BleOpResultCode.SUCCESS;
            }
            if (BleSpenBleDriver.this.isTransactionRunning(WriteCharacteristicTransaction.class)) {
                BleSpenBleDriver.this.finishTransaction(bleOpResultCode, i);
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onCharacteristicWrite(iBleGatt, bluetoothGattCharacteristic, i);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onMtuChanged(IBleGatt iBleGatt, int i, int i2) {
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onMtuChanged : " + iBleGatt.getDevice() + " / " + i + " / " + i2);
            if (BleSpenBleDriver.this.isTransactionRunning(MtuRequestTransaction.class)) {
                BleOpResultData bleOpResultData = new BleOpResultData();
                bleOpResultData.setGattStatusCode(i2);
                if (i2 == 0) {
                    bleOpResultData.setBleResultCode(BleOpResultCode.SUCCESS);
                    bleOpResultData.setMtu(i);
                } else {
                    bleOpResultData.setBleResultCode(BleOpResultCode.UNKNOWN_FAIL);
                    String str2 = BleSpenBleDriver.TAG;
                    Log.e(str2, "GattCallback : onMtuChanged : Unknown status : " + i2);
                }
                BleSpenBleDriver.this.finishTransaction(bleOpResultData);
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onMtuChanged(iBleGatt, i, i2);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onReadRemoteRssi(IBleGatt iBleGatt, int i, int i2) {
            BleOpResultCode bleOpResultCode;
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onReadRemoteRssi : " + iBleGatt.getDevice() + " / " + i + " / " + i2);
            if (i2 != 0) {
                String str2 = BleSpenBleDriver.TAG;
                Log.e(str2, "GattCallback : onReadRemoteRssi : Unknown status : " + i2);
                bleOpResultCode = BleOpResultCode.UNKNOWN_FAIL;
            } else {
                bleOpResultCode = BleOpResultCode.SUCCESS;
            }
            if (BleSpenBleDriver.this.isTransactionRunning(ReadRemoteRssiTransaction.class)) {
                BleOpResultData bleOpResultData = new BleOpResultData(bleOpResultCode);
                bleOpResultData.setGattStatusCode(i2);
                bleOpResultData.setRssi(i);
                BleSpenBleDriver.this.finishTransaction(bleOpResultData);
            }
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onReadRemoteRssi(iBleGatt, i, i2);
            }
        }

        @Override // com.samsung.remotespen.core.device.ble.abstraction.IBleGattCallback
        public void onReliableWriteCompleted(IBleGatt iBleGatt, int i) {
            String str = BleSpenBleDriver.TAG;
            Log.d(str, "GattCallback : onReliableWriteCompleted+ " + iBleGatt.getDevice() + " /  / " + i);
            if (BleSpenBleDriver.this.mExternalGattCallback != null) {
                BleSpenBleDriver.this.mExternalGattCallback.onReliableWriteCompleted(iBleGatt, i);
            }
        }

        public void onConnectionUpdated(IBleGatt iBleGatt, int i, int i2, int i3, int i4) {
            BleSpenBleDriver.this.mConnectionIntervalInNanos = i * 1250000;
            String str = BleSpenBleDriver.TAG;
            Log.i(str, "onConnectionUpdated : ConnInterval=" + (((float) BleSpenBleDriver.this.mConnectionIntervalInNanos) / 1000000.0f) + "ms SlaveLatency=" + i2 + " timeout=" + (i3 * 10) + "ms status=" + i4);
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

    public synchronized void initialize(Context context) {
        this.mContext = context.getApplicationContext();
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService("bluetooth");
        this.mBluetoothManager = bluetoothManager;
        if (bluetoothManager == null) {
            Log.e(TAG, "initialize : failed to obtain bluetooth manager");
            throw new RuntimeException("failed to obtain bluetooth manager");
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        if (adapter == null) {
            Log.e(TAG, "initialize : failed to obtain BluetoothAdapter.");
            throw new RuntimeException("failed to obtain bluetooth adapter");
        }
        this.mTransactionManager = new TransactionManager(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        intentFilter.addAction(ACTION_BLE_ACL_DISCONNECTED);
        intentFilter.setPriority(999);
        this.mContext.registerReceiver(this.mConnectionStateChangeReceiver, intentFilter);
    }

    public synchronized void release(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        close(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.3
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                synchronized (BleSpenBleDriver.this) {
                    BleSpenBleDriver bleSpenBleDriver = BleSpenBleDriver.this;
                    bleSpenBleDriver.mContext.unregisterReceiver(bleSpenBleDriver.mConnectionStateChangeReceiver);
                    BleSpenBleDriver.this.mBluetoothManager = null;
                    BleSpenBleDriver.this.mBluetoothAdapter = null;
                    BleSpenBleDriver.this.mTransactionManager = null;
                    BleSpenBleDriver.this.mContext = null;
                }
                BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                if (operationFinishListener2 != null) {
                    operationFinishListener2.onFinish(bleOpResultData, j);
                }
            }
        });
    }

    public synchronized boolean open(String str, boolean z, boolean z2, final BleSpenDriver.OperationFinishListener operationFinishListener, IBleGattCallback iBleGattCallback) {
        if (this.mBluetoothAdapter == null) {
            Log.e(TAG, "open : mBluetoothAdapter is null");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.DEVICE_FAIL), 0L);
            }
            return false;
        }
        IBleDevice bluetoothDevice = BleEnvManager.getInstance(this.mContext).getBluetoothDevice(this.mContext, str);
        if (bluetoothDevice == null) {
            Log.e(TAG, "open : Failed to get device");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.DEVICE_FAIL), 0L);
            }
            return false;
        } else if (!SepWrapper.BluetoothAdapter.semIsBleEnabled(this.mBluetoothAdapter)) {
            Log.e(TAG, "open : BLE is not enabled");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.BLE_NOT_ENABLED), 0L);
            }
            return false;
        } else if (!isDisconnected()) {
            Log.e(TAG, "open : the driver is alrady opend");
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.ALREADY_OPENED), 0L);
            }
            return false;
        } else {
            String str2 = TAG;
            Log.i(str2, "open : addr=" + bluetoothDevice.getAddress() + " name=" + bluetoothDevice.getName() + " autoReconn=" + z + " isBuiltIn=" + z2);
            setConnectionState(ConnectionState.CONNECTING);
            this.mExternalGattCallback = iBleGattCallback;
            startTransaction(new DeviceOpenTransaction(this.mContext, bluetoothDevice, z, z2, this.mInternalGattCallback), new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.4
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    BleOpResultCode resultCode = bleOpResultData.getResultCode();
                    if (!bleOpResultData.isSuccess()) {
                        String str3 = BleSpenBleDriver.TAG;
                        Log.e(str3, "open : Failed to connect - " + resultCode.name());
                        int i = AnonymousClass7.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[resultCode.ordinal()];
                        if (i != 1 && i != 2) {
                            BleSpenBleDriver.this.setGatt(null);
                            BleSpenBleDriver.this.mExternalGattCallback = null;
                        }
                    }
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, j);
                    }
                }
            }, DEVICE_OPEN_TRANSACTION_TIMEOUT);
            return true;
        }
    }

    /* renamed from: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver$7  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass7 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode;

        static {
            int[] iArr = new int[BleOpResultCode.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode = iArr;
            try {
                iArr[BleOpResultCode.ALREADY_OPENED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleOpResultCode[BleOpResultCode.ALREADY_RUNNING.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public synchronized void close(final BleSpenDriver.OperationFinishListener operationFinishListener) {
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        final Runnable runnable = new Runnable() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.5
            @Override // java.lang.Runnable
            public void run() {
                synchronized (BleSpenBleDriver.this) {
                    if (BleSpenBleDriver.this.mGatt != null) {
                        BleSpenBleDriver.this.setGatt(null);
                        BleSpenBleDriver.this.mExternalGattCallback = null;
                    }
                }
            }
        };
        if (!isDisconnected()) {
            Log.i(TAG, "close : close after disconnect");
            disconnect(new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.6
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    BleOpResultCode resultCode = bleOpResultData.getResultCode();
                    runnable.run();
                    long elapsedRealtime2 = SystemClock.elapsedRealtime() - elapsedRealtime;
                    String str = BleSpenBleDriver.TAG;
                    Log.i(str, "close : disconnection & close finished : " + resultCode.name() + ", " + elapsedRealtime2 + "ms");
                    BleSpenDriver.OperationFinishListener operationFinishListener2 = operationFinishListener;
                    if (operationFinishListener2 != null) {
                        operationFinishListener2.onFinish(bleOpResultData, elapsedRealtime2);
                    }
                }
            });
        } else {
            Log.i(TAG, "close : close immediately");
            runnable.run();
            if (operationFinishListener != null) {
                operationFinishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS), SystemClock.elapsedRealtime() - elapsedRealtime);
            }
        }
    }

    public synchronized void connect(BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new ConnectTransaction(), operationFinishListener);
    }

    public synchronized void disconnect(BleSpenDriver.OperationFinishListener operationFinishListener) {
        Transaction workingTransaction = getWorkingTransaction();
        if (workingTransaction != null && !(workingTransaction instanceof DisconnectTransaction)) {
            String str = TAG;
            Log.i(str, "disconnect : working transaction exists. transaction=" + workingTransaction.getName());
        }
        startTransaction(new DisconnectTransaction(), operationFinishListener, Action.Repeat.REPEATABLE_INTERVAL_MEDIUM_VALUE);
    }

    public synchronized void discoverService(BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new ServiceDiscoveryTransaction(), operationFinishListener);
    }

    public synchronized void enableNotification(UUID uuid, UUID uuid2, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new EnableNotiTransaction(uuid, uuid2, true, false), operationFinishListener);
    }

    public synchronized void enableIndication(UUID uuid, UUID uuid2, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new EnableNotiTransaction(uuid, uuid2, true, true), operationFinishListener);
    }

    public synchronized void disableNotification(UUID uuid, UUID uuid2, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new EnableNotiTransaction(uuid, uuid2, false, false), operationFinishListener);
    }

    public synchronized void readCharacteristic(UUID uuid, UUID uuid2, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new ReadCharacteristicTransaction(uuid, uuid2), operationFinishListener);
    }

    public synchronized void writeCharacteristic(UUID uuid, UUID uuid2, byte[] bArr, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new WriteCharacteristicTransaction(uuid, uuid2, bArr), operationFinishListener);
    }

    public synchronized void writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new WriteCharacteristicTransaction(bluetoothGattCharacteristic), operationFinishListener);
    }

    public synchronized void requestMtu(int i, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new MtuRequestTransaction(i), operationFinishListener);
    }

    public synchronized boolean requestConnectionPriority(int i) {
        String str = TAG;
        Log.i(str, "requestConnectionPriority : " + i);
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            return false;
        }
        return iBleGatt.requestConnectionPriority(i);
    }

    public synchronized void readRemoteRssi(BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(new ReadRemoteRssiTransaction(), operationFinishListener, Action.Repeat.REPEATABLE_INTERVAL_MEDIUM_VALUE);
    }

    public synchronized void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        this.mTransactionManager.cancelAllTransactions(finishListener);
    }

    /* JADX WARN: Code restructure failed: missing block: B:8:0x000d, code lost:
        if (r0 != com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.ConnectionState.CONNECTED) goto L14;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public synchronized boolean isDisconnected() {
        /*
            r2 = this;
            monitor-enter(r2)
            com.samsung.remotespen.core.device.ble.abstraction.IBleGatt r0 = r2.mGatt     // Catch: java.lang.Throwable -> L15
            if (r0 == 0) goto L12
            com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver$ConnectionState r0 = r2.mConnectionState     // Catch: java.lang.Throwable -> L15
            com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver$ConnectionState r1 = com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.ConnectionState.CONNECTING     // Catch: java.lang.Throwable -> L15
            if (r0 == r1) goto L10
            com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver$ConnectionState r1 = com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.ConnectionState.CONNECTED     // Catch: java.lang.Throwable -> L15
            if (r0 == r1) goto L10
            goto L12
        L10:
            r0 = 0
            goto L13
        L12:
            r0 = 1
        L13:
            monitor-exit(r2)
            return r0
        L15:
            r0 = move-exception
            monitor-exit(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver.isDisconnected():boolean");
    }

    public synchronized ConnectionState getConnectionState() {
        return this.mConnectionState;
    }

    public synchronized int getGattConnectionState() {
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            return -1;
        }
        IBleDevice device = iBleGatt.getDevice();
        if (device == null) {
            return -1;
        }
        return device.getConnectionState(this.mContext);
    }

    public synchronized String getConnectedDeviceName() {
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            return null;
        }
        IBleDevice device = iBleGatt.getDevice();
        if (device == null) {
            return null;
        }
        return device.getName();
    }

    public synchronized String getConnectedDeviceAddress() {
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            return null;
        }
        IBleDevice device = iBleGatt.getDevice();
        if (device == null) {
            return null;
        }
        return device.getAddress();
    }

    public synchronized long getConnectionIntervalInNanos() {
        if (this.mGatt == null) {
            Log.e(TAG, "getConnectionIntervalInNanos : GATT is null");
            return 0L;
        }
        ConnectionState connectionState = getConnectionState();
        if (connectionState != ConnectionState.CONNECTED) {
            String str = TAG;
            Log.e(str, "getConnectionIntervalInNanos : not connected state. state=" + connectionState);
            return 0L;
        }
        if (this.mConnectionIntervalInNanos == 0) {
            Log.i(TAG, "getConnectionIntervalInNanos : Connection interval is not detected yet");
        }
        return this.mConnectionIntervalInNanos;
    }

    public synchronized BluetoothGattService getGattService(UUID uuid) {
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            return null;
        }
        return iBleGatt.getService(uuid);
    }

    public synchronized IBleGatt getGatt() {
        return this.mGatt;
    }

    public synchronized boolean requestCharacteristicRead(IBleGatt iBleGatt, UUID uuid, UUID uuid2) {
        String str = TAG;
        Log.d(str, "requestCharacteristicRead : " + BleSpenUuid.getName(uuid2));
        BluetoothGattService service = iBleGatt.getService(uuid);
        if (service == null) {
            Log.d(str, "requestCharacteristicRead : Failed to get service");
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid2);
        if (characteristic != null) {
            if (iBleGatt.readCharacteristic(characteristic)) {
                return true;
            }
            Log.e(str, "requestCharacteristicRead : gatt.readCharacteristic() failed");
            return false;
        }
        Log.d(str, "requestCharacteristicRead : Failed to get characteristic");
        return false;
    }

    public synchronized boolean requestCharacteristicWrite(IBleGatt iBleGatt, UUID uuid, UUID uuid2, byte[] bArr) {
        String str = TAG;
        Log.i(str, "requestCharacteristicWrite : " + BleSpenUuid.getName(uuid2) + ", " + BleUtils.getRawDataDumpStr(bArr));
        BluetoothGattService service = iBleGatt.getService(uuid);
        if (service == null) {
            Log.d(str, "requestCharacteristicWrite : Failed to get service");
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid2);
        if (characteristic != null) {
            characteristic.setValue(bArr);
            if (iBleGatt.writeCharacteristic(characteristic)) {
                return true;
            }
            Log.e(str, "requestCharacteristicWrite : gatt.writeCharacteristic() failed");
            return false;
        }
        Log.d(str, "requestCharacteristicWrite : Failed to get characteristic");
        return false;
    }

    public synchronized boolean requestCharacteristicWrite(IBleGatt iBleGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (bluetoothGattCharacteristic != null) {
            String str = TAG;
            Log.i(str, "requestCharacteristicWrite : " + bluetoothGattCharacteristic.getUuid() + ", " + BleUtils.getRawDataDumpStr(bluetoothGattCharacteristic.getValue()));
            if (iBleGatt.writeCharacteristic(bluetoothGattCharacteristic)) {
                return true;
            }
            Log.e(str, "requestCharacteristicWrite : gatt.writeCharacteristic() failed");
            return false;
        }
        Log.d(TAG, "requestCharacteristicWrite : Failed to get characteristic");
        return false;
    }

    public synchronized boolean requestCharacteristicNotificationEnable(UUID uuid, UUID uuid2, boolean z, boolean z2) {
        String sb;
        byte[] bArr;
        if (z) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Enable / ");
            sb2.append(z2 ? "indication type" : "notification type");
            sb = sb2.toString();
        } else {
            sb = "Disable";
        }
        String str = TAG;
        Log.d(str, "requestCharacteristicNotificationEnable : " + BleSpenUuid.getName(uuid2) + " " + sb);
        IBleGatt iBleGatt = this.mGatt;
        if (iBleGatt == null) {
            Log.e(str, "requestCharacteristicNotificationEnable : GATT is null!");
            return false;
        }
        BluetoothGattService service = iBleGatt.getService(uuid);
        if (service == null) {
            Log.e(str, "requestCharacteristicNotificationEnable : GATT service is null!");
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid2);
        if (characteristic == null) {
            Log.e(str, "requestCharacteristicNotificationEnable : Not supported characteristic - " + BleSpenUuid.getName(uuid2));
            return false;
        } else if (!iBleGatt.setCharacteristicNotification(characteristic, z)) {
            Log.e(str, "requestCharacteristicNotificationEnable : failed to change notification state");
            return false;
        } else {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleSpenUuid.CHARACTERISTIC_CONFIG);
            if (descriptor != null) {
                if (z) {
                    bArr = z2 ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    bArr = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                descriptor.setValue(bArr);
                iBleGatt.writeDescriptor(descriptor);
            } else {
                Log.e(str, "requestCharacteristicNotificationEnable : descriptor is null!");
            }
            return true;
        }
    }

    public synchronized void setGatt(IBleGatt iBleGatt) {
        IBleGatt iBleGatt2 = this.mGatt;
        if (iBleGatt2 == iBleGatt) {
            return;
        }
        if (iBleGatt2 != null) {
            if (iBleGatt != null) {
                Log.e(TAG, "setGatt : Previous GATT is available!", new Exception());
            }
            this.mGatt.close();
        }
        if (iBleGatt == null) {
            setConnectionState(ConnectionState.NO_CONNECTION);
        }
        this.mGatt = iBleGatt;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onConnectionStateChanged(IBleGatt iBleGatt, int i, int i2) {
        acquireWakeLock();
        if (i2 == 0) {
            String str = TAG;
            Log.i(str, "onConnectionStateChanged : action=" + i2 + ", reason=" + i);
            if (this.mConnectionState != ConnectionState.NO_CONNECTION) {
                setConnectionState(ConnectionState.DISCONNECTED);
            } else {
                Log.e(str, "onConnectionStateChanged : already closed gatt");
            }
            this.mConnectionIntervalInNanos = 0L;
            if (isTransactionRunning(DisconnectTransaction.class)) {
                finishTransaction(BleOpResultCode.SUCCESS, i);
            } else if (isAnyTransactionRunning()) {
                if (i == 133) {
                    finishTransaction(BleOpResultCode.GATT_OPEN_FAIL, i);
                } else {
                    finishTransaction(BleOpResultCode.DISCONNECTED, i);
                }
            }
        } else if (i2 == 2) {
            if (this.mConnectionState != ConnectionState.NO_CONNECTION) {
                setConnectionState(ConnectionState.CONNECTED);
            } else {
                Log.e(TAG, "onConnectionStateChanged : already closed gatt");
            }
            if (isTransactionRunning(DeviceOpenTransaction.class)) {
                DeviceOpenTransaction deviceOpenTransaction = (DeviceOpenTransaction) getWorkingTransaction();
                if (deviceOpenTransaction != null) {
                    deviceOpenTransaction.onGattConnected(i);
                } else {
                    Log.e(TAG, "onConnectionStateChanged : deviceOpenTransaction is null");
                }
            } else if (isTransactionRunning(ConnectTransaction.class)) {
                finishTransaction(BleOpResultCode.SUCCESS, i);
            } else if (isTransactionRunning(DisconnectTransaction.class)) {
                Log.e(TAG, "onConnectionStateChanged : disconnect transaction is running");
            }
        } else {
            String str2 = TAG;
            Log.e(str2, "onConnectionStateChanged : Unexpected state - " + i2);
            if (isAnyTransactionRunning()) {
                finishTransaction(BleOpResultCode.UNKNOWN_FAIL, i);
            }
        }
        IBleGattCallback iBleGattCallback = this.mExternalGattCallback;
        if (iBleGattCallback != null) {
            iBleGattCallback.onConnectionStateChange(iBleGatt, i, i2);
        }
    }

    private void acquireWakeLock() {
        ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "BLE SPen conn state").acquire(500L);
    }

    private void startTransaction(Transaction transaction, BleSpenDriver.OperationFinishListener operationFinishListener) {
        startTransaction(transaction, operationFinishListener, DEFAULT_TRANSACTION_TIMEOUT);
    }

    private void startTransaction(Transaction transaction, BleSpenDriver.OperationFinishListener operationFinishListener, int i) {
        this.mTransactionManager.startTransaction(transaction, operationFinishListener, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishTransaction(BleOpResultCode bleOpResultCode, int i) {
        BleOpResultData bleOpResultData = new BleOpResultData(bleOpResultCode);
        bleOpResultData.setGattStatusCode(i);
        finishTransaction(bleOpResultData);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishTransaction(BleOpResultData bleOpResultData) {
        this.mTransactionManager.finishHeadTransaction(bleOpResultData);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isTransactionRunning(Class<?> cls) {
        return this.mTransactionManager.isTransactionRunning(cls);
    }

    private boolean isAnyTransactionRunning() {
        return this.mTransactionManager.isTransactionRunning(null);
    }

    private Transaction getWorkingTransaction() {
        return this.mTransactionManager.getHeadTransaction();
    }

    private void setConnectionState(ConnectionState connectionState) {
        this.mConnectionState = connectionState;
    }
}
