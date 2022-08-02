package com.royorange.library.core.share

/**
 *  Created by Roy on 2021/8/5
 */
enum class ShareAction {
    /**
     * 自动分享类型
     * 1）如果未设置url、图片、视频、音频，此时分享纯文本
     * 2）如果设置图片，未设置url、视频、音频，此时根据媒体类型，微信中分享纯文本、微博分享混合类型
     * 3）如果设置图片，未设置url、视频、音频，此时根据媒体类型，微信中分享纯文本、微博分享混合类型
     */
    AUTO,
    TEXT,
    IMAGE,
    WEB,
    VIDEO,
    AUDIO
}