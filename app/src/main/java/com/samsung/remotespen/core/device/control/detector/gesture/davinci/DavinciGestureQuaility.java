package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import com.samsung.remotespen.core.device.data.BleSpenGestureHoverState;
import com.samsung.remotespen.core.device.data.BleSpenGestureImpurity;
import com.samsung.remotespen.core.device.data.BleSpenGestureQuality;
import com.samsung.remotespen.core.device.data.BleSpenGestureSize;
import com.samsung.remotespen.core.device.data.BleSpenGestureSpeed;

/* loaded from: classes.dex */
public class DavinciGestureQuaility extends BleSpenGestureQuality {

    /* loaded from: classes.dex */
    public enum GestureQuality {
        NONE,
        GOOD,
        TOO_FAST,
        TOO_SLOW,
        TOO_SMALL,
        HIGH_IMPURITY
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciGestureQuaility$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality;

        static {
            int[] iArr = new int[GestureQuality.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality = iArr;
            try {
                iArr[GestureQuality.TOO_SMALL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality[GestureQuality.TOO_SLOW.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality[GestureQuality.TOO_FAST.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality[GestureQuality.HIGH_IMPURITY.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality[GestureQuality.GOOD.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public DavinciGestureQuaility(GestureQuality gestureQuality) {
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$davinci$DavinciGestureQuaility$GestureQuality[gestureQuality.ordinal()];
        if (i == 1) {
            this.mGestureSize = BleSpenGestureSize.TOO_SMALL;
        } else if (i == 2) {
            this.mGestureSpeed = BleSpenGestureSpeed.TOO_SLOW;
        } else if (i == 3) {
            this.mGestureSpeed = BleSpenGestureSpeed.TOO_FAST;
        } else if (i == 4) {
            this.mGestureImpurity = BleSpenGestureImpurity.HIGH_IMPURITY;
        } else if (i != 5) {
        } else {
            this.mGestureSize = BleSpenGestureSize.GOOD;
            this.mGestureSpeed = BleSpenGestureSpeed.GOOD;
            this.mGestureImpurity = BleSpenGestureImpurity.GOOD;
            this.mGestureHoverState = BleSpenGestureHoverState.GOOD;
        }
    }
}
