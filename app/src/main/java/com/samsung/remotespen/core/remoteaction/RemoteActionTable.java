package com.samsung.remotespen.core.remoteaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class RemoteActionTable implements Parcelable {
    private HashMap<String, Activities> mPackages;
    private static final String TAG = RemoteActionTable.class.getSimpleName();
    public static final Parcelable.Creator<RemoteActionTable> CREATOR = new Parcelable.Creator<RemoteActionTable>() { // from class: com.samsung.remotespen.core.remoteaction.RemoteActionTable.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RemoteActionTable createFromParcel(Parcel parcel) {
            return new RemoteActionTable(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RemoteActionTable[] newArray(int i) {
            return new RemoteActionTable[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public RemoteActionTable(Parcel parcel) {
        HashMap<String, Activities> hashMap = new HashMap<>();
        this.mPackages = hashMap;
        hashMap.clear();
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.mPackages.put(parcel.readString(), (Activities) parcel.readParcelable(Activities.class.getClassLoader()));
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mPackages.size());
        for (Map.Entry<String, Activities> entry : this.mPackages.entrySet()) {
            parcel.writeString(entry.getKey());
            parcel.writeParcelable(entry.getValue(), i);
        }
    }

    public RemoteActionTable() {
        this.mPackages = new HashMap<>();
    }

    public boolean containsPackage(String str) {
        return this.mPackages.containsKey(str);
    }

    public boolean put(String str, String str2, RemoteAction remoteAction) {
        Activities activityMap = getActivityMap(str);
        if (activityMap.containsActivity(str2)) {
            RemoteAction remoteAction2 = activityMap.get(str2);
            if (isRedirection(remoteAction2)) {
                replaceRemoteAction(activityMap, str2, remoteAction);
                return true;
            }
            String str3 = TAG;
            Log.i(str3, "RemoteAction already exist");
            Log.i(str3, "   >> exist  : " + remoteAction2.toString());
            Log.i(str3, "   >> ignored: " + remoteAction.toString());
            return false;
        }
        activityMap.put(str2, remoteAction);
        String str4 = TAG;
        Log.d(str4, "RemoteAction added : " + remoteAction.toString());
        return true;
    }

    private Activities getActivityMap(String str) {
        if (this.mPackages.containsKey(str)) {
            return this.mPackages.get(str);
        }
        Activities activities = new Activities();
        this.mPackages.put(str, activities);
        return activities;
    }

    private boolean isRedirection(RemoteAction remoteAction) {
        return !TextUtils.isEmpty(remoteAction.getRedirectionPackage());
    }

    private void replaceRemoteAction(Activities activities, String str, RemoteAction remoteAction) {
        RemoteAction remove = activities.remove(str);
        activities.put(str, remoteAction);
        String str2 = TAG;
        Log.i(str2, "RemoteAction Replaced");
        Log.i(str2, "   >> from : " + remove.toString());
        Log.i(str2, "   >> to   : " + remoteAction.toString());
    }

    public RemoteAction getRemoteAction(String str, String str2) {
        Activities activities = this.mPackages.get(str);
        if (activities != null) {
            return activities.get(str2);
        }
        return null;
    }

    public int getPackageCount() {
        return this.mPackages.size();
    }

    public int getRemoteActionCount(String str) {
        Activities activities = this.mPackages.get(str);
        if (activities != null) {
            return activities.getSize();
        }
        return 0;
    }

    public int getActionCount(String str, String str2) {
        Activities activities = this.mPackages.get(str);
        if (activities != null) {
            return activities.getActionCount(str2);
        }
        return 0;
    }

    public List<String> getPackageList() {
        if (this.mPackages.isEmpty()) {
            return null;
        }
        return new ArrayList(this.mPackages.keySet());
    }

    public List<String> getActivityList(String str) {
        Activities activities = this.mPackages.get(str);
        if (activities == null) {
            return null;
        }
        return activities.getActivityList();
    }

    /* loaded from: classes.dex */
    public static class Activities implements Parcelable {
        public static final Parcelable.Creator<Activities> CREATOR = new Parcelable.Creator<Activities>() { // from class: com.samsung.remotespen.core.remoteaction.RemoteActionTable.Activities.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Activities createFromParcel(Parcel parcel) {
                return new Activities(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Activities[] newArray(int i) {
                return new Activities[i];
            }
        };
        private HashMap<String, RemoteAction> mActivities;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public Activities(Parcel parcel) {
            HashMap<String, RemoteAction> hashMap = new HashMap<>();
            this.mActivities = hashMap;
            hashMap.clear();
            int readInt = parcel.readInt();
            for (int i = 0; i < readInt; i++) {
                this.mActivities.put(parcel.readString(), (RemoteAction) parcel.readParcelable(RemoteAction.class.getClassLoader()));
            }
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(this.mActivities.size());
            for (Map.Entry<String, RemoteAction> entry : this.mActivities.entrySet()) {
                parcel.writeString(entry.getKey());
                parcel.writeParcelable(entry.getValue(), i);
            }
        }

        public Activities() {
            this.mActivities = new HashMap<>();
        }

        public boolean containsActivity(String str) {
            return this.mActivities.containsKey(str);
        }

        public boolean put(String str, RemoteAction remoteAction) {
            if (containsActivity(str)) {
                String str2 = RemoteActionTable.TAG;
                Log.w(str2, str + " is already exist. ignored");
                return false;
            }
            this.mActivities.put(str, remoteAction);
            return true;
        }

        public RemoteAction get(String str) {
            return this.mActivities.get(str);
        }

        public RemoteAction remove(String str) {
            return this.mActivities.remove(str);
        }

        public int getSize() {
            return this.mActivities.size();
        }

        public int getActionCount(String str) {
            RemoteAction remoteAction = this.mActivities.get(str);
            if (remoteAction != null) {
                return remoteAction.getActionCount();
            }
            return 0;
        }

        public List<String> getActivityList() {
            if (this.mActivities.isEmpty()) {
                return null;
            }
            return new ArrayList(this.mActivities.keySet());
        }

        public void clear() {
            this.mActivities.clear();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            List<String> activityList = getActivityList();
            if (activityList != null) {
                for (String str : activityList) {
                    sb.append(this.mActivities.get(str).toString());
                }
            }
            return sb.toString();
        }
    }

    public void clear() {
        List<String> packageList = getPackageList();
        if (packageList == null) {
            return;
        }
        for (String str : packageList) {
            Activities activities = this.mPackages.get(str);
            if (activities != null) {
                activities.clear();
            }
        }
        this.mPackages.clear();
    }

    public List<String> removeByPackageName(String str) {
        ArrayList arrayList = new ArrayList();
        this.mPackages.remove(str);
        List<String> packageList = getPackageList();
        if (packageList == null) {
            return null;
        }
        for (String str2 : packageList) {
            Activities activities = this.mPackages.get(str2);
            List<String> activityList = getActivityList(str2);
            if (activityList != null) {
                for (String str3 : activityList) {
                    if (str.equals(activities.get(str3).getResourcePackage())) {
                        activities.remove(str3);
                    }
                }
            }
            if (activities.getSize() == 0) {
                this.mPackages.remove(str2);
                arrayList.add(str2);
            }
        }
        return arrayList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> packageList = getPackageList();
        if (packageList != null) {
            for (String str : packageList) {
                sb.append(this.mPackages.get(str).toString());
            }
        }
        return sb.toString();
    }

    public void dump() {
        List<String> packageList = getPackageList();
        if (packageList != null) {
            String str = TAG;
            Log.d(str, "dump : pkgCount = " + packageList.size());
            for (String str2 : packageList) {
                String str3 = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("dump : ");
                sb.append(str2 + "  /  " + this.mPackages.get(str2).toString());
                Log.d(str3, sb.toString());
            }
        }
    }
}
