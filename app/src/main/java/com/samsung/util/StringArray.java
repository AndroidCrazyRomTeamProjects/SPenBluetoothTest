package com.samsung.util;

import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class StringArray extends ArrayList<String> {
    public String getConcatedString(String str) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(str);
        }
        return sb.toString();
    }
}
