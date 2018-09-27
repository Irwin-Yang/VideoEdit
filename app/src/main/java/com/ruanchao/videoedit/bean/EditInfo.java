package com.ruanchao.videoedit.bean;

public class EditInfo {

    public static final int EDIT_TYPE_VIDEO_TO_GIF = 0;
    public static final int EDIT_TYPE_IMAGE_TO_VIDEO = 2;

    public VideoInfo videoInfo;

    public int editType;

    public Music music;

    public WaterInfo waterInfo;

    public GifInfo gifInfo;

    public static class GifInfo{
        public double startTime;
        public double endTime;
        public int frameRate;
    }
}
