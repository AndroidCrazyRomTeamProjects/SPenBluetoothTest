package com.samsung.remotespen.core.penaction.serialize;

import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class PenActionClassTypeArray extends ArrayList<ClassItem> {
    public String getTypeByClass(Class<?> cls) {
        Iterator<ClassItem> it = iterator();
        while (it.hasNext()) {
            ClassItem next = it.next();
            if (next.cls == cls) {
                return next.strType;
            }
        }
        return null;
    }

    public Class<?> getClassByType(String str) {
        Iterator<ClassItem> it = iterator();
        while (it.hasNext()) {
            ClassItem next = it.next();
            if (next.strType.equals(str)) {
                return next.cls;
            }
        }
        return null;
    }

    public void add(String str, Class<?> cls) {
        add(new ClassItem(str, cls));
    }
}
