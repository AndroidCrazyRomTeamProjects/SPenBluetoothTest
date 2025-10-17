package com.samsung.remotespen.core.device.control.factory.shared;

import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.ble.constants.BleSpenUuid;
import com.samsung.remotespen.core.device.control.factory.BleSpenBleDriver;
import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.control.factory.FmmDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.FmmConfig;
import com.samsung.remotespen.core.device.data.FmmConfigPolicy;
import com.samsung.remotespen.core.device.util.BleUtils;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;

/* loaded from: classes.dex */
public class GenericFmmDriver extends FmmDriver {
    public static final int FMM_CONFIG_MAX_BYTES_SIZE = 83;
    public static final int FMM_CONFIG_POLICY_MAX_BYTES_SIZE = 21;
    private static final byte FMM_FIELD_ADV_INTERVAL = 10;
    private static final byte FMM_FIELD_E2E = 6;
    private static final byte FMM_FIELD_FMM_TOKEN = 2;
    private static final byte FMM_FIELD_IV = 7;
    private static final byte FMM_FIELD_LOST_INTERVAL = 9;
    private static final byte FMM_FIELD_MAX_N = 4;
    private static final byte FMM_FIELD_REGION = 5;
    private static final byte FMM_FIELD_SECRET_KEY = 3;
    private static final byte FMM_FIELD_SFFL_INTERVAL = 11;
    private static final byte FMM_FIELD_SUPPORT_FINDING_SERVICE = 1;
    private static final byte FMM_FIELD_TIMEUNIT = 12;
    private static final byte FMM_FIELD_VERSION = 8;
    private static final int FMM_LONG_FIELD_SIZE = 16;
    private static final byte REQUIRED_FMM_FIELD_COUNT = 7;
    private static final byte REQUIRED_FMM_POLICY_FIELD_COUNT = 5;
    private static final String TAG = "GenericFmmDriver";

