package com.royorange.library.core.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.royorange.library.core.SocialPlatform
import com.royorange.library.core.SocialSDK
import com.royorange.library.core.processor.wechat.WechatProcessor
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * Created by Roy on 2022/7/8
 */
open class WechatEntryActivity : Activity(), IWXAPIEventHandler {
    private var processor = (SocialSDK.instance.provideProcessor(applicationContext,SocialPlatform.WECHAT) as WechatProcessor)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processor.api?.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processor.api?.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq) {
        finish()
    }

    override fun onResp(baseResp: BaseResp) {
        if (baseResp is SendAuth.Resp){
            processor.handleAuthResponse(baseResp)
        }
        finish()
    }
}