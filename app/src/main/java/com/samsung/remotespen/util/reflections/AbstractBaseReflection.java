package com.samsung.remotespen.util.reflections;

//import com.samsung.aboutpage.Constants;
import com.samsung.util.debug.Log;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/* loaded from: classes.dex */
public abstract class AbstractBaseReflection {
    private static final String TAG = "AbstractBaseReflection";
    public Class<?> mBaseClass = null;
    private ArrayList<String> mNameList = new ArrayList<>();
    private ArrayList<Object> mReflectionList = new ArrayList<>();
    private HashMap<String, Class<?>> mClassMap = new HashMap<>();

    public abstract String getBaseClassName();

    public void loadStaticFields() {
    }

    public AbstractBaseReflection() {
        loadReflection();
    }

    public AbstractBaseReflection(String str) {
        loadReflection(str);
    }

    public AbstractBaseReflection(Class<?> cls) {
        loadReflection(cls);
    }

    public void loadReflection() {
        loadReflection(getBaseClassName());
    }

    public void loadReflection(String str) {
        loadReflection(getClass(str));
    }

    public void loadReflection(Class<?> cls) {
        this.mBaseClass = cls;
        if (cls == null) {
            Log.d(TAG, "There's no class.");
        } else {
            loadStaticFields();
        }
    }

