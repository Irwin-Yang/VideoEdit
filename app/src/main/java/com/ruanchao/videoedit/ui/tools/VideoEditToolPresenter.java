package com.ruanchao.videoedit.ui.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ruanchao.videoedit.base.BasePresenter;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FFmpegUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.tool.ImageToVideoView;

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
        switch (editInfo.editType) {
            case EditInfo.EDIT_TYPE_VIDEO_TO_GIF:
                result = videoToGif(editInfo);
                break;
            case EditInfo.EDIT_TYPE_IMAGE_TO_VIDEO:
                result = imageToVideo(editInfo);
                break;
            default:
                break;
        }
        return result;
    }

    private int imageToVideo(EditInfo editInfo) {

        String outTempPath = Constans.VIDEO_TEMP_PATH + mOutFileTime + ".mp4";
        String outPath = Constans.VIDEO_PATH + "/" + mOutFileTime + ".mp4";
        String cmd = "";
        if (editInfo.imageInfo.effect == ImageToVideoView.EFFECT_NO){
            cmd =  String.format("ffmpeg -y -threads 2 -loop 1  -i  %s -t %d -r 25 " +
                            "-vf scale=480:-1 -y %s",
                    editInfo.videoInfo.getVideoPath(),
                    editInfo.imageInfo.duration,
                    outTempPath);
        }else if (editInfo.imageInfo.effect == ImageToVideoView.EFFECT_ZOOM){

            //使用Options类来获取
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//这个参数设置为true才有效，
            Bitmap bmp = BitmapFactory.decodeFile(editInfo.videoInfo.getVideoPath(), options);//这里的bitmap是个空
            int outHeight=options.outHeight;
            int outWidth= options.outWidth;
            double scale = outWidth/480.0;
            int height = (int) (outHeight /scale);
            cmd = String.format("ffmpeg -threads 2 -loop 1 -i %s -vf " +
                            "zoompan=z='if(lte(on,%d),zoom+0.003,zoom-0.003)':d=%d:fps=25:s=480x%d -t %d %s",
                    editInfo.videoInfo.getVideoPath(),
                    editInfo.imageInfo.duration * 25/2,
                    editInfo.imageInfo.duration * 25,
                    height,
                    editInfo.imageInfo.duration,
                    outTempPath);
        }
        Log.i(TAG,"cmd:" + cmd);
        int result = FFmpegCmd.execute(cmd);
        //移到视频文件夹
        if (result == 0 && FileUtil.moveFile(outTempPath, Constans.VIDEO_PATH)){
            editInfo.videoInfo.setVideoPath(outPath);
            editInfo.videoInfo.setVideoTime(mOutFileTime);
            editInfo.videoInfo.setVideoName(mOutFileTime + ".mp4");
            editInfo.videoInfo.setVideoTitle(DateUtil.timeToDate(mOutFileTime));
            editInfo.videoInfo.setEditSuccess(true);
            editInfo.videoInfo.setType(VideoInfo.TYPE_VIDEO);
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
        String cmd = String.format("ffmpeg -y -threads 2 -ss %f -t %f -i %s -r %d %s",
                gifInfo.startTime,
                gifInfo.endTime,
                editInfo.videoInfo.getVideoPath(),
                gifInfo.frameRate, outTempPath);
        Log.i(TAG,"cmd:" + cmd);
        int result = FFmpegCmd.execute(cmd);
        //移到视频文件夹
        if (result == 0 && FileUtil.moveFile(outTempPath, Constans.IMAGE_PATH)){
            editInfo.videoInfo.setImagePath(outPath);
            editInfo.videoInfo.setVideoTime(mOutFileTime);
            editInfo.videoInfo.setVideoName(mOutFileTime + ".gif");
            editInfo.videoInfo.setVideoTitle(DateUtil.timeToDate(mOutFileTime));
            editInfo.videoInfo.setEditSuccess(true);
            editInfo.videoInfo.setType(VideoInfo.TYPE_GIF);
        }
        return result;
    }
}
