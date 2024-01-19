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

import java.util.List;

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

    private String buildUriWithParams(String bkLanguage, Integer offset, Integer limit) {
        StringBuilder sb = new StringBuilder();
        sb.append(URI_GET_CURRENT_ANNOUNCEMENTS);
        sb.append("?platform=");
        sb.append(bkNoticeConfig.getAppCode());
        if (StringUtils.isNotBlank(bkLanguage)) {
            sb.append("&language=");
            sb.append(bkLanguage);
        }
        if (offset != null) {
            sb.append("&offset=");
            sb.append(offset);
        }
        if (limit != null) {
            sb.append("&limit=");
            sb.append(limit);
        }
        return sb.toString();
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
        return bkNoticeConfig.getApiBaseUrl() + path;
    }

    private Header[] buildBkApiRequestHeaders(BkApiAuthorization authorization) {
        Header[] header = new Header[2];
        header[0] = new BasicHeader("Content-Type", "application/json");
        header[1] = new BasicHeader(BK_API_AUTH_HEADER, JsonUtils.toJson(authorization));
        return header;
    }

}
