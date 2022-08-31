package com.royorange.library.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.royorange.library.core.auth.SocialAuthListener
import com.royorange.library.core.param.*
import com.royorange.library.core.processor.base.BaseProcessor
import com.royorange.library.core.processor.sina.WeiboProcessor
import com.royorange.library.core.processor.wechat.WechatProcessor
import com.royorange.library.core.share.ShareAction

/**
 * Created by Roy on 2021/8/5
 */
class SocialSDK private constructor() {
    /**
     * 分享配置，初始化时需要优先初始化配置
     */
    private lateinit var config: MutableMap<SocialPlatform, PlatformConfig>

    private val processorMap = HashMap<SocialPlatform, BaseProcessor>()

    companion object {
        @JvmStatic
        val instance: SocialSDK by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SocialSDK()
        }

        @JvmStatic
        fun setupConfig(builder: PlatformConfig.Builder){
            if (!instance::config.isInitialized){
                instance.config = builder.build()
            }
        }
    }

    /**
     * 预初始化社交平台sdk，优化一些sdk使用时的异常
     * 必须保证已获取隐私权限, 需要先设置配置项
     *
     */
    fun prefetchSdkInit(context: Context){
        if(!this::config.isInitialized){
            return
        }
        // 微博提前初始化，否则可能无法调起
        if(instance.config[SocialPlatform.WEIBO]!=null){
            instance.buildProcessor(context.applicationContext,SocialPlatform.WEIBO)
        }
    }

    fun provideProcessor(context: Context,platform: SocialPlatform):BaseProcessor{
        return buildProcessor(context,platform)
    }

    /**
     * 获取第三方平台认证信息
     * 由于微博sdk要求通过activity传参获取认证服务，因此统一使用activity
     *
     * @param platform 第三方平台枚举 [SocialPlatform]
     * @param listener 认证信息回调
     */
    fun getAuthorizedInfo(activity: Activity,platform: SocialPlatform,listener: SocialAuthListener?){
        val processor = buildProcessor(activity.applicationContext,platform)
        processor.getAuthorizedData(activity,listener)
    }

    /**
     * 获取第三方平台认证信息
     * 非必须使用activity作为入参时可使用
     *
     * @param context
     * @param platform 第三方平台枚举 [SocialPlatform]
     * @param listener 认证信息回调
     */
    fun getAuthorizedInfo(context: Context,platform: SocialPlatform,listener: SocialAuthListener?){
        val processor = buildProcessor(context.applicationContext,platform)
        processor.getAuthorizedData(context,listener)
    }

    /**
     * 处理部分sdk要求的activity信息接收
     */
    fun handleActivityResult(activity: Activity, requestCode:Int, resultCode:Int, intent:Intent?){
        if(instance.config[SocialPlatform.WEIBO]!=null){
            val processor = provideProcessor(activity,SocialPlatform.WEIBO) as WeiboProcessor
            processor.handleActivityResult(activity, requestCode, resultCode, intent)
        }
    }

    fun shareImage(param: ShareImageParam){
        innerShare(param)
    }

    fun shareWeb(param: ShareWebParam){
        innerShare(param)
    }

    fun share(param: ShareParam){
        when(param){
            is ShareImageParam -> shareImage(param)
            is ShareWebParam -> shareWeb(param)
        }
    }

    private fun innerShare(param:ShareParam){
        if(!this::config.isInitialized){
            error("请先初始化分享配置")
        }
        if(param.platform == null){
            error("platform 未设置")
        }
        val processor = buildProcessor(param.activity.applicationContext,param.platform!!)
        when(param.action){
            ShareAction.VIDEO ->{
                processor.shareVideo(param as ShareVideoParam)
            }
            ShareAction.AUDIO ->{
                processor.shareAudio(param as ShareAudioParam)
            }
            ShareAction.IMAGE ->{
                processor.shareImage(param as ShareImageParam)
            }
            ShareAction.WEB ->{
                processor.shareWeb(param as ShareWebParam)
            }
            else -> {}
        }
    }

    private fun buildProcessor(context: Context,platform: SocialPlatform): BaseProcessor {
        if(processorMap[platform] != null){
           return processorMap[platform]!!
        }
        val processor = when(platform){
            SocialPlatform.WEIBO->{
                WeiboProcessor(context,config[SocialPlatform.WEIBO]?: error("微博config未设置"))
            }
//            SocialPlatform.QQ->{
//                WechatProcessor(context,config[SocialPlatform.QQ]?: error("QQ config未设置"))
//            }
            SocialPlatform.WECHAT,SocialPlatform.WECHAT_TIMELINE->{
                WechatProcessor(context,config[SocialPlatform.WECHAT]?: error("微信config未设置"))
            }
            else->{
                error("platform not support")
            }
        }
        if(!processor.checkConfig()){
            error("share config error,check if the key and secret is set")
        }
        processor.init()
        processorMap[platform] = processor
        return processor
    }

}