package com.ruanchao.videoedit.ui.tools;

import android.content.Context;
import android.util.Log;

import com.ruanchao.videoedit.base.BasePresenter;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FFmpegUtil;
import com.ruanchao.videoedit.util.FileUtil;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoEditToolPresenter extends BasePresenter<IVideoEditToolView>{

    private static final String TAG = VideoEditToolPresenter.class.getSimpleName();
    private Context context;
    private long mOutFileTime = System.currentTimeMillis();
    public VideoEditToolPresenter(Context context){
        this.context = context;
    }

    public void doEditVideo(final EditInfo editInfo, Subscriber<EditInfo> subscriber){

        mSubscriptions.add(Observable.create(new Observable.OnSubscribe<EditInfo>() {
            @Override
            public void call(Subscriber<? super EditInfo> subscriber) {
                int result = -1;
                try {
                    result = editVideo(editInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (result == 0){
                    subscriber.onNext(editInfo);
                }else {
                    subscriber.onError(new Throwable());
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }

    private int editVideo(EditInfo editInfo){
        int result = -1;
        switch (editInfo.editType){
            case EditInfo.EDIT_TYPE_VIDEO_TO_GIF:
                result = videoToGif(editInfo);
                break;
                default:
                    break;
        }
        return result;
    }

    private int videoToGif(EditInfo editInfo) {
        EditInfo.GifInfo gifInfo = editInfo.gifInfo;
        File file = new File(Constans.VIDEO_TEMP_PATH);
        if (!file.exists()){
            if (!file.mkdirs()){
                return -1;
            }
        }
        String outTempPath = Constans.VIDEO_TEMP_PATH + mOutFileTime + ".gif";
        String outPath = Constans.IMAGE_PATH + "/" + mOutFileTime + ".gif";
        String cmd = String.format("ffmpeg -ss %f -t %f -i %s -r %d %s",
                gifInfo.startTime,
                gifInfo.endTime,
                editInfo.videoInfo.getVideoPath(),
                gifInfo.frameRate, outTempPath);
        Log.i(TAG,"cmd:" + cmd);
        int result = FFmpegCmd.execute(cmd);
        //移到视频文件夹
        if (result == 0 && FileUtil.moveFile(outTempPath, Constans.IMAGE_PATH)){
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setImagePath(outPath);
            videoInfo.setVideoTime(mOutFileTime);
            videoInfo.setVideoName(mOutFileTime + ".gif");
            videoInfo.setVideoTitle(DateUtil.timeToDate(mOutFileTime));
            videoInfo.setEditSuccess(true);
            videoInfo.setType(VideoInfo.TYPE_GIF);
        }
        return result;
    }
}
