package com.royorange.library.core.api.model

/**
 *  Created by Roy on 2022/7/13
 */
class WeiboAuth: BaseAuthInfo() {
    var uid:String? = null
    var refreshToken:String? = null
    var expiresIn:Long? = null
}