package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.util.ViewHelper;
import com.samsung.util.debug.Assert;
import com.samsung.util.shortcut.AppShortcut;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class AppLaunchActionData extends PenActionData {
    private static final String KEY_COMPONENT_NAME = "component_name";
    private static final String KEY_EXECUTABLE_ID = "executable_id";
    private static final String KEY_LAUNCH_TYPE = "launch_type";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String TAG = AppLaunchActionData.class.getSimpleName();
    private String mComponentName;
    private String mExecutableId;
    private LaunchType mLaunchType;
    private String mPackageName;

    /* loaded from: classes.dex */
    public enum LaunchType {
        ACTIVITY,
        SERVICE,
        EXECUTABLE,
        DUAL_IM
    }

    public static AppLaunchActionData createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 4;
        Assert.e(z, "Incorrect field count. count=" + split.length + " data=" + str);
        return new AppLaunchActionData(LaunchType.valueOf(split[0]), split[1], split[2], split[3]);
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x003f  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x0047  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData create(org.json.JSONObject r7) {
        /*
            r0 = 0
            java.lang.String r1 = "launch_type"
            java.lang.String r1 = r7.getString(r1)     // Catch: org.json.JSONException -> L22
            java.lang.String r2 = "package_name"
            java.lang.String r2 = r7.getString(r2)     // Catch: org.json.JSONException -> L1f
            java.lang.String r3 = "component_name"
            java.lang.String r3 = r7.getString(r3)     // Catch: org.json.JSONException -> L1c
            java.lang.String r4 = "executable_id"
            java.lang.String r7 = r7.getString(r4)     // Catch: org.json.JSONException -> L1a
            goto L3d
        L1a:
            r7 = move-exception
            goto L26
        L1c:
            r7 = move-exception
            r3 = r0
            goto L26
        L1f:
            r7 = move-exception
            r2 = r0
            goto L25
        L22:
            r7 = move-exception
            r1 = r0
            r2 = r1
        L25:
            r3 = r2
        L26:
            java.lang.String r4 = com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "create : e="
            r5.append(r6)
            r5.append(r7)
            java.lang.String r7 = r5.toString()
            com.samsung.util.debug.Log.e(r4, r7)
            r7 = r0
        L3d:
            if (r1 != 0) goto L47
            java.lang.String r7 = com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData.TAG
            java.lang.String r1 = "create : launchType is null"
            com.samsung.util.debug.Log.e(r7, r1)
            return r0
        L47:
            com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData r0 = new com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData
            com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData$LaunchType r1 = com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData.LaunchType.valueOf(r1)
            r0.<init>(r1, r2, r3, r7)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData.create(org.json.JSONObject):com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData");
    }

    public AppLaunchActionData(AppShortcut appShortcut) {
        this.mPackageName = appShortcut.packageName;
        this.mComponentName = appShortcut.componentName;
        this.mExecutableId = appShortcut.executableId;
        int i = appShortcut.dataType;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    this.mLaunchType = LaunchType.DUAL_IM;
                    return;
                } else {
                    this.mLaunchType = null;
                    return;
                }
            }
        } else if (AppShortcut.COMPONENT_TYPE_ACTIVITY.equals(appShortcut.componentType)) {
            this.mLaunchType = LaunchType.ACTIVITY;
            return;
        } else if (AppShortcut.COMPONENT_TYPE_SERVICE.equals(appShortcut.componentType)) {
            this.mLaunchType = LaunchType.SERVICE;
            return;
        }
        this.mLaunchType = LaunchType.EXECUTABLE;
    }

    public AppLaunchActionData(LaunchType launchType, String str, String str2, String str3) {
        this.mLaunchType = launchType;
        this.mPackageName = str;
        this.mComponentName = str2;
        this.mExecutableId = str3;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        AppShortcut appShortcut = getAppShortcut();
        if (appShortcut == null) {
            return null;
        }
        try {
            appShortcut.loadItemResources(context, false, false, true);
            return appShortcut.appName;
        } catch (RuntimeException unused) {
            Log.e(TAG, "getLabel : RuntimeException is occurred");
            return null;
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "getLabel : e=" + e, e);
            return null;
        }
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getComponentName() {
        return this.mComponentName;
    }

    public LaunchType getLaunchType() {
        return this.mLaunchType;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_LAUNCH_TYPE, this.mLaunchType.name());
            jSONObject.put("package_name", this.mPackageName);
            jSONObject.put(KEY_COMPONENT_NAME, this.mComponentName);
            jSONObject.put(KEY_EXECUTABLE_ID, this.mExecutableId);
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getActionForLogging(Context context) {
        return this.mLaunchType.name().replace(ViewHelper.QUALIFIER_DELIMITER, Constants.packageName.NONE);
    }

    /* renamed from: com.samsung.remotespen.core.penaction.actiondata.AppLaunchActionData$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType;

        static {
            int[] iArr = new int[LaunchType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType = iArr;
            try {
                iArr[LaunchType.ACTIVITY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType[LaunchType.SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType[LaunchType.EXECUTABLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType[LaunchType.DUAL_IM.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public AppShortcut getAppShortcut() {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$AppLaunchActionData$LaunchType[this.mLaunchType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        return null;
                    }
                    return new AppShortcut(2, this.mPackageName, this.mComponentName, AppShortcut.COMPONENT_TYPE_ACTIVITY, this.mExecutableId);
                }
                return new AppShortcut(1, this.mPackageName, this.mComponentName, null, this.mExecutableId);
            }
            return new AppShortcut(0, this.mPackageName, this.mComponentName, AppShortcut.COMPONENT_TYPE_SERVICE, this.mExecutableId);
        }
        return new AppShortcut(0, this.mPackageName, this.mComponentName, AppShortcut.COMPONENT_TYPE_ACTIVITY, this.mExecutableId);
    }
}
