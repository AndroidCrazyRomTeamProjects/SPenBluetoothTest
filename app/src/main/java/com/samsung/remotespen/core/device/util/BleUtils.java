package com.samsung.remotespen.core.device.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.graphics.PointF;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;
import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.util.SpenGestureManagerWrapper;
import com.samsung.util.sep.SepWrapper;
import java.util.List;

/* loaded from: classes.dex */
public class BleUtils {
    private static final int DECIMAL_RADIX = 10;
    private static final int HEX_RADIX = 16;
    private static final int MANUFACTURER_ID_SAMSUNG = 117;
    private static final String TAG = "BleUtils";

    public static int getSamsungManufacturerId() {
        return 117;
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService("bluetooth");
        if (bluetoothManager == null) {
            Log.e(TAG, "getBluetoothAdapter : failed to obtain bluetooth manager", new Exception());
            return null;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            Log.e(TAG, "getBluetoothAdapter : failed to obtain BluetoothAdapter.", new Exception());
            return null;
        }
        return adapter;
    }

    public static String getBluetoothDeviceName(Context context, String str) {
        IBleDevice bluetoothDevice = BleEnvManager.getInstance(context).getBluetoothDevice(context, str);
        if (bluetoothDevice == null) {
            Log.e(TAG, "getBluetoothDeviceName : failed to get BLE device");
            return null;
        }
        return bluetoothDevice.getName();
    }

