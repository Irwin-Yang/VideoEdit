package com.ruanchao.videoedit.ui.video;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.ruanchao.videoedit.base.BasePresenter;
import com.ruanchao.videoedit.bean.Music;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.bean.WaterInfo;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VideoEditPresenter extends BasePresenter<IVideoEditView>{

    private static final String TAG = VideoEditPresenter.class.getSimpleName();
    private Context context;
    private long mOutFileTime = System.currentTimeMillis();
    private String mOutFileName = mOutFileTime + ".mp4";
    private String mTempOutPath = Constans.VIDEO_TEMP_PATH + mOutFileName;
    private String mOutPath = Constans.VIDEO_PATH + File.separator + mOutFileName;

    public VideoEditPresenter(Application context) {
        this.context = context;
        mSubscriptions = new CompositeSubscription();
    }

    public void doEditVideo(final VideoInfo inputVideoInfo, final WaterInfo waterInfo, final Music bgMusicInfo, Subscriber<VideoInfo> subscriber) {
        mSubscriptions.add(Observable.create(new Observable.OnSubscribe<VideoInfo>() {
            @Override
            public void call(Subscriber<? super VideoInfo> subscriber) {
                int result = ffmpegEditVideo(inputVideoInfo,waterInfo,bgMusicInfo);
                //移到视频文件夹
                VideoInfo videoInfo = null;
                if (result == 0 && FileUtil.moveFile(mTempOutPath, Constans.VIDEO_PATH)){
                    videoInfo = new VideoInfo();
                    videoInfo.setVideoPath(mOutPath);
                    videoInfo.setVideoTime(mOutFileTime);
                    videoInfo.setVideoName(mOutFileName);
                    videoInfo.setVideoTitle(DateUtil.timeToDate(mOutFileTime));
                    videoInfo.setType(VideoInfo.TYPE_VIDEO);
                }
                subscriber.onNext(videoInfo);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));

    }

    private int ffmpegEditVideo(VideoInfo inputVideoInfo, WaterInfo mWaterInfo,Music bgMusicInfo) {
        String mInputVideo = inputVideoInfo.getVideoPath();
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("ffmpeg -y -i %s ",mInputVideo));
        if (bgMusicInfo != null && mWaterInfo != null){

        } else if (bgMusicInfo != null){
            String bgMusic = String.format(" -ss %s -t %f -i %s -c:v copy -map 0:v:0 -map 1:a:0",
                    bgMusicInfo.getMusicStartTime(),
                    inputVideoInfo.getDuration()/1000.0,
                    bgMusicInfo.getPath());
            sb.append(bgMusic);
        }else if (mWaterInfo != null) {

            String water = String.format(" -i %s -filter_complex [1:v]scale=90:-1[img1];[0:v][img1]overlay='%s':%s",
                    mWaterInfo.getWaterPath(),
                    mWaterInfo.getxPosition(),
                    mWaterInfo.getyPosition());
            Log.i(TAG,"cmd:" + water);
            sb.append(water);
        }
        sb.append(String.format("  -b:v 1000k %s",mTempOutPath));
        Log.i(TAG,"execute cmd:" + sb.toString());
        return FFmpegCmd.execute(sb.toString());
    }

    public VideoInfo getMediaInfo(VideoInfo inputVideoInfo) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(inputVideoInfo.getVideoPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputVideoInfo.setDuration(mediaPlayer.getDuration());
        return inputVideoInfo;
    }
}
