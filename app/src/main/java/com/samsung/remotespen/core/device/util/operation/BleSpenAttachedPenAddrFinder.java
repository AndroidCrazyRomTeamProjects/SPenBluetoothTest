package com.samsung.remotespen.core.device.util.operation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.remotespen.core.device.ble.abstraction.IBleDevice;
import com.samsung.remotespen.core.device.ble.abstraction.IScanResult;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import com.samsung.remotespen.core.device.data.BleSpenScanFilter;
import com.samsung.remotespen.core.device.util.BleUtils;
import com.samsung.remotespen.core.device.util.operation.BleScanManager;
import com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.CommonUtils;
import com.samsung.util.debug.Assert;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class BleSpenAttachedPenAddrFinder implements BleCancellableOperation {
    private static final int MAX_ALLOWABLE_LINEAR_CHARGING_STATE_LEN = 2;
    private static final int MAX_ALLOWABLE_RESPONSE_DELAY = 480;
    private static final int PHASE_DURATION_LOWER = 800;
    private static final int PHASE_DURATION_UPPER = 800;
    private static final int REQUIRED_MATCH_PHASE_COUNT_FOR_MULTIPLE_DEVICE = 9;
    private static final int REQUIRED_MATCH_PHASE_COUNT_FOR_ONE_DEVICE = 7;
    private static final String TAG = "BleSpenAttachedPenAddrFinder";
    private static final int TICTOC_READY_DELAY = 500;
    private AdvertisementControlListener mAdvertisementControlListener;
    private BleCancellableOperation.FinishListener mCancelFinishListener;
    private Context mContext;
    private BleCancellableOperation mCurWorkingOp;
    private Options mOptions;
    private ResultListener mResultListener;
    private int mScanDuration;
    private long mScanStartTime;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    private Thread mWorkerThread;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private SpenInsertionEventDetector.Listener mSpenInsertionListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.1
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            BleSpenAttachedPenAddrFinder.this.onSpenInsertionEvent(z);
        }
    };
    private ArrayList<Phase> mPhaseHistory = new ArrayList<>();
    private boolean mIsCancelledManually = false;
    private BleScanManager mBleScanManager = new BleScanManager();
    private BleStandAloneModeEnabler mBleStandAloneModeEnabler = new BleStandAloneModeEnabler();

    /* loaded from: classes.dex */
    public interface AdvertisementControlListener {
        void enableSpenAdvertisement(boolean z);

        void onFinishTicToc();

        void onPrepareTicToc();
    }

    /* loaded from: classes.dex */
    public static class Options {
        public int mMaxAllowableResponseDelay = BleSpenAttachedPenAddrFinder.MAX_ALLOWABLE_RESPONSE_DELAY;
        public int mRequiredMatchPhaseCountForMultipleDevice = 9;
        public int mRequiredMatchPhaseCountForOneDevice = 7;
        public int mPhaseDurationLower = 800;
        public int mPhaseDurationUpper = 800;
        public int mMaxAllowableLinearChargingStateLen = 2;
    }

    /* loaded from: classes.dex */
    public enum ResultCode {
        SUCCESS,
        TIMEOUT,
        PEN_DETACHED,
        BLE_NOT_ENABLED,
        ALREADY_WORKING,
        ALREADY_SCANNING,
        CANCELLED,
        UNKNOWN
    }

    /* loaded from: classes.dex */
    public interface ResultListener {
        void onFinishOnePhase(Phase phase);

        void onFinished(ResultData resultData, long j);
    }

    /* loaded from: classes.dex */
    public static class ResultData {
        private String mFoundPenAddress;
        public ArrayList<PatternItem> mMatchPatterns;
        public ArrayList<Phase> mPhaseTable;
        private ResultCode mResultCode;

        /* loaded from: classes.dex */
        public static class PatternItem {
            public String mAddress;
            public String mMatchPattern;

            public PatternItem(String str, String str2) {
                this.mAddress = str;
                this.mMatchPattern = str2;
            }
        }

        public ResultData(ResultCode resultCode) {
            this.mMatchPatterns = new ArrayList<>();
            this.mPhaseTable = new ArrayList<>();
            this.mResultCode = resultCode;
        }

        public ResultData(ResultCode resultCode, String str) {
            this(resultCode);
            this.mFoundPenAddress = str;
        }

        public ResultCode getResultCode() {
            return this.mResultCode;
        }

        public String getFoundPenAddress() {
            return this.mFoundPenAddress;
        }

        public void addMatchPattern(PatternItem patternItem) {
            this.mMatchPatterns.add(patternItem);
        }

        public String getMatchPattern() {
            return getMatchPattern(this.mFoundPenAddress);
        }

        public String getMatchPattern(String str) {
            if (str == null) {
                return null;
            }
            Iterator<PatternItem> it = this.mMatchPatterns.iterator();
            while (it.hasNext()) {
                PatternItem next = it.next();
                if (next.mAddress.equalsIgnoreCase(str)) {
                    return next.mMatchPattern;
                }
            }
            return null;
        }

        public String getChargingStateStr() {
            ArrayList<Phase> arrayList = this.mPhaseTable;
            if (arrayList == null || arrayList.isEmpty()) {
                return "NO PHASE INFO";
            }
            StringBuilder sb = new StringBuilder();
            Iterator<Phase> it = this.mPhaseTable.iterator();
            while (it.hasNext()) {
                sb.append(it.next().mIsCharging ? "C" : ".");
            }
            return sb.toString();
        }
    }

    /* loaded from: classes.dex */
    public static class BdAddrStringArray extends ArrayList<String> {
        private static final String TAG = BdAddrStringArray.class.getSimpleName();

        @Override // java.util.ArrayList, java.util.AbstractList, java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean add(String str) {
            if (contains(str)) {
                return false;
            }
            return super.add((BdAddrStringArray) str);
        }

        @Override // java.util.ArrayList, java.util.AbstractCollection, java.util.Collection, java.util.List
        public boolean addAll(Collection<? extends String> collection) {
            for (String str : collection) {
                add(str);
            }
            return true;
        }

        public void intersect(BdAddrStringArray bdAddrStringArray) {
            for (int size = size() - 1; size >= 0; size--) {
                if (!bdAddrStringArray.contains(get(size))) {
                    remove(size);
                }
            }
        }

        public void exclude(BdAddrStringArray bdAddrStringArray) {
            int size = bdAddrStringArray.size();
            for (int i = 0; i < size; i++) {
                int indexOf = indexOf(bdAddrStringArray.get(i));
                if (indexOf >= 0) {
                    remove(indexOf);
                }
            }
        }

        public void dump(String str) {
            String str2 = TAG;
            Log.v(str2, "dump : --- " + str + " ---");
            Iterator<String> it = iterator();
            while (it.hasNext()) {
                String str3 = TAG;
                Log.v(str3, "dump : " + it.next());
            }
        }
    }

    /* loaded from: classes.dex */
    public static class Phase {
        public boolean mIsCharging = false;
        private BdAddrStringArray mScannedAddrs = new BdAddrStringArray();
        public long mStartTime;
        public long mStopTime;

        public Phase(int i) {
        }

        public void start(boolean z) {
            this.mIsCharging = z;
            this.mStartTime = getCurrentSystemTime();
        }

        public void end() {
            this.mStopTime = getCurrentSystemTime();
        }

        public long getDuration() {
            long j = this.mStartTime;
            if (j != 0) {
                long j2 = this.mStopTime;
                if (j2 == 0) {
                    return -1L;
                }
                return j2 - j;
            }
            return -1L;
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x0013 A[Catch: all -> 0x000b, TryCatch #0 {all -> 0x000b, blocks: (B:5:0x0005, B:11:0x000e, B:13:0x0013, B:14:0x0028, B:16:0x0036, B:22:0x0042), top: B:30:0x0005 }] */
        /* JADX WARN: Removed duplicated region for block: B:16:0x0036 A[Catch: all -> 0x000b, TRY_LEAVE, TryCatch #0 {all -> 0x000b, blocks: (B:5:0x0005, B:11:0x000e, B:13:0x0013, B:14:0x0028, B:16:0x0036, B:22:0x0042), top: B:30:0x0005 }] */
        /* JADX WARN: Removed duplicated region for block: B:22:0x0042 A[Catch: all -> 0x000b, TRY_ENTER, TRY_LEAVE, TryCatch #0 {all -> 0x000b, blocks: (B:5:0x0005, B:11:0x000e, B:13:0x0013, B:14:0x0028, B:16:0x0036, B:22:0x0042), top: B:30:0x0005 }] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public synchronized java.lang.String getMatchMark(java.lang.String r6, java.lang.String[] r7) {
            /*
                r5 = this;
                monitor-enter(r5)
                r0 = 1
                r1 = 0
                if (r7 == 0) goto Ld
                int r2 = r7.length     // Catch: java.lang.Throwable -> Lb
                r3 = 4
                if (r2 != r3) goto Ld
                r2 = r0
                goto Le
            Lb:
                r6 = move-exception
                goto L4e
            Ld:
                r2 = r1
            Le:
                com.samsung.util.debug.Assert.e(r2)     // Catch: java.lang.Throwable -> Lb
                if (r7 != 0) goto L28
                java.lang.String r7 = com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.access$000()     // Catch: java.lang.Throwable -> Lb
                java.lang.String r2 = "marks is null"
                com.samsung.util.debug.Log.w(r7, r2)     // Catch: java.lang.Throwable -> Lb
                java.lang.String r7 = "O"
                java.lang.String r2 = "o"
                java.lang.String r3 = ":"
                java.lang.String r4 = "."
                java.lang.String[] r7 = new java.lang.String[]{r7, r2, r3, r4}     // Catch: java.lang.Throwable -> Lb
            L28:
                r1 = r7[r1]     // Catch: java.lang.Throwable -> Lb
                r0 = r7[r0]     // Catch: java.lang.Throwable -> Lb
                r2 = 2
                r2 = r7[r2]     // Catch: java.lang.Throwable -> Lb
                r3 = 3
                r7 = r7[r3]     // Catch: java.lang.Throwable -> Lb
                boolean r3 = r5.mIsCharging     // Catch: java.lang.Throwable -> Lb
                if (r3 == 0) goto L42
                com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder$BdAddrStringArray r7 = r5.mScannedAddrs     // Catch: java.lang.Throwable -> Lb
                boolean r6 = r7.contains(r6)     // Catch: java.lang.Throwable -> Lb
                if (r6 == 0) goto L40
                monitor-exit(r5)
                return r1
            L40:
                monitor-exit(r5)
                return r2
            L42:
                com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder$BdAddrStringArray r1 = r5.mScannedAddrs     // Catch: java.lang.Throwable -> Lb
                boolean r6 = r1.contains(r6)     // Catch: java.lang.Throwable -> Lb
                if (r6 == 0) goto L4c
                monitor-exit(r5)
                return r7
            L4c:
                monitor-exit(r5)
                return r0
            L4e:
                monitor-exit(r5)
                throw r6
            */
            throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.Phase.getMatchMark(java.lang.String, java.lang.String[]):java.lang.String");
        }

        public synchronized BdAddrStringArray getScannedAddrs() {
            BdAddrStringArray bdAddrStringArray;
            bdAddrStringArray = new BdAddrStringArray();
            bdAddrStringArray.addAll(this.mScannedAddrs);
            return bdAddrStringArray;
        }

        public synchronized boolean addScannedAddr(String str) {
            return this.mScannedAddrs.add(str);
        }

        private long getCurrentSystemTime() {
            return SystemClock.elapsedRealtimeNanos() / 1000000;
        }
    }

    public BleSpenAttachedPenAddrFinder(Context context, AdvertisementControlListener advertisementControlListener) {
        this.mContext = context.getApplicationContext();
        Assert.notNull(advertisementControlListener);
        this.mAdvertisementControlListener = advertisementControlListener;
        this.mSpenInsertionEventDetector = SpenInsertionEventDetector.getInstance(context);
        this.mOptions = new Options();
    }

    public void startTicToc(BleSpenScanFilter bleSpenScanFilter, int i, ResultListener resultListener) {
        startTicToc(bleSpenScanFilter, i, this.mOptions, resultListener);
    }

    public void startTicToc(final BleSpenScanFilter bleSpenScanFilter, final int i, Options options, ResultListener resultListener) {
        this.mResultListener = resultListener;
        if (this.mWorkerThread != null) {
            Log.e(TAG, "startTicToc : Pairing logic is in progress");
            invokeResultListener(ResultCode.ALREADY_WORKING, null);
        } else if (this.mBleScanManager.isScanning()) {
            Log.e(TAG, "startTicToc : Already scanning");
            invokeResultListener(ResultCode.ALREADY_SCANNING, null);
        } else {
            this.mOptions = options;
            BleStandAloneModeEnabler bleStandAloneModeEnabler = this.mBleStandAloneModeEnabler;
            this.mCurWorkingOp = bleStandAloneModeEnabler;
            bleStandAloneModeEnabler.enableBleStandAloneMode(this.mContext, new BleStandAloneModeEnabler.Listener() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.2
                @Override // com.samsung.remotespen.core.device.util.operation.BleStandAloneModeEnabler.Listener
                public void onFinish(BleOpResultData bleOpResultData) {
                    BleSpenAttachedPenAddrFinder.this.mCurWorkingOp = null;
                    BleOpResultCode resultCode = bleOpResultData.getResultCode();
                    String str = BleSpenAttachedPenAddrFinder.TAG;
                    Log.d(str, "startTicToc : BLE enable result=" + resultCode);
                    if (resultCode == BleOpResultCode.SUCCESS) {
                        BleSpenAttachedPenAddrFinder bleSpenAttachedPenAddrFinder = BleSpenAttachedPenAddrFinder.this;
                        bleSpenAttachedPenAddrFinder.mScanStartTime = bleSpenAttachedPenAddrFinder.getCurrentSystemTime();
                        BleSpenAttachedPenAddrFinder.this.mScanDuration = i;
                        if (!BleSpenAttachedPenAddrFinder.this.mSpenInsertionEventDetector.isInserted()) {
                            Log.e(BleSpenAttachedPenAddrFinder.TAG, "startTicToc : SPen is detached");
                            BleSpenAttachedPenAddrFinder.this.invokeResultListener(ResultCode.PEN_DETACHED, null);
                            return;
                        }
                        BleSpenAttachedPenAddrFinder.this.mSpenInsertionEventDetector.registerListener(BleSpenAttachedPenAddrFinder.this.mSpenInsertionListener);
                        BleSpenAttachedPenAddrFinder.this.mWorkerThread = new Thread(new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                AnonymousClass2 anonymousClass2 = AnonymousClass2.this;
                                BleSpenAttachedPenAddrFinder.this.doTicTocAction(bleSpenScanFilter);
                            }
                        });
                        BleSpenAttachedPenAddrFinder.this.mWorkerThread.start();
                    } else if (resultCode == BleOpResultCode.CANCELLED) {
                        BleSpenAttachedPenAddrFinder.this.invokeResultListener(ResultCode.CANCELLED, null);
                    } else {
                        BleSpenAttachedPenAddrFinder.this.invokeResultListener(ResultCode.BLE_NOT_ENABLED, null);
                    }
                }
            });
        }
    }

    @Override // com.samsung.remotespen.core.device.data.BleCancellableOperation
    public void cancelOperation(BleCancellableOperation.FinishListener finishListener) {
        Thread thread;
        String str = TAG;
        Log.d(str, "cancelOperation");
        boolean z = this.mCurWorkingOp != null || ((thread = this.mWorkerThread) != null && thread.isAlive());
        if (z) {
            boolean z2 = this.mCancelFinishListener == null;
            Assert.e(z2, "Cancel listener is already registered : " + this.mCancelFinishListener);
            this.mCancelFinishListener = finishListener;
        }
        if (this.mCurWorkingOp != null) {
            Log.d(str, "cancelOperation : Cancelling the operation : " + this.mCurWorkingOp.getClass().getSimpleName());
            this.mCurWorkingOp.cancelOperation(null);
        }
        Thread thread2 = this.mWorkerThread;
        if (thread2 != null && thread2.isAlive()) {
            Log.d(str, "cancelOperation : Cancelling the worker thread..");
            this.mIsCancelledManually = true;
            this.mWorkerThread.interrupt();
        } else {
            Log.e(str, "cancelOperation : Worker thread is not working");
        }
        if (z || finishListener == null) {
            return;
        }
        finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
    }

    public void setFinderOptions(Options options) {
        this.mOptions = options;
    }

    public Options getFinderOptions() {
        return this.mOptions;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doTicTocAction(BleSpenScanFilter bleSpenScanFilter) {
        ResultCode resultCode;
        Log.i(TAG, "doTicTocAction");
        BleScanManager.ScanListener scanListener = new BleScanManager.ScanListener() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.3
            @Override // com.samsung.remotespen.core.device.util.operation.BleScanManager.ScanListener
            public void onFinishScan() {
            }

            @Override // com.samsung.remotespen.core.device.util.operation.BleScanManager.ScanListener
            public void onScanResult(IScanResult iScanResult, ArrayList<IBleDevice> arrayList) {
                BleSpenAttachedPenAddrFinder.this.onDeviceScanned(iScanResult.getDevice().getAddress(), iScanResult.getTimestampNanos() / 1000000, iScanResult.getRssi());
            }
        };
        synchronized (this.mPhaseHistory) {
            this.mPhaseHistory.clear();
        }
        String str = null;
        BdAddrStringArray bdAddrStringArray = new BdAddrStringArray();
        ResultCode resultCode2 = ResultCode.UNKNOWN;
        this.mAdvertisementControlListener.onPrepareTicToc();
        int i = 0;
        this.mAdvertisementControlListener.enableSpenAdvertisement(false);
        sleep(500L);
        this.mBleScanManager.startScan(this.mContext, this.mScanDuration + 5000, BleUtils.getSpenDeviceTypeUuid(true), bleSpenScanFilter, scanListener);
        while (true) {
            if (getCurrentSystemTime() - this.mScanStartTime > this.mScanDuration) {
                resultCode = ResultCode.TIMEOUT;
                break;
            } else if (!this.mSpenInsertionEventDetector.isInserted()) {
                resultCode = ResultCode.PEN_DETACHED;
                break;
            } else if (!BleUtils.isBleEnabled(this.mContext)) {
                resultCode = ResultCode.BLE_NOT_ENABLED;
                break;
            } else if (this.mIsCancelledManually) {
                resultCode = ResultCode.CANCELLED;
                break;
            } else {
                str = getFilteredFinalPenAddr(bdAddrStringArray.size());
                if (str != null) {
                    resultCode = ResultCode.SUCCESS;
                    break;
                }
                boolean generateNextAdvertisementState = generateNextAdvertisementState();
                Phase lastPhase = getLastPhase();
                Options options = this.mOptions;
                int i2 = options.mPhaseDurationLower;
                int i3 = options.mPhaseDurationUpper;
                if (lastPhase != null && lastPhase.mIsCharging && !generateNextAdvertisementState) {
                    int i4 = options.mMaxAllowableResponseDelay;
                    i2 += i4;
                    i3 += i4;
                }
                int generateRandomNumber = generateRandomNumber(i2, i3);
                String str2 = TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("doTicTocAction : isCharge = ");
                sb.append(generateNextAdvertisementState);
                sb.append(" duration=");
                sb.append(generateRandomNumber);
                sb.append(" phase=");
                int i5 = i + 1;
                sb.append(i5);
                Log.d(str2, sb.toString());
                final Phase phase = new Phase(i);
                synchronized (this.mPhaseHistory) {
                    this.mPhaseHistory.add(phase);
                }
                phase.start(generateNextAdvertisementState);
                this.mAdvertisementControlListener.enableSpenAdvertisement(generateNextAdvertisementState);
                sleep(generateRandomNumber);
                phase.end();
                synchronized (this.mPhaseHistory) {
                    bdAddrStringArray.addAll(phase.getScannedAddrs());
                }
                final ResultListener resultListener = this.mResultListener;
                if (resultListener != null) {
                    this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.4
                        @Override // java.lang.Runnable
                        public void run() {
                            resultListener.onFinishOnePhase(phase);
                        }
                    });
                }
                i = i5;
            }
        }
        String str3 = TAG;
        Log.i(str3, "doTicTocAction : finished. addr = " + str + " phaseCnt=" + i + " elapsed=" + (getCurrentSystemTime() - this.mScanStartTime));
        this.mBleScanManager.stopScan();
        this.mAdvertisementControlListener.onFinishTicToc();
        invokeResultListener(resultCode, str);
        printSummary();
        resetState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onDeviceScanned(String str, long j, int i) {
        Phase phaseByTime;
        if (str == null || (phaseByTime = getPhaseByTime(j)) == null) {
            return;
        }
        Phase prevPhase = getPrevPhase(phaseByTime);
        long j2 = j - phaseByTime.mStartTime;
        if (prevPhase != null && prevPhase.mIsCharging && !phaseByTime.mIsCharging && j2 < this.mOptions.mMaxAllowableResponseDelay) {
            String str2 = TAG;
            Log.v(str2, "onDeviceScanned : ignoring " + str + " rssi=" + i + " delay=" + j2);
        } else if (phaseByTime.addScannedAddr(str)) {
            String str3 = TAG;
            Log.v(str3, "onDeviceScanned : adding " + str + " rssi=" + i + " delay=" + j2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenInsertionEvent(boolean z) {
        String str = TAG;
        Log.v(str, "onSpenInsertionEvent : " + z);
        if (z || this.mWorkerThread == null) {
            return;
        }
        Log.d(str, "onSpenInsertionEvent : pen detached. interrupting worker thread");
        this.mWorkerThread.interrupt();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invokeResultListener(ResultCode resultCode, String str) {
        final long currentSystemTime = this.mScanStartTime != 0 ? getCurrentSystemTime() - this.mScanStartTime : 0L;
        String str2 = TAG;
        Log.d(str2, "invokeResultListener : " + resultCode + " elapsed=" + currentSystemTime + "ms " + str);
        final ResultListener resultListener = this.mResultListener;
        final ResultData resultData = new ResultData(resultCode, str);
        for (Map.Entry<String, MatchPatternItem> entry : buildMatchPatternTable().entrySet()) {
            resultData.addMatchPattern(new ResultData.PatternItem(entry.getKey(), entry.getValue().getMatchPattern()));
        }
        synchronized (this.mPhaseHistory) {
            resultData.mPhaseTable.addAll(this.mPhaseHistory);
        }
        this.mHandler.post(new Runnable() { // from class: com.samsung.remotespen.core.device.util.operation.BleSpenAttachedPenAddrFinder.5
            @Override // java.lang.Runnable
            public void run() {
                BleSpenAttachedPenAddrFinder.this.mSpenInsertionEventDetector.unregisterListener(BleSpenAttachedPenAddrFinder.this.mSpenInsertionListener);
                ResultListener resultListener2 = resultListener;
                if (resultListener2 != null) {
                    resultListener2.onFinished(resultData, currentSystemTime);
                }
                if (BleSpenAttachedPenAddrFinder.this.mCancelFinishListener != null) {
                    Log.d(BleSpenAttachedPenAddrFinder.TAG, "invokeResultListener : invoking cancel finish listener...");
                    BleSpenAttachedPenAddrFinder.this.mCancelFinishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
                    BleSpenAttachedPenAddrFinder.this.mCancelFinishListener = null;
                }
            }
        });
    }

    private void resetState() {
        this.mScanDuration = 0;
        this.mScanStartTime = 0L;
        this.mIsCancelledManually = false;
        this.mResultListener = null;
        this.mWorkerThread = null;
        synchronized (this.mPhaseHistory) {
            this.mPhaseHistory.clear();
        }
    }

    private Phase getPhaseByTime(long j) {
        synchronized (this.mPhaseHistory) {
            for (int size = this.mPhaseHistory.size() - 1; size >= 0; size--) {
                Phase phase = this.mPhaseHistory.get(size);
                if (j >= phase.mStartTime) {
                    long j2 = phase.mStopTime;
                    if (j2 == 0 || j < j2) {
                        return phase;
                    }
                }
            }
            return null;
        }
    }

    private Phase getPrevPhase(Phase phase) {
        synchronized (this.mPhaseHistory) {
            int indexOf = this.mPhaseHistory.indexOf(phase);
            if (indexOf <= 0) {
                return null;
            }
            return this.mPhaseHistory.get(indexOf - 1);
        }
    }

    private Phase getLastPhase() {
        synchronized (this.mPhaseHistory) {
            if (this.mPhaseHistory.size() == 0) {
                return null;
            }
            ArrayList<Phase> arrayList = this.mPhaseHistory;
            return arrayList.get(arrayList.size() - 1);
        }
    }

    private String getFilteredFinalPenAddr(int i) {
        Options options = this.mOptions;
        int i2 = options.mRequiredMatchPhaseCountForMultipleDevice;
        if (i == 1) {
            i2 = options.mRequiredMatchPhaseCountForOneDevice;
        }
        synchronized (this.mPhaseHistory) {
            BdAddrStringArray bdAddrStringArray = new BdAddrStringArray();
            BdAddrStringArray bdAddrStringArray2 = new BdAddrStringArray();
            int size = this.mPhaseHistory.size();
            if (size < i2) {
                return null;
            }
            int i3 = size - 1;
            int i4 = i3;
            while (true) {
                if (i4 < 0) {
                    i4 = -1;
                    break;
                } else if (this.mPhaseHistory.get(i4).mIsCharging) {
                    break;
                } else {
                    i4--;
                }
            }
            if (i4 == -1) {
                return null;
            }
            while (i3 > i4) {
                bdAddrStringArray2.addAll(this.mPhaseHistory.get(i3).getScannedAddrs());
                i3--;
            }
            bdAddrStringArray.addAll(this.mPhaseHistory.get(i4).getScannedAddrs());
            bdAddrStringArray.exclude(bdAddrStringArray2);
            if (i4 == 0) {
                if (bdAddrStringArray.size() == 1) {
                    String str = bdAddrStringArray.get(0);
                    Log.d(TAG, "getFilteredFinalPenAddr : device found(head) : " + str + " phase=" + size + " bdCnt=" + i);
                    return str;
                }
                return null;
            }
            for (int i5 = i4 - 1; i5 >= 0; i5--) {
                Phase phase = this.mPhaseHistory.get(i5);
                BdAddrStringArray scannedAddrs = phase.getScannedAddrs();
                if (phase.mIsCharging) {
                    bdAddrStringArray.intersect(scannedAddrs);
                } else {
                    bdAddrStringArray.exclude(scannedAddrs);
                }
                int i6 = size - i5;
                if (bdAddrStringArray.size() == 0) {
                    return null;
                }
                if (bdAddrStringArray.size() == 1 && i6 >= i2) {
                    String str2 = bdAddrStringArray.get(0);
                    Log.d(TAG, "getFilteredFinalPenAddr : device found : " + str2 + " phase=" + i6 + " bdCnt=" + i);
                    return str2;
                }
            }
            return null;
        }
    }

    private int generateRandomNumber(int i, int i2) {
        Assert.e(i2 >= i);
        return (int) (i + ((i2 - i) * Math.random()));
    }

    private boolean generateNextAdvertisementState() {
        synchronized (this.mPhaseHistory) {
            boolean z = true;
            if (this.mPhaseHistory.size() == 0) {
                return true;
            }
            if (getLinearChargingStateLenFromLastPhase() >= this.mOptions.mMaxAllowableLinearChargingStateLen) {
                Phase lastPhase = getLastPhase();
                if (lastPhase == null) {
                    Assert.fail("Last phase is null");
                    return true;
                }
                if (lastPhase.mIsCharging) {
                    z = false;
                }
                return z;
            }
            if (generateRandomNumber(0, 2) != 0) {
                z = false;
            }
            return z;
        }
    }

    private int getLinearChargingStateLenFromLastPhase() {
        synchronized (this.mPhaseHistory) {
            int size = this.mPhaseHistory.size();
            int i = 0;
            if (size == 0) {
                return 0;
            }
            boolean z = this.mPhaseHistory.get(size - 1).mIsCharging;
            for (int size2 = this.mPhaseHistory.size() - 1; size2 >= 0 && this.mPhaseHistory.get(size2).mIsCharging == z; size2--) {
                i++;
            }
            return i;
        }
    }

    private void sleep(long j) {
        CommonUtils.sleep(j);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long getCurrentSystemTime() {
        return SystemClock.elapsedRealtimeNanos() / 1000000;
    }

    private HashMap<String, MatchPatternItem> buildMatchPatternTable() {
        HashMap<String, MatchPatternItem> hashMap;
        synchronized (this.mPhaseHistory) {
            BdAddrStringArray bdAddrStringArray = new BdAddrStringArray();
            Iterator<Phase> it = this.mPhaseHistory.iterator();
            while (it.hasNext()) {
                bdAddrStringArray.addAll(it.next().getScannedAddrs());
            }
            hashMap = new HashMap<>();
            Iterator<String> it2 = bdAddrStringArray.iterator();
            while (it2.hasNext()) {
                hashMap.put(it2.next(), new MatchPatternItem());
            }
            String[] strArr = {"O", "o", ":", "."};
            Iterator<Phase> it3 = this.mPhaseHistory.iterator();
            while (it3.hasNext()) {
                Phase next = it3.next();
                BdAddrStringArray scannedAddrs = next.getScannedAddrs();
                Iterator<String> it4 = bdAddrStringArray.iterator();
                while (it4.hasNext()) {
                    String next2 = it4.next();
                    MatchPatternItem matchPatternItem = hashMap.get(next2);
                    matchPatternItem.mBuilder.append(next.getMatchMark(next2, strArr));
                    if (next.mIsCharging) {
                        if (scannedAddrs.contains(next2)) {
                            matchPatternItem.mMatchedCount++;
                        } else {
                            matchPatternItem.mUnmatchedCount++;
                        }
                    } else if (scannedAddrs.contains(next2)) {
                        matchPatternItem.mUnmatchedCount++;
                    } else {
                        matchPatternItem.mMatchedCount++;
                    }
                }
            }
        }
        return hashMap;
    }

    private void printSummary() {
        HashMap<String, MatchPatternItem> buildMatchPatternTable = buildMatchPatternTable();
        Set<String> keySet = buildMatchPatternTable.keySet();
        String str = TAG;
        Log.i(str, "printSummary : " + keySet.size() + " devices detected");
        for (Map.Entry<String, MatchPatternItem> entry : buildMatchPatternTable.entrySet()) {
            MatchPatternItem value = entry.getValue();
            String str2 = TAG;
            Log.i(str2, "printSummary : " + entry.getKey() + " MC:" + value.mMatchedCount + " UMC:" + value.mUnmatchedCount + " " + value.mBuilder.toString());
        }
    }
}
