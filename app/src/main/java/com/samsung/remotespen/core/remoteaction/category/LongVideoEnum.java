package com.samsung.remotespen.core.remoteaction.category;

import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.util.RemoteActionUtil;
import com.samsung.util.usage.SAUtils;

/* loaded from: classes.dex */
public enum LongVideoEnum {
    GESTURE_LEFT { // from class: com.samsung.remotespen.core.remoteaction.category.LongVideoEnum.1
        @Override // com.samsung.remotespen.core.remoteaction.category.LongVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_LEFT);
            preference.setPreference(Action.Preference.PREFERENCE_MOTION_ONLY, SAUtils.AirCommandMain.S_PEN_ACTION_QUICK_SETTINGS_OPEN);
            Action.Builder builder = new Action.Builder();
            builder.setId("left").setLabel("@string/remotespen_detail_action_gesture_left").setIcon(null).setPriority(1).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_LEFT")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_RIGHT { // from class: com.samsung.remotespen.core.remoteaction.category.LongVideoEnum.2
        @Override // com.samsung.remotespen.core.remoteaction.category.LongVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_RIGHT);
            preference.setPreference(Action.Preference.PREFERENCE_MOTION_ONLY, SAUtils.AirCommandMain.S_PEN_ACTION_QUICK_SETTINGS_OPEN);
            Action.Builder builder = new Action.Builder();
            builder.setId("right").setLabel("@string/remotespen_detail_action_gesture_right").setIcon(null).setPriority(2).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_RIGHT")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_UP { // from class: com.samsung.remotespen.core.remoteaction.category.LongVideoEnum.3
        @Override // com.samsung.remotespen.core.remoteaction.category.LongVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_UP);
            preference.setPreference(Action.Preference.PREFERENCE_MOTION_ONLY, SAUtils.AirCommandMain.S_PEN_ACTION_QUICK_SETTINGS_OPEN);
            Action.Builder builder = new Action.Builder();
            builder.setId("up").setLabel("@string/remotespen_volume_up").setIcon(null).setPriority(3).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("VOLUME_UP")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_DOWN { // from class: com.samsung.remotespen.core.remoteaction.category.LongVideoEnum.4
        @Override // com.samsung.remotespen.core.remoteaction.category.LongVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_DOWN);
            preference.setPreference(Action.Preference.PREFERENCE_MOTION_ONLY, SAUtils.AirCommandMain.S_PEN_ACTION_QUICK_SETTINGS_OPEN);
            Action.Builder builder = new Action.Builder();
            builder.setId("down").setLabel("@string/remotespen_volume_down").setIcon(null).setPriority(4).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("VOLUME_DOWN")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    };

    public abstract Action getRemoteAction();

    public static RemoteAction getRemoteActions() {
        RemoteAction remoteAction = new RemoteAction();
        remoteAction.setVersion(1.2f);
        remoteAction.setSwiftAction(true);
        for (LongVideoEnum longVideoEnum : (LongVideoEnum[]) LongVideoEnum.class.getEnumConstants()) {
            remoteAction.addAction(longVideoEnum.getRemoteAction());
        }
        return remoteAction;
    }
}
