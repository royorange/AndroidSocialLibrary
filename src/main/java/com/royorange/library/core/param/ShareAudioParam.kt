package com.royorange.library.core.param

import android.app.Activity
import android.content.Context
import com.royorange.library.core.share.ShareAction

/**
 *  Created by Roy on 2021/8/11
 */
class ShareAudioParam(activity: Activity):ShareParam(activity) {
    init {
        action(ShareAction.AUDIO)
    }
    var audioUrl: String? = null
        private set

    fun audio(audio: String?): ShareParam {
        this.audioUrl = audio
        return this
    }
}