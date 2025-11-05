package com.samsung.remotespen.core.remoteaction;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.samsung.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class RemoteAction implements Parcelable {
    private static final int MAX_ACTION_COUNT = 10;
    private static final String VERSION_1_0 = "1.0";
    private static final String VERSION_1_2 = "1.2";
    private String mActionSetLabel;
    private List<Action> mActions;
    private String mEnableKey;
    private boolean mIsSwiftActionDeclared;
    private String mRedirectionActivity;
    private String mRedirectionPackage;
    private String mResourcePackage;
    private float mVersion;
    private boolean[] priorityMap;
    private static final String TAG = RemoteAction.class.getSimpleName();
    public static final Parcelable.Creator<RemoteAction> CREATOR = new Parcelable.Creator<RemoteAction>() { // from class: com.samsung.remotespen.core.remoteaction.RemoteAction.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RemoteAction createFromParcel(Parcel parcel) {
            return new RemoteAction(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RemoteAction[] newArray(int i) {
            return new RemoteAction[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public RemoteAction(Parcel parcel) {
        this.mIsSwiftActionDeclared = false;
        this.mActions = new ArrayList();
        this.priorityMap = new boolean[10];
        this.mVersion = parcel.readFloat();
        this.mResourcePackage = parcel.readString();
        this.mRedirectionPackage = parcel.readString();
        this.mRedirectionActivity = parcel.readString();
        parcel.readTypedList(this.mActions, Action.CREATOR);
        this.priorityMap = parcel.createBooleanArray();
    }

    public RemoteAction() {
        this.mIsSwiftActionDeclared = false;
        this.mActions = new ArrayList();
        this.priorityMap = new boolean[10];
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.mVersion);
        parcel.writeString(this.mResourcePackage);
        parcel.writeString(this.mRedirectionPackage);
        parcel.writeString(this.mRedirectionActivity);
        parcel.writeTypedList(this.mActions);
        parcel.writeBooleanArray(this.priorityMap);
    }

    public void setVersion(float f) {
        this.mVersion = f;
    }

    public void setEnableKey(String str) {
        this.mEnableKey = str;
    }

    public void setSwiftAction(boolean z) {
        this.mIsSwiftActionDeclared = z;
    }

    public boolean isSwiftActionDeclared() {
        return this.mIsSwiftActionDeclared;
    }

    public void setResourcePackage(String str) {
        this.mResourcePackage = str;
    }

    public void setRedirectionPackage(String str) {
        this.mRedirectionPackage = str;
    }

    public void setRedirectionActivity(String str) {
        this.mRedirectionActivity = str;
    }

    public void setActionSetLabel(String str) {
        this.mActionSetLabel = str;
    }

    public String getActionSetLabel() {
        return this.mActionSetLabel;
    }

    public void addAction(Action action) {
        if (isValidAction(this.mVersion, action)) {
            this.mActions.add(action);
            if (action.getHide()) {
                return;
            }
            this.priorityMap[action.getPriority() - 1] = true;
        }
    }

    public String getActionLabelFromResource(Context context, String str) {
        int resourceId;
        Action action = getAction(str);
        if (action == null || (resourceId = CommonUtils.getResourceId(context, this.mResourcePackage, action.getLabel())) <= 0) {
            return null;
        }
        return CommonUtils.getStringFromPackage(context, this.mResourcePackage, resourceId);
    }

    private boolean isValidAction(float f, Action action) {
        if (action.isEnabled()) {
            if (!isValidVersion(f, action)) {
                String str = TAG;
                Log.w(str, "version = " + f + ", " + action.getPreference() + ", " + action.getRepeat());
                return false;
            }
            if (!action.getHide()) {
                int priority = action.getPriority();
                if (priority < 1 || priority > 10) {
                    String str2 = TAG;
                    Log.w(str2, "priority should be [1~10 ]! current = " + priority);
                    return false;
                } else if (this.priorityMap[priority - 1]) {
                    String str3 = TAG;
                    Log.w(str3, "priority (" + priority + ") is already exist. ignore");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isValidVersion(float f, Action action) {
        String f2 = Float.toString(f);
        f2.hashCode();
        return !f2.equals(VERSION_1_0) ? f2.equals(VERSION_1_2) : action.getPreference() == null && action.getRepeat() == null;
    }

    private boolean isOnlyConsistedWith(int i) {
        int size = this.mActions.size();
        for (int i2 = 0; i2 < size; i2++) {
            if (this.mActions.get(i2).getActionCategory() != i) {
                return false;
            }
        }
        return true;
    }

    public boolean isOnlyConsistedWithMotionOnlyActions() {
        return isOnlyConsistedWith(2);
    }

    public boolean isOnlyConsistedWithButtonOnlyActions() {
        return isOnlyConsistedWith(1);
    }

    public float getVersion() {
        return this.mVersion;
    }

    public String getEnableKey() {
        return this.mEnableKey;
    }

    public int getActionCount() {
        return this.mActions.size();
    }

    public Action getAction(int i) {
        int actionCount = getActionCount();
        if (i >= actionCount) {
            String str = TAG;
            Log.d(str, "Index(" + i + ") Out Of Bounds(" + actionCount + ")");
            return null;
        }
        return this.mActions.get(i);
    }

    public Action getAction(String str) {
        if (str == null) {
            return null;
        }
        for (Action action : this.mActions) {
            if (str.equals(action.getId())) {
                return action;
            }
        }
        return null;
    }

    public String getResourcePackage() {
        return this.mResourcePackage;
    }

    public String getRedirectionPackage() {
        return this.mRedirectionPackage;
    }

    public String getRedirectionActivity() {
        return this.mRedirectionActivity;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(this.mVersion);
        sb.append(";");
        sb.append(this.mResourcePackage);
        sb.append(";");
        sb.append(this.mRedirectionPackage);
        sb.append(";");
        sb.append(this.mRedirectionActivity);
        sb.append(";");
        int actionCount = getActionCount();
        for (int i = 0; i < actionCount; i++) {
            sb.append(this.mActions.get(i).toString());
            if (i < actionCount - 1) {
                sb.append(";");
            }
        }
        sb.append("] ");
        return sb.toString();
    }
}
