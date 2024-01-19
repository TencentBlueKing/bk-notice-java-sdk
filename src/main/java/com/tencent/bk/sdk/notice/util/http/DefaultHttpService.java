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

import com.tencent.bk.sdk.notice.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class DefaultHttpService implements IHttpService {

    private static final Charset charset = StandardCharsets.UTF_8;
    private final CloseableHttpClient httpClient;

    public DefaultHttpService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String doHttpGet(String uri, Header[] headers) throws IOException {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeaders(headers);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        return EntityUtils.toString(httpResponse.getEntity(), charset);
    }

    @Override
    public String doHttpPost(String uri, Header[] headers, Object body) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeaders(headers);
        httpPost.setEntity(new ByteArrayEntity(JsonUtils.toJson(body).getBytes(charset)));
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity(), charset);
    }
}
