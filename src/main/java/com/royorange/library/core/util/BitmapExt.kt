package com.royorange.library.core.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.round
import kotlin.math.sqrt

/**
 *  Created by Roy on 2022/7/8
 */  
fun Bitmap.compressSize(maxSize:Int):Bitmap{
    val baos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, baos)
    var options = 100
    while (baos.toByteArray().size > maxSize && options > 0) {
        baos.reset()
        compress(Bitmap.CompressFormat.JPEG, options, baos)
        options -= 5
    }
    return this
}

fun Bitmap.compressIfOverMaxByte(limitByteSize:Int):Bitmap{
    return if(byteCount>limitByteSize){
        val scaledHeight:Int
        val scaledWidth:Int
        if(width>height){
            val base = round(width/height.toFloat())
            // 默认以argb8888作为图片质量格式，每个像素占4个字节
            scaledHeight = sqrt(limitByteSize / (4*base)).toInt()
            scaledWidth = (height * base).toInt()
        }else{
            val base = round(height/width.toFloat())
            scaledWidth = sqrt(limitByteSize / (4*base)).toInt()
            scaledHeight = (width * base).toInt()
        }
        val newBitmap = Bitmap.createScaledBitmap(this,scaledWidth,scaledHeight,true)
        this.recycle()
        newBitmap
    }else{
        this
    }
}

fun Bitmap.createNewBitmap(maxSize: Int):Bitmap{
    val baos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, baos)
    var options = 100
    while (baos.toByteArray().size > maxSize && options > 0) {
        baos.reset()
        compress(Bitmap.CompressFormat.JPEG, options, baos)
        options -= 5
    }
    return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().size)
}

fun Bitmap.toByteArray():ByteArray{
    val baos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, baos)
    return baos.toByteArray()
}