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
    public String path;
    private long videoTime;
    private String videoTitle = "";
    private String videoName;
    public float duration;
    private boolean isEditSuccess = false;
    private String imagePath;
    public int width;
    public int height;
    public int rotation;//旋转角度
    public int cutPoint;//剪切的开始点
    public int cutDuration;//剪切的时长

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
