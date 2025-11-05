package com.samsung.remotespen.core.penaction.trigger;

/* JADX WARN: Enum visitor error
jadx.core.utils.exceptions.JadxRuntimeException: Init of enum SINGLE_CLICK uses external variables
	at jadx.core.dex.visitors.EnumVisitor.createEnumFieldByConstructor(EnumVisitor.java:444)
	at jadx.core.dex.visitors.EnumVisitor.processEnumFieldByRegister(EnumVisitor.java:391)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromFilledArray(EnumVisitor.java:320)
	at jadx.core.dex.visitors.EnumVisitor.extractEnumFieldsFromInsn(EnumVisitor.java:258)
	at jadx.core.dex.visitors.EnumVisitor.convertToEnum(EnumVisitor.java:151)
	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:100)
 */
/* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
/* loaded from: classes.dex */
public final class PenActionTriggerType {
    private static final /* synthetic */ PenActionTriggerType[] $VALUES;
    public static final PenActionTriggerType DOUBLE_CLICK;
    public static final PenActionTriggerType GESTURE_CIRCLE_CCW;
    public static final PenActionTriggerType GESTURE_CIRCLE_CW;
    public static final PenActionTriggerType GESTURE_DOWN;
    public static final PenActionTriggerType GESTURE_LEFT;
    public static final PenActionTriggerType GESTURE_POINTY_DOWN;
    public static final PenActionTriggerType GESTURE_POINTY_LEFT;
    public static final PenActionTriggerType GESTURE_POINTY_RIGHT;
    public static final PenActionTriggerType GESTURE_POINTY_UP;
    public static final PenActionTriggerType GESTURE_RIGHT;
    public static final PenActionTriggerType GESTURE_SHAKE;
    public static final PenActionTriggerType GESTURE_TRIANGLE;
    public static final PenActionTriggerType GESTURE_UNKNOWN;
    public static final PenActionTriggerType GESTURE_UP;
    public static final PenActionTriggerType LONG_CLICK;
    public static final PenActionTriggerType SECONDARY_DOUBLE_CLICK;
    public static final PenActionTriggerType SECONDARY_LONG_CLICK;
    public static final PenActionTriggerType SECONDARY_SINGLE_CLICK;
    public static final PenActionTriggerType SINGLE_CLICK;
    private String mGestureType;
    private boolean mIsGlobalActionType;
    private PenActionTriggerCategory mPenActionTriggerCategory;
    private String mTriggerNameForSa;

    public static PenActionTriggerType valueOf(String str) {
        return (PenActionTriggerType) Enum.valueOf(PenActionTriggerType.class, str);
    }

    public static PenActionTriggerType[] values() {
        return (PenActionTriggerType[]) $VALUES.clone();
    }

