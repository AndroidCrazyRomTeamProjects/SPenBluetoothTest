package com.samsung.remotespen.core.device.control.factory.shared;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class LogComposer {
    private static final int OPERATION_TIMEOUT = 10000;
    private static final String TAG = "LogComposer";
    private BleSpenDriver.OperationFinishListener mFinishListener;
    private ArrayList<Byte> mByteList = new ArrayList<>();
    private Handler mTimeOutHandler = new Handler() { // from class: com.samsung.remotespen.core.device.control.factory.shared.LogComposer.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            BleOpResultData bleOpResultData = new BleOpResultData();
            bleOpResultData.setBleResultCode(BleOpResultCode.TIMEOUT);
            LogComposer.this.mFinishListener.onFinish(bleOpResultData, 0L);
            Log.e(LogComposer.TAG, "handleMessage TIME OUT");
            super.handleMessage(message);
        }
    };

    public LogComposer(BleSpenDriver.OperationFinishListener operationFinishListener) {
        this.mFinishListener = operationFinishListener;
    }

    public void setInitByte(byte[] bArr) {
        this.mTimeOutHandler.sendEmptyMessageDelayed(0, 10000L);
        appendData(bArr);
    }

    public synchronized void appendData(byte[] bArr) {
        for (byte b : bArr) {
            this.mByteList.add(Byte.valueOf(b));
        }
    }

    public void finishComposer() {
        this.mTimeOutHandler.removeMessages(0);
        byte[] copyByteList = copyByteList(this.mByteList);
        BleOpResultData bleOpResultData = new BleOpResultData();
        bleOpResultData.setBleResultCode(BleOpResultCode.SUCCESS);
        bleOpResultData.setByteData(copyByteList);
        this.mFinishListener.onFinish(bleOpResultData, 0L);
    }

    private byte[] copyByteList(ArrayList<Byte> arrayList) {
        int size = arrayList.size();
        String str = TAG;
        Log.d(str, "total bytes size :" + size);
        byte[] bArr = new byte[size];
        Iterator<Byte> it = arrayList.iterator();
        int i = 0;
        while (it.hasNext()) {
            bArr[i] = it.next().byteValue();
            i++;
        }
        return bArr;
    }
}
