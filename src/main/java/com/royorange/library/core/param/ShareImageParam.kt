package com.royorange.library.core.param

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.royorange.library.core.share.ShareAction

/**
 *  Created by Roy on 2021/8/11
 */
class ShareImageParam(activity: Activity):ShareParam(activity) {
    init{
        action(ShareAction.IMAGE)
    }
    var bitmap:Bitmap? = null
        private set
    var images: List<String>? = null
        private set

    fun bitmap(bitmap: Bitmap): ShareImageParam {
        this.bitmap = bitmap
        return this
    }

    fun images(images: List<String>?): ShareParam {
        this.images = images
        return this
    }

    fun image(image: String?): ShareParam {
        if(this.images == null){
            this.images = mutableListOf()
        }
        if(!TextUtils.isEmpty(image)){
            (this.images as MutableList).add(image!!)
        }
        return this
    }
}