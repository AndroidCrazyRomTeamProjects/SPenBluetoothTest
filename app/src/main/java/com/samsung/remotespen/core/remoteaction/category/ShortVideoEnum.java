package com.samsung.remotespen.core.remoteaction.category;

import com.samsung.remotespen.core.remoteaction.Action;
import com.samsung.remotespen.core.remoteaction.RemoteAction;
import com.samsung.remotespen.core.remoteaction.util.RemoteActionUtil;

/* loaded from: classes.dex */
public enum ShortVideoEnum {
    GESTURE_LEFT { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.1
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_LEFT);
            Action.Builder builder = new Action.Builder();
            builder.setId("left").setLabel("@string/remotespen_detail_action_gesture_left").setIcon(null).setPriority(1).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_LEFT")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_RIGHT { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.2
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_RIGHT);
            Action.Builder builder = new Action.Builder();
            builder.setId("right").setLabel("@string/remotespen_detail_action_gesture_right").setIcon(null).setPriority(2).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_RIGHT")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_UP { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.3
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_UP);
            Action.Builder builder = new Action.Builder();
            builder.setId("up").setLabel("@string/remotespen_detail_action_gesture_up").setIcon(null).setPriority(3).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_UP")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_DOWN { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.4
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_SWIPE_DOWN);
            Action.Builder builder = new Action.Builder();
            builder.setId("down").setLabel("@string/remotespen_detail_action_gesture_down").setIcon(null).setPriority(4).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+DPAD_DOWN")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_CLICK { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.5
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_CLICK);
            Action.Builder builder = new Action.Builder();
            builder.setId(Action.Preference.GESTURE_CLICK).setLabel("@string/remotespen_guide_primary_title").setIcon(null).setPriority(5).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+C")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_DOUBLE_CLICK { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.6
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_DOUBLE_CLICK);
            Action.Builder builder = new Action.Builder();
            builder.setId(Action.Preference.GESTURE_DOUBLE_CLICK).setLabel("@string/remotespen_guide_secondary_title").setIcon(null).setPriority(6).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("CTRL_LEFT+SHIFT_RIGHT+D")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_CW { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.7
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_CIRCLE_CW);
            Action.Builder builder = new Action.Builder();
            builder.setId("cw").setLabel("@string/remotespen_volume_up").setIcon(null).setPriority(7).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("VOLUME_UP")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    },
    GESTURE_CCW { // from class: com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum.8
        @Override // com.samsung.remotespen.core.remoteaction.category.ShortVideoEnum
        public Action getRemoteAction() {
            Action.Preference preference = new Action.Preference(Action.Preference.PREFERENCE_GESTURE, Action.Preference.GESTURE_CIRCLE_CCW);
            Action.Builder builder = new Action.Builder();
            builder.setId("ccw").setLabel("@string/remotespen_volume_down").setIcon(null).setPriority(8).setHide(false).setKeyShortcut(RemoteActionUtil.getKeyShortcut("VOLUME_DOWN")).setRepeat(null).setPreference(preference);
            return builder.build();
        }
    };

    public abstract Action getRemoteAction();

    public static RemoteAction getRemoteActions() {
        RemoteAction remoteAction = new RemoteAction();
        remoteAction.setVersion(1.2f);
        for (ShortVideoEnum shortVideoEnum : (ShortVideoEnum[]) ShortVideoEnum.class.getEnumConstants()) {
            remoteAction.addAction(shortVideoEnum.getRemoteAction());
        }
        return remoteAction;
    }
}
