package com.ruanchao.videoedit.ui.video;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.util.BitmapUtil;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DensityUtil;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;

public class TestActivity extends AppCompatActivity {

    private ImageView mTestImage;
    private MediaPlayer mediaPlayer;
    private SurfaceTexture mSurfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mTestImage = findViewById(R.id.iv_test_image);
        //initGpuImage();
    }

    private void initGpuImage() {
        try {
            String imageFile = Constans.IMAGE_PATH + "/test.jpg";
            File file = new File(imageFile);
            Bitmap bitmap;
            if(file.exists()) {
                int requestWidth = DensityUtil.getWindowWidth();
                int requestHeight = DensityUtil.getWindowHeight();
                bitmap = BitmapUtil.getFitSampleBitmap(imageFile, requestWidth, requestHeight);
                //bitmap = BitmapFactory.decodeStream(getAssets().open("resource/test.jpg"));
                GPUImage gpuImage = new GPUImage(this);
                gpuImage.setImage(bitmap);
                gpuImage.setFilter(new GPUImageGrayscaleFilter());
                Bitmap bitmap1 = gpuImage.getBitmapWithFilterApplied();
                mTestImage.setImageBitmap(bitmap1);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("test","err:" + e.getMessage());
        }
    }


    public void initPlayer(SurfaceTexture surfaceTexture) {

        mediaPlayer = new MediaPlayer();

        try {
            String mPath = Constans.VIDEO_PATH + "/test.mp4";
            Uri videoUri = Uri.parse(mPath);
            mediaPlayer.setDataSource(this, videoUri);
            Surface s = new Surface(surfaceTexture);
            mediaPlayer.setSurface(s);
            s.release();
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }

    public void stopPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
