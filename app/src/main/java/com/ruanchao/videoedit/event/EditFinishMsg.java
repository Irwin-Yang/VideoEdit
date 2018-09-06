package com.ruanchao.videoedit.event;

import com.ruanchao.videoedit.bean.VideoInfo;

public class EditFinishMsg {

    private VideoInfo videoInfo;

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public EditFinishMsg(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }
}
