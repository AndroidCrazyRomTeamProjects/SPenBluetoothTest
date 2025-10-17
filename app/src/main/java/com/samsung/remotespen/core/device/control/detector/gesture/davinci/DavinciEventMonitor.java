package com.samsung.remotespen.core.device.control.detector.gesture.davinci;

import android.text.TextUtils;
import android.util.Log;

import com.samsung.aboutpage.Constants;
import com.samsung.remotespen.core.device.control.detector.gesture.Event;
import com.samsung.remotespen.core.device.control.detector.gesture.EventHistoryManager;
import com.samsung.remotespen.core.device.control.detector.gesture.GestureScore;
import com.samsung.remotespen.core.device.control.detector.gesture.JudgeState;
import com.samsung.remotespen.core.device.control.detector.gesture.MonitorState;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import java.util.List;

/* loaded from: classes.dex */
public abstract class DavinciEventMonitor {
    public static final int SHAPE_3P_CCW = 2;
    public static final int SHAPE_3P_CW = 4;
    public static final int SHAPE_3P_IN_LINE = 1;
    private static final String TAG = "DavinciEventMonitor";
    private GestureScore mDetectedGestureScore;
    private BleSpenGestureType mMe;
    private DavinciEventMonitor mNextMonitor;
    private GestureScore mOnGoingGestureScore;
    private EventHistoryManager mRawEventHistoryManager;
    private JudgeState mState = JudgeState.IDLE;

    public abstract String getTag();

    public abstract void onInitialize();

    public abstract MonitorState onJudge(Event event, int[] iArr);

    public abstract void updateTravelDistance(GestureScore gestureScore, Event event);

    public abstract BleSpenGestureType whoAmI();

    public DavinciEventMonitor(DavinciEventMonitor davinciEventMonitor) {
        BleSpenGestureType whoAmI = whoAmI();
        this.mMe = whoAmI;
        this.mNextMonitor = davinciEventMonitor;
        this.mOnGoingGestureScore = new GestureScore(whoAmI);
    }

    public final void setRawEventHistoryManager(EventHistoryManager eventHistoryManager) {
        this.mRawEventHistoryManager = eventHistoryManager;
        DavinciEventMonitor davinciEventMonitor = this.mNextMonitor;
        if (davinciEventMonitor != null) {
            davinciEventMonitor.setRawEventHistoryManager(eventHistoryManager);
        }
    }

    public final EventHistoryManager getRawEventHistoryManager() {
        return this.mRawEventHistoryManager;
    }

    public final BleSpenGestureType getType() {
        return this.mMe;
    }

    public final void reset() {
        this.mState = JudgeState.IDLE;
        DavinciEventMonitor davinciEventMonitor = this.mNextMonitor;
        if (davinciEventMonitor != null) {
            davinciEventMonitor.reset();
        }
        this.mOnGoingGestureScore.reset();
        this.mDetectedGestureScore = null;
        onInitialize();
    }

    public JudgeState getJudgeState() {
        return this.mState;
    }

    public void setJudgeState(JudgeState judgeState) {
        this.mState = judgeState;
    }

    public final String judgeStateToString() {
        if (this.mNextMonitor != null) {
            return judgeStateToString(this) + this.mNextMonitor.judgeStateToString();
        }
        return judgeStateToString(this);
    }

    private String judgeStateToString(DavinciEventMonitor davinciEventMonitor) {
        if (davinciEventMonitor.getType() == BleSpenGestureType.UNKNOWN) {
            return Constants.packageName.NONE;
        }
        return "[" + davinciEventMonitor.getType().toString() + ": " + davinciEventMonitor.getJudgeState().toString() + "], ";
    }

    public final MonitorState judge(Event event) {
        return judge(event, new int[2]);
    }

    public final MonitorState judge(Event event, int[] iArr) {
        int eventCount;
        EventHistoryManager eventHistoryManager = this.mRawEventHistoryManager;
        if (eventHistoryManager == null) {
            Log.e(TAG, "judge : EventHistoryManager doesn't exist!!. Unable to judge gesture");
            eventCount = 1;
        } else {
            eventCount = eventHistoryManager.getEventCount();
        }
        JudgeState judgeState = getJudgeState();
        if (getJudgeState() == JudgeState.IDLE || getJudgeState() == JudgeState.NOT_APPLICABLE) {
            this.mOnGoingGestureScore.reset();
            Log.d(tag(), "onJudge : JudgeState is " + getJudgeState() + ", Reset JudgeScore!!");
            this.mOnGoingGestureScore.setStartIndex(eventCount + (-1));
        }
        MonitorState onJudge = onJudge(event, iArr);
        int i = AnonymousClass1.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[getJudgeState().ordinal()];
        if (i == 1 || i == 2) {
            EventHistoryManager eventHistoryManager2 = this.mRawEventHistoryManager;
            if (eventHistoryManager2 != null) {
                eventCount = eventHistoryManager2.getEventCount();
            }
            this.mOnGoingGestureScore.setEndIndex(eventCount - 1);
            updateTravelDistance(this.mOnGoingGestureScore, event);
        } else if (i == 3 && judgeState == JudgeState.SATISFACTION) {
            recordCurrentGestureScore();
            Log.d(tag(), "Record Score : " + this.mDetectedGestureScore);
        }
        if (onJudge == MonitorState.STAY_DETECTED || onJudge == MonitorState.FILTERING) {
            return onJudge;
        }
        DavinciEventMonitor davinciEventMonitor = this.mNextMonitor;
        if (davinciEventMonitor != null) {
            return davinciEventMonitor.judge(event, iArr);
        }
        return MonitorState.MONITORING;
    }

    /* renamed from: com.samsung.remotespen.core.device.control.detector.gesture.davinci.DavinciEventMonitor$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState;

        static {
            int[] iArr = new int[JudgeState.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState = iArr;
            try {
                iArr[JudgeState.INTERESTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[JudgeState.SATISFACTION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$control$detector$gesture$JudgeState[JudgeState.NOT_APPLICABLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private void recordCurrentGestureScore() {
        this.mDetectedGestureScore = this.mOnGoingGestureScore.m14clone();
        this.mOnGoingGestureScore.reset();
    }

    public GestureScore getGestureScore() {
        return this.mOnGoingGestureScore;
    }

    public void getOngoingGestureScores(List<GestureScore> list) {
        if (this.mState == JudgeState.SATISFACTION) {
            list.add(this.mOnGoingGestureScore);
        }
        DavinciEventMonitor davinciEventMonitor = this.mNextMonitor;
        if (davinciEventMonitor != null) {
            davinciEventMonitor.getOngoingGestureScores(list);
        }
    }

    public void getDetectedGestureScores(List<GestureScore> list) {
        GestureScore gestureScore = this.mDetectedGestureScore;
        if (gestureScore != null) {
            list.add(gestureScore);
        }
        DavinciEventMonitor davinciEventMonitor = this.mNextMonitor;
        if (davinciEventMonitor != null) {
            davinciEventMonitor.getDetectedGestureScores(list);
        }
    }

    private String tag() {
        if (TextUtils.isEmpty(getTag())) {
            return TAG;
        }
        return "DavinciEventMonitor|" + getTag();
    }
}
