package com.ruanchao.videoedit.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by ruanchao on 2018/5/14.
 */

public class VideoInfo implements Serializable,Comparable<VideoInfo> {


    private static final long serialVersionUID=1L;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_GIF = 2;
    private int type = TYPE_VIDEO;
    private String videoPath;
    private long videoTime;
    private String videoTitle = "";
    private String videoName;
    private float duration;
    private boolean isEditSuccess = false;
    private String imagePath;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int compareTo(@NonNull VideoInfo liveVideoInfo) {
        int result = (int) (this.getVideoTime() - liveVideoInfo.getVideoTime());
        if(result == 0){
            return 1;
        }
        return -result;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public boolean isEditSuccess() {
        return isEditSuccess;
    }

    public void setEditSuccess(boolean editSuccess) {
        isEditSuccess = editSuccess;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}
