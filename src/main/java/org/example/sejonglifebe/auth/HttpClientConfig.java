package org.example.sejonglifebe.auth;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.cert.X509Certificate;

@Configuration
public class HttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        try {
            SSLContext sslCtx = SSLContext.getInstance("SSL");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
            SSLSocketFactory sslFactory = sslCtx.getSocketFactory();

            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory, trustAllManager())
                    .hostnameVerifier(hostnameVerifier)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("OkHttpClient 초기화 실패", e);
        }
    }

    private X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        };
    }
}
