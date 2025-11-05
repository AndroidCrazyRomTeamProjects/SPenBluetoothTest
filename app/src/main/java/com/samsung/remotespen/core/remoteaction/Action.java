package com.samsung.remotespen.core.remoteaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import com.samsung.remotespen.core.remoteaction.FeatureWrapper.CscFeatureWrapper;
import com.samsung.remotespen.core.remoteaction.FeatureWrapper.FeatureWrapper;
import com.samsung.remotespen.core.remoteaction.FeatureWrapper.FloatingFeatureWrapper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class Action implements Parcelable {
    private static final String CSC_FEATURE_PREFIX = "CscFeature_";
    private static final String SEC_FLOATING_FEATURE_PREFIX = "SEC_FLOATING_FEATURE_";
    private static final String SEC_PRODUCT_FEATURE_PREFIX = "SEC_PRODUCT_FEATURE_";
    public static final String SEPARATOR_PREFERENCE_GESTURE = "\\|";
    private DisableCondition mDisableCondition;
    private String mIcon;
    private String mId;
    private boolean mIsHide;
    private KeyShortcut mKeyShortcut;
    private String mLabel;
    private Preference mPreference;
    private int mPriority;
    private Repeat mRepeat;
    private static final String TAG = Action.class.getSimpleName();
    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() { // from class: com.samsung.remotespen.core.remoteaction.Action.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Action createFromParcel(Parcel parcel) {
            return new Action(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Action[] newArray(int i) {
            return new Action[i];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Action(Parcel parcel) {
        this.mId = parcel.readString();
        this.mLabel = parcel.readString();
        this.mIcon = parcel.readString();
        this.mPriority = parcel.readInt();
        this.mKeyShortcut = (KeyShortcut) parcel.readParcelable(KeyShortcut.class.getClassLoader());
        this.mDisableCondition = (DisableCondition) parcel.readParcelable(DisableCondition.class.getClassLoader());
        this.mRepeat = (Repeat) parcel.readParcelable(Repeat.class.getClassLoader());
        this.mPreference = (Preference) parcel.readParcelable(Preference.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mId);
        parcel.writeString(this.mLabel);
        parcel.writeString(this.mIcon);
        parcel.writeInt(this.mPriority);
        parcel.writeParcelable(this.mKeyShortcut, i);
        parcel.writeParcelable(this.mDisableCondition, i);
        parcel.writeParcelable(this.mRepeat, i);
        parcel.writeParcelable(this.mPreference, i);
    }

    public Action() {
    }

    public boolean isEnabled() {
        if (TextUtils.isEmpty(this.mId)) {
            Log.w(TAG, "This action is not enabled, id is empty!");
            return false;
        } else if (TextUtils.isEmpty(this.mLabel)) {
            Log.w(TAG, "This action is not enabled, label is empty!");
            return false;
        } else if (!isResourceId(this.mLabel)) {
            Log.w(TAG, "This action is not enabled, label is not resource id!");
            return false;
        } else {
            if (this.mIsHide) {
                if (this.mPriority != 0) {
                    Log.w(TAG, "This action is not enabled, invalid priority, priority should be null or 0");
                    return false;
                }
                Preference preference = this.mPreference;
                if (preference != null && !preference.isValidForHide()) {
                    return false;
                }
            } else if (this.mPriority <= 0) {
                Log.w(TAG, "This action is not enabled, invalid priority, priority should > 0");
                return false;
            } else {
                Preference preference2 = this.mPreference;
                if (preference2 != null && !preference2.isValid()) {
                    return false;
                }
            }
            if (this.mKeyShortcut == null) {
                Log.w(TAG, "This action is not enabled, shortcut is empty");
                return false;
            }
            Repeat repeat = this.mRepeat;
            if (repeat != null && !repeat.isValid()) {
                String str = TAG;
                Log.w(str, "This action is not enabled, invalid repeatable interval : " + this.mRepeat.getRepeatableInterval());
                return false;
            }
            DisableCondition disableCondition = this.mDisableCondition;
            if (disableCondition != null) {
                return !disableCondition.verify();
            }
            return true;
        }
    }

    private static boolean isResourceId(String str) {
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "icon is empty");
            return false;
        } else if (str.startsWith("@")) {
            return true;
        } else {
            String str2 = TAG;
            Log.e(str2, "icon(" + str + ") is not start with @");
            return false;
        }
    }

    public String getId() {
        return this.mId;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public String getIcon() {
        return this.mIcon;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public boolean getHide() {
        return this.mIsHide;
    }

    public KeyShortcut getKeyShortCut() {
        return this.mKeyShortcut;
    }

    public Repeat getRepeat() {
        return this.mRepeat;
    }

    public Preference getPreference() {
        return this.mPreference;
    }

    public int getActionCategory() {
        Preference preference = this.mPreference;
        if (preference == null) {
            return 0;
        }
        return preference.getActionCategory();
    }

    public HashSet<String> getPreferedDefaultGestureActions() {
        Preference preference = this.mPreference;
        if (preference == null) {
            return new HashSet<>();
        }
        return preference.getPreferedDefaultGestureActions();
    }

    public DisableCondition getDisableCondition() {
        return this.mDisableCondition;
    }

    /* loaded from: classes.dex */
    public static class Builder {
        private Action mAction = new Action();

        public Builder setId(String str) {
            this.mAction.mId = str;
            return this;
        }

        public Builder setLabel(String str) {
            this.mAction.mLabel = str;
            return this;
        }

        public Builder setIcon(String str) {
            this.mAction.mIcon = str;
            return this;
        }

        public Builder setPriority(int i) {
            this.mAction.mPriority = i;
            return this;
        }

        public Builder setHide(boolean z) {
            this.mAction.mIsHide = z;
            return this;
        }

        public Builder setKeyShortcut(KeyShortcut keyShortcut) {
            this.mAction.mKeyShortcut = keyShortcut;
            return this;
        }

        public Builder setDisableCondition(DisableCondition disableCondition) {
            this.mAction.mDisableCondition = disableCondition;
            return this;
        }

        public Builder setPreference(Preference preference) {
            this.mAction.mPreference = preference;
            return this;
        }

        public Preference getPreference() {
            return this.mAction.mPreference;
        }

        public Builder setRepeat(Repeat repeat) {
            this.mAction.mRepeat = repeat;
            return this;
        }

        public Action build() {
            return this.mAction;
        }
    }

    /* loaded from: classes.dex */
    public static class Repeat implements Parcelable {
        public static final Parcelable.Creator<Repeat> CREATOR = new Parcelable.Creator<Repeat>() { // from class: com.samsung.remotespen.core.remoteaction.Action.Repeat.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Repeat createFromParcel(Parcel parcel) {
                return new Repeat(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Repeat[] newArray(int i) {
                return new Repeat[i];
            }
        };
        public static final String REPEATABLE_INTERVAL_LONG_STRING = "long";
        public static final int REPEATABLE_INTERVAL_LONG_VALUE = 1000;
        public static final int REPEATABLE_INTERVAL_MAX_VALUE = 3000;
        public static final String REPEATABLE_INTERVAL_MEDIUM_STRING = "medium";
        public static final int REPEATABLE_INTERVAL_MEDIUM_VALUE = 500;
        public static final int REPEATABLE_INTERVAL_MIN_VALUE = 50;
        public static final String REPEATABLE_INTERVAL_SHORT_STRING = "short";
        public static final int REPEATABLE_INTERVAL_SHORT_VALUE = 300;
        private boolean mIsRepeatable;
        private int mRepeatableInterval;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public Repeat() {
            this.mIsRepeatable = false;
        }

        public Repeat(boolean z, String str) {
            this.mIsRepeatable = z;
            this.mRepeatableInterval = parseRepeatableInterval(str);
        }

        public Repeat(Parcel parcel) {
            this.mIsRepeatable = false;
            this.mIsRepeatable = parcel.readByte() != 0;
            this.mRepeatableInterval = parseRepeatableInterval(parcel.readString());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte(this.mIsRepeatable ? (byte) 1 : (byte) 0);
            parcel.writeString(Integer.toString(this.mRepeatableInterval));
        }

        public boolean isRepeatable() {
            return this.mIsRepeatable;
        }

        public int getRepeatableInterval() {
            return this.mRepeatableInterval;
        }

        public boolean isValid() {
            if (this.mIsRepeatable) {
                int i = this.mRepeatableInterval;
                return i >= 50 && i <= 3000;
            }
            return true;
        }

        private int parseRepeatableInterval(String str) {
            if (str == null) {
                return 0;
            }
            char c = 65535;
            switch (str.hashCode()) {
                case -1078030475:
                    if (str.equals(REPEATABLE_INTERVAL_MEDIUM_STRING)) {
                        c = 0;
                        break;
                    }
                    break;
                case 3327612:
                    if (str.equals(REPEATABLE_INTERVAL_LONG_STRING)) {
                        c = 1;
                        break;
                    }
                    break;
                case 109413500:
                    if (str.equals(REPEATABLE_INTERVAL_SHORT_STRING)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return REPEATABLE_INTERVAL_MEDIUM_VALUE;
                case 1:
                    return 1000;
                case 2:
                    return 300;
                default:
                    try {
                        return Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        Log.e(Action.TAG, e.getMessage());
                        return 0;
                    }
            }
        }
    }

    /* loaded from: classes.dex */
    public static class DisableCondition implements Parcelable {
        public static final Parcelable.Creator<DisableCondition> CREATOR = new Parcelable.Creator<DisableCondition>() { // from class: com.samsung.remotespen.core.remoteaction.Action.DisableCondition.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public DisableCondition createFromParcel(Parcel parcel) {
                return new DisableCondition(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public DisableCondition[] newArray(int i) {
                return new DisableCondition[i];
            }
        };
        public static final int MATCH_TYPE_CONTAINS = 1;
        public static final int MATCH_TYPE_EXACTLY = 0;
        private FeatureWrapper mCscFeature;
        private String mFeature;
        private FeatureWrapper mFloatingFeature;
        private int mMatchType;
        private String mValue;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public DisableCondition(String str, String str2) {
            this.mCscFeature = new CscFeatureWrapper();
            this.mFloatingFeature = new FloatingFeatureWrapper();
            this.mFeature = str;
            this.mValue = str2;
            this.mMatchType = 0;
        }

        public DisableCondition(String str, String str2, int i) {
            this.mCscFeature = new CscFeatureWrapper();
            this.mFloatingFeature = new FloatingFeatureWrapper();
            this.mFeature = str;
            this.mValue = str2;
            this.mMatchType = i;
        }

        public DisableCondition(Parcel parcel) {
            this.mCscFeature = new CscFeatureWrapper();
            this.mFloatingFeature = new FloatingFeatureWrapper();
            this.mFeature = parcel.readString();
            this.mValue = parcel.readString();
            this.mMatchType = parcel.readInt();
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.mFeature);
            parcel.writeString(this.mValue);
            parcel.writeInt(this.mMatchType);
        }

        public String getFeature() {
            return this.mFeature;
        }

        public String getValue() {
            return this.mValue;
        }

        public boolean verify() {
            if (TextUtils.isEmpty(this.mFeature)) {
                return false;
            }
            String str = null;
            if (this.mFeature.startsWith(Action.CSC_FEATURE_PREFIX)) {
                str = this.mCscFeature.getString(this.mFeature);
            } else if (this.mFeature.startsWith(Action.SEC_FLOATING_FEATURE_PREFIX)) {
                str = this.mFloatingFeature.getString(this.mFeature);
            } else if (this.mFeature.startsWith(Action.SEC_PRODUCT_FEATURE_PREFIX)) {
                return false;
            }
            if (TextUtils.isEmpty(str)) {
                return false;
            }
            int i = this.mMatchType;
            if (i == 0) {
                return str.equalsIgnoreCase(this.mValue);
            }
            if (i == 1) {
                return str.contains(this.mValue);
            }
            return false;
        }

        public void setCscFeatureClass(FeatureWrapper featureWrapper) {
            this.mCscFeature = featureWrapper;
        }

        public void setFloatingFeatureClass(FeatureWrapper featureWrapper) {
            this.mFloatingFeature = featureWrapper;
        }
    }

    /* loaded from: classes.dex */
    public static class Preference implements Parcelable {
        public static final String GESTURE_CIRCLE_CCW = "circle_ccw";
        public static final String GESTURE_CIRCLE_CW = "circle_cw";
        public static final String GESTURE_CLICK = "click";
        public static final String GESTURE_DOUBLE_CLICK = "double_click";
        public static final String GESTURE_LONG_PRESS = "long_press";
        public static final String GESTURE_SWIPE_DOWN = "swipe_down";
        public static final String GESTURE_SWIPE_LEFT = "swipe_left";
        public static final String GESTURE_SWIPE_RIGHT = "swipe_right";
        public static final String GESTURE_SWIPE_UP = "swipe_up";
        public static final int PREFERENCE_ACTION_CATEGORY_BUTTON_ONLY = 1;
        public static final int PREFERENCE_ACTION_CATEGORY_DEFAULT = 0;
        public static final int PREFERENCE_ACTION_CATEGORY_INVALID = -1;
        public static final int PREFERENCE_ACTION_CATEGORY_MOTION_ONLY = 2;
        public static final String PREFERENCE_BUTTON_ONLY = "button_only";
        public static final String PREFERENCE_GESTURE = "gesture";
        public static final String PREFERENCE_MOTION_ONLY = "motion_only";
        private int mActionCategory;
        private HashSet<String> mPreferedDefaultGestureActions;
        public static final HashSet<String> VALID_GESTURES_ALL = validAllGestures();
        public static final HashSet<String> VALID_GESTURES_BUTTON = validButtonGestures();
        public static final HashSet<String> VALID_GESTURES_MOTION = validMotionGestures();
        public static final HashSet<String> VALID_GESTURES_NO_HIDE = validNoHideGestures();
        public static final Parcelable.Creator<Preference> CREATOR = new Parcelable.Creator<Preference>() { // from class: com.samsung.remotespen.core.remoteaction.Action.Preference.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Preference createFromParcel(Parcel parcel) {
                return new Preference(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public Preference[] newArray(int i) {
                return new Preference[i];
            }
        };

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        private static HashSet<String> validAllGestures() {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(GESTURE_CLICK);
            hashSet.add(GESTURE_DOUBLE_CLICK);
            hashSet.add(GESTURE_SWIPE_LEFT);
            hashSet.add(GESTURE_SWIPE_RIGHT);
            hashSet.add(GESTURE_SWIPE_UP);
            hashSet.add(GESTURE_SWIPE_DOWN);
            hashSet.add(GESTURE_CIRCLE_CCW);
            hashSet.add(GESTURE_CIRCLE_CW);
            hashSet.add(GESTURE_LONG_PRESS);
            return hashSet;
        }

        private static HashSet<String> validNoHideGestures() {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(GESTURE_CLICK);
            hashSet.add(GESTURE_DOUBLE_CLICK);
            hashSet.add(GESTURE_SWIPE_LEFT);
            hashSet.add(GESTURE_SWIPE_RIGHT);
            hashSet.add(GESTURE_SWIPE_UP);
            hashSet.add(GESTURE_SWIPE_DOWN);
            hashSet.add(GESTURE_CIRCLE_CCW);
            hashSet.add(GESTURE_CIRCLE_CW);
            return hashSet;
        }

        private static HashSet<String> validButtonGestures() {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(GESTURE_CLICK);
            hashSet.add(GESTURE_DOUBLE_CLICK);
            return hashSet;
        }

        private static HashSet<String> validMotionGestures() {
            HashSet<String> hashSet = new HashSet<>();
            hashSet.add(GESTURE_SWIPE_LEFT);
            hashSet.add(GESTURE_SWIPE_RIGHT);
            hashSet.add(GESTURE_SWIPE_UP);
            hashSet.add(GESTURE_SWIPE_DOWN);
            hashSet.add(GESTURE_CIRCLE_CCW);
            hashSet.add(GESTURE_CIRCLE_CW);
            return hashSet;
        }

        public Preference() {
            this.mPreferedDefaultGestureActions = new HashSet<>();
            this.mActionCategory = 0;
        }

        public Preference(String str, String str2) {
            this.mPreferedDefaultGestureActions = new HashSet<>();
            setPreference(str, str2);
        }

        public Preference(Parcel parcel) {
            this.mPreferedDefaultGestureActions = new HashSet<>();
            setActionCategory(parcel.readInt());
            while (true) {
                String readString = parcel.readString();
                if (readString == null) {
                    return;
                }
                this.mPreferedDefaultGestureActions.add(readString);
            }
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(this.mActionCategory);
            Iterator<String> it = this.mPreferedDefaultGestureActions.iterator();
            while (it.hasNext()) {
                parcel.writeString(it.next());
            }
        }

        public void setActionCategory(int i) {
            this.mActionCategory = i;
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        public void setPreference(String str, String str2) {
            boolean z;
            str.hashCode();
            switch (str.hashCode()) {
                case -1759551335:
                    if (str.equals(PREFERENCE_BUTTON_ONLY)) {
                        z = false;
                        break;
                    }
                    z = true;
                    break;
                case -863143275:
                    if (str.equals(PREFERENCE_MOTION_ONLY)) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                case -75080375:
                    if (str.equals(PREFERENCE_GESTURE)) {
                        z = true;
                        break;
                    }
                    z = true;
                    break;
                default:
                    z = true;
                    break;
            }
            switch (z) {
                case false:
                    if (Boolean.parseBoolean(str2)) {
                        setActionCategory(1);
                        return;
                    }
                    return;
                case true:
                    if (Boolean.parseBoolean(str2)) {
                        setActionCategory(2);
                        return;
                    }
                    return;
                case true:
                    setPreferedDefaultGestureAction(str2);
                    return;
                default:
                    Log.e(Action.TAG, "Wrong preference name : " + str);
                    setActionCategory(-1);
                    return;
            }
        }

        private void setPreferedDefaultGestureAction(String str) {
            this.mPreferedDefaultGestureActions = parseGestures(str);
        }

        public int getActionCategory() {
            return this.mActionCategory;
        }

        public HashSet<String> getPreferedDefaultGestureActions() {
            return this.mPreferedDefaultGestureActions;
        }

        private HashSet<String> parseGestures(String str) {
            HashSet<String> hashSet = new HashSet<>();
            if (TextUtils.isEmpty(str)) {
                Log.e(Action.TAG, "Gesture is null");
                return hashSet;
            }
            String[] split = str.split(Action.SEPARATOR_PREFERENCE_GESTURE);
            if (split.length == 0) {
                Log.e(Action.TAG, "unable to parse Preference Gesture!");
                return hashSet;
            }
            for (String str2 : split) {
                if (VALID_GESTURES_ALL.contains(str2)) {
                    hashSet.add(str2);
                }
            }
            return hashSet;
        }

        public boolean isValid() {
            int i = this.mActionCategory;
            if (i != 0) {
                if (i != 1) {
                    if (i != 2) {
                        String str = Action.TAG;
                        Log.e(str, "preference type = " + this.mActionCategory);
                        return false;
                    }
                    return isValidPreferenceType(VALID_GESTURES_MOTION);
                }
                return isValidPreferenceType(VALID_GESTURES_BUTTON);
            }
            return isValidPreferenceType(VALID_GESTURES_NO_HIDE);
        }

        public boolean isValidForHide() {
            if (this.mActionCategory != 1) {
                String str = Action.TAG;
                Log.e(str, "this action is hide but preference is not button only : " + this.mActionCategory);
                return false;
            } else if (this.mPreferedDefaultGestureActions.size() != 1) {
                String str2 = Action.TAG;
                Log.e(str2, "this action is hide but gesture have to be only one : " + this.mPreferedDefaultGestureActions);
                return false;
            } else if (this.mPreferedDefaultGestureActions.contains(GESTURE_LONG_PRESS)) {
                return true;
            } else {
                String str3 = Action.TAG;
                Log.e(str3, "this action is hide but gesture is not long click : " + this.mPreferedDefaultGestureActions);
                return false;
            }
        }

        private boolean isValidPreferenceType(HashSet<String> hashSet) {
            HashSet<String> hashSet2 = this.mPreferedDefaultGestureActions;
            if (hashSet2 == null) {
                return true;
            }
            Iterator<String> it = hashSet2.iterator();
            while (it.hasNext()) {
                String next = it.next();
                if (!hashSet.contains(next)) {
                    String str = Action.TAG;
                    Log.e(str, "preference type = " + this.mActionCategory + ", contains " + next);
                    return false;
                }
            }
            return true;
        }
    }

    public String toString() {
        return "{" + this.mId + "," + this.mLabel + "," + this.mIcon + "," + this.mPriority + "," + this.mKeyShortcut.toString() + "}";
    }

    /* loaded from: classes.dex */
    public static class KeyShortcut implements Parcelable {
        public static final Parcelable.Creator<KeyShortcut> CREATOR = new Parcelable.Creator<KeyShortcut>() { // from class: com.samsung.remotespen.core.remoteaction.Action.KeyShortcut.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public KeyShortcut createFromParcel(Parcel parcel) {
                return new KeyShortcut(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public KeyShortcut[] newArray(int i) {
                return new KeyShortcut[i];
            }
        };
        private boolean mIsValid;
        private List<Integer> mKeys;

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public KeyShortcut() {
            this.mKeys = new ArrayList();
            this.mIsValid = true;
        }

        public KeyShortcut(Parcel parcel) {
            ArrayList arrayList = new ArrayList();
            this.mKeys = arrayList;
            this.mIsValid = true;
            parcel.readList(arrayList, null);
            this.mIsValid = parcel.readByte() != 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeList(this.mKeys);
            parcel.writeByte(this.mIsValid ? (byte) 1 : (byte) 0);
        }

        public void addKey(String str) {
            if (this.mKeys.size() >= 4) {
                String str2 = Action.TAG;
                Log.w(str2, "Exceed shortcut key count(4), ignore " + str);
            } else if (this.mIsValid) {
                int convert2KeyCode = convert2KeyCode(str);
                if (convert2KeyCode == 0) {
                    String str3 = Action.TAG;
                    Log.e(str3, convert2KeyCode + " is Invalid Key code, invalid shortcut!");
                    this.mIsValid = false;
                } else if (this.mKeys.contains(Integer.valueOf(convert2KeyCode))) {
                    String str4 = Action.TAG;
                    Log.w(str4, convert2KeyCode + " already exist. ignored!");
                } else {
                    this.mKeys.add(Integer.valueOf(convert2KeyCode));
                }
            }
        }

        public List<Integer> getKeyList() {
            return this.mKeys;
        }

        public boolean isValid() {
            return this.mIsValid;
        }

        private static int convert2KeyCode(String str) {
            if ("CTRL".equals(str) || "SHIFT".equals(str) || "ALT".equals(str)) {
                str = str + "_LEFT";
            }
            try {
                Field field = KeyEvent.class.getField("KEYCODE_" + str);
                if (field != null) {
                    try {
                        return field.getInt(null);
                    } catch (IllegalAccessException unused) {
                        Log.e(Action.TAG, "Unknown error");
                    }
                }
                return 0;
            } catch (NoSuchFieldException unused2) {
                Log.e(Action.TAG, str + " is not exist!");
                return 0;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            List<Integer> list = this.mKeys;
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    sb.append(this.mKeys.get(i));
                    if (i < size - 1) {
                        sb.append(",");
                    }
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
