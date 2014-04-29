package com.android.carair.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 这是SDK中https证书验证的策略类，应用可以实现自定义的X509TrustManager，并设置进来。如果没有设置则采用缺省的HTTPS策略
 * */
class X509TrustManagerStrategy {
	protected static X509TrustManager m_trustMgr = null;
	
	public static X509TrustManager getX509TrustManager()
	{
		
		
		return m_trustMgr;
	}
	
	public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
		
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                String authType) throws CertificateException {
                }
        } };

        // Install the all-trusting trust manager
        try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection
                                .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

	/**
	 * 应用设置定制的X509验证策略，如果应用没有设置则使用缺省的策略
	 * @param mgr 自定义的X509验证
	 */
	public static void setX509TrustManager( X509TrustManager mgr)
	{
		m_trustMgr = mgr;
	}
}
