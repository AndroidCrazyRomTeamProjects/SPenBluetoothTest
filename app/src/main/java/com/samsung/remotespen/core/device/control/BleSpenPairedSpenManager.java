package com.samsung.remotespen.core.device.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.ble.BleEnvManager;
import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFactory;
import com.samsung.remotespen.core.device.control.factory.BleSpenDeviceFeature;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.util.SettingsPreferenceManager;
import com.samsung.util.CommonUtils;
import com.crazyromteam.spenbletest.utils.Assert;
import com.samsung.util.features.ModelFeatures;
import com.samsung.util.features.SpenModelName;
import com.samsung.util.settings.SettingsValueManager;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BleSpenPairedSpenManager {
    private static final String KEY_PAIRED_UNBUNDLED_SPEN_ADDRESS = "paired_unbundled_spen_address";
    private static final String KEY_PAIRED_UNBUNDLED_SPEN_MODEL_NAME = "paired_unbundled_spen_model_name";
    private static final String KEY_UNBUNDLED_SPEN_PAIRING_DETECTED = "unbundled_spen_pairing_detected";
    private static final String PREFERENCE_NAME = "paired_spen";
    private static final String TAG = "BleSpenPairedSpenManager";
    private static BleSpenPairedSpenManager sInstance;
    private AvailableSpenManager mAvailableSpenManager;
    private ArrayList<ChangeListener> mChangeListenerTable;
    private Context mContext;
    private SharedPreferences mSharedPref;
    private Object mSyncObj = new Object();

    /* loaded from: classes.dex */
    public interface ChangeListener {
        void onAdded(BleSpenInstanceId bleSpenInstanceId);

        void onRemoved(BleSpenInstanceId bleSpenInstanceId, boolean z);
    }

    public static synchronized BleSpenPairedSpenManager getInstance(Context context) {
        BleSpenPairedSpenManager bleSpenPairedSpenManager;
        synchronized (BleSpenPairedSpenManager.class) {
            if (sInstance == null) {
                sInstance = new BleSpenPairedSpenManager(context);
            }
            bleSpenPairedSpenManager = sInstance;
        }
        return bleSpenPairedSpenManager;
    }

    private BleSpenPairedSpenManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mSharedPref = context.getSharedPreferences(PREFERENCE_NAME, 0);
        init();
    }

    private void init() {
        this.mAvailableSpenManager = new AvailableSpenManager();
        this.mChangeListenerTable = new ArrayList<>();
        if (ModelFeatures.isProvideBundleSpen(this.mContext) && ModelFeatures.isBundledSpenSupportBle()) {
            this.mAvailableSpenManager.addSpen(SpenInstanceIdHelper.createInstanceId(this.mContext, getBundledSpenModelName(), null));
        }
        String string = this.mSharedPref.getString(KEY_PAIRED_UNBUNDLED_SPEN_MODEL_NAME, null);
        String string2 = this.mSharedPref.getString(KEY_PAIRED_UNBUNDLED_SPEN_ADDRESS, null);
        String str = TAG;
        Log.i(str, "BleSpenPairedSpenManager : unbundled model=" + string + ", addr=" + string2);
        if (TextUtils.isEmpty(string2) || TextUtils.isEmpty(string)) {
            return;
        }
        this.mAvailableSpenManager.addSpen(SpenInstanceIdHelper.createInstanceId(this.mContext, SpenModelName.valueOf(string), string2));
    }

    public void resetSettings(boolean z) {
        removeAllSpen(z, true);
    }

    private void removeAllSpen(boolean z, boolean z2) {
        Iterator<BleSpenInstanceId> it = getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            BleSpenInstanceId next = it.next();
            if (z || !next.isBundledSpen()) {
                removeSpen(next, z2);
            }
        }
    }

    public BleSpenInstanceId addSpen(SpenModelName spenModelName, String str) {
        BundledSpenInstanceId bundledSpenInstanceId;
        Assert.notNull(spenModelName);
        boolean equals = spenModelName.equals(getBundledSpenModelName());
        String str2 = TAG;
        Log.d(str2, "addSpen : " + spenModelName + ", " + str + ", bundled=" + equals);
        if (equals) {
            bundledSpenInstanceId = this.mAvailableSpenManager.getBundledSpenInstance();
            Assert.notNull(bundledSpenInstanceId);
            setBundledSpenAddress(str);
        } else {
            Assert.notNull(str);
            BleSpenInstanceId createInstanceId = SpenInstanceIdHelper.createInstanceId(this.mContext, spenModelName, str);
            synchronized (this.mSyncObj) {
                if (!isPairedSpen(createInstanceId)) {
                    removeAllSpen(false, false);
                    this.mAvailableSpenManager.addSpen(createInstanceId);
                    SettingsValueManager settingsValueManager = SettingsValueManager.getInstance(this.mContext);
                    if (!CommonUtils.isInboxSPen(this.mContext) && !settingsValueManager.isAllowMultiplePensEnabled()) {
                        settingsValueManager.setAllowMultiplePensEnabled(true);
                        Context context = this.mContext;
                        Toast.makeText(context, context.getResources().getString(R.string.remotespen_allow_multiple_pen_turn_on_toast), 0).show();
                    }
                    if (!isUnbundledSpenPairingDetected()) {
                        writeUnbundledSpenPairingDetectedToPreference();
                    }
                    writeUnbundledSpenInstanceToPreference(spenModelName, str);
                    Iterator it = new ArrayList(this.mChangeListenerTable).iterator();
                    while (it.hasNext()) {
                        ((ChangeListener) it.next()).onAdded(createInstanceId);
                    }
                } else {
                    Log.d(str2, "addSpen : already in list");
                }
            }
            bundledSpenInstanceId = createInstanceId;
        }
        SettingsValueManager.getInstance(this.mContext).setNoPairedSpen(false);
        return bundledSpenInstanceId;
    }

    public boolean removeSpen(BleSpenInstanceId bleSpenInstanceId, boolean z) {
        Assert.notNull(bleSpenInstanceId);
        String str = TAG;
        Log.d(str, "removeSpen : " + bleSpenInstanceId.getSpenModelName() + ", isRequestStopService=" + z);
        if (bleSpenInstanceId.isBundledSpen()) {
            setBundledSpenAddress(null);
            return true;
        }
        synchronized (this.mSyncObj) {
            if (isPairedSpen(bleSpenInstanceId)) {
                this.mAvailableSpenManager.removeSpen(bleSpenInstanceId);
                if (getAvailableUnbundledSpenInstanceIds().size() == 0) {
                    SettingsPreferenceManager.getInstance(this.mContext).resetMyDeviceId();
                }
                if (getAvailableSpenInstanceIds().size() == 0) {
                    SettingsValueManager.getInstance(this.mContext).setNoPairedSpen(true);
                }
                if (BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName()).getApplicationFeature().isSupportFmm()) {
                    SettingsValueManager settingsValueManager = SettingsValueManager.getInstance(this.mContext);
                    settingsValueManager.setSpenTipCardLastShowTime(0L);
                    settingsValueManager.setSpenTipCardTotalShowCount(0);
                }
                writeUnbundledSpenInstanceToPreference(null, null);
                Iterator<ChangeListener> it = this.mChangeListenerTable.iterator();
                while (it.hasNext()) {
                    it.next().onRemoved(bleSpenInstanceId, z);
                }
                return true;
            }
            return false;
        }
    }

    public ArrayList<BleSpenInstanceId> getAvailableSpenInstanceIds() {
        return this.mAvailableSpenManager.getAvailableSpenInstanceIds();
    }

    public boolean isPairedSpen(BleSpenInstanceId bleSpenInstanceId) {
        return this.mAvailableSpenManager.contains(bleSpenInstanceId);
    }

    public boolean isUnbundledSpenPaired() {
        return getAvailableUnbundledSpenInstanceIds().size() > 0;
    }

    public boolean isSpenPaired() {
        return getAvailableSpenInstanceIds().size() > 0;
    }

    public ArrayList<BleSpenInstanceId> getAvailableUnbundledSpenInstanceIds() {
        ArrayList<BleSpenInstanceId> arrayList = new ArrayList<>();
        Iterator<BleSpenInstanceId> it = getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            BleSpenInstanceId next = it.next();
            if (!next.isBundledSpen()) {
                arrayList.add(next);
            }
        }
        if (arrayList.size() > 1) {
            Assert.fail("Exceeded max unbundled spen size. current unbundled spens = " + arrayList.size());
        }
        return arrayList;
    }

    public synchronized BleSpenInstanceId getPrimaryUnbundledSpenInstanceId() {
        ArrayList<BleSpenInstanceId> availableUnbundledSpenInstanceIds = getAvailableUnbundledSpenInstanceIds();
        if (availableUnbundledSpenInstanceIds.size() > 0) {
            return availableUnbundledSpenInstanceIds.get(0);
        }
        return null;
    }

    public void registerChangeListener(ChangeListener changeListener) {
        synchronized (this.mSyncObj) {
            if (this.mChangeListenerTable.contains(changeListener)) {
                return;
            }
            this.mChangeListenerTable.add(changeListener);
        }
    }

    public void unregisterChangeListener(ChangeListener changeListener) {
        synchronized (this.mSyncObj) {
            this.mChangeListenerTable.remove(changeListener);
        }
    }

    @Deprecated
    public BleSpenInstanceId getLastPairedSpenInstanceId() {
        BleSpenInstanceId lastAddedSpenInstanceId;
        synchronized (this.mSyncObj) {
            lastAddedSpenInstanceId = this.mAvailableSpenManager.getLastAddedSpenInstanceId();
        }
        return lastAddedSpenInstanceId;
    }

    @Deprecated
    public SpenModelName getLastPairedSpenModelName() {
        synchronized (this.mSyncObj) {
            BleSpenInstanceId lastPairedSpenInstanceId = getLastPairedSpenInstanceId();
            if (lastPairedSpenInstanceId == null) {
                return null;
            }
            return lastPairedSpenInstanceId.getSpenModelName();
        }
    }

    @Deprecated
    public BleSpenDeviceFeature getLastPairedSpenDeviceFeature() {
        synchronized (this.mSyncObj) {
            BleSpenInstanceId lastPairedSpenInstanceId = getLastPairedSpenInstanceId();
            if (lastPairedSpenInstanceId == null) {
                return null;
            }
            return BleSpenDeviceFactory.getInstance(lastPairedSpenInstanceId.getSpenModelName()).getDeviceFeature();
        }
    }

    public BleSpenInstanceId getPrimarySpenInstanceId() {
        synchronized (this.mSyncObj) {
            BleSpenInstanceId bundledSpenInstanceId = getBundledSpenInstanceId();
            if (bundledSpenInstanceId != null) {
                return bundledSpenInstanceId;
            }
            ArrayList<BleSpenInstanceId> availableUnbundledSpenInstanceIds = getAvailableUnbundledSpenInstanceIds();
            if (availableUnbundledSpenInstanceIds.size() > 0) {
                return availableUnbundledSpenInstanceIds.get(availableUnbundledSpenInstanceIds.size() - 1);
            }
            return null;
        }
    }

    public BleSpenInstanceId getBundledSpenInstanceId() {
        BundledSpenInstanceId bundledSpenInstance;
        synchronized (this.mSyncObj) {
            bundledSpenInstance = this.mAvailableSpenManager.getBundledSpenInstance();
        }
        return bundledSpenInstance;
    }

    public SpenModelName getBundledSpenModelName() {
        return ModelFeatures.getSpenModelName(this.mContext);
    }

    public void setBundledSpenAddress(String str) {
        BleUtils.writeBundledBleSpenAddressToEfs(this.mContext, str);
    }

    public boolean isUnbundledSpenPairingDetected() {
        return this.mSharedPref.getBoolean(KEY_UNBUNDLED_SPEN_PAIRING_DETECTED, false);
    }

    private void writeUnbundledSpenPairingDetectedToPreference() {
        SharedPreferences.Editor edit = this.mSharedPref.edit();
        edit.putBoolean(KEY_UNBUNDLED_SPEN_PAIRING_DETECTED, true);
        edit.apply();
    }

    private void writeUnbundledSpenInstanceToPreference(SpenModelName spenModelName, String str) {
        String name = spenModelName != null ? spenModelName.name() : null;
        SharedPreferences.Editor edit = this.mSharedPref.edit();
        if (name != null) {
            edit.putString(KEY_PAIRED_UNBUNDLED_SPEN_MODEL_NAME, name);
        } else {
            edit.remove(KEY_PAIRED_UNBUNDLED_SPEN_MODEL_NAME);
        }
        if (str != null) {
            edit.putString(KEY_PAIRED_UNBUNDLED_SPEN_ADDRESS, str);
        } else {
            edit.remove(KEY_PAIRED_UNBUNDLED_SPEN_ADDRESS);
        }
        edit.apply();
    }

    public boolean setSpenNickName(BleSpenInstanceId bleSpenInstanceId, String str) {
        if (bleSpenInstanceId.isBundledSpen()) {
            Log.e(TAG, "setSpenNickName : Name change of Bundled S Pen not allowed");
            return false;
        }
        IBleDevice bluetoothDevice = BleEnvManager.getInstance(this.mContext).getBluetoothDevice(this.mContext, SpenInstanceIdHelper.from(this.mContext, bleSpenInstanceId).getSpenAddress());
        if (bluetoothDevice == null) {
            Log.e(TAG, "setSpenNickName : device is null");
            return false;
        }
        return bluetoothDevice.setAlias(str);
    }

    private String getDeviceAlias(BleSpenInstanceId bleSpenInstanceId) {
        IBleDevice bluetoothDevice;
        String spenAddress = SpenInstanceIdHelper.from(this.mContext, bleSpenInstanceId).getSpenAddress();
        if (spenAddress == null || (bluetoothDevice = BleEnvManager.getInstance(this.mContext).getBluetoothDevice(this.mContext, spenAddress)) == null || bluetoothDevice.getAlias() == null) {
            return null;
        }
        return getUnbundledSpenDisplayName(bluetoothDevice.getAlias(), bluetoothDevice.getAddress());
    }

    public String getSpenNickName(BleSpenInstanceId bleSpenInstanceId) {
        String deviceAlias = !bleSpenInstanceId.isBundledSpen() ? getDeviceAlias(bleSpenInstanceId) : null;
        if (deviceAlias == null) {
            Log.w(TAG, "getSpenNickName : BT device nickName is null.");
            return getSpenDefaultNickName(bleSpenInstanceId, false);
        }
        return deviceAlias;
    }

    public String getSpenNickNameForExternal(BleSpenInstanceId bleSpenInstanceId) {
        String deviceAlias = !bleSpenInstanceId.isBundledSpen() ? getDeviceAlias(bleSpenInstanceId) : null;
        if (deviceAlias == null) {
            Log.w(TAG, "getSpenNickName : BT device nickName is null.");
            return getSpenDefaultNickName(bleSpenInstanceId, true);
        }
        return deviceAlias;
    }

    private String getUnbundledSpenDisplayName(String str, String str2) {
        String str3 = " (" + str2.replaceAll(":", Constants.packageName.NONE).substring(8) + ")";
        return (str3.isEmpty() || !str.endsWith(str3)) ? str : str.substring(0, str.length() - str3.length());
    }

    private String getSpenDefaultNickName(BleSpenInstanceId bleSpenInstanceId, boolean z) {
        BleSpenDeviceFactory bleSpenDeviceFactory = BleSpenDeviceFactory.getInstance(bleSpenInstanceId.getSpenModelName());
        if ((bleSpenInstanceId.isBundledSpen() && z) || !isUnbundledSpenPaired()) {
            return this.mContext.getString(R.string.remotespen_settings_pairing_guide_spen);
        }
        return bleSpenDeviceFactory.getApplicationFeature().getDisplayModelName(this.mContext);
    }
}
