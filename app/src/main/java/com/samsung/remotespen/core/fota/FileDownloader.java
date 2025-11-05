package com.samsung.remotespen.core.fota;

import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class FileDownloader {
    private static final int REQUEST_SERVER_CONNECT_TIMEOUT = 5000;
    private static final int REQUEST_SERVER_READ_TIMEOUT = 15000;

    public abstract void connectionFail();

    public abstract void connectionSuccess(HttpsURLConnection httpsURLConnection);

    public abstract void exception(Exception exc);

    public abstract String getDownloadURL();

    public abstract String getTag();

    public void startDownload() {
        ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
        newSingleThreadExecutor.execute(new Runnable() { // from class: com.samsung.remotespen.core.fota.FileDownloader.1
            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Removed duplicated region for block: B:26:0x0085  */
            /* JADX WARN: Type inference failed for: r0v1, types: [java.lang.String] */
            /* JADX WARN: Type inference failed for: r0v3 */
            /* JADX WARN: Type inference failed for: r0v4 */
            /* JADX WARN: Type inference failed for: r0v5 */
            /* JADX WARN: Type inference failed for: r0v6, types: [javax.net.ssl.HttpsURLConnection] */
            /* JADX WARN: Type inference failed for: r0v8, types: [javax.net.ssl.HttpsURLConnection] */
            /* JADX WARN: Type inference failed for: r1v14, types: [com.samsung.remotespen.core.fota.FileDownloader] */
            @Override // java.lang.Runnable
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public void run() {
                /*
                    r6 = this;
                    com.samsung.remotespen.core.fota.FileDownloader r0 = com.samsung.remotespen.core.fota.FileDownloader.this
                    java.lang.String r0 = r0.getDownloadURL()
                    if (r0 != 0) goto L19
                    com.samsung.remotespen.core.fota.FileDownloader r0 = com.samsung.remotespen.core.fota.FileDownloader.this
                    java.lang.String r0 = r0.getTag()
                    java.lang.String r1 = "startDownload : downloadURL is null"
                    com.samsung.util.debug.Log.e(r0, r1)
                    com.samsung.remotespen.core.fota.FileDownloader r6 = com.samsung.remotespen.core.fota.FileDownloader.this
                    r6.connectionFail()
                    return
                L19:
                    r1 = 0
                    java.net.URL r2 = new java.net.URL     // Catch: java.lang.Throwable -> L56 java.lang.Exception -> L58
                    r2.<init>(r0)     // Catch: java.lang.Throwable -> L56 java.lang.Exception -> L58
                    java.net.URLConnection r0 = r2.openConnection()     // Catch: java.lang.Throwable -> L56 java.lang.Exception -> L58
                    javax.net.ssl.HttpsURLConnection r0 = (javax.net.ssl.HttpsURLConnection) r0     // Catch: java.lang.Throwable -> L56 java.lang.Exception -> L58
                    com.samsung.remotespen.core.fota.FileDownloader r1 = com.samsung.remotespen.core.fota.FileDownloader.this     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    javax.net.ssl.SSLContext r1 = com.samsung.remotespen.core.fota.FileDownloader.access$000(r1)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    javax.net.ssl.SSLSocketFactory r1 = r1.getSocketFactory()     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r0.setSSLSocketFactory(r1)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r1 = 1
                    r0.setInstanceFollowRedirects(r1)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r1 = 5000(0x1388, float:7.006E-42)
                    r0.setConnectTimeout(r1)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r1 = 15000(0x3a98, float:2.102E-41)
                    r0.setReadTimeout(r1)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    int r1 = r0.getResponseCode()     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r2 = 200(0xc8, float:2.8E-43)
                    if (r1 != r2) goto L4e
                    com.samsung.remotespen.core.fota.FileDownloader r1 = com.samsung.remotespen.core.fota.FileDownloader.this     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r1.connectionSuccess(r0)     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    goto L7d
                L4e:
                    com.samsung.remotespen.core.fota.FileDownloader r1 = com.samsung.remotespen.core.fota.FileDownloader.this     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    r1.connectionFail()     // Catch: java.lang.Exception -> L54 java.lang.Throwable -> L81
                    goto L7d
                L54:
                    r1 = move-exception
                    goto L5c
                L56:
                    r6 = move-exception
                    goto L83
                L58:
                    r0 = move-exception
                    r5 = r1
                    r1 = r0
                    r0 = r5
                L5c:
                    com.samsung.remotespen.core.fota.FileDownloader r2 = com.samsung.remotespen.core.fota.FileDownloader.this     // Catch: java.lang.Throwable -> L81
                    java.lang.String r2 = r2.getTag()     // Catch: java.lang.Throwable -> L81
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L81
                    r3.<init>()     // Catch: java.lang.Throwable -> L81
                    java.lang.String r4 = "startDownload : e = "
                    r3.append(r4)     // Catch: java.lang.Throwable -> L81
                    r3.append(r1)     // Catch: java.lang.Throwable -> L81
                    java.lang.String r3 = r3.toString()     // Catch: java.lang.Throwable -> L81
                    com.samsung.util.debug.Log.e(r2, r3, r1)     // Catch: java.lang.Throwable -> L81
                    com.samsung.remotespen.core.fota.FileDownloader r6 = com.samsung.remotespen.core.fota.FileDownloader.this     // Catch: java.lang.Throwable -> L81
                    r6.exception(r1)     // Catch: java.lang.Throwable -> L81
                    if (r0 == 0) goto L80
                L7d:
                    r0.disconnect()
                L80:
                    return
                L81:
                    r6 = move-exception
                    r1 = r0
                L83:
                    if (r1 == 0) goto L88
                    r1.disconnect()
                L88:
                    throw r6
                */
                throw new UnsupportedOperationException("Method not decompiled: com.samsung.remotespen.core.fota.FileDownloader.AnonymousClass1.run():void");
            }
        });
        newSingleThreadExecutor.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SSLContext createSystemCertificates() {
        TrustManagerFactory trustManagerFactory;
        SSLContext sSLContext;
        SSLContext sSLContext2 = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            KeyStore keyStore2 = KeyStore.getInstance("AndroidCAStore");
            keyStore2.load(null, null);
            Enumeration<String> aliases = keyStore2.aliases();
            while (aliases.hasMoreElements()) {
                String nextElement = aliases.nextElement();
                X509Certificate x509Certificate = (X509Certificate) keyStore2.getCertificate(nextElement);
                if (nextElement.startsWith("system:")) {
                    keyStore.setCertificateEntry(nextElement, x509Certificate);
                }
            }
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sSLContext = SSLContext.getInstance("TLS");
        } catch (IOException e) {
            e = e;
        } catch (KeyManagementException e2) {
            e = e2;
        } catch (KeyStoreException e3) {
            e = e3;
        } catch (NoSuchAlgorithmException e4) {
            e = e4;
        } catch (CertificateException e5) {
            e = e5;
        }
        try {
            sSLContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sSLContext;
        } catch (IOException e6) {
            e = e6;
            sSLContext2 = sSLContext;
            String tag = getTag();
            Log.e(tag, "createSystemCertificates : e = " + e, e);
            return sSLContext2;
        } catch (KeyManagementException e7) {
            e = e7;
            sSLContext2 = sSLContext;
            String tag2 = getTag();
            Log.e(tag2, "createSystemCertificates : e = " + e, e);
            return sSLContext2;
        } catch (KeyStoreException e8) {
            e = e8;
            sSLContext2 = sSLContext;
            String tag3 = getTag();
            Log.e(tag3, "createSystemCertificates : e = " + e, e);
            return sSLContext2;
        } catch (NoSuchAlgorithmException e9) {
            e = e9;
            sSLContext2 = sSLContext;
            String tag4 = getTag();
            Log.e(tag4, "createSystemCertificates : e = " + e, e);
            return sSLContext2;
        } catch (CertificateException e10) {
            e = e10;
            sSLContext2 = sSLContext;
            String tag5 = getTag();
            Log.e(tag5, "createSystemCertificates : e = " + e, e);
            return sSLContext2;
        }
    }
}
