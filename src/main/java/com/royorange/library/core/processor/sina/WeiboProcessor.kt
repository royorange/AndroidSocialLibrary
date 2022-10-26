package com.royorange.library.core.processor.sina

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.royorange.library.core.Constants
import com.royorange.library.core.PlatformConfig
import com.royorange.library.core.SocialPlatform
import com.royorange.library.core.api.model.BaseAuthInfo
import com.royorange.library.core.api.repo.BitmapRepository
import com.royorange.library.core.api.model.Result
import com.royorange.library.core.api.model.WeiboAuth
import com.royorange.library.core.auth.SocialAuthListener
import com.royorange.library.core.param.ShareImageParam
import com.royorange.library.core.param.ShareParam
import com.royorange.library.core.param.ShareWebParam
import com.royorange.library.core.processor.base.BaseProcessor
import com.royorange.library.core.util.toByteArray
import com.sina.weibo.sdk.api.*
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.SdkListener
import com.sina.weibo.sdk.openapi.WBAPIFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Roy on 2021/8/5
 */
class WeiboProcessor(context: Context,config: PlatformConfig) : BaseProcessor(context,config){
    private lateinit var weiboApi:IWBAPI
    private val bitmapRepository = BitmapRepository()

    companion object {
        const val TAG = "SocialSDK"
        const val IMAGE_BYTE_SIZE_LIMIT = 300 * 1024
    }

    override fun checkConfig(): Boolean {
        return !TextUtils.isEmpty(config.appId)&&!TextUtils.isEmpty(config.redirectUrl)
    }

    /**
     * 初始化微博sdk
     */
    override fun init() {
        weiboApi = WBAPIFactory.createWBAPI(context)
        weiboApi.registerApp(context, AuthInfo(context,config.appId,config.redirectUrl,""),object: SdkListener{
            override fun onInitSuccess() {
                Log.d(TAG,"weibo init success")
            }

            override fun onInitFailure(p0: Exception?) {
                Log.e(TAG,"weibo init failed:${p0?.message}")
            }
        })
    }

    override fun getAuthorizedData(context: Context, listener: SocialAuthListener?) {
        if(context !is Activity){
            error("for weibo usage, context must be activity")
        }
        weiboApi.authorize(context,object : WbAuthListener {
            override fun onComplete(token: Oauth2AccessToken?) {
                token?.run {
                    val weiboAuth = WeiboAuth()
                    weiboAuth.uid = uid
                    weiboAuth.accessToken = accessToken
                    weiboAuth.refreshToken = refreshToken
                    weiboAuth.expiresIn = expiresTime
                    listener?.onSuccess(SocialPlatform.WEIBO,weiboAuth)
                }
            }

            override fun onError(p0: UiError?) {
                listener?.onFailure(SocialPlatform.WEIBO,Constants.AUTH_ERROR)
            }

            override fun onCancel() {
                listener?.onFailure(SocialPlatform.WEIBO,Constants.AUTH_CANCEL)
            }
        })
    }

    override fun shareImage(param: ShareImageParam) {
        val msg = WeiboMultiMessage()
        msg.textObject = buildText(param)
        val imageObj = ImageObject()
        // 暂时只处理bitmap
        if(param.bitmap!=null){
            imageObj.imageData = param.bitmap!!.toByteArray()
            param.bitmap!!.recycle()
        }
        msg.imageObject = imageObj
        if(param.url!=null){
            GlobalScope.launch {
                msg.mediaObject = buildWeb(param)
                weiboApi.shareMessage(param.activity,msg,true)
            }
        }else{
            weiboApi.shareMessage(param.activity,msg,true)
        }
    }

    override fun shareWeb(param: ShareWebParam) {
        val msg = WeiboMultiMessage()
        msg.textObject = buildText(param)
        GlobalScope.launch {
            msg.mediaObject = buildWeb(param)
            // 由于微博卡片分享的权限难以申请，因此如果含有图片时，图片单独设置
//            if(msg.mediaObject!=null && msg.mediaObject.thumbData!=null){
//                val imageObj = ImageObject()
//                imageObj.imageData = msg.mediaObject.thumbData
//                msg.imageObject = imageObj
//            }
            withContext(Dispatchers.Main){
                weiboApi.shareMessage(param.activity,msg,true)
            }
        }
    }

    private fun buildText(param:ShareParam):TextObject{
        val textObj = TextObject()
        textObj.text = param.title
        return textObj
    }

    private suspend fun buildWeb(param: ShareParam):WebpageObject{
        val webObj = WebpageObject()
        webObj.title = param.title
        webObj.description = param.description
        webObj.identify = UUID.randomUUID().toString()
        // 设置缩略图时，处理成图片
        if(param.thumbnailUrl!=null){
            val result = try {
                // 暂时只处理一个网络图片url
                bitmapRepository.downloadBitmap(param.thumbnailUrl!![0], IMAGE_BYTE_SIZE_LIMIT)
            } catch(e: Exception) {
                Result.Error(Exception("Network request failed"))
            }
            when (result) {
                is Result.Success<Bitmap> -> {
                    webObj.thumbData = result.data.toByteArray()
                    result.data.recycle()
                }
                else -> (result as Result.Error).exception.message?.let {
                    Log.e("WeiboProcessor",it)
                }
            }
        }else if(param.thumbnail != null){
            webObj.thumbData = param.thumbnail!!.toByteArray()
            param.thumbnail!!.recycle()
        }else if(param.thumbnailRes != null) {
            val bitmap = BitmapFactory.decodeResource(param.activity.resources, param.thumbnailRes!!)
            webObj.thumbData = bitmap.toByteArray()
            bitmap.recycle()
        }else{
            context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName).let {
                val bitmap = it.toBitmap()
                webObj.thumbData = bitmap.toByteArray()
            }
        }
        webObj.actionUrl = param.url
        webObj.defaultText = param.title
        return webObj
    }

    fun handleActivityResult(activity: Activity,requestCode:Int,resultCode:Int,intent:Intent?){
        weiboApi.authorizeCallback(activity,requestCode,resultCode,intent)
    }
}