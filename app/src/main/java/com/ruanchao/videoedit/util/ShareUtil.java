package com.ruanchao.videoedit.util;

import android.app.Activity;
import android.content.Context;

import com.ruanchao.videoedit.MainApplication;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.ui.video.VideoShowActivity;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMEmoji;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.utils.SocializeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareUtil {

    public static void shareVideo(Activity context, String videoUrl, String imageUrl,
                                  String title, String description, SHARE_MEDIA share_media, UMShareListener listener) {

        UMVideo video = new UMVideo(videoUrl);
        video.setThumb(new UMImage(context, imageUrl));
        video.setTitle(title);
        video.setDescription(description);
        new ShareAction(context).withMedia(video)
                .setPlatform(share_media)
                .setCallback(listener)
                .share();
    }

    public static void shareFile(Activity activity, String path, String text, String title,SHARE_MEDIA share_media,
                                 UMShareListener shareListener){
        File file = new File(path);
        new ShareAction(activity)
                .withFile(file)
                .withText(text)
                .withSubject(title)
                .setPlatform(share_media)
                .setCallback(shareListener).share();
    }
    public static void shareEmoji(Activity activity, String path,SHARE_MEDIA share_media, UMShareListener shareListener) {
        File file = new File(path);
        UMEmoji emoji = new UMEmoji(activity, file);
        emoji.setThumb(new UMImage(activity, R.mipmap.ic_launcher));
        new ShareAction(activity)
                .withMedia(emoji)
                .setPlatform(share_media)
                .setCallback(shareListener).share();
    }
}
