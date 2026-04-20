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

package com.tencent.bk.sdk.notice.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tencent.bk.sdk.notice.IBkNoticeClient;
import com.tencent.bk.sdk.notice.config.BkNoticeConfig;
import com.tencent.bk.sdk.notice.exception.BkNoticeException;
import com.tencent.bk.sdk.notice.model.req.BasicHttpReq;
import com.tencent.bk.sdk.notice.model.resp.AnnouncementDTO;
import com.tencent.bk.sdk.notice.model.resp.ApiGwResp;
import com.tencent.bk.sdk.notice.model.resp.BkApiAuthorization;
import com.tencent.bk.sdk.notice.model.resp.BkNoticeApp;
import com.tencent.bk.sdk.notice.util.http.DefaultHttpServiceBuilder;
import com.tencent.bk.sdk.notice.util.http.IHttpService;
import com.tencent.bk.sdk.notice.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@SuppressWarnings("SameParameterValue")
public class BkNoticeClient implements IBkNoticeClient {

    private static final String URI_REGISTER_APPLICATION = "/apigw/v1/register/";
    private static final String URI_GET_CURRENT_ANNOUNCEMENTS = "/apigw/v1/announcement/get_current_announcements/";

    private static final String BK_API_AUTH_HEADER = "X-Bkapi-Authorization";

    private final IHttpService httpService;
    private final BkNoticeConfig bkNoticeConfig;
    private final BkApiAuthorization authorization;

    public BkNoticeClient(BkNoticeConfig bkNoticeConfig) {
        this(DefaultHttpServiceBuilder.get().build(), bkNoticeConfig);
    }

    public BkNoticeClient(IHttpService httpService, BkNoticeConfig bkNoticeConfig) {
        validateApiBaseUrl(bkNoticeConfig.getApiBaseUrl());
        this.httpService = httpService;
        this.bkNoticeConfig = bkNoticeConfig;
        authorization = BkApiAuthorization.appAuthorization(
            bkNoticeConfig.getAppCode(),
            bkNoticeConfig.getAppSecret()
        );
    }

    @Override
    public BkNoticeApp registerApplication() {
        ApiGwResp<BkNoticeApp> resp = requestBkNoticeApi(
            HttpPost.METHOD_NAME,
            URI_REGISTER_APPLICATION,
            null,
            new TypeReference<ApiGwResp<BkNoticeApp>>() {
            }
        );
        return resp.getData();
    }

    @Override
    public List<AnnouncementDTO> getCurrentAnnouncements(String bkLanguage, Integer offset, Integer limit) {
        ApiGwResp<List<AnnouncementDTO>> resp = requestBkNoticeApi(
            HttpGet.METHOD_NAME,
            buildUriWithParams(bkLanguage, offset, limit),
            null,
            new TypeReference<ApiGwResp<List<AnnouncementDTO>>>() {
            }
        );
        return resp.getData();
    }

    private static final Set<String> ALLOWED_LANGUAGES = new HashSet<>(Arrays.asList(
        "zh", "zh-cn", "en", "en-us"
    ));

    private String buildUriWithParams(String bkLanguage, Integer offset, Integer limit) {
        StringBuilder sb = new StringBuilder();
        sb.append(URI_GET_CURRENT_ANNOUNCEMENTS);
        sb.append("?platform=");
        sb.append(encodeParam(bkNoticeConfig.getAppCode()));
        if (StringUtils.isNotBlank(bkLanguage)) {
            String lang = bkLanguage.trim().toLowerCase();
            if (!ALLOWED_LANGUAGES.contains(lang)) {
                throw new BkNoticeException(
                    "Unsupported bkLanguage value: " + bkLanguage
                        + ", allowed values: " + ALLOWED_LANGUAGES
                );
            }
            sb.append("&language=");
            sb.append(encodeParam(lang));
        }
        if (offset != null) {
            sb.append("&offset=");
            sb.append(encodeParam(String.valueOf(offset)));
        }
        if (limit != null) {
            sb.append("&limit=");
            sb.append(encodeParam(String.valueOf(limit)));
        }
        return sb.toString();
    }

