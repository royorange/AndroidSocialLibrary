package com.royorange.library.core.processor.base

import android.content.Context
import com.royorange.library.core.PlatformConfig
import com.royorange.library.core.auth.ISocialAuthorizedData
import com.royorange.library.core.param.ShareAudioParam
import com.royorange.library.core.param.ShareImageParam
import com.royorange.library.core.param.ShareVideoParam
import com.royorange.library.core.param.ShareWebParam
import com.royorange.library.core.share.ISocialShare

/**
 *  Created by Roy on 2021/8/5
 */
abstract class BaseProcessor(val context: Context,var config: PlatformConfig) :
    ISocialShare,ISocialAuthorizedData {

    abstract fun init()

    override fun shareImage(param: ShareImageParam) {
    }

    override fun shareVideo(param: ShareVideoParam) {
    }

    override fun shareAudio(param: ShareAudioParam) {
    }

    override fun shareWeb(param: ShareWebParam) {
    }

}