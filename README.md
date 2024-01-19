

[![license](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://github.com/TencentBlueKing/bk-notice-java-sdk/blob/master/LICENSE.txt) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/TencentBlueKing/bk-notice-java-sdk/pulls)

## Overview

bk-notice-java-sdk是蓝鲸Java系产品用于对接蓝鲸消息通知中心的一套开发工具包。  

## Features

- 注册应用到消息通知中心
- 获取当前生效的所有活动通知与平台公告

## Getting started

### Installation

### maven
```
<dependency>
  <groupId>com.tencent.bk.sdk</groupId>
  <artifactId>bk-notice-java-sdk</artifactId>
  <version>${version}</version>
</dependency>
```

#### gradle
```
implementation "com.tencent.bk.sdk:bk-notice-java-sdk:${version}"
```

### Usage

#### 1. 引入依赖包
Maven坐标：  
com.tencent.bk.sdk:bk-notice-java-sdk:${version}  


#### 2. SDK对外提供的接口
消息通知中心客户端接口：    
com.tencent.bk.sdk.notice.IBkNoticeClient  

默认实现：  
com.tencent.bk.sdk.notice.impl.BkNoticeClient  
 

#### 3. 使用SDK内提供的默认实现
（1）注册应用到消息通知中心  
```java
BkNoticeConfig bkNoticeConfig = new BkNoticeConfig();
bkNoticeConfig.setAppCode("appCode");
bkNoticeConfig.setAppSecret("appSecret");
bkNoticeConfig.setApiBaseUrl("https://bk-notice.apigw.xxx.com/stage");
IBkNoticeClient bkNoticeClient = new BkNoticeClient(bkNoticeConfig);
BkNoticeApp bkNoticeApp = bkNoticeClient.registerApplication();
```
（2）获取当前生效的所有活动通知与平台公告  
```java
BkNoticeConfig bkNoticeConfig = new BkNoticeConfig();
bkNoticeConfig.setAppCode("appCode");
bkNoticeConfig.setAppSecret("appSecret");
bkNoticeConfig.setApiBaseUrl("https://bk-notice.apigw.xxx.com/stage");
IBkNoticeClient bkNoticeClient = new BkNoticeClient(bkNoticeConfig);
List<AnnouncementDTO> announcementList = bkNoticeClient.getCurrentAnnouncements(BkConsts.HEADER_VALUE_LANG_ZH_CN, 0, 10);
```

#### 4. 自定义底层HTTP请求工具
该SDK使用Apache HttpClient库发起Http请求，如须自定义该行为（使用OkHttp、添加监控指标等），可注入自定义的Http服务类实现，实现步骤如下：  

（1）编写自定义的IHttpService实现类
```java
public class MyHttpService implements IHttpService {

    public MyHttpService() {
    }

    @Override
    public String doHttpGet(String uri, Header[] headers) throws IOException {
        // xxx
        return "xxx";
    }

    @Override
    public String doHttpPost(String uri, Header[] headers, Object body) throws IOException {
        // xxx
        return "xxx";
    }
}
```
（2）使用自定义的IHttpService实现类初始化BkNoticeClient并使用
```java
BkNoticeConfig bkNoticeConfig = new BkNoticeConfig();
bkNoticeConfig.setAppCode("appCode");
bkNoticeConfig.setAppSecret("appSecret");
bkNoticeConfig.setApiBaseUrl("https://bk-notice.apigw.xxx.com/stage");
IHttpService httpService = new MyHttpService();
IBkNoticeClient bkNoticeClient = new BkNoticeClient(httpService, bkNoticeConfig);
List<AnnouncementDTO> announcementList = bkNoticeClient.getCurrentAnnouncements(BkConsts.HEADER_VALUE_LANG_ZH_CN, 0, 10);
```

## Roadmap

TODO

## Support

- [蓝鲸论坛](https://bk.tencent.com/s-mart/community)
- [蓝鲸 DevOps 在线视频教程](https://bk.tencent.com/s-mart/video/)
- 联系我们，技术交流QQ群：

<img src="https://github.com/Tencent/bk-PaaS/raw/master/docs/resource/img/bk_qq_group.png" width="250" hegiht="250" align=center />


## BlueKing Community

- [BK-CI](https://github.com/Tencent/bk-ci)：蓝鲸持续集成平台是一个开源的持续集成和持续交付系统，可以轻松将你的研发流程呈现到你面前。
- [BK-BCS](https://github.com/Tencent/bk-bcs)：蓝鲸容器管理平台是以容器技术为基础，为微服务业务提供编排管理的基础服务平台。
- [BK-PaaS](https://github.com/Tencent/bk-PaaS)：蓝鲸PaaS平台是一个开放式的开发平台，让开发者可以方便快捷地创建、开发、部署和管理SaaS应用。
- [BK-SOPS](https://github.com/Tencent/bk-sops)：标准运维（SOPS）是通过可视化的图形界面进行任务流程编排和执行的系统，是蓝鲸体系中一款轻量级的调度编排类SaaS产品。
- [BK-CMDB](https://github.com/Tencent/bk-cmdb)：蓝鲸配置平台是一个面向资产及应用的企业级配置管理平台。
- [BK-JOB](https://github.com/Tencent/bk-job)：蓝鲸作业平台(Job)是一套运维脚本管理系统，具备海量任务并发处理能力。

## Contributing

如果你有好的意见或建议，欢迎给我们提 Issues 或 Pull Requests，为蓝鲸开源社区贡献力量。

## License

基于 MIT 协议， 详细请参考[LICENSE](LICENSE.txt)
