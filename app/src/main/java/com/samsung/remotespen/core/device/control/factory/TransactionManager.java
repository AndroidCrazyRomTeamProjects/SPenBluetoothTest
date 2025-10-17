package com.samsung.remotespen.core.device.control.factory;

import android.util.Log;

import com.samsung.remotespen.core.device.control.factory.BleSpenDriver;
import com.samsung.remotespen.core.device.data.BleCancellableOperation;
import com.samsung.remotespen.core.device.data.BleOpResultCode;
import com.samsung.remotespen.core.device.data.BleOpResultData;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: BleSpenBleDriver.java */
/* loaded from: classes.dex */
public class TransactionManager {
    private static final int MAX_TRANSACTION_QUEUE_SIZE = 4;
    private static final String TAG = "TransactionManager";
    private BleSpenBleDriver mDriver;
    private ArrayList<TransactionItem> mTrQueue = new ArrayList<>();

    /* compiled from: BleSpenBleDriver.java */
    /* loaded from: classes.dex */
    public static class TransactionItem {
        public BleSpenDriver.OperationFinishListener mFinishListener;
        public int mTimeout;
        public Transaction mTransaction;
    }

    public TransactionManager(BleSpenBleDriver bleSpenBleDriver) {
        this.mDriver = bleSpenBleDriver;
    }

