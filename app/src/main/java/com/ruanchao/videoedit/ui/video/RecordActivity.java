package com.ruanchao.videoedit.ui.video;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.opengl.EGL14;
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

import com.pinssible.librecorder.listener.OnMuxFinishListener;
import com.pinssible.librecorder.recorder.AVRecorder;
import com.pinssible.librecorder.recorder.PreviewConfig;
import com.pinssible.librecorder.recorder.RecorderConfig;
import com.pinssible.librecorder.view.GLTextureView;
import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseActivity;
import com.ruanchao.videoedit.bean.FilterInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.DensityUtil;
import com.ruanchao.videoedit.util.FileUtil;
import com.ruanchao.videoedit.view.BothWayProgressBar;
import com.ruanchao.videoedit.view.FilterView;
import com.ruanchao.videoedit.view.RecordButton;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class RecordActivity extends BaseActivity implements BothWayProgressBar.OnProgressEndListener, View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "MainActivity";
    //预览SurfaceView
    private GLTextureView preview;
    //进度条
    private BothWayProgressBar mProgressBar;
    //进度条线程
    private Thread mProgressThread;
    //段视频保存的目录
    private File mTargetFile;
    private long mVideoTime = System.currentTimeMillis();
    private String mVideoName = mVideoTime + ".mp4";
    //当前进度/时间
    private int mProgress;
    //录制最大时间
    public static int mIntervalTime = 20;
    private RecordButton mRecordButton;

    private MyHandler mHandler = new MyHandler(this);
    private boolean isRunning;
    CommonTitleBar toolbar;
    private AVRecorder recorder;
    private boolean hasRecorder = false;
    private RecorderConfig recorderConfig;
    private PreviewConfig previewConfig;
    private int width = 600;
    private int height = 800;
    private ImageView mFilterBtn;
    private View mRecordLayout;
    private FilterView mFilterLayout;

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

        preview = (GLTextureView) findViewById(R.id.surface_show);
        preview.setOnTouchListener(this);
        mProgressBar = (BothWayProgressBar) findViewById(R.
                id.main_progress_bar);
        mProgressBar.setOnProgressEndListener(this);
        File targetDir = new File(Constans.VIDEO_TEMP_PATH);
        if (targetDir.exists()) {
            FileUtil.deleteFile(targetDir);
        }
        mTargetFile = new File(targetDir, mVideoName);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        width = DensityUtil.getWindowWidth();
        height = DensityUtil.getWindowHeight();
        Log.i(TAG,"width:" + width + "  height:"+height);
        mRecordButton = findViewById(R.id.btn_record);
        mRecordButton.setOnTouchListener(this);
        mFilterBtn = findViewById(R.id.iv_filter_button);
        mFilterBtn.setOnClickListener(this);
        mRecordLayout = findViewById(R.id.ll_record_layout);
        mFilterLayout = findViewById(R.id.ll_filter_layout);
        mFilterLayout.setFilterItemOnclickListener(new FilterView.FilterItemOnclickListener() {
            @Override
            public void onItemClick(FilterInfo filterInfo) {
                recorder.setFilter(filterInfo.getFilterID());
            }
        });
        initRecorder();
    }

    private void initRecorder() {
        //create recorder
        try {
            //create config
            recorderConfig = createRecorderConfig();
            previewConfig = createPreviewConfig();
            recorder = new AVRecorder(previewConfig, recorderConfig, preview);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(RecordActivity.this, "Sorry!Create recorder fail!", Toast.LENGTH_SHORT).show();
        }
    }

    private RecorderConfig createRecorderConfig() {
        //setting
        RecorderConfig.VideoEncoderConfig videoConfig = new RecorderConfig.VideoEncoderConfig(width, height,
                5 * 1000 * 1000, EGL14.eglGetCurrentContext());
        RecorderConfig.AudioEncoderConfig audioConfig = new RecorderConfig.AudioEncoderConfig(1, 96 * 1000, 44100);

        OnMuxFinishListener listener = new OnMuxFinishListener() {
            @Override
            public void onMuxFinish() {
                Log.e("TestActivity", "OnMuxFinish");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecordActivity.this, "Saving mp4 finish!😀", Toast.LENGTH_SHORT).show();
                        recordFinish();
                    }
                });
            }

            @Override
            public void onMuxFail(Exception e) {

            }
        };
        return new RecorderConfig(videoConfig, audioConfig,
                mTargetFile, RecorderConfig.SCREEN_ROTATION.VERTICAL, listener);
    }

    @Override
    protected void onDestroy() {
        recorder.release();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recorder.resumePreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        recorder.stopPreview();
    }
    //preview config
    private PreviewConfig createPreviewConfig() {
        return new PreviewConfig(width, height);
    }

    @Override
    public void onProgressEndListener() {
        //视频停止录制
        stopRecordSave(true);
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        if (!recorder.isRecording) {
            if (hasRecorder) {
                try {
                    recorderConfig = createRecorderConfig();
                    recorder.reset(recorderConfig);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(RecordActivity.this, "Sorry!reset recorder fail!", Toast.LENGTH_SHORT).show();
                }
            }
            recorder.startRecording();
        }else {
            Toast.makeText(RecordActivity.this, "正在录制中...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 停止录制 并且保存
     */
    private void stopRecordSave(boolean isSave) {
        if (recorder.isRecording) {
            recorder.stopRecording();
            if (!hasRecorder) {
                hasRecorder = true;
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
            case R.id.iv_filter_button:
                mRecordLayout.setVisibility(View.GONE);
                mFilterLayout.setVisibility(View.VISIBLE);
                break;
                default:
                    break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.btn_record:
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mProgressBar.setCancel(false);
                        //记录按下的Y坐标
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
                        break;
                    case MotionEvent.ACTION_UP:

                        mProgressBar.setVisibility(View.INVISIBLE);
                        //判断是否为录制结束, 或者为成功录制(时间过短)
                        if (mProgress < 50) {
                            //时间太短不保存
                            stopRecordSave(false);
                            Toast.makeText(this, "时间太短", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        //停止录制
                        stopRecordSave(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                break;
            case R.id.surface_show:
                if (mRecordLayout.getVisibility() != View.VISIBLE){
                    mRecordLayout.setVisibility(View.VISIBLE);
                    mFilterLayout.setVisibility(View.GONE);
                }
                break;
                default:
                    break;
        }
        return true;
    }

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

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
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
