package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseActivity;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.BothWayProgressBar;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class RecordActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnTouchListener, BothWayProgressBar.OnProgressEndListener, View.OnClickListener {

    private static final int LISTENER_START = 200;
    private static final String TAG = "MainActivity";
    //预览SurfaceView
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    //底部"按住拍"按钮
    private View mStartButton;
    //进度条
    private BothWayProgressBar mProgressBar;
    //进度条线程
    private Thread mProgressThread;
    //录制视频
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    //屏幕分辨率
    private int videoWidth, videoHeight;
    //判断是否正在录制
    private boolean isRecording;
    //段视频保存的目录
    private File mTargetFile;
    private long mVideoTime = System.currentTimeMillis();
    private String mVideoName = mVideoTime + ".mp4";
    //当前进度/时间
    private int mProgress;
    //录制最大时间
    public static final int MAX_TIME = 10000;
    public static int mIntervalTime = 20;
    //是否上滑取消
    private boolean isCancel;
    //手势处理, 主要用于变焦 (双击放大缩小)
    private GestureDetector mDetector;
    //是否放大
    private boolean isZoomIn = false;

    private MyHandler mHandler;
    private TextView mTvTip;
    private boolean isRunning;
    CommonTitleBar toolbar;
    ImageView mStartImg;
    ImageView mReturnRecord;
    ImageView mRecordSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_record);
        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.titlebar);
        toolbar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_LEFT_BUTTON) {
                    finish();
                }else if (action == CommonTitleBar.ACTION_RIGHT_BUTTON){
                    recordFinish();
                }
            }
        });
        mStartImg = findViewById(R.id.iv_record_img);
        mRecordSuccess = findViewById(R.id.iv_record_success);
        mRecordSuccess.setOnClickListener(this);
        mReturnRecord = findViewById(R.id.iv_return_record);
        mReturnRecord.setOnClickListener(this);
        mSurfaceView = (SurfaceView) findViewById(R.id.main_surface_view);
        mDetector = new GestureDetector(this, new ZoomGestureListener());
        /**
         * 单独处理mSurfaceView的双击事件
         */
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });
        setVideoWidthAndHeight();

        mSurfaceHolder = mSurfaceView.getHolder();
        //设置屏幕分辨率
        mSurfaceHolder.setFixedSize(videoWidth, videoHeight);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mStartButton = findViewById(R.id.main_press_control);
        mTvTip = (TextView) findViewById(R.id.main_tv_tip);

        mStartButton.setOnTouchListener(this);
        mProgressBar = (BothWayProgressBar) findViewById(R.
                id.main_progress_bar);
        mProgressBar.setOnProgressEndListener(this);
        mHandler = new MyHandler(this);
        mMediaRecorder = new MediaRecorder();
    }

    private void setVideoWidthAndHeight() {
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        Camera.Parameters parameter=mCamera.getParameters();
        List<Camera.Size> videoSizeList = parameter.getSupportedVideoSizes();
        videoWidth = 640;
        videoHeight = 480;
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);

        int displayWidth = wm.getDefaultDisplay().getWidth();
        mIntervalTime = (int) (MAX_TIME / (displayWidth / 2.0));

        for (Camera.Size size : videoSizeList){
            if (size.width > videoWidth && size.width < wm.getDefaultDisplay().getHeight()){
                videoWidth = size.width;
                videoHeight = size.height;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    ///////////////////////////////////////////////////////////////////////////
    // SurfaceView回调
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    /**
     * 开启预览
     *
     * @param holder
     */
    private void startPreView(SurfaceHolder holder) {
        Log.d(TAG, "startPreView: ");

        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                //实现Camera自动对焦
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    for (String mode : focusModes) {
                        mode.contains("continuous-video");
                        parameters.setFocusMode("continuous-video");
                    }
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            Log.d(TAG, "surfaceDestroyed: ");
            //停止预览并释放摄像头资源
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 进度条结束后的回调方法
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onProgressEndListener() {
        //视频停止录制
        stopRecordSave(true);
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (mMediaRecorder != null) {
            //没有外置存储, 直接停止录制
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            try {
                //mMediaRecorder.reset();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                //从相机采集视频
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // 从麦克采集音频信息
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoSize(videoWidth, videoHeight);
                //每秒的帧数
                mMediaRecorder.setVideoFrameRate(24);
                //编码格式
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                // 设置帧频率，然后就清晰了
                mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024 * 100);

                File targetDir = new File(Constans.VIDEO_TEMP_PATH);
                if (targetDir.exists()) {
                    FileUtil.deleteFile(targetDir);
                }
                mTargetFile = new File(targetDir, mVideoName);
                if (!targetDir.exists()){
                    targetDir.mkdirs();
                }
                mMediaRecorder.setOutputFile(mTargetFile.getAbsolutePath());
                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                //解决录制视频, 播放器横向问题
                mMediaRecorder.setOrientationHint(90);
                mMediaRecorder.prepare();
                //正式录制
                mMediaRecorder.start();
                isRecording = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 停止录制 并且保存
     */
    private void stopRecordSave(boolean isSave) {
        if (isRecording) {
            isRunning = false;
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            }catch (Exception e){
                e.printStackTrace();
                Log.i(TAG,"err:"+e.getMessage());
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
            }
            isRecording = false;
            if (isSave) {
                mReturnRecord.setVisibility(View.VISIBLE);
                mRecordSuccess.setVisibility(View.VISIBLE);
                mStartImg.setVisibility(View.INVISIBLE);
            }else {
                if (mTargetFile.exists()) {
                    //不保存直接删掉
                    mTargetFile.delete();
                }
            }
        }
    }

    /**
     * 相机变焦
     *
     * @param zoomValue
     */
    public void setZoom(int zoomValue) {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.isZoomSupported()) {//判断是否支持
                int maxZoom = parameters.getMaxZoom();
                if (maxZoom == 0) {
                    return;
                }
                if (zoomValue > maxZoom) {
                    zoomValue = maxZoom;
                }
                parameters.setZoom(zoomValue);
                mCamera.setParameters(parameters);
            }
        }

    }

    public void recordFinish() {
        if (mTargetFile == null || mTargetFile.getAbsolutePath() == null || !mTargetFile.exists()){
            Toast.makeText(this,"请先拍摄视频",Toast.LENGTH_LONG).show();
            return;
        }
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setPath(mTargetFile.getAbsolutePath());
        videoInfo.setType(VideoInfo.TYPE_VIDEO);
        videoInfo.setVideoTitle(DateUtil.timeToDate(mVideoTime));
        videoInfo.setVideoName(mVideoName);
        videoInfo.setVideoTime(mVideoTime);
        VideoEditActivity.start(this,videoInfo);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_return_record:
                mReturnRecord.setVisibility(View.INVISIBLE);
                mRecordSuccess.setVisibility(View.INVISIBLE);
                mStartImg.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_record_success:
                recordFinish();
                break;
                default:
                    break;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Handler处理
    ///////////////////////////////////////////////////////////////////////////
    private static class MyHandler extends Handler {
        private WeakReference<RecordActivity> mReference;
        private RecordActivity mActivity;

        public MyHandler(RecordActivity activity) {
            mReference = new WeakReference<RecordActivity>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mActivity.mProgressBar.setProgress(mActivity.mProgress);
                    break;
            }

        }
    }

    /**
     * 触摸事件的触发
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = false;
        int action = event.getAction();
        float ey = event.getY();
        float ex = event.getX();
        //只监听中间的按钮处
        int vW = v.getWidth();
        int left = LISTENER_START;
        int right = vW - LISTENER_START;

        float downY = 0;

        switch (v.getId()) {
            case R.id.main_press_control: {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (ex > left && ex < right) {
                            mStartImg.setImageResource(R.mipmap.start_record);
                            mProgressBar.setCancel(false);
                            //显示上滑取消
                            mTvTip.setVisibility(View.VISIBLE);
                            mTvTip.setText("↑ 上滑取消");
                            //记录按下的Y坐标
                            downY = ey;
                            // TODO: 2016/10/20 开始录制视频, 进度条开始走
                            mProgressBar.setVisibility(View.VISIBLE);
                            //开始录制
                            Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
                            startRecord();

                            mProgressThread = new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        mProgress = 0;
                                        isRunning = true;
                                        while (isRunning) {
                                            mProgress++;
                                            mHandler.obtainMessage(0).sendToTarget();
                                            Thread.sleep(mIntervalTime);
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            mProgressThread.start();
                            ret = true;
                        }


                        break;
                    case MotionEvent.ACTION_UP:
                        mStartImg.setImageResource(R.mipmap.end_record);
                        if (ex > left && ex < right) {
                            mTvTip.setVisibility(View.INVISIBLE);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            //判断是否为录制结束, 或者为成功录制(时间过短)
                            if (!isCancel) {
                                if (mProgress < 50) {
                                    //时间太短不保存
                                    stopRecordSave(false);
                                    Toast.makeText(this, "时间太短", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                //停止录制
                                stopRecordSave(true);
                            } else {
                                //现在是取消状态,不保存
                                stopRecordSave(false);
                                isCancel = false;
                                Toast.makeText(this, "取消录制", Toast.LENGTH_SHORT).show();
                                mProgressBar.setCancel(false);
                            }

                            ret = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (ex > left && ex < right) {
                            float currentY = event.getY();
                            if (downY - currentY > 10) {
                                isCancel = true;
                                mProgressBar.setCancel(true);
                            }
                        }
                        break;
                }
                break;

            }

        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 变焦手势处理类
    ///////////////////////////////////////////////////////////////////////////
    class ZoomGestureListener extends GestureDetector.SimpleOnGestureListener {
        //双击手势事件
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            super.onDoubleTap(e);
            Log.d(TAG, "onDoubleTap: 双击事件");
            if (mMediaRecorder != null) {
                if (!isZoomIn) {
                    setZoom(20);
                    isZoomIn = true;
                } else {
                    setZoom(0);
                    isZoomIn = false;
                }
            }
            return true;
        }
    }

    public static void startRecordActivity(final Context context){
        AndPermission.with(context)
                .permission(Permission.Group.CAMERA,Permission.Group.MICROPHONE)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        context.startActivity(new Intent(context,RecordActivity.class));
                    }
                }).onDenied(new Action() {
            @Override
            public void onAction(List<String> permissions) {
                Toast.makeText(context,"必须获取相机和录音权限才可以进行拍摄视频", Toast.LENGTH_LONG).show();
            }
        }).start();
    }



}
