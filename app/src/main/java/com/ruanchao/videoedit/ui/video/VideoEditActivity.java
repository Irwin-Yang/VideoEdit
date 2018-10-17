package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.ruanchao.videoedit.MainApplication;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseMvpActivity;
import com.ruanchao.videoedit.bean.Music;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.bean.WaterInfo;
import com.ruanchao.videoedit.event.EditFinishMsg;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.AddWaterView;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.Serializable;

import rx.Subscriber;

public class VideoEditActivity extends BaseMvpActivity<IVideoEditView,VideoEditPresenter> implements View.OnClickListener, IVideoEditView, InvokeListener, MediaPlayer.OnCompletionListener {

    private static final String VIDEO_INFO = "video_info";
    private static final String TAG = VideoEditActivity.class.getSimpleName();
    private CommonTitleBar toolbar;
    private TextView mAddBgm;
    private TextView mAddWatermark;
    private TextView mAddDrawText;
    private Toast mToast;
    private VideoInfo mInputVideoInfo;
    private BottomSheetDialog mBottomSheetDialog;
    private WaterInfo mWaterInfo;
    private Music mMusic;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private Uri imageUri;
    private AddWaterView mAddWaterView;
    private String mMusicPath;
    private View mShadeView;
    private Button mChooseVideo;
    private VideoView mVideoView;
    ImageView mVideoPlayView;
    public static final int REQUEST_CODE = 100;
    private static final int CHOOSE_VIDEO_CODE = 101;

    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        init();
    }

    @Override
    public VideoEditPresenter createPresenter() {
        return new VideoEditPresenter(MainApplication.getContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }
        switch (requestCode){
            case REQUEST_CODE:
                String tempPath = data.getStringExtra(MusicListActivity.MUSIC_PATH);
                if (TextUtils.isEmpty(tempPath)){
                    return;
                }
                String musicPath = Constans.AUDIO_PATH + "/"
                        + System.currentTimeMillis()
                        + tempPath.substring(tempPath.lastIndexOf("."));
                if(FileUtil.copyFileToDirFile(tempPath, musicPath)) {
                    mMusicPath = musicPath;
                }
                break;
            case CHOOSE_VIDEO_CODE:
                Uri uri = data.getData();
                mVideoView.setVideoURI(uri);
                startVideo();
                mShadeView.setVisibility(View.GONE);
                toolbar.getRightImageButton().setVisibility(View.VISIBLE);
                String path = FileUtil.getFilePathByUri(this, uri);
                File file = new File(path);
                mInputVideoInfo = new VideoInfo();
                mInputVideoInfo.setPath(path);
                mInputVideoInfo.setVideoName(file.getName());
                mInputVideoInfo.setVideoTime(file.lastModified());
                mInputVideoInfo.setVideoName(DateUtil.timeToDate(file.lastModified()));
                setVideoInfo();
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
    }

    private void init() {
        mBottomSheetDialog = new BottomSheetDialog(this);
        mToast = Toast.makeText(this,"", Toast.LENGTH_LONG);
        toolbar = findViewById(R.id.titlebar);
        toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_TEXT) {
                    finish();
                }else if (action == CommonTitleBar.ACTION_RIGHT_BUTTON){
                    recordFinish();
                }
            }
        });
        mVideoView = (VideoView) findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setVisibility(View.GONE);        //隐藏进度条
        mVideoView.setMediaController(mediaController);
        mVideoView.setOnCompletionListener(this);
        mAddBgm = findViewById(R.id.tv_add_bgm);
        mAddBgm.setOnClickListener(this);
        mAddWatermark = findViewById(R.id.tv_add_watermark);
        mAddWatermark.setOnClickListener(this);
        mAddDrawText = findViewById(R.id.tv_add_draw_text);
        mAddDrawText.setOnClickListener(this);
        mInputVideoInfo = new VideoInfo();
        mShadeView = findViewById(R.id.ll_shade_view);
        mChooseVideo = findViewById(R.id.btn_choose_video);
        mChooseVideo.setOnClickListener(this);
        mVideoPlayView = findViewById(R.id.iv_video_play);
        mVideoPlayView.setOnClickListener(this);
        Serializable videoInfoSer = getIntent().getSerializableExtra(VIDEO_INFO);
        if (videoInfoSer == null){
            toolbar.getRightImageButton().setVisibility(View.GONE);
        }else {
            mInputVideoInfo = (VideoInfo) videoInfoSer;
            setVideoInfo();
        }
    }

    private void setVideoInfo() {
        mShadeView.setVisibility(View.GONE);
        toolbar.getRightImageButton().setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse(mInputVideoInfo.getPath());
        mVideoView.setVideoURI(videoUri);
        startVideo();
        mInputVideoInfo.setDuration(FFmpegCmd.getVideoDuration(mInputVideoInfo.getPath()));
    }

    private void startVideo(){
        mVideoPlayView.setVisibility(View.GONE);
        mVideoView.start();
    }

    private void recordFinish() {
        mPresenter.doEditVideo(mInputVideoInfo,mWaterInfo,mMusic,new Subscriber<VideoInfo>() {
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
                //删除临时文件背景音乐
                if (!TextUtils.isEmpty(mMusicPath)) {
                    File file = new File(mMusicPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                //TODO 删除临时水印
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(VideoEditActivity.this,"执行失败", Toast.LENGTH_LONG).show();
                if (mProgressDialog != null){
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onNext(VideoInfo videoInfo) {
                Toast.makeText(VideoEditActivity.this,"执行结束", Toast.LENGTH_LONG).show();
                if (videoInfo != null) {
                    if (videoInfo.isEditSuccess()) {
                        //EventBus.getDefault().post(new FinishVideoMsgEvent(videoInfo));
                        mToast.setText("视频保存在：" + videoInfo.getPath());
                        mToast.show();
                    }else {
                        mToast.setText("视频编辑失败，原始视频保存在：" + videoInfo.getPath());
                        mToast.show();
                    }
                }
                EventBus.getDefault().post(new EditFinishMsg(videoInfo));
                finish();
            }
        });
    }

    public static void start(Context context, VideoInfo videoInfo){
        Intent intent = new Intent(context, VideoEditActivity.class);
        intent.putExtra(VIDEO_INFO,videoInfo);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_add_bgm:
                showAddBgMusic();
                break;
            case R.id.tv_add_watermark:
                showAddWater();
                break;
            case R.id.tv_add_draw_text:
                break;
            case R.id.btn_choose_video:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, CHOOSE_VIDEO_CODE);
                break;
            case R.id.iv_video_play:
                startVideo();
                break;
                default:
                    break;
        }
    }

    private void showAddBgMusic() {
        View view = LayoutInflater.from(VideoEditActivity.this).inflate(R.layout.add_bg_music_layout, null);
        Button mPhoneMusic = view.findViewById(R.id.btn_phone_music);
        mPhoneMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicListActivity.startActivityForResult(VideoEditActivity.this, REQUEST_CODE);
            }
        });
        Button mSubmitMusic = view.findViewById(R.id.submit_edit);
        final TextView startTimeView = view.findViewById(R.id.tv_start_time);
        mSubmitMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(mMusicPath)){
                    Toast.makeText(VideoEditActivity.this,"请先选择背景音乐",Toast.LENGTH_LONG).show();
                }else {
                    mMusic = new Music();
                    mMusic.setPath(mMusicPath);
                    mMusic.setMusicStartTime(startTimeView.getText().toString());
                    mAddBgm.setBackground(getResources().getDrawable(R.drawable.edit_bg_shape_click));
                    mBottomSheetDialog.dismiss();
                }
            }
        });
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
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



    public void showAddWater() {
        mAddWaterView = new AddWaterView(VideoEditActivity.this);
        mAddWaterView.setDefaultEndTime(mInputVideoInfo.getDuration());
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

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void showErr() {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        mVideoPlayView.setVisibility(View.VISIBLE);
    }

}
