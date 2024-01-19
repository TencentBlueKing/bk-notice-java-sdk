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

package com.tencent.bk.sdk.notice;

import com.tencent.bk.sdk.notice.config.BkNoticeConfig;
import com.tencent.bk.sdk.notice.consts.BkConsts;
import com.tencent.bk.sdk.notice.impl.BkNoticeClient;
import com.tencent.bk.sdk.notice.model.resp.AnnouncementDTO;
import com.tencent.bk.sdk.notice.model.resp.BkNoticeApp;
import com.tencent.bk.sdk.notice.util.http.DefaultHttpServiceBuilder;
import com.tencent.bk.sdk.notice.util.http.IHttpService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class MainTest {

    @Test
    public void main() {
        BkNoticeConfig bkNoticeConfig = new BkNoticeConfig();
        bkNoticeConfig.setAppCode("");
        bkNoticeConfig.setAppSecret("");
        bkNoticeConfig.setApiBaseUrl("https://bk-notice.apigw.xxx.com/stage");
        IHttpService httpService = DefaultHttpServiceBuilder.get()
            .httpProxyHost("127.0.0.1")
            .httpProxyPort(8888)
            .build();
        IBkNoticeClient bkNoticeClient = new BkNoticeClient(httpService, bkNoticeConfig);
        BkNoticeApp bkNoticeApp = bkNoticeClient.registerApplication();
        log.info("bkNoticeApp=" + bkNoticeApp);
        List<AnnouncementDTO> announcementList = bkNoticeClient.getCurrentAnnouncements(
            BkConsts.HEADER_VALUE_LANG_ZH_CN,
            0,
            10
        );
        log.info("announcementList=" + announcementList);
    }
}
