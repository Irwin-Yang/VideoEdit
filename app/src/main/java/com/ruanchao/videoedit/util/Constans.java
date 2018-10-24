package com.ruanchao.videoedit.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by ruanchao on 2018/7/5.
 */

public class Constans {

    public static final String SDCARD = Environment
            .getExternalStorageDirectory().getAbsoluteFile() + File.separator;
    public static final String FFMPEG_PATH = SDCARD + "smallvideo" + File.separator;

    // 设置拍摄视频临时路径
    public static final String VIDEO_TEMP_PATH = FFMPEG_PATH + "temp" + File.separator;

    // 资源路径
    public static final String VIDEO_SOURCE_PATH = FFMPEG_PATH + "source" + File.separator;

    // 设置拍摄视频路径
    public static final String VIDEO_PATH = FFMPEG_PATH + "video";

    // 设置图片路径
    public static final String IMAGE_PATH = FFMPEG_PATH + "image";

    // 设置音频路径
    public static final String AUDIO_PATH = FFMPEG_PATH + "audio";
    // 设置背景音乐路径
    public static final String MUSIC_PATH = FFMPEG_PATH + "music";

    public static final String VIDEO_PREF = "video_pref";

    public static final String IS_LOADED_RESOURCE = "is_loaded_resource";

    public static final int TYPE_VIDEO_EDIT = 1;

    public static final String VIDEO_CATEGORY = "category";
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2;

    public static final double VIDEO_HANDLE_MAX_DURATION = 20;

}
