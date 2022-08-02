package com.royorange.library.core.auth

import android.content.Context

/**
 *  Created by Roy on 2022/7/1
 */
interface ISocialAuthorizedData {
    fun getAuthorizedData(context: Context,listener: SocialAuthListener?)
}