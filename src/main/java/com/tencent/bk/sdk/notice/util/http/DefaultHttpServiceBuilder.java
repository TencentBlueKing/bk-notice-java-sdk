/*
 * Tencent is pleased to support the open source community by making 蓝鲸消息通知中心Java SDK（bk-notice-java-sdk） available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * 蓝鲸消息通知中心Java SDK（bk-notice-java-sdk） is licensed under the MIT License.
 *
 * License for 蓝鲸消息通知中心Java SDK（bk-notice-java-sdk）:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.tencent.bk.sdk.notice.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DefaultHttpServiceBuilder implements IHttpServiceBuilder {

    private final AtomicBoolean prepared = new AtomicBoolean(false);
    private int connectionRequestTimeout = 15000;
    private int connectionTimeout = 15000;
    private int soTimeout = 15000;
    private int maxConnPerHost = 500;
    private int maxTotalConn = 1000;
    private String userAgent;
    private SSLConnectionSocketFactory sslConnectionSocketFactory = buildSSLConnectionSocketFactory();
    private final PlainConnectionSocketFactory plainConnectionSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
    private String httpProxyHost;
    private int httpProxyPort;
    private String httpProxyUsername;
    private String httpProxyPassword;

    /**
     * 持有client对象,仅初始化一次,避免多service实例的时候造成重复初始化的问题
     */
    private IHttpService httpService;

    private DefaultHttpServiceBuilder() {
    }

    public static DefaultHttpServiceBuilder get() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public IHttpServiceBuilder httpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
        return this;
    }

    @Override
    public IHttpServiceBuilder httpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
        return this;
    }

    @Override
    public IHttpServiceBuilder httpProxyUsername(String httpProxyUsername) {
        this.httpProxyUsername = httpProxyUsername;
        return this;
    }

    @Override
    public IHttpServiceBuilder httpProxyPassword(String httpProxyPassword) {
        this.httpProxyPassword = httpProxyPassword;
        return this;
    }

    public IHttpServiceBuilder sslConnectionSocketFactory(SSLConnectionSocketFactory sslConnectionSocketFactory) {
        this.sslConnectionSocketFactory = sslConnectionSocketFactory;
        return this;
    }

    /**
     * 获取链接的超时时间设置,默认3000ms
     * <p>
     * 设置为零时不超时,一直等待. 设置为负数是使用系统默认设置(非上述的3000ms的默认值,而是httpclient的默认设置).
     * </p>
     *
     * @param connectionRequestTimeout 获取链接的超时时间设置(单位毫秒),默认3000ms
     */
    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * 建立链接的超时时间,默认为5000ms.由于是在链接池获取链接,此设置应该并不起什么作用
     * <p>
     * 设置为零时不超时,一直等待. 设置为负数是使用系统默认设置(非上述的5000ms的默认值,而是httpclient的默认设置).
     * </p>
     *
     * @param connectionTimeout 建立链接的超时时间设置(单位毫秒),默认5000ms
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * 默认NIO的socket超时设置,默认5000ms.
     *
     * @param soTimeout 默认NIO的socket超时设置,默认5000ms.
     * @see java.net.SocketOptions#SO_TIMEOUT
     */
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    /**
     * 每路的最大链接数,默认10
     *
     * @param maxConnPerHost 每路的最大链接数,默认10
     */
    public void setMaxConnPerHost(int maxConnPerHost) {
        this.maxConnPerHost = maxConnPerHost;
    }

    /**
     * 最大总连接数,默认50
     *
     * @param maxTotalConn 最大总连接数,默认50
     */
    public void setMaxTotalConn(int maxTotalConn) {
        this.maxTotalConn = maxTotalConn;
    }

    /**
     * 自定义httpclient的User Agent
     *
     * @param userAgent User Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    private synchronized void prepare() {
        if (prepared.get()) {
            return;
        }
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", this.plainConnectionSocketFactory)
            .register("https", this.sslConnectionSocketFactory)
            .build();

        @SuppressWarnings("resource")
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(this.maxTotalConn);
        connectionManager.setDefaultMaxPerRoute(this.maxConnPerHost);
        connectionManager.setDefaultSocketConfig(
            SocketConfig.copy(SocketConfig.DEFAULT)
                .setSoTimeout(this.soTimeout)
                .build()
        );

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionManagerShared(true)
            .setDefaultRequestConfig(RequestConfig.custom()
                .setSocketTimeout(this.soTimeout)
                .setConnectTimeout(this.connectionTimeout)
                .setConnectionRequestTimeout(this.connectionRequestTimeout)
                .build())
            .evictExpiredConnections()
            .evictIdleConnections(5, TimeUnit.SECONDS)
            .disableAuthCaching()
            .disableCookieManagement()
            .disableAutomaticRetries();

        if (StringUtils.isNotBlank(this.httpProxyHost) && StringUtils.isNotBlank(this.httpProxyUsername)) {
            // 使用代理服务器 需要用户认证的代理服务器
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(this.httpProxyHost, this.httpProxyPort),
                new UsernamePasswordCredentials(this.httpProxyUsername, this.httpProxyPassword));
            httpClientBuilder.setDefaultCredentialsProvider(provider);
            httpClientBuilder.setProxy(new HttpHost(this.httpProxyHost, this.httpProxyPort));
        }

        if (StringUtils.isNotBlank(this.userAgent)) {
            httpClientBuilder.setUserAgent(this.userAgent);
        }

        this.httpService = new DefaultHttpService(httpClientBuilder.build());
        prepared.set(true);
    }

    private SSLConnectionSocketFactory buildSSLConnectionSocketFactory() {
        try {
            SSLContext sslcontext = SSLContexts.custom()
                //忽略掉对服务器端证书的校验
                .loadTrustMaterial((TrustStrategy) (chain, authType) -> true).build();

            return new SSLConnectionSocketFactory(
                sslcontext,
                null,
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public IHttpService build() {
        if (!prepared.get()) {
            prepare();
        }
        return this.httpService;
    }

    private static class SingletonHolder {
        private static final DefaultHttpServiceBuilder INSTANCE = new DefaultHttpServiceBuilder();
    }

}
