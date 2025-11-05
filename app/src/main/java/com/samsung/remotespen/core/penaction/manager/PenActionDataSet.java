package com.samsung.remotespen.core.penaction.manager;

import android.util.Log;

import com.samsung.remotespen.core.penaction.trigger.PenActionTriggerType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public final class PenActionDataSet implements Cloneable {
    private static final String TAG = PenActionDataSet.class.getSimpleName();
    private ActionMap mActions;

    /* loaded from: classes.dex */
    public static class ActionMap extends HashMap<PenActionTriggerType, ActionItem> {
    }

    public PenActionDataSet() {
        this.mActions = new ActionMap();
    }

    public PenActionDataSet(ActionMap actionMap) {
        this.mActions = new ActionMap();
        ActionMap actionMap2 = new ActionMap();
        this.mActions = actionMap2;
        actionMap2.putAll(actionMap);
    }

    public ActionItem getActionItem(PenActionTriggerType penActionTriggerType) {
        return this.mActions.get(penActionTriggerType);
    }

    public Set<PenActionTriggerType> getPenActionTriggerTypeList() {
        return new HashSet(this.mActions.keySet());
    }

    public void setActionItem(PenActionTriggerType penActionTriggerType, ActionItem actionItem) {
        if (penActionTriggerType == null) {
            Log.e(TAG, "setActionItem : type is null!");
        } else if (actionItem == null) {
            this.mActions.remove(penActionTriggerType);
        } else {
            this.mActions.put(penActionTriggerType, actionItem);
        }
    }

    /* renamed from: clone */
    public PenActionDataSet m23clone() {
        try {
            PenActionDataSet penActionDataSet = (PenActionDataSet) super.clone();
            penActionDataSet.mActions = new ActionMap();
            for (Map.Entry<PenActionTriggerType, ActionItem> entry : this.mActions.entrySet()) {
                penActionDataSet.mActions.put(entry.getKey(), entry.getValue().m22clone());
            }
            return penActionDataSet;
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "clone : e=" + e);
            return null;
        }
    }
}
