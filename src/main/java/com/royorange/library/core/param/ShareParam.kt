package com.royorange.library.core.param

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.royorange.library.core.share.ShareAction
import com.royorange.library.core.SocialPlatform

/**
 * Created by Roy on 2021/8/5
 */
open class ShareParam(val activity:Activity) {
    var title: String? = null
        private set
    var description: String? = null
        private set
    var url: String? = null
        private set
    var thumbnail: Bitmap? = null
        private set
    var thumbnailUrl: MutableList<String>? = null
        private set
    var thumbnailRes: Int? = null
        private set
    var platform: SocialPlatform? = null
        private set
    var action: ShareAction? = null
        private set

    fun platform(platform: SocialPlatform): ShareParam {
        this.platform = platform
        return this
    }

    fun title(title: String?): ShareParam {
        this.title = title
        return this
    }

    fun description(description: String?): ShareParam {
        this.description = description
        return this
    }

    fun url(url: String?): ShareParam {
        this.url = url
        return this
    }

    fun thumbnail(thumbnail: Bitmap?): ShareParam {
        this.thumbnail = thumbnail
        return this
    }

    fun thumbnail(thumbnail: String?): ShareParam {
        if(thumbnailUrl == null) {
            thumbnailUrl = mutableListOf()
        }
        if(thumbnail!=null){
            thumbnailUrl!!.add(thumbnail)
        }
        return this
    }

    fun thumbnail(thumbnail: Int): ShareParam {
        this.thumbnailRes = thumbnail
        return this
    }

    fun action(action: ShareAction):ShareParam{
        this.action = action
        return this
    }

}