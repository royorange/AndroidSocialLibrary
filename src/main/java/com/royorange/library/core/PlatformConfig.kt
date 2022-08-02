package com.royorange.library.core

/**
 * Created by Roy on 2021/8/5
 */
class PlatformConfig private constructor() {
    var appId: String? = null
    var secret: String? = null
    var redirectUrl: String? = null
    var provider:String? = null

    class Builder {
        private val configs: MutableMap<SocialPlatform, PlatformConfig> = mutableMapOf()

        fun weibo(
            id: String,
            url: String?,
            provider:String?
        ): Builder {
            val config = PlatformConfig()
            config.appId = id
            config.redirectUrl = url
            config.provider = provider
            configs[SocialPlatform.WEIBO] = config
            return this
        }

        fun wechat(id: String,secret: String): Builder{
            val config = PlatformConfig()
            config.appId = id
            config.secret = secret
            configs[SocialPlatform.WECHAT] = config
            return this
        }

        fun build():MutableMap<SocialPlatform, PlatformConfig>{
            return configs
        }
    }
}