    static {
        PenActionTriggerCategory penActionTriggerCategory = PenActionTriggerCategory.BUTTON;
        PenActionTriggerType penActionTriggerType = new PenActionTriggerType("SINGLE_CLICK", 0, "button_click", penActionTriggerCategory, false, "Single");
        SINGLE_CLICK = penActionTriggerType;
        PenActionTriggerType penActionTriggerType2 = new PenActionTriggerType("DOUBLE_CLICK", 1, "button_double_click", penActionTriggerCategory, false, "Double");
        DOUBLE_CLICK = penActionTriggerType2;
        PenActionTriggerType penActionTriggerType3 = new PenActionTriggerType("LONG_CLICK", 2, "button_long_click", penActionTriggerCategory, true, "Long");
        LONG_CLICK = penActionTriggerType3;
        PenActionTriggerType penActionTriggerType4 = new PenActionTriggerType("SECONDARY_SINGLE_CLICK", 3, "secondary_button_click", penActionTriggerCategory, true, "2ndPenSingle");
        SECONDARY_SINGLE_CLICK = penActionTriggerType4;
        PenActionTriggerType penActionTriggerType5 = new PenActionTriggerType("SECONDARY_DOUBLE_CLICK", 4, "secondary_button_double_click", penActionTriggerCategory, true, "2ndPenDouble");
        SECONDARY_DOUBLE_CLICK = penActionTriggerType5;
        PenActionTriggerType penActionTriggerType6 = new PenActionTriggerType("SECONDARY_LONG_CLICK", 5, "secondary_button_long_click", penActionTriggerCategory, true, "2ndPenLong");
        SECONDARY_LONG_CLICK = penActionTriggerType6;
        PenActionTriggerCategory penActionTriggerCategory2 = PenActionTriggerCategory.GESTURE;
        PenActionTriggerType penActionTriggerType7 = new PenActionTriggerType("GESTURE_UNKNOWN", 6, "motion_unknown", penActionTriggerCategory2, false, "Unknown");
        GESTURE_UNKNOWN = penActionTriggerType7;
        PenActionTriggerType penActionTriggerType8 = new PenActionTriggerType("GESTURE_UP", 7, "motion_swipe_top", penActionTriggerCategory2, false, "Up");
        GESTURE_UP = penActionTriggerType8;
        PenActionTriggerType penActionTriggerType9 = new PenActionTriggerType("GESTURE_DOWN", 8, "motion_swipe_bottom", penActionTriggerCategory2, false, "Down");
        GESTURE_DOWN = penActionTriggerType9;
        PenActionTriggerType penActionTriggerType10 = new PenActionTriggerType("GESTURE_LEFT", 9, "motion_swipe_left", penActionTriggerCategory2, false, "Left");
        GESTURE_LEFT = penActionTriggerType10;
        PenActionTriggerType penActionTriggerType11 = new PenActionTriggerType("GESTURE_RIGHT", 10, "motion_swipe_right", penActionTriggerCategory2, false, "Right");
        GESTURE_RIGHT = penActionTriggerType11;
        PenActionTriggerType penActionTriggerType12 = new PenActionTriggerType("GESTURE_SHAKE", 11, "motion_shake", penActionTriggerCategory2, true, "Shake");
        GESTURE_SHAKE = penActionTriggerType12;
        PenActionTriggerType penActionTriggerType13 = new PenActionTriggerType("GESTURE_CIRCLE_CW", 12, "motion_circle_cw", penActionTriggerCategory2, false, "ClockWise");
        GESTURE_CIRCLE_CW = penActionTriggerType13;
        PenActionTriggerType penActionTriggerType14 = new PenActionTriggerType("GESTURE_CIRCLE_CCW", 13, "motion_circle_ccw", penActionTriggerCategory2, false, "CounterClockWise");
        GESTURE_CIRCLE_CCW = penActionTriggerType14;
        PenActionTriggerType penActionTriggerType15 = new PenActionTriggerType("GESTURE_POINTY_UP", 14, "motion_pointy_top", penActionTriggerCategory2, true, "UpAndDown");
        GESTURE_POINTY_UP = penActionTriggerType15;
        PenActionTriggerType penActionTriggerType16 = new PenActionTriggerType("GESTURE_POINTY_DOWN", 15, "motion_pointy_bottom", penActionTriggerCategory2, true, "DownAndUp");
        GESTURE_POINTY_DOWN = penActionTriggerType16;
        PenActionTriggerType penActionTriggerType17 = new PenActionTriggerType("GESTURE_POINTY_LEFT", 16, "motion_pointy_left", penActionTriggerCategory2, true, "LeftAndBack");
        GESTURE_POINTY_LEFT = penActionTriggerType17;
        PenActionTriggerType penActionTriggerType18 = new PenActionTriggerType("GESTURE_POINTY_RIGHT", 17, "motion_pointy_right", penActionTriggerCategory2, true, "RightAndBack");
        GESTURE_POINTY_RIGHT = penActionTriggerType18;
        PenActionTriggerType penActionTriggerType19 = new PenActionTriggerType("GESTURE_TRIANGLE", 18, "motion_triangle", penActionTriggerCategory2, false, "Triangle");
        GESTURE_TRIANGLE = penActionTriggerType19;
        $VALUES = new PenActionTriggerType[]{penActionTriggerType, penActionTriggerType2, penActionTriggerType3, penActionTriggerType4, penActionTriggerType5, penActionTriggerType6, penActionTriggerType7, penActionTriggerType8, penActionTriggerType9, penActionTriggerType10, penActionTriggerType11, penActionTriggerType12, penActionTriggerType13, penActionTriggerType14, penActionTriggerType15, penActionTriggerType16, penActionTriggerType17, penActionTriggerType18, penActionTriggerType19};
    }

    private PenActionTriggerType(String str, int i, String str2, PenActionTriggerCategory penActionTriggerCategory, boolean z, String str3) {
        this.mGestureType = str2;
        this.mPenActionTriggerCategory = penActionTriggerCategory;
        this.mIsGlobalActionType = z;
        this.mTriggerNameForSa = str3;
    }

    public String getActionName() {
        return this.mGestureType;
    }

    public PenActionTriggerCategory getCategory() {
        return this.mPenActionTriggerCategory;
    }

    public boolean isKnownGestureTriggerType() {
        return getCategory() == PenActionTriggerCategory.GESTURE && this != GESTURE_UNKNOWN;
    }

    public boolean isGlobalActionType() {
        return this.mIsGlobalActionType;
    }

    public boolean isGlobalButtonActionType() {
        return this.mIsGlobalActionType && this.mPenActionTriggerCategory == PenActionTriggerCategory.BUTTON;
    }

    public boolean isGlobalGestureActionType() {
        return this.mIsGlobalActionType && this.mPenActionTriggerCategory == PenActionTriggerCategory.GESTURE;
    }

    public String getTriggerNameForSamsungAnalytics() {
        return this.mTriggerNameForSa;
    }
}