    public synchronized void startTransaction(final Transaction transaction, final BleSpenDriver.OperationFinishListener operationFinishListener, int i) {
        String str = TAG;
        Log.d(str, "startTransaction : " + transaction.getName());
        int size = this.mTrQueue.size();
        if (size >= 4) {
            Log.e(str, "startTransaction : Transaction queue full :  cnt=" + size + " requestedTr=" + transaction.getName());
            for (int i2 = 0; i2 < size; i2++) {
                String str2 = TAG;
                Log.e(str2, "startTransaction : queued [" + i2 + "] = " + this.mTrQueue.get(i2).mTransaction.getName());
            }
            if (operationFinishListener != null) {
                BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.GATT_TRANSACTION_FULL);
                bleOpResultData.setMessage("Too many queued transactions");
                operationFinishListener.onFinish(bleOpResultData, 0L);
            }
            return;
        }
        BleSpenDriver.OperationFinishListener operationFinishListener2 = new BleSpenDriver.OperationFinishListener() { // from class: com.samsung.remotespen.core.device.control.factory.TransactionManager.1
            @Override // com.samsung.remotespen.core.device.control.factory.BleSpenDriver.OperationFinishListener
            public void onFinish(BleOpResultData bleOpResultData2, long j) {
                synchronized (TransactionManager.this) {
                    TransactionItem popHeadTransaction = TransactionManager.this.popHeadTransaction();
                    if (popHeadTransaction == null) {
                        String str3 = TransactionManager.TAG;
                        Log.e(str3, "onFinish: Head transaction is null : req = " + transaction.getName());
                        BleSpenDriver.OperationFinishListener operationFinishListener3 = operationFinishListener;
                        if (operationFinishListener3 != null) {
                            operationFinishListener3.onFinish(bleOpResultData2, j);
                        }
                    } else if (transaction != popHeadTransaction.mTransaction) {
                        String str4 = TransactionManager.TAG;
                        Log.e(str4, "onFinish: Finished transaction not matched : req=" + transaction.getName() + ", head=" + popHeadTransaction.mTransaction.getName());
                        BleSpenDriver.OperationFinishListener operationFinishListener4 = operationFinishListener;
                        if (operationFinishListener4 != null) {
                            operationFinishListener4.onFinish(bleOpResultData2, j);
                        }
                    } else {
                        String str5 = TransactionManager.TAG;
                        Log.d(str5, "onFinish : " + transaction.getName());
                        TransactionItem headTransactionItem = TransactionManager.this.getHeadTransactionItem();
                        if (headTransactionItem != null) {
                            String str6 = TransactionManager.TAG;
                            Log.d(str6, "startTransaction : Starting next queued transaction.. : " + headTransactionItem.mTransaction.getName());
                            headTransactionItem.mTransaction.start(TransactionManager.this.mDriver, headTransactionItem.mFinishListener, headTransactionItem.mTimeout);
                        } else {
                            Log.d(TransactionManager.TAG, "startTransaction : no enqueued transaction");
                        }
                        BleSpenDriver.OperationFinishListener operationFinishListener5 = operationFinishListener;
                        if (operationFinishListener5 != null) {
                            operationFinishListener5.onFinish(bleOpResultData2, j);
                        }
                    }
                }
            }
        };
        TransactionItem transactionItem = new TransactionItem();
        transactionItem.mTransaction = transaction;
        transactionItem.mTimeout = i;
        transactionItem.mFinishListener = operationFinishListener2;
        this.mTrQueue.add(transactionItem);
        if (this.mTrQueue.size() == 1) {
            transaction.start(this.mDriver, operationFinishListener2, i);
        } else {
            Log.d(str, "startTransaction : enqueued");
        }
    }

    public synchronized void finishHeadTransaction(BleOpResultData bleOpResultData) {
        TransactionItem headTransactionItem = getHeadTransactionItem();
        if (headTransactionItem != null) {
            finishTransaction(headTransactionItem, bleOpResultData);
        } else {
            Log.e(TAG, "finishHeadTransaction : No queued transaction!");
        }
    }

    public synchronized Transaction getHeadTransaction() {
        TransactionItem headTransactionItem = getHeadTransactionItem();
        if (headTransactionItem != null) {
            return headTransactionItem.mTransaction;
        }
        return null;
    }

    public synchronized boolean isTransactionRunning(Class<?> cls) {
        Transaction headTransaction = getHeadTransaction();
        if (headTransaction == null) {
            return false;
        }
        if (cls == null || cls.isInstance(headTransaction)) {
            return headTransaction.isWorking();
        }
        return false;
    }

    public synchronized void cancelAllTransactions(BleCancellableOperation.FinishListener finishListener) {
        String str = TAG;
        Log.d(str, "cancelAllTransactions : cnt=" + this.mTrQueue.size());
        Iterator it = new ArrayList(this.mTrQueue).iterator();
        while (it.hasNext()) {
            TransactionItem transactionItem = (TransactionItem) it.next();
            if (transactionItem != null) {
                Transaction transaction = transactionItem.mTransaction;
                if (transaction.isFinished()) {
                    String str2 = TAG;
                    Log.d(str2, "cancelAllTransactions : Already finished transaction : " + transaction.getName());
                } else {
                    String str3 = TAG;
                    Log.d(str3, "cancelAllTransactions : Cancelling the transaction.." + transaction.getName());
                    BleOpResultData bleOpResultData = new BleOpResultData(BleOpResultCode.CANCELLED);
                    bleOpResultData.setMessage("Cancelled forcefully on " + str3);
                    finishTransaction(transactionItem, bleOpResultData);
                }
            } else {
                Log.e(TAG, "cancelAllTransactions : transaction is null!");
            }
        }
        if (finishListener != null) {
            finishListener.onFinish(new BleOpResultData(BleOpResultCode.SUCCESS));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized TransactionItem getHeadTransactionItem() {
        if (this.mTrQueue.size() <= 0) {
            return null;
        }
        return this.mTrQueue.get(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized TransactionItem popHeadTransaction() {
        TransactionItem headTransactionItem = getHeadTransactionItem();
        if (headTransactionItem != null) {
            this.mTrQueue.remove(0);
            return headTransactionItem;
        }
        Log.e(TAG, "popHeadTransaction : Empty transaction queue!");
        return null;
    }

    private void finishTransaction(TransactionItem transactionItem, BleOpResultData bleOpResultData) {
        if (transactionItem != null) {
            transactionItem.mTransaction.finish(bleOpResultData);
        }
    }
}
