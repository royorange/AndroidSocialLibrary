package com.royorange.library.core.api.repo

import android.util.JsonReader
import com.royorange.library.core.api.model.Result
import com.royorange.library.core.api.model.UserInfo
import com.royorange.library.core.api.model.WechatAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL

/**
 *  Created by Roy on 2022/7/13
 */
class WechatRepository {
    suspend fun requestAuthInfo(
            appId: String,
            secret:String,
            code:String
    ): Result<WechatAuth> {
        return withContext(Dispatchers.IO) {
            // Blocking network request code
            val url = URL("https://api.weixin.qq.com/sns/oauth2/access_token?appid=${appId}&secret=${secret}&code=${code}&grant_type=authorization_code")
            url.openStream()?.run {
                val reader = JsonReader(InputStreamReader(this,"UTF-8"))
                reader.beginObject()
                val auth = WechatAuth()
                var error = ""
                var errorCode = 0
                while (reader.hasNext()){
                    when(reader.nextName()){
                        "access_token" -> {
                            auth.accessToken = reader.nextString()
                        }
                        "expires_in" -> {
                            auth.expiresIn = reader.nextInt()
                        }
                        "refresh_token" -> {
                            auth.refreshToken = reader.nextString()
                        }
                        "openid" -> {
                            auth.openId = reader.nextString()
                        }
                        "scope" -> {
                            auth.scope = reader.nextString()
                        }
                        "errcode"-> {
                            errorCode = reader.nextInt()
                        }
                        "errmsg"-> {
                            error = reader.nextString()
                        }
                        else -> {
                            reader.skipValue()
                        }
                    }
                }
                reader.endObject()
                reader.close()
                if(error.isNotEmpty()){
                    return@withContext Result.Error(Exception("$errorCode:$error"))
                }
                return@withContext Result.Success(auth)
            }
            return@withContext Result.Error(Exception("Cannot open HttpURLConnection"))
        }
    }
    suspend fun requestUserInfo(
            accessToken: String,
            openId:String
    ): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.weixin.qq.com/sns/userinfo?access_token=${accessToken}&openid=${openId}")
            url.openStream()?.run {
                val reader = JsonReader(InputStreamReader(this,"UTF-8"))
                reader.beginObject()
                val user = UserInfo()
                var error = ""
                var errorCode = 0
                while (reader.hasNext()){
                    when(reader.nextName()){
                        "nickname" -> {
                            user.nickname = reader.nextString()
                        }
                        "headimgurl" -> {
                            user.avatar = reader.nextString()
                        }
                        "unionid" -> {
                            user.unionId = reader.nextString()
                        }
                        "errcode"-> {
                            errorCode = reader.nextInt()
                        }
                        "errmsg"-> {
                            error = reader.nextString()
                        }
                        else -> {
                            reader.skipValue()
                        }
                    }
                }
                reader.endObject()
                reader.close()
                if(error.isNotEmpty()){
                    return@withContext Result.Error(Exception("$errorCode:$error"))
                }
                return@withContext Result.Success(user)
            }
            return@withContext Result.Error(Exception("Cannot open HttpURLConnection"))
        }
    }
}