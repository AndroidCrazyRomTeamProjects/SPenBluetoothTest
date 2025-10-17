package com.samsung.remotespen.core.device.control.detector.gesture;

/* loaded from: classes.dex */
public enum JudgeState {
    NOT_APPLICABLE { // from class: com.samsung.remotespen.core.device.control.detector.gesture.JudgeState.1
        @Override // java.lang.Enum
        public String toString() {
            return "n/a";
        }
    },
    IDLE { // from class: com.samsung.remotespen.core.device.control.detector.gesture.JudgeState.2
        @Override // java.lang.Enum
        public String toString() {
            return "idle";
        }
    },
    INTERESTED { // from class: com.samsung.remotespen.core.device.control.detector.gesture.JudgeState.3
        @Override // java.lang.Enum
        public String toString() {
            return "interested";
        }
    },
    SATISFACTION { // from class: com.samsung.remotespen.core.device.control.detector.gesture.JudgeState.4
        @Override // java.lang.Enum
        public String toString() {
            return "satisfaction";
        }
    }
}
