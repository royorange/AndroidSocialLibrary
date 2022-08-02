package com.royorange.library.core.api.repo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.royorange.library.core.api.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.round
import kotlin.math.sqrt

/**
 *  Created by Roy on 2022/7/11
 */
class BitmapRepository {
    // Function that makes the network request, blocking the current thread
    suspend fun downloadBitmap(
        downloadUrl: String,limitByteSize:Int
    ): Result<Bitmap> {
        return withContext(Dispatchers.IO) {
            // Blocking network request code
            val url = URL(downloadUrl)
            url.openStream()?.run {
                val raw = BitmapFactory.decodeStream(this)
                var ret = if(limitByteSize>0 && raw.byteCount>limitByteSize){
                    var height:Int
                    var width:Int
                    if(raw.width>raw.height){
                        val base = round(raw.width/raw.height.toFloat())
                        // 默认以argb8888作为图片质量格式，每个像素占4个字节
                        height = sqrt(limitByteSize / (4*base)).toInt()
                        width = (height * base).toInt()
                    }else{
                        val base = round(raw.height/raw.width.toFloat())
                        width = sqrt(limitByteSize / (4*base)).toInt()
                        height = (width * base).toInt()
                    }
                    val newBitmap = Bitmap.createScaledBitmap(raw,width,height,true)
                    raw.recycle()
                    newBitmap
                }else{
                    raw
                }
                return@withContext Result.Success(ret)
            }
            return@withContext Result.Error(Exception("Cannot open HttpURLConnection"))
        }
    }
}