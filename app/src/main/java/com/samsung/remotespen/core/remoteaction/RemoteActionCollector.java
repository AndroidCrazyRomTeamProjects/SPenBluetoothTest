package com.samsung.remotespen.core.remoteaction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.samsung.remotespen.core.penaction.manager.RuleEnableManager;
import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.FeatureWrapper.FeatureWrapper;
import com.samsung.remotespen.core.remoteaction.category.LongVideoEnum;
import com.samsung.remotespen.core.remoteaction.category.ReadingEnum;
import com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum;
import com.samsung.scpm.ScpmConstants;
import com.samsung.scpm.ScpmEncryptUtil;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class RemoteActionCollector {
    private static final String MATCH_TYPE_CONTAINS = "contains";
    private static final String MATCH_TYPE_EXACTLY = "exactly";
    private static final String META_KEY_REMOTE_ACTION = "com.samsung.android.support.REMOTE_ACTION";
    private static final String SEPARATOR_KEY_COMPOSITION = "\\+";
    private static final String TAG = "RemoteActionCollector";
    private static final String XML_ATTRIBUTE_ACTIONSET_LABEL = "actionset_label";
    private static final String XML_ATTRIBUTE_ACTIVITY = "activity";
    private static final String XML_ATTRIBUTE_ENABLE_KEY = "enable_key";
    private static final String XML_ATTRIBUTE_FEATURE = "feature";
    private static final String XML_ATTRIBUTE_HIDE = "hide";
    private static final String XML_ATTRIBUTE_ICON = "icon";
    private static final String XML_ATTRIBUTE_ID = "id";
    private static final String XML_ATTRIBUTE_LABEL = "label";
    private static final String XML_ATTRIBUTE_MATCH_TYPE = "match_type";
    private static final String XML_ATTRIBUTE_PACKAGE = "package";
    private static final String XML_ATTRIBUTE_PREFERENCE_NAME = "name";
    private static final String XML_ATTRIBUTE_PREFERENCE_VALUE = "value";
    private static final String XML_ATTRIBUTE_PRIORITY = "priority";
    private static final String XML_ATTRIBUTE_REPEATABLE = "repeatable";
    private static final String XML_ATTRIBUTE_REPEATABLE_INTERVAL = "repeatable_interval";
    private static final String XML_ATTRIBUTE_SWIFT_ACTION = "swift_action";
    private static final String XML_ATTRIBUTE_TRIGGER_KEY = "trigger_key";
    private static final String XML_ATTRIBUTE_VALUE = "value";
    private static final String XML_ATTRIBUTE_VERSION = "version";
    private static final String XML_TAG_ACTION = "action";
    private static final String XML_TAG_DISABLE_CONDITION = "disable-condition";
    private static final String XML_TAG_PREFERENCE = "preference";
    private static final String XML_TAG_REDIRECT_REMOTE_ACTIONS = "redirect-remote-actions";
    private static final String XML_TAG_REMOTE_ACTIONS = "remote-actions";
    private CollectCompleteListener mCollectCompleteListener;
    private Context mContext;
    private FeatureWrapper mCscFeatureClass;
    private FeatureWrapper mFloatingFeatureClass;
    private MetaCollectThread mMetaCollectThread;
    private PackageManager mPackageManager;
    private RemoteActionTable mDeputyTable = new RemoteActionTable();
    private ArrayList<String> mScpmPackageList = new ArrayList<>();

    /* loaded from: classes.dex */
    public enum Category {
        SV,
        LV,
        RD
    }

    /* loaded from: classes.dex */
    public interface CollectCompleteListener {
        void onComplete(RemoteActionTable remoteActionTable);
    }

    public RemoteActionCollector(Context context, PackageManager packageManager) {
        this.mContext = context;
        this.mPackageManager = packageManager;
    }

    public void setCollectCompleteListener(CollectCompleteListener collectCompleteListener) {
        this.mCollectCompleteListener = collectCompleteListener;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendCollectComplete(RemoteActionTable remoteActionTable) {
        CollectCompleteListener collectCompleteListener = this.mCollectCompleteListener;
        if (collectCompleteListener != null) {
            collectCompleteListener.onComplete(remoteActionTable);
        }
    }

    public void collect() {
        collectImmediately();
    }

    private void collectImmediately() {
        long currentTimeMillis = System.currentTimeMillis();
        RemoteActionTable collectAllMeta = collectAllMeta();
        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
        String str = TAG;
        Log.d(str, "collectImmediately : elapsed = " + currentTimeMillis2);
        sendCollectComplete(collectAllMeta);
    }

    /* loaded from: classes.dex */
    public class MetaCollectThread extends AsyncTask<Void, Void, RemoteActionTable> {
        private long mStartTime;

        private MetaCollectThread() {
        }

        @Override // android.os.AsyncTask
        public void onPreExecute() {
            super.onPreExecute();
            this.mStartTime = System.currentTimeMillis();
        }

        @Override // android.os.AsyncTask
        public RemoteActionTable doInBackground(Void... voidArr) {
            return RemoteActionCollector.this.collectAllMeta();
        }

        @Override // android.os.AsyncTask
        public void onPostExecute(RemoteActionTable remoteActionTable) {
            long currentTimeMillis = System.currentTimeMillis() - this.mStartTime;
            String str = RemoteActionCollector.TAG;
            Log.d(str, "onPostExecute : elapsed = " + currentTimeMillis);
            super.onPostExecute((MetaCollectThread) remoteActionTable);
            RemoteActionCollector.this.sendCollectComplete(remoteActionTable);
        }
    }

    public RemoteActionTable collectAllMeta() {
        List<ResolveInfo> queryIntentActivities = queryIntentActivities(this.mPackageManager, new Intent(META_KEY_REMOTE_ACTION), 128);
        RemoteActionTable remoteActionTable = new RemoteActionTable();
        this.mDeputyTable.clear();
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String str = activityInfo.packageName;
            String str2 = activityInfo.name;
            XmlPullParser loadXmlMetaData = loadXmlMetaData(activityInfo, this.mPackageManager, META_KEY_REMOTE_ACTION);
            if (loadXmlMetaData != null) {
                composeRemoteActionMapInPackages(remoteActionTable, loadXmlMetaData, str, str2);
            }
        }
        StringBuilder sb = new StringBuilder();
        String str3 = (this.mContext.getApplicationContext().getDataDir().toString() + '/' + Environment.DIRECTORY_DOWNLOADS + '/') + ScpmConstants.INTENT_BLOCKLIST_NAME;
        if (new File(str3).exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(str3);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                if (!new ScpmEncryptUtil().decrypt(this.mContext, fileInputStream, byteArrayOutputStream)) {
                    Log.e(TAG, "Fail to decrypt");
                }
                BufferedReader bufferedReader = new BufferedReader(new StringReader(byteArrayOutputStream.toString()));
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                updateMetaFromJson(remoteActionTable, sb.toString());
                bufferedReader.close();
                byteArrayOutputStream.close();
                fileInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "collectAllMeta json from scpm : " + e);
            }
        } else {
            try {
                InputStream open = this.mContext.getAssets().open(ScpmConstants.INTENT_BLOCKLIST_NAME);
                byte[] bArr = new byte[open.available()];
                open.read(bArr);
                updateMetaFromJson(remoteActionTable, new String(Base64.getDecoder().decode(bArr), "UTF-8"));
                open.close();
            } catch (IOException e2) {
                Log.e(TAG, "collectAllMeta json from asset : " + e2);
            }
        }
        return remoteActionTable;
    }

    public void composeRemoteActionMapInPackages(RemoteActionTable remoteActionTable, XmlPullParser xmlPullParser, String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return;
        }
        try {
            List<RemoteAction> parseXmlToRemoteActionList = parseXmlToRemoteActionList(xmlPullParser, str);
            if (parseXmlToRemoteActionList == null) {
                Log.e(TAG, "remoteAction is not parsed properly!!");
                return;
            }
            for (RemoteAction remoteAction : parseXmlToRemoteActionList) {
                if (isRedirectionRemoteAction(remoteAction)) {
                    this.mDeputyTable.put(remoteAction.getRedirectionPackage(), remoteAction.getRedirectionActivity(), remoteAction);
                    if (!isInstalledPackage(remoteAction.getRedirectionPackage())) {
                    }
                }
                remoteActionTable.put(remoteAction.getRedirectionPackage() != null ? remoteAction.getRedirectionPackage() : str, remoteAction.getRedirectionActivity() != null ? remoteAction.getRedirectionActivity() : str2, remoteAction);
            }
        } catch (XmlPullParserException unused) {
            Log.e(TAG, "parsing failed!!");
        }
    }

    private boolean isInstalledPackage(String str) {
        try {
            this.mPackageManager.getPackageInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public RemoteActionTable updateMetaFromJson(RemoteActionTable remoteActionTable, String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONArray jSONArray = jSONObject.getJSONArray("SV");
            JSONArray jSONArray2 = jSONObject.getJSONArray("LV");
            JSONArray jSONArray3 = jSONObject.getJSONArray("RD");
            this.mScpmPackageList.clear();
            updateRemoteActionTable(remoteActionTable, jSONArray, Category.SV);
            updateRemoteActionTable(remoteActionTable, jSONArray2, Category.LV);
            updateRemoteActionTable(remoteActionTable, jSONArray3, Category.RD);
        } catch (JSONException e) {
            String str2 = TAG;
            Log.e(str2, "updateMetaFromJson : " + e);
        }
        return remoteActionTable;
    }

    private void updateRemoteActionTable(RemoteActionTable remoteActionTable, JSONArray jSONArray, Category category) {
        RemoteAction remoteActionByCategory;
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                String[] split = jSONArray.get(i).toString().split("/");
                String str = split[0];
                String str2 = split[1];
                if (!this.mScpmPackageList.contains(str)) {
                    this.mScpmPackageList.add(str);
                }
                if (isInstalledPackage(str) && (remoteActionByCategory = getRemoteActionByCategory(category)) != null) {
                    remoteActionByCategory.setResourcePackage("com.samsung");
                    remoteActionByCategory.setRedirectionPackage(str);
                    remoteActionByCategory.setRedirectionActivity(str2);
                    remoteActionByCategory.setEnableKey(RuleEnableManager.getKey(str));
                    remoteActionTable.put(str, str2, remoteActionByCategory);
                }
            } catch (JSONException unused) {
                return;
            }
        }
    }

    /* renamed from: com.samsung.remotespen.core.remoteaction.RemoteActionCollector$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$remoteaction$RemoteActionCollector$Category;

        static {
            int[] iArr = new int[Category.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$remoteaction$RemoteActionCollector$Category = iArr;
            try {
                iArr[Category.SV.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$remoteaction$RemoteActionCollector$Category[Category.LV.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$remoteaction$RemoteActionCollector$Category[Category.RD.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private RemoteAction getRemoteActionByCategory(Category category) {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$remoteaction$RemoteActionCollector$Category[category.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    return null;
                }
                return ReadingEnum.getRemoteActions();
            }
            return LongVideoEnum.getRemoteActions();
        }
        return ShortVideoEnum.getRemoteActions();
    }

    public RemoteActionTable updateMata(RemoteActionTable remoteActionTable, String str) {
        if (isDelegatingPackage(remoteActionTable, str)) {
            Log.d(TAG, "DelegatingPackage is updated!!. refresh RemoteActionTable!!");
            collect();
            return null;
        } else if (isScpmPackage(str)) {
            Log.d(TAG, "SCPM package is updated!!. refresh RemoteActionTable!!");
            collect();
            return null;
        } else {
            remoteActionTable.removeByPackageName(str);
            if (isInstalledPackage(str)) {
                Intent intent = new Intent(META_KEY_REMOTE_ACTION);
                intent.setPackage(str);
                List<ResolveInfo> queryIntentActivities = queryIntentActivities(this.mPackageManager, intent, 128);
                if (queryIntentActivities == null || queryIntentActivities.isEmpty()) {
                    List<RemoteAction> findRemoteActionFromDeputyTable = findRemoteActionFromDeputyTable(str);
                    if (findRemoteActionFromDeputyTable != null) {
                        for (RemoteAction remoteAction : findRemoteActionFromDeputyTable) {
                            if (isInstalledPackage(remoteAction.getRedirectionPackage())) {
                                remoteActionTable.put(remoteAction.getRedirectionPackage(), remoteAction.getRedirectionActivity(), remoteAction);
                            }
                        }
                    }
                    return remoteActionTable;
                }
                for (ResolveInfo resolveInfo : queryIntentActivities) {
                    ActivityInfo activityInfo = resolveInfo.activityInfo;
                    String str2 = activityInfo.name;
                    XmlPullParser loadXmlMetaData = loadXmlMetaData(activityInfo, this.mPackageManager, META_KEY_REMOTE_ACTION);
                    if (loadXmlMetaData != null) {
                        updateRemoteActionMapInPackages(remoteActionTable, loadXmlMetaData, str, str2);
                    }
                }
                return remoteActionTable;
            }
            return remoteActionTable;
        }
    }

    private boolean isDelegatingPackage(RemoteActionTable remoteActionTable, String str) {
        List<String> packageList = remoteActionTable.getPackageList();
        if (packageList == null) {
            return false;
        }
        for (String str2 : packageList) {
            List<String> activityList = remoteActionTable.getActivityList(str2);
            if (activityList != null) {
                for (String str3 : activityList) {
                    RemoteAction remoteAction = remoteActionTable.getRemoteAction(str2, str3);
                    if (remoteAction != null && isRedirectionRemoteAction(remoteAction) && str.equals(remoteAction.getResourcePackage())) {
                        String str4 = TAG;
                        Log.d(str4, str + " is delegating package");
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    private boolean isScpmPackage(String str) {
        return this.mScpmPackageList.contains(str);
    }

    private boolean isRedirectionRemoteAction(RemoteAction remoteAction) {
        return !TextUtils.isEmpty(remoteAction.getRedirectionPackage());
    }

    private List<RemoteAction> findRemoteActionFromDeputyTable(String str) {
        List<String> activityList;
        if (this.mDeputyTable == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        if (this.mDeputyTable.getRemoteActionCount(str) > 0 && (activityList = this.mDeputyTable.getActivityList(str)) != null) {
            for (String str2 : activityList) {
                arrayList.add(this.mDeputyTable.getRemoteAction(str, str2));
            }
        }
        return arrayList;
    }

    public List<ResolveInfo> queryIntentActivities(PackageManager packageManager, Intent intent, int i) {
        return packageManager.queryIntentActivities(intent, i);
    }

    public XmlPullParser loadXmlMetaData(ActivityInfo activityInfo, PackageManager packageManager, String str) {
        return activityInfo.loadXmlMetaData(packageManager, str);
    }

    private void updateRemoteActionMapInPackages(RemoteActionTable remoteActionTable, XmlPullParser xmlPullParser, String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return;
        }
        try {
            List<RemoteAction> parseXmlToRemoteActionList = parseXmlToRemoteActionList(xmlPullParser, str);
            if (parseXmlToRemoteActionList == null || parseXmlToRemoteActionList.isEmpty()) {
                return;
            }
            for (RemoteAction remoteAction : parseXmlToRemoteActionList) {
                String redirectionPackage = remoteAction.getRedirectionPackage() != null ? remoteAction.getRedirectionPackage() : str;
                String redirectionActivity = remoteAction.getRedirectionActivity() != null ? remoteAction.getRedirectionActivity() : str2;
                if (isInstalledPackage(redirectionPackage)) {
                    remoteActionTable.put(redirectionPackage, redirectionActivity, remoteAction);
                }
            }
        } catch (XmlPullParserException unused) {
            Log.e(TAG, "parsing failed!!");
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:133:0x0265  */
    /* JADX WARN: Removed duplicated region for block: B:148:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.util.List<com.samsung.remotespen.core.remoteaction.RemoteAction> parseXmlToRemoteActionList(org.xmlpull.v1.XmlPullParser r23, java.lang.String r24) throws org.xmlpull.v1.XmlPullParserException {
        /*
            Method dump skipped, instructions count: 660
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.remoteaction.RemoteActionCollector.parseXmlToRemoteActionList(org.xmlpull.v1.XmlPullParser, java.lang.String):java.util.List");
    }

    private String getAttributeStringValue(XmlPullParser xmlPullParser, String str) {
        String attributeValue = xmlPullParser.getAttributeValue(null, str);
        if (TextUtils.isEmpty(attributeValue)) {
            return null;
        }
        attributeValue.trim();
        if (TextUtils.isEmpty(attributeValue)) {
            return null;
        }
        return attributeValue;
    }

    private float getAttributeFloatValue(XmlPullParser xmlPullParser, String str, float f) {
        String attributeStringValue = getAttributeStringValue(xmlPullParser, str);
        try {
            return Float.parseFloat(attributeStringValue);
        } catch (NumberFormatException unused) {
            String str2 = TAG;
            Log.w(str2, "NumberFormatException, unable to parseFloat " + str + " value = " + attributeStringValue);
            return f;
        }
    }

    private int getAttributeIntValue(XmlPullParser xmlPullParser, String str, int i) {
        String attributeStringValue = getAttributeStringValue(xmlPullParser, str);
        try {
            return Integer.parseInt(attributeStringValue);
        } catch (NumberFormatException unused) {
            String str2 = TAG;
            Log.w(str2, "NumberFormatException, unable to parseInt " + str + " value = " + attributeStringValue);
            return i;
        }
    }

    private boolean getAttributeBooleanValue(XmlPullParser xmlPullParser, String str, boolean z) {
        String attributeStringValue = getAttributeStringValue(xmlPullParser, str);
        return attributeStringValue != null ? Boolean.parseBoolean(attributeStringValue) : z;
    }

    private Action.KeyShortcut getKeyShortcut(String str) {
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "keyShortcut is null");
            return null;
        }
        String[] split = str.split(SEPARATOR_KEY_COMPOSITION);
        if (split.length == 0) {
            Log.e(TAG, "unable to parse KeyShortcut!");
            return null;
        }
        Action.KeyShortcut keyShortcut = new Action.KeyShortcut();
        for (String str2 : split) {
            keyShortcut.addKey(str2);
        }
        return keyShortcut;
    }

    private Action.Repeat createRepeat(Boolean bool, String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return new Action.Repeat(bool.booleanValue(), str);
    }

    private Action.Preference parsePreference(Action.Preference preference, String str, String str2) {
        if (preference != null) {
            preference.setPreference(str, str2);
            return preference;
        }
        return new Action.Preference(str, str2);
    }

    public void setCscFeatureClass(FeatureWrapper featureWrapper) {
        this.mCscFeatureClass = featureWrapper;
    }

    public void setFloatingFeatureClass(FeatureWrapper featureWrapper) {
        this.mFloatingFeatureClass = featureWrapper;
    }
}
