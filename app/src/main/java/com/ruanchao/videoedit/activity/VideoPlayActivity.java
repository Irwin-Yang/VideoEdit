package com.ruanchao.videoedit.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.bean.WaterInfo;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.AddWaterView;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoPlayActivity extends BaseActivity implements View.OnClickListener, Toolbar.OnMenuItemClickListener, InvokeListener {

    private static final String VIDEO_PATH = "video_path";
    private static final int WHAT_ADD_DANMU = 100;
    private static final String TAG = VideoPlayActivity.class.getSimpleName();
    Toolbar toolbar;
    TextView mAddBgm;
    TextView mAddWatermark;
    TextView mAddDrawText;
    String mInputVideo;
    String mInputBgm;
    String mInputLogo;
    long mOutFileTime = System.currentTimeMillis();
    String mOutFileName = mOutFileTime + ".mp4";
    String mTempOutPath = Constans.VIDEO_TEMP_PATH + mOutFileName;
    String mOutPath = Constans.VIDEO_PATH + File.separator + mOutFileName;
    long mDuration;//时长(毫秒)
    ProgressDialog mProgressDialog;
    Toast mToast;
    int mVideoWidth;
    int mVideoHeight;
    private BottomSheetDialog mBottomSheetDialog;
    private WaterInfo mWaterInfo;
    boolean hasBgMusic = true;
    boolean hasSubtitle = true;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private Uri imageUri;
    private AddWaterView mAddWaterView;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_ADD_DANMU:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }

    private void init() {
        mToast = Toast.makeText(this,"", Toast.LENGTH_LONG);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.video_edit_menu);
        toolbar.setOnMenuItemClickListener(this);
        VideoView mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setMediaController(new MediaController(this));
        mInputVideo = getIntent().getStringExtra(VIDEO_PATH);
        Uri videoUri = Uri.parse(mInputVideo);
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
        mAddBgm = findViewById(R.id.tv_add_bgm);
        mAddBgm.setOnClickListener(this);
        mAddWatermark = findViewById(R.id.tv_add_watermark);
        mAddWatermark.setOnClickListener(this);
        mAddDrawText = findViewById(R.id.tv_add_draw_text);
        mAddDrawText.setOnClickListener(this);
        getMediaInfo();
    }



    public static void start(Context context, String videoPath){
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra(VIDEO_PATH,videoPath);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_add_bgm:
                mInputBgm = Constans.VIDEO_SOURCE_PATH + "bg1.mp3";
                break;
            case R.id.tv_add_watermark:
                mInputLogo = Constans.VIDEO_SOURCE_PATH + "icon1.png";
                showAddWater();
                break;
            case R.id.tv_add_draw_text:
                break;
                default:
                    break;
        }
    }

    /**
     *  获取TakePhoto实例
     * @return
     */
    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, mTakeResultListener));
        }
        return takePhoto;
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_success){
            doEditVideo();
        }
        return true;
    }

    private void doEditVideo() {

        Observable.create(new Observable.OnSubscribe<VideoInfo>() {
            @Override
            public void call(Subscriber<? super VideoInfo> subscriber) {
                int result = ffmpegEditVideo();
               //移到视频文件夹
                VideoInfo videoInfo = null;
                if (result == 0 && FileUtil.moveFile(mTempOutPath, Constans.VIDEO_PATH)){
                    videoInfo = new VideoInfo();
                    videoInfo.setVideoPath(mOutPath);
                    videoInfo.setVideoTime(mOutFileTime);
                    videoInfo.setVideoTitle(mOutFileName);
                }
                subscriber.onNext(videoInfo);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<VideoInfo>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        showProgressDialog("提示","视频编辑中，请稍后。。。");
                    }

                    @Override
                    public void onCompleted() {
                        if (mProgressDialog != null){
                            mProgressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(VideoPlayActivity.this,"执行失败", Toast.LENGTH_LONG).show();
                        if (mProgressDialog != null){
                            mProgressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onNext(VideoInfo videoInfo) {
                        Toast.makeText(VideoPlayActivity.this,"执行结束", Toast.LENGTH_LONG).show();
                        if (videoInfo != null) {
                            //EventBus.getDefault().post(new FinishVideoMsgEvent(videoInfo));
                            mToast.setText("视频保存在：" + mOutPath);
                            mToast.show();
                        }
                        finish();
                    }
                });

    }

    private void getMediaInfo() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mInputVideo);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDuration = mediaPlayer.getDuration();
        mVideoWidth = mediaPlayer.getVideoWidth();
        mVideoHeight = mediaPlayer.getVideoHeight();
    }

    private int ffmpegEditVideo() {

        StringBuffer sb = new StringBuffer();
        sb.append(String.format("ffmpeg -y -i %s ",mInputVideo));
        String cmd = null;
        if (mWaterInfo != null) {

             cmd = String.format(" -i %s -filter_complex [1:v]scale=90:-1[img1];[0:v][img1]overlay='%s':%s",
                     mWaterInfo.getWaterPath(),
                     mWaterInfo.getxPosition(),
                     mWaterInfo.getyPosition());
            Log.i(TAG,"cmd:" + cmd);
        }

        sb.append(String.format("  -b:v 1000k %s",mTempOutPath));
        if (cmd != null) {
            return FFmpegCmd.execute(sb.toString());
        }
        return -1;
    }

    private void showProgressDialog(String title, String msg){
        mProgressDialog = ProgressDialog.show(this, title, msg, false, false);
    }

    public void showAddWater() {
        mBottomSheetDialog = new BottomSheetDialog(this);
        mAddWaterView = new AddWaterView(VideoPlayActivity.this);
        mAddWaterView.setDefaultEndTime(mDuration);
        mAddWaterView.setOnSubmitListener(new AddWaterView.OnSubmitListener() {
            @Override
            public void onSubmit(WaterInfo waterInfo) {
                mWaterInfo = waterInfo;
                mBottomSheetDialog.dismiss();
                mAddWatermark.setBackground(getResources().getDrawable(R.drawable.edit_bg_shape_click));
            }

            @Override
            public void onChoosePhoto() {
                choosePhoto();
            }

            @Override
            public void onChooseFile() {
                chooseFileImage();
            }
        });
        mBottomSheetDialog.setContentView(mAddWaterView);
        mBottomSheetDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, invokeParam, mTakeResultListener);
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }

    TakePhoto.TakeResultListener mTakeResultListener = new TakePhoto.TakeResultListener() {
        @Override
        public void takeSuccess(TResult result) {
            Log.i(TAG, "takeSuccess：" + result.getImage().getOriginalPath());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAddWaterView.setPhotoImagePath(imageUri.getPath());
                }
            });
        }

        @Override
        public void takeFail(TResult result, String msg) {
            Log.i(TAG, "takeFail:" + msg);
        }

        @Override
        public void takeCancel() {
            Log.i(TAG, getResources().getString(R.string.msg_operation_canceled));
        }
    };

    public void choosePhoto() {
        File file = new File(Constans.VIDEO_TEMP_PATH + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        imageUri = Uri.fromFile(file);

        getTakePhoto().onPickFromGalleryWithCrop(imageUri, getCropOptions());
    }

    private void chooseFileImage() {
        getTakePhoto().onPickFromDocumentsWithCrop(imageUri, getCropOptions());
    }

    private CropOptions getCropOptions(){
        CropOptions.Builder builder = new CropOptions.Builder();
        builder.setOutputX(400).setOutputY(400);
        builder.setWithOwnCrop(true);
        CropOptions cropOptions = builder.create();
        return cropOptions;
    }


}
