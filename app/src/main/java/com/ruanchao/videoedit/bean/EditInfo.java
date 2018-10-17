package com.ruanchao.videoedit.bean;

import android.media.effect.Effect;

public class EditInfo {

    public static final int EDIT_TYPE_VIDEO_TO_GIF = 0;
    public static final int EDIT_TYPE_VIDEO_FORMAT_CHANGE = 1;
    public static final int EDIT_TYPE_IMAGE_TO_VIDEO = 2;

    public VideoInfo videoInfo;

    public int editType;

    public Music music;

    public WaterInfo waterInfo;

    public GifInfo gifInfo;

    public ImageInfo imageInfo;

    public String videoFormat;

    public static class GifInfo{
        public double startTime;
        public double endTime;
        public int frameRate;
    }

    public static class ImageInfo{
        public int duration;
        public int effect = 0;
    }
}