    @Override // com.samsung.remotespen.core.device.control.factory.FmmDriver
    public void requestGetFmmConfig(UUID uuid, BleSpenBleDriver bleSpenBleDriver, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.isSupportPolicy = false;
        bleSpenBleDriver.readCharacteristic(uuid, BleSpenUuid.FMM_CONFIG, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                bleOpResultData.setFmmConfig(GenericFmmDriver.this.getFmmConfigFullData(bleOpResultData.getByteData()));
                operationFinishListener.onFinish(bleOpResultData, j);
            }
        });
    }

    /* JADX WARN: Code restructure failed: missing block: B:15:0x0028, code lost:
        com.samsung.util.debug.Log.d(com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG, "getFmmConfigFullData : length less than 0 ");
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public com.samsung.remotespen.core.device.data.FmmConfig getFmmConfigFullData(byte[] r10) {
        /*
            r9 = this;
            r0 = 0
            if (r10 != 0) goto Lb
            java.lang.String r9 = com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG
            java.lang.String r10 = "getFmmConfigFullData fail : byteData is null "
            com.samsung.util.debug.Log.e(r9, r10)
            return r0
        Lb:
            int r1 = r10.length
            if (r1 != 0) goto L16
            java.lang.String r9 = com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG
            java.lang.String r10 = "getFmmConfigFullData fail : fmmLength is 0 "
            com.samsung.util.debug.Log.e(r9, r10)
            return r0
        L16:
            com.samsung.remotespen.core.device.data.FmmConfig r2 = new com.samsung.remotespen.core.device.data.FmmConfig
            r2.<init>()
            r3 = 0
            r4 = r3
            r5 = r4
        L1e:
            if (r4 >= r1) goto L5a
            int r6 = r4 + 1
            r4 = r10[r4]     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            int r4 = r4 + (-1)
            if (r4 >= 0) goto L30
            java.lang.String r9 = com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            java.lang.String r10 = "getFmmConfigFullData : length less than 0 "
            com.samsung.util.debug.Log.d(r9, r10)     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            goto L5a
        L30:
            int r7 = r6 + 1
            r6 = r10[r6]     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            if (r4 <= 0) goto L3e
            byte[] r8 = new byte[r4]     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            java.lang.System.arraycopy(r10, r7, r8, r3, r4)     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
            r9.composeFmmConfig(r2, r6, r8)     // Catch: java.lang.ArrayIndexOutOfBoundsException -> L42
        L3e:
            int r4 = r4 + r7
            int r5 = r5 + 1
            goto L1e
        L42:
            r9 = move-exception
            java.lang.String r10 = com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getFmmConfigFullData : Unable to convert byteData to FmmConfig : "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r9 = r1.toString()
            com.samsung.util.debug.Log.e(r10, r9)
            return r0
        L5a:
            r9 = 7
            if (r5 == r9) goto L78
            r9 = 12
            if (r5 == r9) goto L78
            java.lang.String r9 = com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r1 = "getFmmConfigFullData : readFieldCount :"
            r10.append(r1)
            r10.append(r5)
            java.lang.String r10 = r10.toString()
            com.samsung.util.debug.Log.e(r9, r10)
            return r0
        L78:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.getFmmConfigFullData(byte[]):com.samsung.remotespen.core.device.data.FmmConfig");
    }

    private void composeFmmConfig(FmmConfig fmmConfig, byte b, byte[] bArr) {
        switch (b) {
            case 1:
                composeSupportFindingService(fmmConfig, bArr);
                return;
            case 2:
                composeFmmToken(fmmConfig, bArr);
                return;
            case 3:
                composeSecretKey(fmmConfig, bArr);
                return;
            case 4:
                composeMaxN(fmmConfig, bArr);
                return;
            case 5:
                composeRegion(fmmConfig, bArr);
                return;
            case 6:
                composeE2E(fmmConfig, bArr);
                return;
            case 7:
                composeIV(fmmConfig, bArr);
                return;
            case 8:
                composeVersion(fmmConfig, bArr);
                return;
            case 9:
                composeLostInterval(fmmConfig, bArr);
                return;
            case 10:
                composeAdvInterval(fmmConfig, bArr);
                return;
            case 11:
                composeShuffleInterval(fmmConfig, bArr);
                return;
            case 12:
                composeTimeUnit(fmmConfig, bArr);
                return;
            default:
                return;
        }
    }

    private void composeIV(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setIv(isEmptyData(bArr) ? Constants.packageName.NONE : new String(Base64.getEncoder().encode(bArr), Charset.defaultCharset()).trim());
    }

    private boolean isEmptyData(byte[] bArr) {
        for (byte b : bArr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private void composeE2E(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setE2e(new String(bArr, Charset.defaultCharset()).trim());
    }

    private void composeSupportFindingService(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setFindingSupport(new String(bArr, Charset.defaultCharset()).trim());
    }

    private void composeFmmToken(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setFmmToken(new String(bArr, Charset.defaultCharset()).trim());
    }

    private void composeSecretKey(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setSecretKey(isEmptyData(bArr) ? Constants.packageName.NONE : new String(Base64.getEncoder().encode(bArr), Charset.defaultCharset()).trim());
    }

    private void composeMaxN(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setMaxN(((short) ((bArr[0] & 255) << 8)) | (bArr[1] & 255));
    }

    private void composeRegion(FmmConfig fmmConfig, byte[] bArr) {
        fmmConfig.setRegion(bArr[0] & 255);
    }

    private void composeVersion(FmmConfig fmmConfig, byte[] bArr) {
        byte b = bArr[0];
        String str = TAG;
        Log.d(str, "composeVersion value :" + ((int) b));
        if (fmmConfig.getPolicy() == null) {
            fmmConfig.setPolicy(new FmmConfigPolicy());
            fmmConfig.getPolicy().setVersion(b);
            this.isSupportPolicy = true;
        }
    }

    private void composeLostInterval(FmmConfig fmmConfig, byte[] bArr) {
        int i = ((bArr[0] & 255) << 16) | ((bArr[1] & 255) << 8) | (bArr[2] & 255);
        if (fmmConfig.getPolicy() == null) {
            Log.e(TAG, "composeLostInterval policy object is null, need to check version field.");
        } else {
            fmmConfig.getPolicy().setLostModeInterval(i);
        }
    }

    private void composeAdvInterval(FmmConfig fmmConfig, byte[] bArr) {
        int i = ((bArr[0] & 255) << 8) | (bArr[1] & 255);
        if (fmmConfig.getPolicy() == null) {
            Log.e(TAG, "composeAdvInterval policy object is null, need to check version field.");
        } else {
            fmmConfig.getPolicy().setAdvertiseInterval(i);
        }
    }

    private void composeShuffleInterval(FmmConfig fmmConfig, byte[] bArr) {
        int i = ((bArr[0] & 255) << 16) | ((bArr[1] & 255) << 8) | (bArr[2] & 255);
        if (fmmConfig.getPolicy() == null) {
            Log.e(TAG, "composeShuffleInterval policy object is null, need to check version field.");
        } else {
            fmmConfig.getPolicy().setShuffleInterval(i);
        }
    }

    private void composeTimeUnit(FmmConfig fmmConfig, byte[] bArr) {
        int i = ((bArr[0] & 255) << 8) | (bArr[1] & 255);
        if (fmmConfig.getPolicy() == null) {
            Log.e(TAG, "composeTimeUnit policy object is null, need to check version field.");
        } else {
            fmmConfig.getPolicy().setRound(i);
        }
    }

    @Override // com.samsung.remotespen.core.device.control.factory.FmmDriver
    public void requestSetFmmConfig(final UUID uuid, final BleSpenBleDriver bleSpenBleDriver, final FmmConfig fmmConfig, final BleSpenDriver.OperationFinishListener operationFinishListener) {
        bleSpenBleDriver.writeCharacteristic(uuid, BleSpenUuid.FMM_CONFIG, convertFmmConfigToBytes(fmmConfig), new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.2
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData, long j) {
                if (bleOpResultData.getResultCode() == BleOpResultCode.SUCCESS && fmmConfig.getPolicy() != null) {
                    GenericFmmDriver genericFmmDriver = GenericFmmDriver.this;
                    if (genericFmmDriver.isSupportPolicy) {
                        genericFmmDriver.requestSetFmmConfigPolicy_Table(uuid, bleSpenBleDriver, fmmConfig.getPolicy());
                    }
                }
                operationFinishListener.onFinish(bleOpResultData, j);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestSetFmmConfigPolicy_Table(UUID uuid, BleSpenBleDriver bleSpenBleDriver, FmmConfigPolicy fmmConfigPolicy) {
        if (fmmConfigPolicy == null) {
            return;
        }
        final byte[] convertFmmConfigPolicyTable = convertFmmConfigPolicyTable(fmmConfigPolicy);
        if (convertFmmConfigPolicyTable == null) {
            Log.d(TAG, "requestSetFmmConfigPolicy_Table : no table data");
        } else {
            bleSpenBleDriver.writeCharacteristic(uuid, BleSpenUuid.OBFUSCATION_TABLE, convertFmmConfigPolicyTable, new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.shared.GenericFmmDriver.3
                @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
                public void onFinish(BleOpResultData bleOpResultData, long j) {
                    String str = GenericFmmDriver.TAG;
                    Log.d(str, "requestSetFmmConfigPolicy_Table finish. byte :" + BleUtils.getRawDataDumpStr(convertFmmConfigPolicyTable));
                }
            });
        }
    }

    public byte[] convertFmmConfigToBytes(FmmConfig fmmConfig) {
        int i = (fmmConfig.getPolicy() == null || !this.isSupportPolicy) ? 83 : 104;
        byte[] bArr = new byte[i];
        for (int i2 = 0; i2 < i; i2++) {
            bArr[i2] = 0;
        }
        try {
            int addIV = addIV(bArr, addE2E(bArr, addRegionField(bArr, addMaxNField(bArr, addSecretKeyField(bArr, addFmmTokenField(bArr, addSupportFindingService(bArr, 0, fmmConfig.getFindingSupport()), fmmConfig.getFmmToken()), fmmConfig.getSecretKey()), fmmConfig.getMaxN()), fmmConfig.getRegion()), fmmConfig.getE2e()), fmmConfig.getIv());
            if (fmmConfig.getPolicy() != null && this.isSupportPolicy) {
                addTimeUnitField(bArr, addShuffleIntervalField(bArr, addAdvIntervalField(bArr, addLostIntervalField(bArr, addVersionField(bArr, addIV, fmmConfig.getPolicy().getVersion()), fmmConfig.getPolicy().getLostModelInterval()), fmmConfig.getPolicy().getAdvertiseInterval()), fmmConfig.getPolicy().getShuffleInterval()), fmmConfig.getPolicy().getRound());
            }
            return bArr;
        } catch (ArrayIndexOutOfBoundsException unused) {
            Log.e(TAG, "convertFmmConfigToBytes : Unable to convert FmmConfig to Byte Array, Since requested data size is too large.");
            return null;
        }
    }

    public byte[] convertFmmConfigPolicyTable(FmmConfigPolicy fmmConfigPolicy) {
        ArrayList<String> table = fmmConfigPolicy.getTable();
        if (table == null || table.size() == 0) {
            return null;
        }
        int size = table.size();
        int i = 1;
        byte[] bArr = new byte[(size * 16) + 1];
        bArr[0] = (byte) (size & 255);
        Iterator<String> it = table.iterator();
        while (it.hasNext()) {
            byte[] bytes = it.next().getBytes();
            String str = TAG;
            Log.v(str, "convertFmmConfigPolicyTable data : " + BleUtils.getRawDataDumpStr(bytes));
            System.arraycopy(bytes, 0, bArr, i, 16);
            i += 16;
        }
        return bArr;
    }

    private int addSupportFindingService(byte[] bArr, int i, String str) throws ArrayIndexOutOfBoundsException {
        int i2 = i + 1;
        bArr[i] = FMM_FIELD_FMM_TOKEN;
        int i3 = i2 + 1;
        bArr[i2] = FMM_FIELD_SUPPORT_FINDING_SERVICE;
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        int i4 = i3 + 1;
        bArr[i3] = bytes.length > 0 ? bytes[0] : (byte) 0;
        return i4;
    }

    private int addE2E(byte[] bArr, int i, String str) throws ArrayIndexOutOfBoundsException {
        int i2 = i + 1;
        bArr[i] = FMM_FIELD_FMM_TOKEN;
        int i3 = i2 + 1;
        bArr[i2] = FMM_FIELD_E2E;
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        int i4 = i3 + 1;
        bArr[i3] = bytes.length > 0 ? bytes[0] : (byte) 0;
        return i4;
    }

    private int addIV(byte[] bArr, int i, String str) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        byte[] decode = Base64.getDecoder().decode(str);
        int i2 = i + 1;
        bArr[i] = 17;
        int i3 = i2 + 1;
        bArr[i2] = 7;
        if (decode.length > 0) {
            System.arraycopy(decode, 0, bArr, i3, decode.length);
        } else {
            System.arraycopy(new byte[16], 0, bArr, i3, 16);
        }
        return i3 + 16;
    }

    private int addFmmTokenField(byte[] bArr, int i, String str) throws ArrayIndexOutOfBoundsException {
        byte[] bytes = str.getBytes(Charset.defaultCharset());
        int i2 = i + 1;
        bArr[i] = (byte) (bytes.length + 1);
        int i3 = i2 + 1;
        bArr[i2] = FMM_FIELD_FMM_TOKEN;
        System.arraycopy(bytes, 0, bArr, i3, bytes.length);
        return i3 + bytes.length;
    }

    private int addSecretKeyField(byte[] bArr, int i, String str) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        byte[] decode = Base64.getDecoder().decode(str);
        int i2 = i + 1;
        bArr[i] = 17;
        int i3 = i2 + 1;
        bArr[i2] = FMM_FIELD_SECRET_KEY;
        if (decode.length > 0) {
            System.arraycopy(decode, 0, bArr, i3, decode.length);
        } else {
            System.arraycopy(new byte[16], 0, bArr, i3, 16);
        }
        return i3 + 16;
    }

    private int addMaxNField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_SECRET_KEY;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_MAX_N;
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i2 >> 8) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i2 & 255);
        return i6;
    }

    private int addRegionField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_FMM_TOKEN;
        int i4 = i3 + 1;
        bArr[i3] = 5;
        int i5 = i4 + 1;
        bArr[i4] = (byte) (i2 & 255);
        return i5;
    }

    private int addVersionField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_FMM_TOKEN;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_VERSION;
        int i5 = i4 + 1;
        bArr[i4] = (byte) (i2 & 255);
        return i5;
    }

    private int addLostIntervalField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_MAX_N;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_LOST_INTERVAL;
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i2 >> 16) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) ((i2 >> 8) & 255);
        int i7 = i6 + 1;
        bArr[i6] = (byte) (i2 & 255);
        return i7;
    }

    private int addAdvIntervalField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_SECRET_KEY;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_ADV_INTERVAL;
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i2 >> 8) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i2 & 255);
        return i6;
    }

    private int addShuffleIntervalField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_MAX_N;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_SFFL_INTERVAL;
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i2 >> 16) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) ((i2 >> 8) & 255);
        int i7 = i6 + 1;
        bArr[i6] = (byte) (i2 & 255);
        return i7;
    }

    private int addTimeUnitField(byte[] bArr, int i, int i2) throws ArrayIndexOutOfBoundsException {
        int i3 = i + 1;
        bArr[i] = FMM_FIELD_SECRET_KEY;
        int i4 = i3 + 1;
        bArr[i3] = FMM_FIELD_TIMEUNIT;
        int i5 = i4 + 1;
        bArr[i4] = (byte) ((i2 >> 8) & 255);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i2 & 255);
        return i6;
    }
}
