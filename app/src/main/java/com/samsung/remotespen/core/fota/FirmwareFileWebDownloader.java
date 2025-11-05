package com.samsung.remotespen.core.fota;

import android.content.Context;
import android.util.Log;

import com.crazyromteam.spenbletest.utils.Assert;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.net.ssl.HttpsURLConnection;

/* loaded from: classes.dex */
public class FirmwareFileWebDownloader extends FileDownloader {
    private static final String TAG = "FirmwareFileWebDownloader";
    private Context mContext;
    private FinishListener mFinishListener;
    private File mFirmwareFile;
    private FirmwareInfo mFirmwareInfo;

    /* loaded from: classes.dex */
    public interface FinishListener {
        void onFinish(boolean z);
    }

    public FirmwareFileWebDownloader(Context context) {
        this.mContext = context;
    }

    public void abort() {
        deleteFile(this.mFirmwareFile);
    }

    public void download(FirmwareInfo firmwareInfo, File file, FinishListener finishListener) {
        this.mFirmwareInfo = firmwareInfo;
        this.mFirmwareFile = file;
        this.mFinishListener = finishListener;
        deleteFile(file);
        Assert.notNull(finishListener, "listener is null");
        try {
            this.mFirmwareFile.createNewFile();
            startDownload();
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "download: e = " + e);
            this.mFinishListener.onFinish(false);
        }
    }

    private void deleteFile(File file) {
        if (file == null || !file.exists() || file.delete()) {
            return;
        }
        Log.d(TAG, "deleteFile : Failed to delete the file");
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public String getTag() {
        return TAG;
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public String getDownloadURL() {
        return this.mFirmwareInfo.getDownloadUrl();
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void connectionSuccess(HttpsURLConnection httpsURLConnection) {
        try {
            try {
                InputStream inputStream = httpsURLConnection.getInputStream();
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(this.mFirmwareFile);
                    byte[] bArr = new byte[httpsURLConnection.getContentLength()];
                    for (int read = inputStream.read(bArr); read > 0; read = inputStream.read(bArr)) {
                        fileOutputStream.write(bArr, 0, read);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "connectionSuccess: e = " + e);
                this.mFinishListener.onFinish(false);
            }
        } finally {
            this.mFinishListener.onFinish(true);
        }
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void connectionFail() {
        this.mFinishListener.onFinish(false);
        deleteFile(this.mFirmwareFile);
    }

    @Override // com.samsung.remotespen.core.fota.FileDownloader
    public void exception(Exception exc) {
        this.mFinishListener.onFinish(false);
        deleteFile(this.mFirmwareFile);
    }
}
