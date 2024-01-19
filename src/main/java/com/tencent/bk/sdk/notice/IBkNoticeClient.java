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


import com.tencent.bk.sdk.notice.model.resp.AnnouncementDTO;
import com.tencent.bk.sdk.notice.model.resp.BkNoticeApp;

import java.util.List;

public interface IBkNoticeClient {

    /**
     * 将当前系统注册到消息通知中心
     *
     * @return 注册成功的系统信息
     */
    BkNoticeApp registerApplication();

    /**
     * 获取当前对当前系统生效的公告列表
     *
     * @param bkLanguage 蓝鲸国际化语言Header取值，具体取值见{@link com.tencent.bk.sdk.notice.consts.BkConsts}
     * @param offset     偏移量
     * @param limit      返回的最大结果数量
     * @return 对当前系统生效的公告列表
     */
    List<AnnouncementDTO> getCurrentAnnouncements(String bkLanguage, Integer offset, Integer limit);

}
