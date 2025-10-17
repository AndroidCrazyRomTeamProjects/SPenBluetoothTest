package com.samsung.remotespen.util.reflections;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import com.samsung.util.ReflectionUtils;
import com.samsung.util.debug.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/* loaded from: classes.dex */
public class BleSpenChargeLockStateChangedListenerReflection extends Binder implements IInterface, InvocationHandler {
    private static final String DESCRIPTOR = "com.samsung.android.content.smartclip.IBleSpenChargeLockStateChangedListener";
    private static final String TAG = BleSpenChargeLockStateChangedListenerReflection.class.getSimpleName();
    private static final int TRANSACTION_onChanged = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.samsung.remotespen.util.reflections.BleSpenChargeLockStateChangedListenerReflection.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1 && BleSpenChargeLockStateChangedListenerReflection.this.mListener != null) {
                BleSpenChargeLockStateChangedListenerReflection.this.mListener.onChanged(((Boolean) message.obj).booleanValue());
            }
            super.handleMessage(message);
        }
    };
    private ChargeLockStateChangedListener mListener;

    /* loaded from: classes.dex */
    public interface ChargeLockStateChangedListener {
        void onChanged(boolean z);
    }

    @Override // android.os.IInterface
    public IBinder asBinder() {
        return this;
    }

    private BleSpenChargeLockStateChangedListenerReflection(ChargeLockStateChangedListener chargeLockStateChangedListener) {
        attachInterface(this, DESCRIPTOR);
        this.mListener = chargeLockStateChangedListener;
    }

    public static Object newInstance(ChargeLockStateChangedListener chargeLockStateChangedListener) {
        Class<?> processObserverStubClass = getProcessObserverStubClass();
        if (processObserverStubClass != null) {
            return Proxy.newProxyInstance(processObserverStubClass.getClassLoader(), processObserverStubClass.getInterfaces(), new BleSpenChargeLockStateChangedListenerReflection(chargeLockStateChangedListener));
        }
        return null;
    }

    private static Class<?> getProcessObserverStubClass() {
        try {
            return ReflectionUtils.getClassByName("com.samsung.android.content.smartclip.IBleSpenChargeLockStateChangedListener$Stub");
        } catch (ClassNotFoundException e) {
            String str = TAG;
            Log.e(str, e.toString());
            Log.d(str, "failed getProcessObserverStubClass");
            return null;
        }
    }

    @Override // java.lang.reflect.InvocationHandler
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        String name = method.getName();
        String str = TAG;
        Log.d(str, "invoke : method=" + name);
        if (name.equals("asBinder")) {
            return asBinder();
        }
        return null;
    }

    @Override // android.os.Binder
    public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
        String str = TAG;
        Log.d(str, "onTransact : code = " + i);
        if (i == 1) {
            parcel.enforceInterface(DESCRIPTOR);
            Message.obtain(this.mHandler, 1, Boolean.valueOf(parcel.readInt() > 0)).sendToTarget();
            return true;
        } else if (i == 1598968902) {
            parcel2.writeString(DESCRIPTOR);
            return true;
        } else {
            return super.onTransact(i, parcel, parcel2, i2);
        }
    }
}
