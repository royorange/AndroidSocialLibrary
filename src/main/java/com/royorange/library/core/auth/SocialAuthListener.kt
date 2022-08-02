package com.royorange.library.core.auth

import com.royorange.library.core.SocialPlatform
import com.royorange.library.core.api.model.BaseAuthInfo

/**
 *  Created by Roy on 2022/7/1
 */
interface SocialAuthListener {
    fun onSuccess(platform: SocialPlatform, authInfo: BaseAuthInfo)
    fun onFailure(platform: SocialPlatform, code: Int)
}