package com.samsung.remotespen.external;

import android.content.Context;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSelfTestResultEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import com.samsung.remotespen.external.CommandDispatcher;

/* loaded from: classes.dex */
public class SensorCmdDispatcher extends CommandDispatcher {
    private static final String CMD_ENABLE_SENSOR_LOGGING = "enableSensorLogging";
    private static final String CMD_SEND_SELF_TEST_CMD = "sendSelfTestCmd";
    private static final String TAG = "SensorCmdDispatcher";
    private BleSpenManager mBleSpenMgr;
    private Context mContext;
    private MotionSensorLogger mMotionSensorLogger;

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void init() {
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public void release() {
    }

    public SensorCmdDispatcher(Context context, BleSpenManager bleSpenManager, CommandDispatcher.IDispatchEnvironment iDispatchEnvironment) {
        super(context, iDispatchEnvironment);
        this.mContext = context;
        this.mBleSpenMgr = bleSpenManager;
    }

    @Override // com.samsung.remotespen.external.CommandDispatcher
    public boolean dispatchCommand(Transaction transaction) {
        String str = (String) transaction.mCommand;
        str.hashCode();
        if (str.equals(CMD_SEND_SELF_TEST_CMD)) {
            onRequestBleSpenSelfTestResult(transaction);
            return true;
        } else if (str.equals(CMD_ENABLE_SENSOR_LOGGING)) {
            onRequestBleSpenMotionSensorLogging(transaction);
            return true;
        } else {
            return false;
        }
    }

    private void onRequestBleSpenMotionSensorLogging(Transaction transaction) {
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else {
            Boolean bool = (Boolean) transaction.getParameterFromBundle(BindingApiConstants.BUNDLE_KEY_IS_ENABLE);
            if (bool == null) {
                String str = TAG;
                Log.e(str, "onRequestBleSpenMotionSensorLogging : Invalid parameter : isEnable is missing");
                sendErrorResponse(transaction, "Invalid parameter : isEnable is missing");
                return;
            }
            String str2 = (String) transaction.getParameterFromBundle(BindingApiConstants.BUNDLE_KEY_LOGGING_TAG);
            if (bool.booleanValue() && str2 == null) {
                String str3 = TAG;
                Log.e(str3, "onRequestBleSpenMotionSensorLogging : Invalid parameter : loggingTag is missing");
                sendErrorResponse(transaction, "Invalid parameter : loggingTag is missing");
                return;
            }
            String str4 = TAG;
            Log.d(str4, "onRequestBleSpenMotionSensorLogging : isEnable=" + bool + ", tag=" + str2);
            if (bool.booleanValue()) {
                if (this.mMotionSensorLogger == null) {
                    this.mMotionSensorLogger = new MotionSensorLogger(this.mContext);
                }
                this.mMotionSensorLogger.startLogging(str2);
                sendSuccessResponse(transaction, null);
                return;
            }
            MotionSensorLogger motionSensorLogger = this.mMotionSensorLogger;
            if (motionSensorLogger != null) {
                motionSensorLogger.stopLogging();
            }
            sendSuccessResponse(transaction, null);
        }
    }

    private void onRequestBleSpenSelfTestResult(Transaction transaction) {
        Log.v(TAG, "onRequestBleSpenSelfTestResult");
        if (!isRemoteSpenServiceRunning()) {
            sendServiceNotRunningResponse(transaction);
            return;
        }
        BleSpenInstanceId targetSpenInstanceIdWithDefault = getTargetSpenInstanceIdWithDefault(transaction);
        if (targetSpenInstanceIdWithDefault == null || !this.mPairedSpenManager.isPairedSpen(targetSpenInstanceIdWithDefault)) {
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_INVALID_SPEN_ID);
        } else if (!isConnectedToBleSpen(targetSpenInstanceIdWithDefault)) {
            sendNotConnectedResponse(transaction);
        } else if (this.mBleSpenMgr == null) {
            sendBleSpenNotSupportedResponse(transaction);
        } else {
            BleSpenInstanceId bundledSpenInstanceId = this.mPairedSpenManager.getBundledSpenInstanceId();
            this.mBleSpenMgr.setSpenOperationMode(bundledSpenInstanceId, BleSpenOperationMode.SENSOR_ON, new AnonymousClass1(transaction, bundledSpenInstanceId));
        }
    }

    /* renamed from: com.samsung.remotespen.external.SensorCmdDispatcher$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass1 implements BleSpenManager.OpFinishListener {
        public final /* synthetic */ BleSpenInstanceId val$targetSpenInstanceId;
        public final /* synthetic */ Transaction val$transaction;

        public AnonymousClass1(Transaction transaction, BleSpenInstanceId bleSpenInstanceId) {
            this.val$transaction = transaction;
            this.val$targetSpenInstanceId = bleSpenInstanceId;
        }

        @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
        public void onFinish(BleSpenManager.OpResultData opResultData) {
            if (!opResultData.isSuccess()) {
                String str = SensorCmdDispatcher.TAG;
                Log.e(str, "onSelfTestResult : Change to SENSOR_ON mode failed. code=" + opResultData.getResultCode());
                SensorCmdDispatcher.this.sendErrorResponse(this.val$transaction, opResultData.getResultCode().toString());
                return;
            }
            SensorCmdDispatcher.this.mBleSpenMgr.setSpenOperationMode(this.val$targetSpenInstanceId, BleSpenOperationMode.SENSOR_DEFAULT, new C00151());
        }

