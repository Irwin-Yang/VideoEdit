package com.ruanchao.videoedit.ui.video;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ruanchao.videoedit.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaCodecRecordActivity extends AppCompatActivity {


    /**
     * 采用Camera + OpengGL + MediaCodec +进MediaMuxer行视频录制。
     *
     *关于android平台的视频录制，首先我们要确定我们的需求，录制音视频本地保存为mp4文件。
     * 实现音视频录制的方案有很多，比如原生的sdk，通过Camera进行数据采集，
     * 注意：是Android.hardware包下的Camera而不是Android.graphics包下的，后者是用于矩阵变换的一个类，
     * 而前者才是通过硬件采集摄像头的数据，并且返回到java层。然后使用SurfaceView进行画面预览，
     * 使用MediaCodec对数据进行编解码，最后通过MediaMuxer将音视频混合打包成为一个mp4文件。
     * 当然也可以直接使用MediaRecorder类进行录制，该类是封装好了的视频录制类，但是不利于功能扩展，
     * 比如如果我们想在录制的视频上加上我们自己的logo，也就是常说的加水印，
     * 或者是录制一会儿 然后暂停 然后继续录制的功能，也就是断点续录的话 就不是那么容易实现了。
     * 而本篇文章，作为后面系列的基础，我们就不讲解常规的视频录制的方案了，
     * 有兴趣的可以查看本文前面附上的一些链接。因为我们后期会涉及到给视频加滤镜、加水印、加美颜等功能，
     * 所以就不能使用常规的视频录制方案了，而是采用Camera + OpengGL + MediaCodec +进MediaMuxer行视频录制。
     *
     */

    private Camera mCamera;
    private static final int minPictureWidth=720;
    private static final float rate=1.778f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec_record);
        initView();
    }

    private void initView() {

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera != null) {
            /**选择当前设备允许的预览尺寸*/
            Camera.Parameters param = mCamera.getParameters();
            Camera.Size preSize = getPropPictureSize(param.getSupportedPreviewSizes(), rate,
                    minPictureWidth);
            Camera.Size picSize = getPropPictureSize(param.getSupportedPictureSizes(), rate,
                    minPictureWidth);
            param.setPictureSize(picSize.width, picSize.height);
            param.setPreviewSize(preSize.width, preSize.height);

            mCamera.setParameters(param);
        }
    }

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth){
        Collections.sort(list, sizeComparator);
        int i = 0;
        for(Camera.Size s:list){
            if((s.height >= minWidth) && equalRate(s, th)){
                break;
            }
            i++;
        }
        if(i == list.size()){
            i = 0;
        }
        return list.get(i);
    }

    private static boolean equalRate(Camera.Size s, float rate){
        float r = (float)(s.width)/(float)(s.height);
        if(Math.abs(r - rate) <= 0.03) {
            return true;
        }else{
            return false;
        }
    }

    private Comparator<Camera.Size> sizeComparator=new Comparator<Camera.Size>() {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    };
}
