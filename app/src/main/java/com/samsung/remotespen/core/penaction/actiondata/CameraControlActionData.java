package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.R;
import com.samsung.aboutpage.Constants;
import com.samsung.util.ViewHelper;
import com.crazyromteam.spenbletest.utils.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class CameraControlActionData extends PenActionData {
    private static final String KEY_ACTION_TYPE = "action_type";
    private static final String TAG = CameraControlActionData.class.getSimpleName();
    private ActionType mActionType;

    /* loaded from: classes.dex */
    public enum ActionType {
        SHUTTER
    }

    public static CameraControlActionData createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 1;
        Assert.e(z, "Incorrect field count. count=" + split.length + " data=" + str);
        return new CameraControlActionData(ActionType.valueOf(split[0]));
    }

    public static CameraControlActionData create(JSONObject jSONObject) {
        String str;
        try {
            str = jSONObject.getString(KEY_ACTION_TYPE);
        } catch (JSONException e) {
            String str2 = TAG;
            Log.e(str2, "create : e=" + e);
            str = null;
        }
        if (str == null) {
            return null;
        }
        return new CameraControlActionData(ActionType.valueOf(str));
    }

    public CameraControlActionData(ActionType actionType) {
        this.mActionType = actionType;
    }

    /* renamed from: com.samsung.remotespen.core.penaction.actiondata.CameraControlActionData$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$CameraControlActionData$ActionType;

        static {
            int[] iArr = new int[ActionType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$CameraControlActionData$ActionType = iArr;
            try {
                iArr[ActionType.SHUTTER.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        if (AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$CameraControlActionData$ActionType[this.mActionType.ordinal()] != 1) {
            return null;
        }
        return context.getString(R.string.remotespen_shutter);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public JSONObject encodeToJsonObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(KEY_ACTION_TYPE, this.mActionType.name());
        } catch (JSONException e) {
            String str = TAG;
            Log.e(str, "encodeToJsonObject: e = " + e);
        }
        return jSONObject;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getActionForLogging(Context context) {
        return this.mActionType.name().replace(ViewHelper.QUALIFIER_DELIMITER, Constants.packageName.NONE);
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }

    public int getKeyEvent() {
        return AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$CameraControlActionData$ActionType[this.mActionType.ordinal()] != 1 ? -1 : 25;
    }
}
