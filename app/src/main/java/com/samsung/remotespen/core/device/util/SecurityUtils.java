package com.samsung.remotespen.core.device.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.util.SettingsPreferenceManager;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* loaded from: classes.dex */
public class SecurityUtils {
    private static final int ADV_LENGTH = 14;
    private static final String DUMMY_ACCOUNT_ID = "no_account_id";
    private static final String HASH_ENC = "cl2ty09r3ifkj30f";
    private static final String HASH_FUNCTION = "HmacSHA256";
    private static final String TAG = "SecurityUtils";
    private static final int TRUNCATED_HASH_LENGTH = 6;
    private static final int VARIABLE_FACTOR_LENGTH = 2;

    public static byte[] createLastConnectIdentifier(Context context) {
        int nextInt = new Random(System.currentTimeMillis()).nextInt(65535);
        String format = String.format("%04x", Integer.valueOf(nextInt));
        byte[] createDeviceHash = createDeviceHash(context, format, 6);
        if (createDeviceHash == null) {
            Log.e(TAG, "createLastConnectorIdentifyHash : unable to create DeviceHash.");
            return null;
        }
        byte[] createAccountHash = createAccountHash(context, format, 6);
        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putInt(nextInt);
        byte[] array = allocate.array();
        byte[] bArr = new byte[14];
        System.arraycopy(createDeviceHash, 0, bArr, 0, 6);
        System.arraycopy(createAccountHash, 0, bArr, 6, 6);
        System.arraycopy(array, 2, bArr, 12, 2);
        return bArr;
    }

    public static boolean isLastConnectedDevice(Context context, byte[] bArr) {
        if (bArr == null || bArr.length != 14) {
            return false;
        }
        return isDeviceHashMatched(context, getDeviceHashFromAdv(bArr), getVariableFactorFromAdv(bArr));
    }

    public static boolean isLastConnectedAccount(Context context, byte[] bArr) {
        if (bArr == null || bArr.length != 14) {
            return false;
        }
        String variableFactorFromAdv = getVariableFactorFromAdv(bArr);
        byte[] accountHashFromAdv = getAccountHashFromAdv(bArr);
        if (isFilledWithZero(accountHashFromAdv)) {
            accountHashFromAdv = createEmptyAccountHash(variableFactorFromAdv, accountHashFromAdv.length);
        }
        return isAccountHashMatched(context, accountHashFromAdv, variableFactorFromAdv);
    }

    public static boolean isDeviceEmpty(byte[] bArr) {
        return bArr != null && bArr.length == 14 && isFilledWithZero(getDeviceHashFromAdv(bArr));
    }

    public static boolean isAccountEmpty(byte[] bArr) {
        if (bArr == null || bArr.length != 14) {
            return false;
        }
        byte[] accountHashFromAdv = getAccountHashFromAdv(bArr);
        if (isFilledWithZero(accountHashFromAdv)) {
            return true;
        }
        byte[] createEmptyAccountHash = createEmptyAccountHash(getVariableFactorFromAdv(bArr), accountHashFromAdv.length);
        if (createEmptyAccountHash == null) {
            return false;
        }
        return Arrays.equals(accountHashFromAdv, createEmptyAccountHash);
    }

    public static byte[] createDeviceHash(Context context, String str, int i) {
        String myDeviceId = SettingsPreferenceManager.getInstance(context).getMyDeviceId();
        if (TextUtils.isEmpty(myDeviceId)) {
            return null;
        }
        return createHash(myDeviceId, str, i);
    }

    public static byte[] createAccountHash(Context context, String str, int i) {
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.osp.app.signin");
        if (accountsByType.length == 0) {
            return createEmptyAccountHash(str, i);
        }
        return createHash(accountsByType[0].name, str, i);
    }

    private static boolean isDeviceHashMatched(Context context, byte[] bArr, String str) {
        byte[] createDeviceHash = createDeviceHash(context, str, bArr.length);
        if (createDeviceHash == null) {
            return false;
        }
        return Arrays.equals(bArr, createDeviceHash);
    }

    private static boolean isAccountHashMatched(Context context, byte[] bArr, String str) {
        byte[] createAccountHash;
        if (bArr == null || (createAccountHash = createAccountHash(context, str, bArr.length)) == null) {
            return false;
        }
        return Arrays.equals(bArr, createAccountHash);
    }

    private static byte[] getTruncatedHash(byte[] bArr, int i) {
        if (bArr == null) {
            return null;
        }
        byte[] bArr2 = new byte[i];
        System.arraycopy(bArr, 0, bArr2, 0, i);
        return bArr2;
    }

    private static byte[] generateHmacSha256(String str, String str2) {
        try {
            Mac mac = Mac.getInstance(HASH_FUNCTION);
            mac.init(new SecretKeySpec(str2.getBytes(Charset.forName("UTF-8")), HASH_FUNCTION));
            return mac.doFinal(str.getBytes(Charset.forName("UTF-8")));
        } catch (InvalidKeyException e) {
            String str3 = TAG;
            Log.e(str3, "generateHmacSha256 : InvalidKeyException = " + e);
            return null;
        } catch (NoSuchAlgorithmException e2) {
            String str4 = TAG;
            Log.e(str4, "generateHmacSha256 : NoSuchAlgorithmException = " + e2);
            return null;
        }
    }

    private static String byteToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", Integer.valueOf(bArr[i] & 255)));
        }
        return sb.toString();
    }

    private static boolean isFilledWithZero(byte[] bArr) {
        for (byte b : bArr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private static byte[] getDeviceHashFromAdv(byte[] bArr) {
        byte[] bArr2 = new byte[6];
        System.arraycopy(bArr, 0, bArr2, 0, 6);
        return bArr2;
    }

    private static byte[] getAccountHashFromAdv(byte[] bArr) {
        byte[] bArr2 = new byte[6];
        System.arraycopy(bArr, 6, bArr2, 0, 6);
        return bArr2;
    }

    private static String getVariableFactorFromAdv(byte[] bArr) {
        byte[] bArr2 = new byte[2];
        System.arraycopy(bArr, 12, bArr2, 0, 2);
        return byteToHexString(bArr2);
    }

    private static byte[] createEmptyAccountHash(String str, int i) {
        return createHash(DUMMY_ACCOUNT_ID, str, i);
    }

    private static byte[] createHash(String str, String str2, int i) {
        return getTruncatedHash(generateHmacSha256(str + str2, HASH_ENC), i);
    }
}
