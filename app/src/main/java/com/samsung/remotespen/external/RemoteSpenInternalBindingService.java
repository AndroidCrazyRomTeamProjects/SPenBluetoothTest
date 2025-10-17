package com.samsung.remotespen.external;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.samsung.android.sdk.penremote.AirMotionEvent;
import com.samsung.android.sdk.penremote.ButtonEvent;
import com.samsung.android.sdk.penremote.ISPenRemoteService;
import com.samsung.android.sdk.penremote.ISpenEventListener;
import com.samsung.android.sdk.penremote.SpenEvent;
import com.samsung.android.sdk.spenremote.SpenEventDispatcher;
import com.samsung.launcher.util.Constants;
import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.SpenEventDispatchChecker;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenAirMotionEvent;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenChargeState;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenOperationMode;
import com.samsung.remotespen.core.device.data.BleSpenSensorEvent;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import com.samsung.remotespen.external.CommandDispatcher;
import com.samsung.remotespen.main.RemoteSpenServiceHelper;
import com.samsung.remotespen.util.SpenInsertionEventDetector;
import com.samsung.util.permission.BluetoothPermissionPopupActivity;
import com.samsung.util.permission.PermissionUtil;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class RemoteSpenInternalBindingService extends Service {
    private static final float AIR_MOTION_SCALE = 2000.0f;
    public static final int BINDER_TYPE_AIDL_3RD_PARTY = 2;
    public static final int BINDER_TYPE_AIDL_SAMSUNG = 1;
    public static final int BINDER_TYPE_MESSENGER_SAMSUNG = 0;
    private static final long MIN_AIR_MOTION_BROADCAST_INTERVAL = 35;
    private BleSpenManager mBleSpenMgr;
    private ArrayList<CommandDispatcher> mCommandDispatcherList;
    private long mLastAirMotionBroadcastTime;
    private SpenEventDispatchChecker mSpenEventDispatchChecker;
    private SpenInsertionEventDetector mSpenInsertionEventDetector;
    public static final String TAG = RemoteSpenInternalBindingService.class.getSimpleName();
    private static String PERMISSION_ACCESS_AIRCOMMAND = Constants.PERMISSION_ACCESS_AIRCOMMAND;
    private SpenInsertionEventDetector.Listener mSpenInsertionEventListener = new SpenInsertionEventDetector.Listener() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.1
        @Override // com.samsung.remotespen.util.SpenInsertionEventDetector.Listener
        public void onInsertEvent(boolean z) {
            RemoteSpenInternalBindingService.this.onSpenInsertEvent(z);
        }
    };
    private Point mSuspendedAirMotionMovement = new Point();
    private CommandDispatcher.IDispatchEnvironment mResponseSender = new CommandDispatcher.IDispatchEnvironment() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.2
        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public void sendSuccessResponse(Transaction transaction, Object obj) {
            RemoteSpenInternalBindingService.this.sendSuccessResponse(transaction, obj);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public void sendServiceNotRunningResponse(Transaction transaction) {
            RemoteSpenInternalBindingService.this.sendServiceNotRunningResponse(transaction);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public void sendNotConnectedResponse(Transaction transaction) {
            RemoteSpenInternalBindingService.this.sendNotConnectedResponse(transaction);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public void sendBleSpenNotSupportedResponse(Transaction transaction) {
            RemoteSpenInternalBindingService.this.sendBleSpenNotSupportedResponse(transaction);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public void sendErrorResponse(Transaction transaction, String str) {
            RemoteSpenInternalBindingService.this.sendErrorResponse(transaction, str);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public boolean isRemoteSpenServiceRunning() {
            return RemoteSpenInternalBindingService.this.isRemoteSpenServiceRunning();
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public boolean startRemoteSpenService(boolean z) {
            return RemoteSpenInternalBindingService.this.startRemoteSpenService(z);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public boolean isConnectedToBleSpen(BleSpenInstanceId bleSpenInstanceId) {
            return RemoteSpenInternalBindingService.this.isConnectedToBleSpen(bleSpenInstanceId);
        }

        @Override // com.samsung.remotespen.external.CommandDispatcher.IDispatchEnvironment
        public boolean isSupportBleSpen() {
            return RemoteSpenInternalBindingService.this.mBleSpenMgr != null;
        }
    };
    public final SpenEventDispatcher.Stub mExternalPenBinder = new SpenEventDispatcher.Stub() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.3
        @Override // com.samsung.android.sdk.spenremote.SpenEventDispatcher
        public void dispatchEvent(int i, SpenEvent spenEvent) {
            if (RemoteSpenInternalBindingService.this.checkPermission(RemoteSpenInternalBindingService.PERMISSION_ACCESS_AIRCOMMAND)) {
                if (i == 0) {
                    ButtonEvent buttonEvent = new ButtonEvent(spenEvent);
                    String str = RemoteSpenInternalBindingService.TAG;
                    Log.v(str, "Button Event : delayed time = " + (SystemClock.elapsedRealtime() - buttonEvent.getTimeStamp()) + ", action = " + buttonEvent.getAction());
                } else if (i == 1) {
                    AirMotionEvent airMotionEvent = new AirMotionEvent(spenEvent);
                    String str2 = RemoteSpenInternalBindingService.TAG;
                    Log.v(str2, "Motion Event : delayed time = " + (SystemClock.elapsedRealtime() - airMotionEvent.getTimeStamp()) + ", x = " + airMotionEvent.getDeltaX() + ", y = " + airMotionEvent.getDeltaY());
                }
            }
        }
    };
    private final AliveRemoteCallbackList<ISpenEventListener> mButtonEventListener = new AliveRemoteCallbackList<>("Button");
    private final AliveRemoteCallbackList<ISpenEventListener> mAirMotionEventListener = new AliveRemoteCallbackList<>("AirMotion");
    public final ISPenRemoteService.Stub m3rdPartyBinder = new ISPenRemoteService.Stub() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.4
        @Override // com.samsung.android.sdk.penremote.ISPenRemoteService
        public void registerSpenEventListener(int i, ISpenEventListener iSpenEventListener) {
            if (RemoteSpenInternalBindingService.this.mBleSpenMgr != null) {
                boolean z = !RemoteSpenInternalBindingService.this.is3rdPartyReceiveSpenEvent();
                if (i == 0) {
                    RemoteSpenInternalBindingService.this.mButtonEventListener.register((AliveRemoteCallbackList) iSpenEventListener, getCallerAppPackageName());
                    Log.i(RemoteSpenInternalBindingService.TAG, "registerSpenEventListener : registered button callback count = " + RemoteSpenInternalBindingService.this.mButtonEventListener.getRegisteredCallbackCount() + ", " + getCallerInformation());
                } else if (i == 1) {
                    boolean z2 = (RemoteSpenInternalBindingService.this.is3rdPartyReceiveAirMotionEvent() || RemoteSpenInternalBindingService.this.mSpenInsertionEventDetector.isInserted()) ? false : true;
                    if (RemoteSpenInternalBindingService.this.mAirMotionEventListener.register((AliveRemoteCallbackList) iSpenEventListener, getCallerAppPackageName()) && z2) {
                        RemoteSpenInternalBindingService.this.motionSensorAlwaysOn(true);
                    }
                    Log.i(RemoteSpenInternalBindingService.TAG, "registerSpenEventListener : registered air motion callback count = " + RemoteSpenInternalBindingService.this.mAirMotionEventListener.getRegisteredCallbackCount() + ", " + getCallerInformation());
                }
                if (z && RemoteSpenInternalBindingService.this.is3rdPartyReceiveSpenEvent()) {
                    RemoteSpenInternalBindingService.this.mBleSpenMgr.registerSensorEventListener(RemoteSpenInternalBindingService.this.mSensorEventListener);
                    return;
                }
                return;
            }
            Log.e(RemoteSpenInternalBindingService.TAG, "registerSpenEventListener : Not supports BLE SPen");
        }

        @Override // com.samsung.android.sdk.penremote.ISPenRemoteService
        public void unregisterSpenEventListener(int i, ISpenEventListener iSpenEventListener) {
            if (RemoteSpenInternalBindingService.this.mBleSpenMgr != null) {
                if (i == 0) {
                    RemoteSpenInternalBindingService.this.mButtonEventListener.unregister(iSpenEventListener);
                    String str = RemoteSpenInternalBindingService.TAG;
                    Log.i(str, "unregisterSpenEventListener : registered button callback count = " + RemoteSpenInternalBindingService.this.mButtonEventListener.getRegisteredCallbackCount() + ", " + getCallerInformation());
                } else if (i == 1) {
                    RemoteSpenInternalBindingService.this.mAirMotionEventListener.unregister(iSpenEventListener);
                    if (!RemoteSpenInternalBindingService.this.is3rdPartyReceiveAirMotionEvent()) {
                        RemoteSpenInternalBindingService.this.motionSensorAlwaysOn(false);
                    }
                    String str2 = RemoteSpenInternalBindingService.TAG;
                    Log.i(str2, "unregisterSpenEventListener : registered air motion callback count = " + RemoteSpenInternalBindingService.this.mAirMotionEventListener.getRegisteredCallbackCount() + ", " + getCallerInformation());
                }
                if (RemoteSpenInternalBindingService.this.is3rdPartyReceiveSpenEvent()) {
                    return;
                }
                Log.i(RemoteSpenInternalBindingService.TAG, "unregisterSpenEventListener : all listeners are unregistered. UnregisterSensorEventListener");
                RemoteSpenInternalBindingService.this.mBleSpenMgr.unregisterSensorEventListener(RemoteSpenInternalBindingService.this.mSensorEventListener);
                return;
            }
            Log.e(RemoteSpenInternalBindingService.TAG, "unregisterSpenEventListener : Not supports BLE SPen");
        }

        private String getCallerInformation() {
            int callingPid = Binder.getCallingPid();
            return "PID=" + callingPid + ", PKG=" + getCallerAppPackageName();
        }

        private String getCallerAppPackageName() {
            return RemoteSpenInternalBindingService.this.getPackageManager().getNameForUid(Binder.getCallingUid());
        }
    };
    private BleSpenManager.SensorEventListener mSensorEventListener = new BleSpenManager.SensorEventListener() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.5
        @Override // com.samsung.remotespen.core.device.BleSpenManager.SensorEventListener
        public void onSpenSensorEvent(BleSpenInstanceId bleSpenInstanceId, BleSpenSensorEvent bleSpenSensorEvent) {
            int i = AnonymousClass10.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[bleSpenSensorEvent.getSensorType().ordinal()];
            if (i == 1) {
                BleSpenButtonEvent bleSpenButtonEvent = (BleSpenButtonEvent) bleSpenSensorEvent;
                SpenEvent spenEvent = new SpenEvent(2);
                if (bleSpenButtonEvent.getAction() != BleSpenButtonEvent.Action.BUTTON_DOWN) {
                    if (bleSpenButtonEvent.getAction() != BleSpenButtonEvent.Action.BUTTON_UP) {
                        if (bleSpenButtonEvent.getAction() == BleSpenButtonEvent.Action.SINGLE_CLICKED) {
                            r2 = 100.0f;
                        } else if (bleSpenButtonEvent.getAction() == BleSpenButtonEvent.Action.DOUBLE_CLICKED) {
                            r2 = 101.0f;
                        }
                    }
                    spenEvent.setValue(0, r2);
                    spenEvent.setTimeStamp(bleSpenSensorEvent.getEventTime());
                    RemoteSpenInternalBindingService remoteSpenInternalBindingService = RemoteSpenInternalBindingService.this;
                    remoteSpenInternalBindingService.broadcastSpenEventTo3rdPartyBinder(remoteSpenInternalBindingService.mButtonEventListener, spenEvent);
                    RemoteSpenInternalBindingService.this.mLastAirMotionBroadcastTime = 0L;
                    RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.set(0, 0);
                }
                r2 = 0.0f;
                spenEvent.setValue(0, r2);
                spenEvent.setTimeStamp(bleSpenSensorEvent.getEventTime());
                RemoteSpenInternalBindingService remoteSpenInternalBindingService2 = RemoteSpenInternalBindingService.this;
                remoteSpenInternalBindingService2.broadcastSpenEventTo3rdPartyBinder(remoteSpenInternalBindingService2.mButtonEventListener, spenEvent);
                RemoteSpenInternalBindingService.this.mLastAirMotionBroadcastTime = 0L;
                RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.set(0, 0);
            } else if (i != 2) {
            } else {
                BleSpenAirMotionEvent bleSpenAirMotionEvent = (BleSpenAirMotionEvent) bleSpenSensorEvent;
                long elapsedRealtime = SystemClock.elapsedRealtime();
                int x = bleSpenAirMotionEvent.getX();
                int y = bleSpenAirMotionEvent.getY();
                if (elapsedRealtime - RemoteSpenInternalBindingService.this.mLastAirMotionBroadcastTime < RemoteSpenInternalBindingService.MIN_AIR_MOTION_BROADCAST_INTERVAL) {
                    RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.offset(x, y);
                    return;
                }
                float f = (x + RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.x) / RemoteSpenInternalBindingService.AIR_MOTION_SCALE;
                float f2 = (-(y + RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.y)) / RemoteSpenInternalBindingService.AIR_MOTION_SCALE;
                if (f > 1.0f) {
                    f = 1.0f;
                } else if (f < -1.0f) {
                    f = -1.0f;
                }
                r2 = f2 <= 1.0f ? f2 < -1.0f ? -1.0f : f2 : 1.0f;
                if (Math.abs(f) > 1.0E-5f || Math.abs(r2) > 1.0E-5f) {
                    SpenEvent spenEvent2 = new SpenEvent(2);
                    spenEvent2.setTimeStamp(bleSpenSensorEvent.getEventTime());
                    spenEvent2.setValue(0, f);
                    spenEvent2.setValue(1, r2);
                    RemoteSpenInternalBindingService remoteSpenInternalBindingService3 = RemoteSpenInternalBindingService.this;
                    remoteSpenInternalBindingService3.broadcastSpenEventTo3rdPartyBinder(remoteSpenInternalBindingService3.mAirMotionEventListener, spenEvent2);
                    RemoteSpenInternalBindingService.this.mLastAirMotionBroadcastTime = elapsedRealtime;
                    RemoteSpenInternalBindingService.this.mSuspendedAirMotionMovement.set(0, 0);
                }
            }
        }
    };
    public Messenger mMessenger = new Messenger(new Handler() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.8
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            RemoteSpenInternalBindingService.this.dispatchRequest(message);
        }
    });

    /* renamed from: com.samsung.remotespen.external.RemoteSpenInternalBindingService$10  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass10 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType;

        static {
            int[] iArr = new int[BleSpenSensorType.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType = iArr;
            try {
                iArr[BleSpenSensorType.BUTTON.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$device$data$BleSpenSensorType[BleSpenSensorType.AIR_GESTURE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastSpenEventTo3rdPartyBinder(RemoteCallbackList<ISpenEventListener> remoteCallbackList, SpenEvent spenEvent) {
        int beginBroadcast = remoteCallbackList.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                remoteCallbackList.getBroadcastItem(i).onEvent(spenEvent);
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "broadcastSpenEventTo3rdPartyBinder : e=" + e);
            }
        }
        remoteCallbackList.finishBroadcast();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSpenInsertEvent(boolean z) {
        if (is3rdPartyReceiveAirMotionEvent()) {
            if (z) {
                motionSensorAlwaysOn(false);
            } else {
                motionSensorAlwaysOn(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean is3rdPartyReceiveAirMotionEvent() {
        return !this.mAirMotionEventListener.isEmpty();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void motionSensorAlwaysOn(boolean z) {
        BleSpenManager bleSpenManager = this.mBleSpenMgr;
        if (bleSpenManager == null) {
            return;
        }
        if (!bleSpenManager.isAnySpenConnected()) {
            Log.d(TAG, "motionSensorAlwaysOn: Connected Spen is not exist.");
            return;
        }
        Iterator<BleSpenInstanceId> it = BleSpenPairedSpenManager.getInstance(this).getAvailableSpenInstanceIds().iterator();
        while (it.hasNext()) {
            BleSpenInstanceId next = it.next();
            if (z) {
                this.mBleSpenMgr.setSpenOperationMode(next, BleSpenOperationMode.SENSOR_ON, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.6
                    @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                    public void onFinish(BleSpenManager.OpResultData opResultData) {
                        String str = RemoteSpenInternalBindingService.TAG;
                        Log.d(str, "setSpenOperationMode SENSOR_ON : result=" + opResultData.getResultCode());
                    }
                });
            } else {
                this.mBleSpenMgr.setSpenOperationMode(next, BleSpenOperationMode.SENSOR_DEFAULT, new BleSpenManager.OpFinishListener() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.7
                    @Override // com.samsung.remotespen.core.device.BleSpenManager.OpFinishListener
                    public void onFinish(BleSpenManager.OpResultData opResultData) {
                        String str = RemoteSpenInternalBindingService.TAG;
                        Log.d(str, "setSpenOperationMode DEFAULT : result=" + opResultData.getResultCode());
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean checkPermission(String str) {
        boolean z = checkCallingPermission(str) == 0;
        if (!z) {
            String str2 = TAG;
            Log.e(str2, "checkPermission : Requires " + str + " permission. caller PID=" + Binder.getCallingPid() + " UID=" + Binder.getCallingUid());
        }
        return z;
    }

    @Override // android.app.Service
    public void onCreate() {
        String str = TAG;
        Log.i(str, "onCreate");
        super.onCreate();
        this.mCommandDispatcherList = new ArrayList<>();
        BleSpenManager bleSpenManager = BleSpenManager.getInstance(this);
        this.mBleSpenMgr = bleSpenManager;
        if (bleSpenManager == null) {
            Log.e(str, "onCreate : BLE SPen is not supported!");
            return;
        }
        SpenEventDispatchChecker spenEventDispatchChecker = bleSpenManager.getSpenEventDispatchChecker();
        this.mSpenEventDispatchChecker = spenEventDispatchChecker;
        spenEventDispatchChecker.setSdkStateListener(new SpenEventDispatchChecker.SdkStateListener() { // from class: com.samsung.remotespen.external.RemoteSpenInternalBindingService.9
            @Override // com.samsung.remotespen.core.device.SpenEventDispatchChecker.SdkStateListener
            public boolean isReceivesSpenEventViaSdk(String str2) {
                return RemoteSpenInternalBindingService.this.isReceivesSpenEventViaSdk(str2);
            }
        });
        SpenInsertionEventDetector spenInsertionEventDetector = SpenInsertionEventDetector.getInstance(getBaseContext());
        this.mSpenInsertionEventDetector = spenInsertionEventDetector;
        spenInsertionEventDetector.registerListener(this.mSpenInsertionEventListener);
        this.mCommandDispatcherList.add(new BatteryCmdDispatcher(this, this.mBleSpenMgr, this.mResponseSender));
        this.mCommandDispatcherList.add(new DeviceCmdDispatcher(this, this.mBleSpenMgr, this.mResponseSender));
        this.mCommandDispatcherList.add(new ConnectionCmdDispatcher(this, this.mBleSpenMgr, this.mResponseSender));
        this.mCommandDispatcherList.add(new SensorCmdDispatcher(this, this.mBleSpenMgr, this.mResponseSender));
        this.mCommandDispatcherList.add(new CommonCmdDispatcher(this, this.mBleSpenMgr, this.mResponseSender));
        Iterator<CommandDispatcher> it = this.mCommandDispatcherList.iterator();
        while (it.hasNext()) {
            it.next().init();
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    @Override // android.app.Service
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        SpenInsertionEventDetector spenInsertionEventDetector = this.mSpenInsertionEventDetector;
        if (spenInsertionEventDetector != null) {
            spenInsertionEventDetector.unregisterListener(this.mSpenInsertionEventListener);
        }
        SpenEventDispatchChecker spenEventDispatchChecker = this.mSpenEventDispatchChecker;
        if (spenEventDispatchChecker != null) {
            spenEventDispatchChecker.setSdkStateListener(null);
        }
        Iterator<CommandDispatcher> it = this.mCommandDispatcherList.iterator();
        while (it.hasNext()) {
            it.next().release();
        }
        PermissionUtil.sendBroadcastForClose(this, BluetoothPermissionPopupActivity.class.getSimpleName());
        Log.i(TAG, "sendBroadcastForClose");
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean is3rdPartyReceiveSpenEvent() {
        return (this.mAirMotionEventListener.isEmpty() && this.mButtonEventListener.isEmpty()) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isReceivesSpenEventViaSdk(String str) {
        return this.mAirMotionEventListener.contains(str) || this.mButtonEventListener.contains(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchRequest(Message message) {
        Transaction transaction = new Transaction(message);
        if (transaction.mCommand == null) {
            Log.e(TAG, "dispatchRequest : CMD is null!");
            return;
        }
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("dispatchRequest : cmd=");
        sb.append(transaction.mCommand);
        sb.append(", reqId=");
        sb.append(transaction.mRequestId);
        sb.append(", reqReply=");
        boolean z = false;
        sb.append(transaction.mReplyTo != null);
        Log.i(str, sb.toString());
        if (!(transaction.mCommand instanceof String)) {
            Log.e(str, "dispatchRequest : cmd is not string type!");
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_UNSUPPORTED_CMD);
            return;
        }
        int i = message.sendingUid;
        if (!(checkPermission(PERMISSION_ACCESS_AIRCOMMAND, 0, i) == 0)) {
            Log.e(str, "checkPermission : Requires " + PERMISSION_ACCESS_AIRCOMMAND + " permission. UID=" + i + " pkg=" + getPackageManager().getNameForUid(i));
            sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_PERMISSION_REQUIRED);
            return;
        }
        Iterator<CommandDispatcher> it = this.mCommandDispatcherList.iterator();
        while (it.hasNext() && !(z = it.next().dispatchCommand(transaction))) {
        }
        if (z) {
            return;
        }
        String str2 = TAG;
        Log.e(str2, "dispatchRequest : Unsupported cmd : " + transaction.mCommand);
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_UNSUPPORTED_CMD);
    }

    private void putValueToBundle(Bundle bundle, String str, Object obj) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Integer) {
            bundle.putInt(str, ((Integer) obj).intValue());
        } else if (obj instanceof Boolean) {
            bundle.putBoolean(str, ((Boolean) obj).booleanValue());
        } else if (obj instanceof String) {
            bundle.putString(str, (String) obj);
        } else if (obj instanceof Bundle) {
            bundle.putBundle(str, (Bundle) obj);
        } else if (obj instanceof ArrayList) {
            bundle.putParcelableArrayList(str, (ArrayList) obj);
        } else if (obj instanceof BleSpenChargeState) {
            bundle.putString(str, String.valueOf(obj));
        } else {
            String str2 = TAG;
            Log.e(str2, "putValueToBundle : unexpected value type. value=" + obj + ", key=" + str);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendSuccessResponse(Transaction transaction, Object obj) {
        transaction.setSuccessResult(obj);
        sendResponse(transaction);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendServiceNotRunningResponse(Transaction transaction) {
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_SERVICE_NOT_RUNNING);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendNotConnectedResponse(Transaction transaction) {
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_NOT_CONNECTED);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendBleSpenNotSupportedResponse(Transaction transaction) {
        sendErrorResponse(transaction, BindingApiConstants.ERR_MSG_BLE_SPEN_NOT_SUPPORTED);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendErrorResponse(Transaction transaction, String str) {
        transaction.setErrorResult(str);
        sendResponse(transaction);
    }

    private void sendResponse(Transaction transaction) {
        String str = TAG;
        Log.i(str, "sendResponse : reqId=" + transaction.mRequestId + " cmd=" + transaction.mCommand + " value=" + transaction.mResultValue + " msg=" + transaction.mErrorMsg);
        if (transaction.mReplyTo == null) {
            Log.e(str, "sendResponse : replyTo is null");
        } else if (transaction.mIsSuccess == null) {
            Log.e(str, "sendResponse : success state is null");
        } else {
            Message obtain = Message.obtain();
            Bundle bundle = new Bundle();
            putValueToBundle(bundle, BindingApiConstants.BUNDLE_KEY_IS_SUCCESS, transaction.mIsSuccess);
            putValueToBundle(bundle, BindingApiConstants.BUNDLE_KEY_REQUEST_ID, transaction.mRequestId);
            putValueToBundle(bundle, BindingApiConstants.BUNDLE_KEY_RESULT_VALUE, transaction.mResultValue);
            putValueToBundle(bundle, BindingApiConstants.BUNDLE_KEY_ERROR_MSG, transaction.mErrorMsg);
            obtain.setData(bundle);
            try {
                transaction.mReplyTo.send(obtain);
            } catch (RemoteException e) {
                String str2 = TAG;
                Log.e(str2, "sendResponse : e=" + e, e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isRemoteSpenServiceRunning() {
        return RemoteSpenServiceHelper.isServiceRunning();
    }

    public boolean startRemoteSpenService(boolean z) {
        return RemoteSpenServiceHelper.startService(this, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isConnectedToBleSpen(BleSpenInstanceId bleSpenInstanceId) {
        BleSpenManager bleSpenManager = this.mBleSpenMgr;
        return bleSpenManager != null && bleSpenManager.getConnectionState(bleSpenInstanceId) == BleSpenManager.ConnectionState.CONNECTED;
    }
}
