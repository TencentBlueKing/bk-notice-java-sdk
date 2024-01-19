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

public interface IHttpServiceBuilder {

    /**
     * 构建HttpService实例
     *
     * @return new instance of IHttpService
     */
    IHttpService build();

    /**
     * 设置代理服务器地址
     *
     * @param httpProxyHost 代理服务器地址
     * @return Builder
     */
    IHttpServiceBuilder httpProxyHost(String httpProxyHost);

    /**
     * 代理服务器端口
     *
     * @param httpProxyPort 代理服务器端口
     * @return Builder
     */
    IHttpServiceBuilder httpProxyPort(int httpProxyPort);

    /**
     * 代理服务器用户名
     *
     * @param httpProxyUsername 代理认证用户名
     * @return Builder
     */
    IHttpServiceBuilder httpProxyUsername(String httpProxyUsername);

    /**
     * 代理服务器密码
     *
     * @param httpProxyPassword 代理认证密码
     * @return Builder
     */
    IHttpServiceBuilder httpProxyPassword(String httpProxyPassword);
}
