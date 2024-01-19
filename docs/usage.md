
### 使用文档
bk-notice-java-sdk 使用文档

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
