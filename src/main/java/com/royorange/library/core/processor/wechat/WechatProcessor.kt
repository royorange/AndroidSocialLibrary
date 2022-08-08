package com.royorange.library.core.processor.wechat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.royorange.library.core.Constants
import com.royorange.library.core.PlatformConfig
import com.royorange.library.core.SocialPlatform
import com.royorange.library.core.api.model.Result
import com.royorange.library.core.api.model.UserInfo
import com.royorange.library.core.api.model.WechatAuth
import com.royorange.library.core.api.repo.BitmapRepository
import com.royorange.library.core.api.repo.WechatRepository
import com.royorange.library.core.auth.SocialAuthListener
import com.royorange.library.core.param.ShareImageParam
import com.royorange.library.core.param.ShareWebParam
import com.royorange.library.core.processor.base.BaseProcessor
import com.royorange.library.core.util.compressIfOverMaxByte
import com.royorange.library.core.util.toByteArray
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 *  Created by Roy on 2021/8/5
 */
class WechatProcessor(context: Context,config:PlatformConfig): BaseProcessor(context,config) {
    lateinit var api: IWXAPI
    private val bitmapRepository = BitmapRepository()
    private val wechatRepository = WechatRepository()
    private var authListener:SocialAuthListener? = null

    override fun init() {
        api = WXAPIFactory.createWXAPI(context.applicationContext, config.appId, true)
        if(!api.isWXAppInstalled){
            Log.e(Constants.TAG,"Wechat not installed")
        }
        api.registerApp(config.appId)
    }

    override fun checkConfig(): Boolean {
        return config.appId?.isNotEmpty()?:false && config.secret?.isNotEmpty()?:false
    }

    override fun getAuthorizedData(context: Context, listener: SocialAuthListener?) {
        this.authListener = listener
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo";
        req.state = "socialSdk${System.currentTimeMillis()}"
        api.sendReq(req)
    }

    override fun shareImage(param: ShareImageParam) {
        val msg = WXMediaMessage()
        if(param.bitmap!=null){
            val obj = if(param.bitmap!!.byteCount>IMAGE_BYTE_SIZE_LIMIT){
                WXImageObject(param.bitmap!!.compressIfOverMaxByte(IMAGE_BYTE_SIZE_LIMIT))
            }else{
                WXImageObject(param.bitmap)
            }
            if(!param.bitmap!!.isRecycled){
                param.bitmap!!.recycle()
            }
            msg.mediaObject = obj
            sendReq(msg,param.platform!!)
        }else if(param.images?.isNotEmpty() == true){
            // todo 下载在线图片
            GlobalScope.launch {
                val bitmap = downloadImageBitmap(param.images!![0])
                if(bitmap!=null){
                    val obj = WXImageObject(bitmap)
                    if(!bitmap.isRecycled){
                        bitmap.recycle()
                    }
                    msg.mediaObject = obj
                    sendReq(msg,param.platform!!)
                }
            }
        }else{
            error("bitmap or images is null")
        }
        
    }

    override fun shareWeb(param: ShareWebParam) {
        GlobalScope.launch {
            val msg = WXMediaMessage()
            msg.title = param.title
            msg.description = param.description
            val webObj = WXWebpageObject()
            webObj.webpageUrl = param.url
            msg.mediaObject = webObj
            if(param.thumbnailUrl!=null){
                val bitmap = downloadImageBitmap(param.thumbnailUrl!![0])
                if(bitmap!=null){
                    msg.thumbData = bitmap.toByteArray()
                    bitmap.recycle()
                }
            }else if(param.thumbnail != null){
                val bitmap = param.thumbnail
                msg.thumbData = bitmap!!.toByteArray()
                bitmap.recycle()
            }else if(param.thumbnailRes != null) {
                val bitmap = BitmapFactory.decodeResource(context.resources,param.thumbnailRes!!)
                msg.thumbData = bitmap.toByteArray()
                bitmap.recycle()
            }else{
                context.applicationContext.packageManager.getApplicationIcon(context.applicationContext.packageName)?.let {
                    val bitmap = it.toBitmap()
                    if (!bitmap.isRecycled){
                        msg.thumbData = bitmap.toByteArray()
                        bitmap.recycle()
                    }
                }
            }
            sendReq(msg,param.platform!!)
        }
    }
    
    private suspend fun downloadImageBitmap(url:String):Bitmap?{
        val result = try {
            // 暂时只处理一个网络图片url
            bitmapRepository.downloadBitmap(url,THUMBNAIL_BYTE_SIZE_LIMIT)
        } catch(e: Exception) {
            Result.Error(Exception("Network request failed,${e.message}"))
        }

        return when (result) {
            is Result.Success<Bitmap> -> {
                result.data
            }
            else -> (result as Result.Error).exception.message?.let {
                Log.e(Constants.TAG,it)
                null
            }
        }
    }

    private fun sendReq(message:WXMediaMessage,platform: SocialPlatform){
        val req = SendMessageToWX.Req()
        val transition = when(message.mediaObject){
            is WXTextObject ->{
                buildTransition("text")
            }
            is WXImageObject ->{
                buildTransition("image")
            }
            is WXWebpageObject ->{
                buildTransition("webpage")
            }
            else -> {
                buildTransition("")
            }
        }
        req.transaction = transition
        req.message = message
        req.scene = when(platform){
            SocialPlatform.WECHAT -> SendMessageToWX.Req.WXSceneSession
            SocialPlatform.WECHAT_TIMELINE -> SendMessageToWX.Req.WXSceneTimeline
            else -> SendMessageToWX.Req.WXSceneSession
        }
        api?.sendReq(req)
    }

    fun handleAuthResponse(resp: SendAuth.Resp){
        when(resp.errCode){
            CODE_AUTH_SUCCESS->{
                GlobalScope.launch {
                    val result = try {
                        wechatRepository.requestAuthInfo(config.appId!!,config.secret!!,resp.code)
                    }catch(e: Exception) {
                        Result.Error(Exception("wechat auth request failed,${e.message}"))
                    }
                    when (result) {
                        is Result.Success<WechatAuth> -> {
                            // 查询用户基本信息
                            val userInfoResult = try {
                                wechatRepository.requestUserInfo(result.data.accessToken!!,result.data.openId!!)
                            }catch(e: Exception) {
                                Result.Error(Exception("wechat auth request failed,${e.message}"))
                            }
                            when (userInfoResult) {
                                is Result.Success<UserInfo> -> {
                                    result.data.userInfo = userInfoResult.data
                                }
                            }
                            authListener?.onSuccess(SocialPlatform.WECHAT,result.data)
                        }
                        else -> {
                            (result as Result.Error).exception?.run {
                                Log.e(Constants.TAG,message?:"")
                            }
                            authListener?.onFailure(SocialPlatform.WECHAT,Constants.AUTH_ERROR)
                        }
                    }
                }
            }
            CODE_AUTH_CANCEL,CODE_AUTH_DENIED->{
                authListener?.onFailure(SocialPlatform.WECHAT,Constants.AUTH_CANCEL)
            }
        }
    }

    private fun buildTransition(type:String):String{
        return type + System.currentTimeMillis()
    }

    companion object {
        const val IMAGE_BYTE_SIZE_LIMIT = 10 * 1024 * 1024
        const val THUMBNAIL_BYTE_SIZE_LIMIT = 32 * 1024
        const val CODE_AUTH_SUCCESS = 0
        const val CODE_AUTH_CANCEL = -2
        const val CODE_AUTH_DENIED = -4
    }
}