        /* renamed from: com.samsung.remotespen.external.SensorCmdDispatcher$1$1  reason: invalid class name and collision with other inner class name */
        /* loaded from: classes.dex */
        public class C00151 implements BleSpenManager.OpFinishListener {
            public C00151() {
            }

            @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
            public void onFinish(BleSpenManager.OpResultData opResultData) {
                if (!opResultData.isSuccess()) {
                    String str = SensorCmdDispatcher.TAG;
                    Log.e(str, "onSelfTestResult : Change to DEFAULT mode failed. code=" + opResultData.getResultCode());
                    AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                    SensorCmdDispatcher.this.sendErrorResponse(anonymousClass1.val$transaction, opResultData.getResultCode().toString());
                    return;
                }
                SensorCmdDispatcher.this.mBleSpenMgr.setSpenOperationMode(AnonymousClass1.this.val$targetSpenInstanceId, BleSpenOperationMode.SENSOR_LOW_POWER_OFF, new C00161());
            }

            /* renamed from: com.samsung.remotespen.external.SensorCmdDispatcher$1$1$1  reason: invalid class name and collision with other inner class name */
            /* loaded from: classes.dex */
            public class C00161 implements BleSpenManager.OpFinishListener {
                public C00161() {
                }

                @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                public void onFinish(BleSpenManager.OpResultData opResultData) {
                    if (!opResultData.isSuccess()) {
                        String str = SensorCmdDispatcher.TAG;
                        Log.e(str, "onSelfTestResult : Change to SENSOR_LOW_POWER_OFF mode failed. code=" + opResultData.getResultCode());
                        AnonymousClass1 anonymousClass1 = AnonymousClass1.this;
                        SensorCmdDispatcher.this.sendErrorResponse(anonymousClass1.val$transaction, opResultData.getResultCode().toString());
                        return;
                    }
                    final BleSpenManager.SensorEventListener sensorEventListener = new BleSpenManager.SensorEventListener() { // from class: com.samsung.remotespen.external.SensorCmdDispatcher.1.1.1.1
                        @Override // com.samsung.remotespen.core.device.BleSpenManager.SensorEventListener
                        public void onSpenSensorEvent(BleSpenInstanceId bleSpenInstanceId, final BleSpenSensorEvent bleSpenSensorEvent) {
                            if (bleSpenInstanceId.equals(AnonymousClass1.this.val$targetSpenInstanceId) && bleSpenSensorEvent.getSensorType() == BleSpenSensorType.ACCELEROMETER_AND_GYROSCOPE && (bleSpenSensorEvent instanceof BleSpenSelfTestResultEvent)) {
                                SensorCmdDispatcher.this.mBleSpenMgr.setSpenOperationMode(AnonymousClass1.this.val$targetSpenInstanceId, BleSpenOperationMode.SENSOR_LOW_POWER_ON, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.SensorCmdDispatcher.1.1.1.1.1
                                    @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                                    public void onFinish(BleSpenManager.OpResultData opResultData2) {
                                        if (!opResultData2.isSuccess()) {
                                            String str2 = SensorCmdDispatcher.TAG;
                                            Log.e(str2, "onSelfTestResult : Change to SENSOR_LOW_POWER_ON mode failed. code=" + opResultData2.getResultCode());
                                            AnonymousClass1 anonymousClass12 = AnonymousClass1.this;
                                            SensorCmdDispatcher.this.sendErrorResponse(anonymousClass12.val$transaction, opResultData2.getResultCode().toString());
                                            return;
                                        }
                                        AnonymousClass1 anonymousClass13 = AnonymousClass1.this;
                                        SensorCmdDispatcher.this.sendSuccessResponse(anonymousClass13.val$transaction, ((BleSpenSelfTestResultEvent) bleSpenSensorEvent).getSummaryString());
                                        SensorCmdDispatcher.this.mBleSpenMgr.unregisterSensorEventListener(this);
                                        SensorCmdDispatcher.this.mBleSpenMgr.enableSelfTestNotification(AnonymousClass1.this.val$targetSpenInstanceId, false, null);
                                    }
                                });
                            }
                        }
                    };
                    SensorCmdDispatcher.this.mBleSpenMgr.registerSensorEventListener(sensorEventListener);
                    SensorCmdDispatcher.this.mBleSpenMgr.enableSelfTestNotification(AnonymousClass1.this.val$targetSpenInstanceId, true, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.SensorCmdDispatcher.1.1.1.2
                        @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                        public void onFinish(BleSpenManager.OpResultData opResultData2) {
                            if (opResultData2.isSuccess()) {
                                return;
                            }
                            String str2 = SensorCmdDispatcher.TAG;
                            Log.e(str2, "onSelfTestResult : Failed to perform self test. code=" + opResultData2.getResultCode());
                            AnonymousClass1 anonymousClass12 = AnonymousClass1.this;
                            SensorCmdDispatcher.this.sendErrorResponse(anonymousClass12.val$transaction, opResultData2.getResultCode().toString());
                            SensorCmdDispatcher.this.mBleSpenMgr.unregisterSensorEventListener(sensorEventListener);
                        }
                    });
                }
            }
        }
    }
}