    private static String encodeParam(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过ESB请求消息通知中心API的统一入口，监控数据埋点位置
     *
     * @param method        Http方法
     * @param path          请求地址
     * @param reqBody       请求体内容
     * @param typeReference 指定了返回值类型的EsbResp TypeReference对象
     * @param <R>           泛型：返回值类型
     * @return 返回值类型实例
     */
    private <R> ApiGwResp<R> requestBkNoticeApi(String method,
                                                String path,
                                                BasicHttpReq reqBody,
                                                TypeReference<ApiGwResp<R>> typeReference) {
        Header[] headers = buildBkApiRequestHeaders(authorization);
        String responseStr;
        String completeUrl = buildCompleteUrl(path);
        try {
            switch (method) {
                case HttpGet.METHOD_NAME:
                    responseStr = httpService.doHttpGet(completeUrl, headers);
                    break;
                case HttpPost.METHOD_NAME:
                    responseStr = httpService.doHttpPost(completeUrl, headers, reqBody);
                    break;
                default:
                    throw new BkNoticeException("method " + method + " not support yet");
            }
            if (log.isDebugEnabled()) {
                log.debug(
                    "method={},path={},reqBody={},responseStr={}",
                    method,
                    path,
                    JsonUtils.toJson(reqBody),
                    responseStr
                );
            }
            ApiGwResp<R> response = JsonUtils.fromJson(responseStr, typeReference);
            if (!response.isSuccess()) {
                log.warn("Failure response:{}", responseStr);
            }
            return response;
        } catch (Exception e) {
            throw new BkNoticeException("Fail to requestBkNoticeApi", e);
        }
    }

    private String buildCompleteUrl(String path) {
        String completeUrl = bkNoticeConfig.getApiBaseUrl() + path;
        URI uri;
        try {
            uri = new URI(completeUrl);
        } catch (Exception e) {
            throw new BkNoticeException("Invalid complete URL: " + completeUrl, e);
        }
        // 协议校验：仅允许 HTTPS 或 HTTP 协议
        if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
            throw new BkNoticeException(
                "Only HTTPS or HTTP protocol is allowed, got: " + uri.getScheme()
            );
        }
        // 域名校验
        String host = uri.getHost();
        if (StringUtils.isBlank(host)) {
            throw new BkNoticeException("Complete URL must contain a valid host");
        }
        // 每次请求前都重新解析并对所有 A/AAAA 记录进行 SSRF 校验，
        // 防止 DNS rebinding、多记录轮询等绕过手段
        validateHostNotInternal(host);
        return completeUrl;
    }

    /**
     * 校验 apiBaseUrl 的合法性：仅允许 HTTPS 协议，禁止内网/本地地址。
     * 采用 fail-closed 策略：DNS 解析失败时直接阻断。
     */
    private static void validateApiBaseUrl(String apiBaseUrl) {
        if (StringUtils.isBlank(apiBaseUrl)) {
            throw new BkNoticeException("apiBaseUrl must not be blank");
        }

        URI uri;
        try {
            uri = new URI(apiBaseUrl);
        } catch (Exception e) {
            throw new BkNoticeException("Invalid apiBaseUrl: " + apiBaseUrl, e);
        }

        // 1. 仅允许 HTTPS 协议
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new BkNoticeException(
                "Only HTTPS protocol is allowed for apiBaseUrl, got: " + uri.getScheme()
            );
        }

        // 2. 域名不能为空
        String host = uri.getHost();
        if (StringUtils.isBlank(host)) {
            throw new BkNoticeException("apiBaseUrl must contain a valid host");
        }

        // 3. 禁止内网/本地/链路本地地址（全量解析 + fail-closed）
        validateHostNotInternal(host);
    }

    /**
     * 对 host 的所有解析结果（A/AAAA 记录）逐一做 SSRF 校验。
     * 设计要点（防御 DNS rebinding 与多记录绕过）：
     * 1. 使用 {@link InetAddress#getAllByName(String)} 获取全部地址，而非仅首个结果；
     * 2. 任一地址命中 loopback / siteLocal / linkLocal / anyLocal / 保留段则拒绝；
     * 3. DNS 解析失败采用 fail-closed 策略，直接抛出异常阻断请求。
     */
    private static void validateHostNotInternal(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // fail-closed：无法解析即视为不安全
            throw new BkNoticeException(
                "Cannot resolve host for SSRF validation, request blocked: " + host, e
            );
        }
        if (addresses == null || addresses.length == 0) {
            throw new BkNoticeException(
                "No IP address resolved for host, request blocked: " + host
            );
        }
        for (InetAddress address : addresses) {
            if (isInternalAddress(address)) {
                throw new BkNoticeException(
                    "Host must not resolve to a local/internal/metadata address: "
                        + host + " -> " + address.getHostAddress()
                );
            }
        }
    }

    /**
     * 判断是否为内网/本地/保留地址。
     */
    private static boolean isInternalAddress(InetAddress address) {
        if (address.isLoopbackAddress()
            || address.isSiteLocalAddress()
            || address.isLinkLocalAddress()
            || address.isAnyLocalAddress()
            || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int b0 = bytes[0] & 0xFF;
            int b1 = bytes[1] & 0xFF;
            // 0.0.0.0/8
            if (b0 == 0) {
                return true;
            }
        } else if (bytes.length == 16) {
            // IPv6：fc00::/7（Unique Local Address）
            if ((bytes[0] & 0xFE) == 0xFC) {
                return true;
            }
            // IPv4-mapped IPv6：::ffff:x.x.x.x，回退到 IPv4 判断
            boolean isV4Mapped = true;
            for (int i = 0; i < 10; i++) {
                if (bytes[i] != 0) {
                    isV4Mapped = false;
                    break;
                }
            }
            if (isV4Mapped
                && (bytes[10] & 0xFF) == 0xFF
                && (bytes[11] & 0xFF) == 0xFF) {
                int b0 = bytes[12] & 0xFF;
                int b1 = bytes[13] & 0xFF;
                if (b0 == 169 && b1 == 254) {
                    return true;
                }
                if (b0 == 100 && b1 >= 64 && b1 <= 127) {
                    return true;
                }
                if (b0 == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private Header[] buildBkApiRequestHeaders(BkApiAuthorization authorization) {
        Header[] header = new Header[2];
        header[0] = new BasicHeader("Content-Type", "application/json");
        header[1] = new BasicHeader(BK_API_AUTH_HEADER, JsonUtils.toJson(authorization));
        return header;
    }

}
