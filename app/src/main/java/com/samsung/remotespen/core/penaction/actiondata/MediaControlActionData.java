package com.samsung.remotespen.core.penaction.actiondata;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.R;
import com.samsung.aboutpage.Constants;
import com.samsung.util.ViewHelper;
import com.samsung.util.debug.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MediaControlActionData extends PenActionData {
    private static final String KEY_ACTION_TYPE = "action_type";
    private static final String TAG = MediaControlActionData.class.getSimpleName();
    private ActionType mActionType;

    /* loaded from: classes.dex */
    public enum ActionType {
        PLAY_PAUSE,
        NEXT_TRACK,
        PREV_TRACK,
        VOLUME_UP,
        VOLUME_DOWN
    }

    public static MediaControlActionData createFromLegacyData(String str, String str2) {
        String[] split = str.split(str2);
        boolean z = split.length == 1;
        Assert.e(z, "Incorrect field count. count=" + split.length + " data=" + str);
        return new MediaControlActionData(ActionType.valueOf(split[0]));
    }

    public static MediaControlActionData create(JSONObject jSONObject) {
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
        return new MediaControlActionData(ActionType.valueOf(str));
    }

    public MediaControlActionData(ActionType actionType) {
        this.mActionType = actionType;
    }

    /* renamed from: com.samsung.remotespen.core.penaction.actiondata.MediaControlActionData$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType;

        static {
            int[] iArr = new int[ActionType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType = iArr;
            try {
                iArr[ActionType.PLAY_PAUSE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[ActionType.NEXT_TRACK.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[ActionType.PREV_TRACK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[ActionType.VOLUME_UP.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[ActionType.VOLUME_DOWN.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public String getLabel(Context context) {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[this.mActionType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        if (i != 5) {
                            return null;
                        }
                        return context.getString(R.string.remotespen_volume_down);
                    }
                    return context.getString(R.string.remotespen_volume_up);
                }
                return context.getString(R.string.remotespen_play_prev_track);
            }
            return context.getString(R.string.remotespen_play_next_track);
        }
        return context.getString(R.string.remotespen_play_pause);
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

    public int getKeyEvent() {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$actiondata$MediaControlActionData$ActionType[this.mActionType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        return i != 5 ? -1 : 25;
                    }
                    return 24;
                }
                return 88;
            }
            return 87;
        }
        return 85;
    }

    @Override // com.samsung.remotespen.core.penaction.actiondata.PenActionData
    public Object accept(Context context, SimpleActionDataVisitor simpleActionDataVisitor) {
        return simpleActionDataVisitor.visit(context, this);
    }

    public ActionType getActionType() {
        return this.mActionType;
    }
}
