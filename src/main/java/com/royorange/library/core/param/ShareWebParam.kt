package com.royorange.library.core.param

import android.app.Activity
import android.content.Context
import com.royorange.library.core.share.ShareAction

/**
 *  Created by Roy on 2021/8/11
 */
class ShareWebParam(activity: Activity):ShareParam(activity) {
    init {
        action(ShareAction.WEB)
    }
}