    public static boolean isBleEnabled(Context context) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            return SepWrapper.BluetoothAdapter.semIsBleEnabled(bluetoothAdapter);
        }
        return false;
    }

    public static boolean isBluetoothEnabled(Context context) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    public static boolean setBluetoothEnable(Context context) {
        try {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
            if (bluetoothAdapter != null) {
                return bluetoothAdapter.enable();
            }
            return false;
        } catch (SecurityException e) {
            String str = TAG;
            Log.e(str, "setBluetoothEnable :" + e.toString());
            return false;
        }
    }

    public static boolean removeBond(Context context, String str) {
        String str2 = TAG;
        Log.d(str2, "removeBond : " + str);
        try {
            BluetoothDevice remoteDevice = getBluetoothAdapter(context).getRemoteDevice(str);
            if (remoteDevice == null) {
                Log.e(str2, "removeBond : failed to get device! " + str);
                return false;
            }
            SepWrapper.BluetoothDevice.semRemoveBond(remoteDevice);
            return true;
        } catch (Exception e) {
            String str3 = TAG;
            Log.e(str3, "removeBond :" + e.toString());
            return false;
        }
    }

    public static void resetBondedDevice(Context context) {
        Log.d(TAG, "resetBondedDevice");
        String readBundledBleSpenAddressFromEfs = readBundledBleSpenAddressFromEfs(context);
        if (readBundledBleSpenAddressFromEfs == null || readBundledBleSpenAddressFromEfs.isEmpty()) {
            return;
        }
        removeBond(context, readBundledBleSpenAddressFromEfs);
    }

    public static void writeBundledBleSpenAddressToEfs(Context context, String str) {
        String str2 = TAG;
        Log.i(str2, "writeBundledBleSpenAddressToEfs : [" + str + "]");
        SpenGestureManagerWrapper spenGestureManagerWrapper = new SpenGestureManagerWrapper(context);
        String readBundledBleSpenAddressFromEfs = readBundledBleSpenAddressFromEfs(context);
        if (readBundledBleSpenAddressFromEfs != null && !TextUtils.isEmpty(readBundledBleSpenAddressFromEfs) && !readBundledBleSpenAddressFromEfs.equals(str)) {
            removeBond(context, readBundledBleSpenAddressFromEfs);
        }
        spenGestureManagerWrapper.setBleSpenAddress(str);
    }

    public static String readBundledBleSpenAddressFromEfs(Context context) {
        String bleSpenAddress = new SpenGestureManagerWrapper(context).getBleSpenAddress();
        if (bleSpenAddress == null || isValidBluetoothAddress(bleSpenAddress)) {
            return bleSpenAddress;
        }
        String str = TAG;
        Log.e(str, "readBundledBleSpenAddressFromEfs : Invalid BD address! address = [" + bleSpenAddress + "]");
        return null;
    }

    public static boolean disableBleStandAloneMode(Context context) {
        try {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
            if (bluetoothAdapter == null) {
                Log.e(TAG, "disableBleStandAloneMode : failed to obtain BluetoothAdapter.");
                return false;
            }
            if (!isBleEnabled(context)) {
                Log.d(TAG, "disableBleStandAloneMode : BLE is disabled");
            }
            boolean semSetStandAloneBleMode = SepWrapper.BluetoothAdapter.semSetStandAloneBleMode(bluetoothAdapter, false);
            String str = TAG;
            Log.i(str, "disableBleStandAloneMode : isSuccess=" + semSetStandAloneBleMode);
            return semSetStandAloneBleMode;
        } catch (SecurityException e) {
            String str2 = TAG;
            Log.e(str2, "disableBleStandAloneMode :" + e.toString());
            return false;
        }
    }

    public static String convertBtStateToString(int i) {
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        switch (i) {
                            case 10:
                                return "BT_OFF";
                            case 11:
                                return "BT_TURNING_ON";
                            case 12:
                                return "BT_ON";
                            case 13:
                                return "BT_TURNING_OFF";
                            case 14:
                                return "BLE_TURNING_ON";
                            case 15:
                                return "BLE_ON";
                            case 16:
                                return "BLE_TURNING_OFF";
                            default:
                                return "UNKNOWN(" + i + ")";
                        }
                    }
                    return "BT_DISCONNECTING";
                }
                return "BT_CONNECTED";
            }
            return "BT_CONNECTING";
        }
        return "BT_DISCONNECTED";
    }

    public static String convertBondStateToString(int i) {
        switch (i) {
            case 10:
                return "BOND_NONE";
            case 11:
                return "BOND_BONDING";
            case 12:
                return "BOND_BONDED";
            default:
                return "Unknown - " + i;
        }
    }

    public static String getCharactersticInfoStr(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append(BleSpenUuid.getName(bluetoothGattCharacteristic.getUuid()));
        sb.append(", ");
        byte[] value = bluetoothGattCharacteristic.getValue();
        sb.append("RAWDATA : ");
        if (value != null) {
            sb.append(getRawDataDumpStr(value, Math.min(value.length, 128)));
        } else {
            sb.append("null");
        }
        if (z) {
            List<BluetoothGattDescriptor> descriptors = bluetoothGattCharacteristic.getDescriptors();
            sb.append("DESC_CNT=" + descriptors.size() + " ");
            for (BluetoothGattDescriptor bluetoothGattDescriptor : descriptors) {
                sb.append("[ ");
                sb.append(getDescriptorInfoStr(bluetoothGattDescriptor));
                sb.append("] ");
            }
        }
        return sb.toString();
    }

    public static String getDescriptorInfoStr(BluetoothGattDescriptor bluetoothGattDescriptor) {
        return "DESC : " + BleSpenUuid.getName(bluetoothGattDescriptor.getUuid()) + ", " + getRawDataDumpStr(bluetoothGattDescriptor.getValue());
    }

    public static String getRawDataHexDumpStr(byte[] bArr) {
        return getRawDataDumpStr(bArr, Preference.DEFAULT_ORDER, 16);
    }

    public static String getRawDataDumpStr(byte[] bArr) {
        return getRawDataDumpStr(bArr, Preference.DEFAULT_ORDER, 10);
    }

    public static String getRawDataDumpStr(byte[] bArr, int i) {
        return getRawDataDumpStr(bArr, i, 10);
    }

    private static String getRawDataDumpStr(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            return "[nodata]";
        }
        int min = Math.min(bArr.length, i);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i3 = 0; i3 < min; i3++) {
            if (i2 == 16) {
                sb.append(String.format("%02X", Byte.valueOf(bArr[i3])));
            } else {
                sb.append(Integer.valueOf(bArr[i3] & 255));
            }
            if (i3 < bArr.length - 1) {
                sb.append(" ");
            }
        }
        int length = bArr.length - min;
        if (length > 0) {
            sb.append(" .. +" + length + " bytes");
        }
        sb.append("]");
        return sb.toString();
    }

    public static PointF scaleMotionEvent(PointF pointF, float f) {
        PointF pointF2 = new PointF();
        float f2 = pointF.x / f;
        pointF2.x = f2;
        float f3 = (-pointF.y) / f;
        pointF2.y = f3;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        } else if (f2 < -1.0f) {
            f2 = -1.0f;
        }
        pointF2.x = f2;
        if (f3 > 1.0f) {
            f3 = 1.0f;
        } else if (f3 < -1.0f) {
            f3 = -1.0f;
        }
        pointF2.y = f3;
        pointF2.x = (float) (Math.round(f2 * 1000000.0f) / 1000000.0d);
        pointF2.y = (float) (Math.round(pointF2.y * 1000000.0f) / 1000000.0d);
        return pointF2;
    }

    public static boolean isValidBluetoothAddress(String str) {
        return BluetoothAdapter.checkBluetoothAddress(str);
    }

    public static ParcelUuid getSpenDeviceTypeUuid(boolean z) {
        return z ? ParcelUuid.fromString("EDFEC62E-9910-0BAC-5241-D8BDA6932A2F") : ParcelUuid.fromString("EDFEC62E-9910-0BAC-5241-D8BDA6932A30");
    }
}
