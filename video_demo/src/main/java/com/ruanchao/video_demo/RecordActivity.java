package com.ruanchao.video_demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.List;

public class RecordActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback, View.OnClickListener {

    TextureView mTextureView;
    Camera mCamera;
    private static final String TAG = RecordActivity.class.getSimpleName();
    int mVideoWidth;
    int mVideoHeight;
    H264Encoder mEncoder;
    String outPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + System.currentTimeMillis() + "-record.mp4";

    /**
     * 开源框架 SVideoRecorder
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        mTextureView = findViewById(R.id.tv_preview);
        mTextureView.setSurfaceTextureListener(this);
        Button mStopRecord = findViewById(R.id.btn_stop_record);
        mStopRecord.setOnClickListener(this);
        Button mStartRecord = findViewById(R.id.btn_start_record);
        mStartRecord.setOnClickListener(this);
        initCamera();
    }

    /**
     * 初始化相机，以及参数配置
     */
    private void initCamera() {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        Camera.Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(),
                DensityUtil.getWindowWidth(RecordActivity.this),
                DensityUtil.getWindowHeight(RecordActivity.this));
        Log.e(TAG,"width:" + s.width + "  height:" + s.height);
        mVideoWidth = s.width;
        mVideoHeight = s.height;
        parameters.setPreviewSize(mVideoWidth, mVideoHeight);
        parameters.setPictureSize(s.width, s.height);
        List<String> focusModes = parameters.getSupportedFocusModes();
        //必须要设置自动对焦  并且在清单中配置对焦权限
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else
        if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(this);
    }

    public static void start(Context context){
        Intent intent = new Intent(context, RecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            mEncoder = new H264Encoder(width,height);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.release();
            mCamera.stopPreview();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //相机返回的数据
        if (mEncoder != null){
            mEncoder.putData(data);
        }

    }

    /**
     * 获取相机所支持的最佳尺寸
     * @param sizes
     * @param width
     * @param height
     * @return
     */
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_record:
                mEncoder.startEncoder(outPath);
                break;
            case R.id.btn_stop_record:
                mEncoder.stop();
                    break;
                    default:
                        break;

        }
    }
}