    public Class<?> getClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(str + " Unable to load class " + e);
            return null;
        }
    }

    public Class<?> loadClassIfNeeded(String str) {
        Class<?> cls = this.mClassMap.get(str);
        if (cls == null && (cls = getClass(str)) != null) {
            this.mClassMap.put(str, cls);
        }
        return cls;
    }

    private Object getReflectionInstance(String str) {
        synchronized (this.mNameList) {
            if (str == null) {
                return null;
            }
            int size = this.mNameList.size();
            for (int i = 0; i < size; i++) {
                String str2 = this.mNameList.get(i);
                int length = str2.length();
                if (length == str.length()) {
                    int i2 = length - 1;
                    char[] charArray = str2.toCharArray();
                    char[] charArray2 = str.toCharArray();
                    for (int i3 = 0; i3 < length && (charArray[i3] & charArray2[i3]) == charArray[i3]; i3++) {
                        if (i3 == i2) {
                            return this.mReflectionList.get(i);
                        }
                    }
                }
            }
            return null;
        }
    }

    private void addReflectionInstance(String str, Object obj) {
        synchronized (this.mNameList) {
            this.mNameList.add(str);
            this.mReflectionList.add(obj);
        }
    }

    public Constructor loadConstructorIfNeeded(Class<?>[] clsArr) {
        String uniqueConstructorName = getUniqueConstructorName(clsArr);
        Object reflectionInstance = getReflectionInstance(uniqueConstructorName);
        if (reflectionInstance != null) {
            return (Constructor) reflectionInstance;
        }
        Constructor<?> constructor = null;
        if (this.mBaseClass != null && uniqueConstructorName != null && !uniqueConstructorName.isEmpty()) {
            if (clsArr == null) {
                clsArr = new Class[0];
            }
            try {
                try {
                    Constructor<?> constructor2 = this.mBaseClass.getConstructor(clsArr);
                    addReflectionInstance(uniqueConstructorName, constructor2);
                    return constructor2;
                } catch (NoSuchMethodException unused) {
                    constructor = this.mBaseClass.getDeclaredConstructor(clsArr);
                    constructor.setAccessible(true);
                    addReflectionInstance(uniqueConstructorName, constructor);
                    return constructor;
                }
            } catch (NoSuchMethodException e) {
                PrintStream printStream = System.err;
                printStream.println(getBaseClassName() + " No method " + e);
            }
        }
        return constructor;
    }

    public Object createInstance() {
        return createInstance(new Object[0]);
    }

    public Object createInstance(Object... objArr) {
        return createInstance(null, objArr);
    }

    public Object createInstance(Class<?>[] clsArr, Object... objArr) {
        if (objArr == null) {
            objArr = new Object[0];
        }
        Constructor loadConstructorIfNeeded = loadConstructorIfNeeded(clsArr);
        if (loadConstructorIfNeeded == null) {
            Log.d(getBaseClassName(), "Cannot invoke there's no constructor.");
            return null;
        }
        try {
            loadConstructorIfNeeded.setAccessible(true);
            return loadConstructorIfNeeded.newInstance(objArr);
        } catch (IllegalAccessException e) {
            Logger.getLogger(this.getBaseClassName() + " IllegalAccessException encountered invoking constructor " + e);
            return null;
        } catch (InstantiationException e2) {
            Log.e(TAG, " createInstance : e=" + e2.toString());
            Logger.getLogger(this.getBaseClassName() + " InstantiationException encountered invoking constructor " + e2);
            return null;
        } catch (InvocationTargetException e3) {
            Logger.getLogger(this.getBaseClassName() + " InvocationTargetException encountered invoking constructor " + e3);
            return null;
        }
    }

    public Field loadFieldIfNeeded(String str) {
        Field field = null;
        if (str != null && !str.isEmpty()) {
            String uniqueFieldName = getUniqueFieldName(str);
            Object reflectionInstance = getReflectionInstance(uniqueFieldName);
            if (reflectionInstance != null) {
                return (Field) reflectionInstance;
            }
            Class<?> cls = this.mBaseClass;
            if (cls == null) {
                return null;
            }
            try {
                try {
                    Field field2 = cls.getField(str);
                    addReflectionInstance(uniqueFieldName, field2);
                    return field2;
                } catch (NoSuchFieldException unused) {
                    field = this.mBaseClass.getDeclaredField(str);
                    field.setAccessible(true);
                    addReflectionInstance(uniqueFieldName, field);
                    return field;
                }
            } catch (NoSuchFieldException e) {
                PrintStream printStream = System.err;
                printStream.println(getBaseClassName() + " No field " + e);
            }
        }
        return field;
    }

    public Object getNormalValue(Object obj, String str) {
        if (obj == null || str == null || str.isEmpty()) {
            String baseClassName = getBaseClassName();
            Log.d(baseClassName, "Cannot get value : " + str);
            return null;
        }
        Field loadFieldIfNeeded = loadFieldIfNeeded(str);
        if (loadFieldIfNeeded == null) {
            String baseClassName2 = getBaseClassName();
            Log.d(baseClassName2, "Cannot get value : " + str);
            return null;
        }
        try {
            return loadFieldIfNeeded.get(obj);
        } catch (IllegalAccessException e) {
            PrintStream printStream = System.err;
            printStream.println(this.getBaseClassName() + " IllegalAccessException encountered get " + str + e);
            return null;
        }
    }

    public void setNormalValue(Object obj, String str, Object obj2) {
        if (obj == null || str == null || str.isEmpty()) {
            Log.d(TAG, "Cannot set value : " + str);
            return;
        }
        Field loadFieldIfNeeded = loadFieldIfNeeded(str);
        if (loadFieldIfNeeded == null) {
            Log.d(TAG, "Cannot set value : " + str);
            return;
        }
        try {
            loadFieldIfNeeded.set(obj, obj2);
        } catch (IllegalAccessException e) {
            Log.e(TAG, " IllegalAccessException encountered set " + str + e);
        }
    }

    public boolean getBooleanStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        if (staticValue == null) {
            return false;
        }
        return ((Boolean) staticValue).booleanValue();
    }

    public int getIntStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        if (staticValue == null) {
            return -1;
        }
        return ((Integer) staticValue).intValue();
    }

    public long getLongStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        if (staticValue == null) {
            return 0L;
        }
        return ((Long) staticValue).longValue();
    }

    public float getFloatStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        if (staticValue == null) {
            return 0.0f;
        }
        return ((Float) staticValue).floatValue();
    }

    public double getDoubleStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        if (staticValue == null) {
            return 0.0d;
        }
        return ((Double) staticValue).doubleValue();
    }

    public String getStringStaticValue(String str) {
        Object staticValue = getStaticValue(str);
        return staticValue == null ? Constants.packageName.NONE : (String) staticValue;
    }

    public Object getStaticValue(String str) {
        if (this.mBaseClass == null || str == null || str.isEmpty()) {
            String baseClassName = getBaseClassName();
            Log.d(baseClassName, "Cannot get static value : " + str);
            return null;
        }
        try {
            try {
                Field declaredField = this.mBaseClass.getDeclaredField(str);
                declaredField.setAccessible(true);
                return declaredField.get(null);
            } catch (IllegalAccessException e) {
                Logger.getLogger(this.getBaseClassName() + " IllegalAccessException encountered get " + str + e);
                return null;
            } catch (NoSuchFieldException e2) {
                Logger.getLogger(this.getBaseClassName() + " No field " + e2);
                return null;
            }
        } catch (IllegalAccessException e3) {
            Logger.getLogger(this.getBaseClassName() + " IllegalAccessException encountered get " + str + e3);
            return null;
        } catch (NoSuchFieldException unused) {
            return this.mBaseClass.getField(str).get(null);
        }
    }

    public Method loadMethodIfNeeded(String str, Class<?>[] clsArr) {
        String uniqueMethodName = getUniqueMethodName(str, clsArr);
        Object reflectionInstance = getReflectionInstance(uniqueMethodName);
        if (reflectionInstance != null) {
            return (Method) reflectionInstance;
        }
        if (this.mBaseClass != null && str != null && !str.isEmpty()) {
            if (clsArr == null) {
                clsArr = new Class[0];
            }
            try {
                try {
                    Method method = this.mBaseClass.getMethod(str, clsArr);
                    addReflectionInstance(uniqueMethodName, method);
                    return method;
                } catch (NoSuchMethodException unused) {
                    Method declaredMethod = this.mBaseClass.getDeclaredMethod(str, clsArr);
                    declaredMethod.setAccessible(true);
                    addReflectionInstance(uniqueMethodName, declaredMethod);
                    return declaredMethod;
                }
            } catch (NoSuchMethodException e) {
                PrintStream printStream = System.err;
                printStream.println(getBaseClassName() + " No method " + e);
            }
        }
        return null;
    }

    public Object invokeNormalMethod(Object obj, String str) {
        return invokeNormalMethod(obj, str, new Object[0]);
    }

    public Object invokeNormalMethod(Object obj, String str, Object... objArr) {
        return invokeNormalMethod(obj, str, null, objArr);
    }

    public Object invokeNormalMethod(Object obj, String str, Class<?>[] clsArr, Object... objArr) {
        if (obj == null || str == null || str.isEmpty()) {
            String baseClassName = getBaseClassName();
            Log.d(baseClassName, "Cannot invoke " + str);
            return null;
        }
        if (objArr == null) {
            objArr = new Object[0];
        }
        Method loadMethodIfNeeded = loadMethodIfNeeded(str, clsArr);
        if (loadMethodIfNeeded == null) {
            String baseClassName2 = getBaseClassName();
            Log.d(baseClassName2, "Cannot invoke there's no method reflection : " + str);
            return null;
        }
        try {
            return loadMethodIfNeeded.invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            PrintStream printStream = System.err;
            printStream.println(this.getBaseClassName() + " IllegalAccessException encountered invoking " + str + e);
            return null;
        } catch (InvocationTargetException e2) {
            PrintStream printStream2 = System.err;
            printStream2.println(this.getBaseClassName() + " InvocationTargetException encountered invoking " + str + e2);
            StringBuilder sb = new StringBuilder();
            sb.append(" invokeNormalMethod : e=");
            sb.append(e2.toString());
            Log.e(TAG, sb.toString());
            return null;
        }
    }

    public Object invokeStaticMethod(String str) {
        return invokeStaticMethod(str, new Object[0]);
    }

    public Object invokeStaticMethod(String str, Object... objArr) {
        return invokeStaticMethod(str, null, objArr);
    }

    public Object invokeStaticMethod(String str, Class<?>[] clsArr, Object... objArr) {
        if (str == null || str.isEmpty()) {
            String baseClassName = getBaseClassName();
            Log.d(baseClassName, "Cannot invoke " + str);
            return null;
        }
        if (objArr == null) {
            objArr = new Object[0];
        }
        Method loadMethodIfNeeded = loadMethodIfNeeded(str, clsArr);
        if (loadMethodIfNeeded == null) {
            String baseClassName2 = getBaseClassName();
            Log.d(baseClassName2, "Cannot invoke there's no method reflection : " + str);
            return null;
        }
        try {
            return loadMethodIfNeeded.invoke(null, objArr);
        } catch (IllegalAccessException e) {
            PrintStream printStream = System.err;
            printStream.println(this.getBaseClassName() + " IllegalAccessException encountered invoking " + str + e);
            return null;
        } catch (InvocationTargetException e2) {
            PrintStream printStream2 = System.err;
            printStream2.println(this.getBaseClassName() + " InvocationTargetException encountered invoking " + str + e2);
            return null;
        }
    }

    private String getUniqueConstructorName(Class<?>[] clsArr) {
        String baseClassName = getBaseClassName();
        if (clsArr == null) {
            return baseClassName + "_EMPTY";
        }
        for (Class<?> cls : clsArr) {
            try {
                baseClassName = baseClassName + cls.getName();
            } catch (NullPointerException e) {
                System.err.println(getBaseClassName() + " getUniqueConstructorName " + e);
            }
        }
        return baseClassName;
    }

    private String getUniqueFieldName(String str) {
        return "FIELD_" + str;
    }

    private String getUniqueMethodName(String str, Class<?>[] clsArr) {
        if (clsArr == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        for (Class<?> cls : clsArr) {
            if (cls != null) {
                sb.append(cls.getName());
            }
        }
        return sb.toString();
    }

    public Class<?> getClassType() {
        return this.mBaseClass;
    }
}
