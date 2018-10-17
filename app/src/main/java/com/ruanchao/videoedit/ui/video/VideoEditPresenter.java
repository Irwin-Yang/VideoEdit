package com.ruanchao.videoedit.ui.video;

import android.app.Application;
import android.content.Context;
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

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoEditPresenter extends BasePresenter<IVideoEditView>{

    private static final String TAG = VideoEditPresenter.class.getSimpleName();
    private Context context;
    private long mOutFileTime = System.currentTimeMillis();
    private String mOutFileName = mOutFileTime + ".mp4";
    private String mTempOutPath = Constans.VIDEO_TEMP_PATH + mOutFileName;
    private String mOutPath = Constans.VIDEO_PATH + File.separator + mOutFileName;

    public VideoEditPresenter(Application context) {
        this.context = context;
    }

    public void doEditVideo(final VideoInfo inputVideoInfo, final WaterInfo waterInfo, final Music bgMusicInfo, Subscriber<VideoInfo> subscriber) {
        mSubscriptions.add(Observable.create(new Observable.OnSubscribe<VideoInfo>() {
            @Override
            public void call(Subscriber<? super VideoInfo> subscriber) {
                int result = -1;
                try {
                    result = ffmpegEditVideo(inputVideoInfo,waterInfo,bgMusicInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //移到视频文件夹
                if (result == 0 && FileUtil.moveFile(mTempOutPath, Constans.VIDEO_PATH)){
                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.setPath(mOutPath);
                    videoInfo.setVideoTime(mOutFileTime);
                    videoInfo.setVideoName(mOutFileName);
                    videoInfo.setVideoTitle(DateUtil.timeToDate(mOutFileTime));
                    videoInfo.setEditSuccess(true);
                    videoInfo.setType(VideoInfo.TYPE_VIDEO);
                    subscriber.onNext(videoInfo);
                }else {
                    FileUtil.moveFile(inputVideoInfo.getPath(), Constans.VIDEO_PATH);
                    inputVideoInfo.setEditSuccess(false);
                    subscriber.onNext(inputVideoInfo);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));

    }

    private int ffmpegEditVideo(VideoInfo inputVideoInfo, WaterInfo mWaterInfo,Music bgMusicInfo) throws Exception{
        String mInputVideo = inputVideoInfo.getPath();
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("ffmpeg -y -threads 2 -i %s ",mInputVideo));
        //控制视频最大时长为20s,避免视频过大耗时
        double editVideoDuration = inputVideoInfo.getDuration() >20 ? 20:inputVideoInfo.getDuration();

        if (bgMusicInfo != null && mWaterInfo != null){

            String cmd = String.format("-i %s -ss %s -t %f -i %s " +
                    "-filter_complex [1:v]scale=90:-1[img1];[0:v][img1]overlay='%s':%s[v1];[0:a][2:a]amix=inputs=2:duration=first[v2] " +
                    "-map [v2] -map [v1] ",
                    mWaterInfo.getWaterPath(),
                    bgMusicInfo.getMusicStartTime(),
                    editVideoDuration,
                    bgMusicInfo.getPath(),
                    mWaterInfo.getxPosition(),
                    mWaterInfo.getyPosition()
                    );
            sb.append(cmd);

        } else if (bgMusicInfo != null){
            String bgMusic = String.format(" -ss %s -t %f -i %s -filter_complex [0:a][1:a]amix=inputs=2:duration=first[aout] -map 0:v:0 -map [aout] -ac 2 ",
                    bgMusicInfo.getMusicStartTime(),
                    editVideoDuration,
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
        sb.append(String.format(" -pix_fmt yuv420p -ar 44100 -b:v 1400k %s",mTempOutPath));
        Log.i(TAG,"execute cmd:" + sb.toString());
        return FFmpegCmd.execute(sb.toString());
    }
}
