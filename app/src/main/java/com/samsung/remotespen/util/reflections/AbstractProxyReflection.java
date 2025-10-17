package com.samsung.remotespen.util.reflections;

import com.google.dexmaker.stock.ProxyBuilder;
import com.samsung.debug.Log;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.commons.lang.builder.HashCodeBuilder;

/* loaded from: classes.dex */
public abstract class AbstractProxyReflection {
    public static final String TAG = "AbstractProxyReflection";
    public Class<?> mBaseClass;
    public String mClassName;
    public Object mProxyInstance;

    /* loaded from: classes.dex */
    public class InvocationHooker implements InvocationHandler {
        public InvocationHooker() {
        }

        @Override // java.lang.reflect.InvocationHandler
        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            if ("hashCode".equals(method.getName())) {
                return Integer.valueOf(AbstractProxyReflection.this.getHashCode());
            }
            if ("equals".equals(method.getName())) {
                return Boolean.TRUE;
            }
            return AbstractProxyReflection.this.invokeInternal(obj, method, objArr);
        }
    }

    public AbstractProxyReflection(String str) {
        this(str, new Class[0], new Object[0]);
    }

    public AbstractProxyReflection(String str, Class<?>[] clsArr, Object[] objArr) {
        this.mBaseClass = null;
        this.mProxyInstance = null;
        this.mClassName = str;
        try {
            this.mBaseClass = Class.forName(str);
        } catch (ClassNotFoundException e) {
            PrintStream printStream = System.err;
            printStream.println("AbstractProxyReflection Unable to instantiate class " + e);
        }
        Class<?> cls = this.mBaseClass;
        if (cls == null) {
            Log.d(TAG, "There's no " + this.mClassName);
            return;
        }
        try {
            try {
                this.mProxyInstance = Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{this.mBaseClass}, new InvocationHooker());
            } catch (IOException e2) {
                Log.e(TAG, "Occur IOException during build proxy instance : " + e2.toString());
            }
        } catch (Exception unused) {
            this.mProxyInstance = ProxyBuilder.forClass(this.mBaseClass).constructorArgTypes(clsArr).constructorArgValues(objArr).handler(new InvocationHooker()).build();
            Log.i(TAG, "Create proxy instance for concrete class : " + this.mClassName);
        }
    }

    public Class<?> getClassType() {
        return this.mBaseClass;
    }

    public Object getProxyInstance() {
        return this.mProxyInstance;
    }

    public Object invokeInternal(Object obj, Method method, Object[] objArr) {
        try {
            return ProxyBuilder.callSuper(obj, method, objArr);
        } catch (Throwable th) {
            Log.e(TAG, " invokeInternal : e=" + th.toString());
            return null;
        }
    }

    public int getHashCode() {
        Log.i(TAG, "Create reflection hash code : " + this.mClassName);
        return HashCodeBuilder.reflectionHashCode(this.mProxyInstance);
    }
}
