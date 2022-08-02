package com.royorange.library.core.api.model

/**
 *  Created by Roy on 2022/7/13
 */
class WechatAuth: BaseAuthInfo() {
    var refreshToken:String? = null
    var openId:String? = null
    var scope:String? = null
    var expiresIn:Int? = null
    var userInfo: UserInfo? = null
}