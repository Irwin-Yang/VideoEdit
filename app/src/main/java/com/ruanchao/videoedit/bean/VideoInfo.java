package com.ruanchao.videoedit.bean;

import android.support.annotation.NonNull;

/**
 * Created by ruanchao on 2018/5/14.
 */

public class VideoInfo implements Comparable<VideoInfo> {

    private String videoPath;
    private long videoTime;
    private String videoTitle;

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public long getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(long videoTime) {
        this.videoTime = videoTime;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    @Override
    public int compareTo(@NonNull VideoInfo liveVideoInfo) {
        int result = (int) (this.getVideoTime() - liveVideoInfo.getVideoTime());
        if(result == 0){
            return 1;
        }
        return -result;
    }
}
