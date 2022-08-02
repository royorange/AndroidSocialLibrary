package com.royorange.library.core.param

import android.app.Activity
import android.content.Context

/**
 *  Created by Roy on 2021/8/11
 */
class ShareVideoParam(activity: Activity):ShareParam(activity) {
    var videoUrl: String? = null
        private set

    fun video(video: String?): ShareParam {
        this.videoUrl = video
        return this
    }
}