package com.samsung.remotespen.core.penaction.manager;

import java.util.function.Predicate;

/* compiled from: R8$$SyntheticClass */
/* loaded from: classes.dex */
public final /* synthetic */ class PenActionRuleManager$$ExternalSyntheticLambda0 implements Predicate {
    public static final /* synthetic */ PenActionRuleManager$$ExternalSyntheticLambda0 INSTANCE = new PenActionRuleManager$$ExternalSyntheticLambda0();

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        boolean contains;
        contains = ((String) obj).contains("com.samsung.android.app.notes");
        return contains;
    }
}
