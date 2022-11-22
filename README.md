# AndroidSocialLibrary
由于第三方（umeng之流）各种sdk内部混乱且权限请求很迷惑，因此自己造轮子用于分享、第三方登录等。
对应社媒的初始化会在使用对应api时惰性进行，开机可不需要提前获取相应权限；也可以手动提前初始化。

目前微博的sdk在初始化时会自动请求权限，因此需要再之前进行权限声明以避免被平台拒绝。

### 目前支持的社媒及sdk版本
1. 微信
  - 版本：最新
  - 功能：纯文本、图文、图片、url分享，微信授权登录/绑定
  - 暂未支持：视频、音乐分享
2. 新浪微博
  - 版本：11.12.0
  - 功能：纯文本、图文、图片分享, 注意：**由于URL分享至微博卡片效果较难申请，使用一般的url拼接文字的形式**
  - 暂未支持：微博登录，其他分享

### ❌ 已知问题BUG
1. 微博分享时，首次有时会没有跳转打开微博app

## 项目配置
根据需要添加对应的社媒sdk，不用可以不加以减少包大小
```
  implementation 'com.tencent.mm.opensdk:wechat-sdk-android:+'
  implementation 'io.github.sinaweibosdk:core:11.12.0@aar'
```

## 初始化
初始化仅设置id、key等信息，不会获取用户权限
```
SocialSDK.setupConfig(PlatformConfig.Builder()
 .wechat(微信ID, 微信SECRET)
 .weibo(微博id, 微博redirect url, 自定义provider))
```
