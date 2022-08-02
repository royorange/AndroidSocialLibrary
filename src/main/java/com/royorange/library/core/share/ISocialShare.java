package com.royorange.library.core.share;

import com.royorange.library.core.param.ShareAudioParam;
import com.royorange.library.core.param.ShareImageParam;
import com.royorange.library.core.param.ShareParam;
import com.royorange.library.core.param.ShareVideoParam;
import com.royorange.library.core.param.ShareWebParam;

/**
 * Created by Roy on 2021/8/5
 */
public interface ISocialShare {

    boolean checkConfig();

    void shareImage(ShareImageParam param);

    void shareVideo(ShareVideoParam param);

    void shareAudio(ShareAudioParam param);

    void shareWeb(ShareWebParam param);
}
