package com.samsung.remotespen.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.samsung.remotespen.core.device.BleSpenManager;
import com.samsung.remotespen.core.device.control.BleSpenPairedSpenManager;
import com.samsung.remotespen.core.device.data.BleSpenButtonEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureEvent;
import com.samsung.remotespen.core.device.data.BleSpenGestureType;
import com.samsung.remotespen.core.device.data.BleSpenInstanceId;
import com.samsung.remotespen.core.device.data.BleSpenSensorId;
import com.samsung.remotespen.core.device.data.BleSpenSensorType;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerCategory;
import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class VirtualAirActionEventReceiver {
    private static final String ACTION_INJECT_VIRTUAL_SPEN_AIR_ACTION = "com.samsung.action.INJECT_VIRTUAL_AIR_ACTION";
    private static final String KEY_VIRTUAL_SPEN_AIR_ACTION_TYPE = "air_action_type";
    private static final String PERMISSION_VIRTUAL_SPEN_AIR_ACTION = "com.samsung.permission.ACCESS_AIRCOMMAND";
    private static final String TAG = "VirtualAirActionEventReceiver";
    private Context mContext;
    private BleSpenManager.EventListener mListener;
    private BroadcastReceiver mVirtualSpenButtonReceiver = new BroadcastReceiver() { // from class: com.samsung.remotespen.main.VirtualAirActionEventReceiver.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            BleSpenButtonEvent.Action action = BleSpenButtonEvent.Action.SINGLE_CLICKED;
            Object[] objArr = {PenActionTriggerType.SINGLE_CLICK, action};
            BleSpenButtonEvent.Action action2 = BleSpenButtonEvent.Action.DOUBLE_CLICKED;
            Object[] objArr2 = {PenActionTriggerType.DOUBLE_CLICK, action2};
            PenActionTriggerType penActionTriggerType = PenActionTriggerType.LONG_CLICK;
            BleSpenButtonEvent.Action action3 = BleSpenButtonEvent.Action.LONG_CLICK_STARTED;
            Object[] objArr3 = {penActionTriggerType, action3};
            BleSpenButtonEvent.Action action4 = BleSpenButtonEvent.Action.LONG_CLICK_FINISHED;
            Object[] objArr4 = {PenActionTriggerType.SECONDARY_SINGLE_CLICK, action};
            Object[] objArr5 = {PenActionTriggerType.SECONDARY_DOUBLE_CLICK, action2};
            PenActionTriggerType penActionTriggerType2 = PenActionTriggerType.SECONDARY_LONG_CLICK;
            Object[][] objArr6 = {objArr, objArr2, objArr3, new Object[]{penActionTriggerType, action4}, objArr4, objArr5, new Object[]{penActionTriggerType2, action3}, new Object[]{penActionTriggerType2, action4}, new Object[]{PenActionTriggerType.GESTURE_UNKNOWN, BleSpenGestureType.UNKNOWN}, new Object[]{PenActionTriggerType.GESTURE_UP, BleSpenGestureType.SWIPE_UP}, new Object[]{PenActionTriggerType.GESTURE_DOWN, BleSpenGestureType.SWIPE_DOWN}, new Object[]{PenActionTriggerType.GESTURE_LEFT, BleSpenGestureType.SWIPE_LEFT}, new Object[]{PenActionTriggerType.GESTURE_RIGHT, BleSpenGestureType.SWIPE_RIGHT}, new Object[]{PenActionTriggerType.GESTURE_SHAKE, BleSpenGestureType.SHAKE}, new Object[]{PenActionTriggerType.GESTURE_CIRCLE_CW, BleSpenGestureType.CIRCLE_CW}, new Object[]{PenActionTriggerType.GESTURE_CIRCLE_CCW, BleSpenGestureType.CIRCLE_CCW}, new Object[]{PenActionTriggerType.GESTURE_POINTY_UP, BleSpenGestureType.POINTY_UP}, new Object[]{PenActionTriggerType.GESTURE_POINTY_DOWN, BleSpenGestureType.POINTY_DOWN}, new Object[]{PenActionTriggerType.GESTURE_POINTY_LEFT, BleSpenGestureType.POINTY_LEFT}, new Object[]{PenActionTriggerType.GESTURE_POINTY_RIGHT, BleSpenGestureType.POINTY_RIGHT}};
            String action5 = intent.getAction();
            if (!VirtualAirActionEventReceiver.ACTION_INJECT_VIRTUAL_SPEN_AIR_ACTION.equals(action5)) {
                Log.e(VirtualAirActionEventReceiver.TAG, "VirtualAirActionEventReceiver : unexpected action : " + action5);
            } else if (VirtualAirActionEventReceiver.this.mListener != null) {
                String stringExtra = intent.getStringExtra(VirtualAirActionEventReceiver.KEY_VIRTUAL_SPEN_AIR_ACTION_TYPE);
                Log.v(VirtualAirActionEventReceiver.TAG, "VirtualAirActionEventReceiver : airActionType = " + stringExtra);
                try {
                    PenActionTriggerType valueOf = PenActionTriggerType.valueOf(stringExtra);
                    boolean z = false;
                    for (int i = 0; i < 20; i++) {
                        Object[] objArr7 = objArr6[i];
                        if (objArr7[0].equals(valueOf)) {
                            int i2 = AnonymousClass2.$SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerCategory[valueOf.getCategory().ordinal()];
                            if (i2 == 1) {
                                ArrayList<BleSpenInstanceId> availableSpenInstanceIds = BleSpenPairedSpenManager.getInstance(VirtualAirActionEventReceiver.this.mContext).getAvailableSpenInstanceIds();
                                if (availableSpenInstanceIds.size() > 0) {
                                    VirtualAirActionEventReceiver.this.mListener.onButtonEvent(availableSpenInstanceIds.get(0), new BleSpenButtonEvent((BleSpenButtonEvent.Action) objArr7[1], System.currentTimeMillis(), BleSpenSensorType.BUTTON, VirtualAirActionEventReceiver.this.isSecondaryButton(objArr7[0]) ? BleSpenSensorId.SECONDARY_BUTTON : BleSpenSensorId.PRIMARY_BUTTON, null));
                                    z = true;
                                }
                            } else if (i2 == 2) {
                                ArrayList<BleSpenInstanceId> availableSpenInstanceIds2 = BleSpenPairedSpenManager.getInstance(VirtualAirActionEventReceiver.this.mContext).getAvailableSpenInstanceIds();
                                if (availableSpenInstanceIds2.size() > 0) {
                                    BleSpenGestureEvent bleSpenGestureEvent = new BleSpenGestureEvent();
                                    bleSpenGestureEvent.setGestureStartTime(System.currentTimeMillis());
                                    bleSpenGestureEvent.setGestureType((BleSpenGestureType) objArr7[1]);
                                    VirtualAirActionEventReceiver.this.mListener.onAirGestureActionEvent(availableSpenInstanceIds2.get(0), bleSpenGestureEvent);
                                    z = true;
                                }
                            }
                        }
                    }
                    if (z) {
                        return;
                    }
                    Log.e(VirtualAirActionEventReceiver.TAG, "VirtualAirActionEventReceiver : no mappingTable action. triggerType = " + valueOf + ", category = " + valueOf.getCategory());
                } catch (IllegalArgumentException e) {
                    Log.e(VirtualAirActionEventReceiver.TAG, "VirtualAirActionEventReceiver : Failed to get PenActionTriggerType ", e);
                }
            }
        }
    };

    /* renamed from: com.samsung.remotespen.main.VirtualAirActionEventReceiver$2  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass2 {
        public static final /* synthetic */ int[] $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerCategory;

        static {
            int[] iArr = new int[PenActionTriggerCategory.values().length];
            $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerCategory = iArr;
            try {
                iArr[PenActionTriggerCategory.BUTTON.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$samsung$android$service$aircommand$remotespen$core$penaction$trigger$PenActionTriggerCategory[PenActionTriggerCategory.GESTURE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public VirtualAirActionEventReceiver(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void registerReceiver(BleSpenManager.EventListener eventListener) {
        this.mListener = eventListener;
        this.mContext.registerReceiver(this.mVirtualSpenButtonReceiver, new IntentFilter(ACTION_INJECT_VIRTUAL_SPEN_AIR_ACTION), "com.samsung.permission.ACCESS_AIRCOMMAND", null);
    }

    public void unregisterReceiver() {
        this.mContext.unregisterReceiver(this.mVirtualSpenButtonReceiver);
        this.mListener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSecondaryButton(Object obj) {
        return obj.equals(PenActionTriggerType.SECONDARY_SINGLE_CLICK) || obj.equals(PenActionTriggerType.SECONDARY_DOUBLE_CLICK) || obj.equals(PenActionTriggerType.SECONDARY_LONG_CLICK);
    }
}
