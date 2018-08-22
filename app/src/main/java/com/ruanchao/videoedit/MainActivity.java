package com.ruanchao.videoedit;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.util.FFmpegUtil;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "smallvideo";
    private static final String srcFile = PATH + File.separator + "hello.mp4";
    private static final String appendVideo = PATH + File.separator + "test.mp4";
    private static final String TAG = "MainActivity";
    TextView mRecordVideo;
    TextView mEditVideo;
    TextView mCutVideo;
    TextView mCutImage;
    TextView mVideoToGif;
    TextView mVideoChangeCode;
    TextView mImageToVideo;
    TextView mAddWater;
    TextView mVideoToAudio;
    TextView mAddBgMusic;
    TextView mVideoPress;
    TextView mMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button mBtnTest = findViewById(R.id.btn_test);
        mBtnTest.setOnClickListener(this);
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_test:
                testVideo();
                break;
            case R.id.bt_record_video:
                break;
            case R.id.bt_edit_video:
                break;
            case R.id.bt_video_cut:
                break;
            case R.id.bt_cut_image:
                break;
            case R.id.tv_video_to_gif:
                break;
            case R.id.tv_video_change_code:
                break;
            case R.id.tv_image_to_video:
                break;
            case R.id.tv_video_add_water:
                break;
            case R.id.tv_video_to_audio:
                break;
            case R.id.tv_video_add_bg:
                break;
            case R.id.tv_video_press:
                break;
            case R.id.tv_more:
                break;

            default:
                break;
        }
    }

    private void testVideo() {
        String commandLine = null;
        int handleType =4;
        switch (handleType){
            case 0://视频转码:mp4转flv、wmv, 或者flv、wmv转Mp4
                String transformVideo = PATH + File.separator + "transformVideo.flv";
                commandLine = FFmpegUtil.transformVideo(srcFile, transformVideo);
                break;
            case 1://视频剪切
                String cutVideo = PATH + File.separator + "cutVideo.mp4";
                int startTime = 0;
                int duration = 20;
                commandLine = FFmpegUtil.cutVideo(srcFile, startTime, duration, cutVideo);
                break;
            case 2://视频合并
//                commandLine = FFmpegUtil.toTs(srcFile, ts1);
//                concatStep ++;
//                String concatVideo = PATH + File.separator + "concatVideo.mp4";
//                String appendVideo = PATH + File.separator + "test.mp4";
//                File concatFile = new File(PATH + File.separator + "fileList.txt");
//                try {
//                    FileOutputStream fileOutputStream = new FileOutputStream(concatFile);
//                    fileOutputStream.write(("file \'" + srcFile + "\'").getBytes());
//                    fileOutputStream.write("\n".getBytes());
//                    fileOutputStream.write(("file \'" + appendVideo + "\'").getBytes());
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                commandLine = FFmpegUtil.concatVideo(srcFile, concatFile.getAbsolutePath(), concatVideo);
                break;
            case 3://视频截图
                String screenShot = PATH + File.separator + "screenShot.jpg";
                String size = "1080x720";
                commandLine = FFmpegUtil.screenShot(srcFile, size, screenShot);
                break;
            case 4://视频添加水印
                //1、图片
                String photo = PATH + File.separator + "launcher.png";
                String photoMark = PATH + File.separator + "photoMark.mp4";
                commandLine = FFmpegUtil.addWaterMark(appendVideo, photo, photoMark);
                //2、文字
                //String text = "Hello,FFmpeg";
                //String textPath = PATH + File.separator + "text.jpg";
                //boolean result = BitmapUtil.textToPicture(textPath, text, this);
                //Log.i(TAG, "text to pitcture result=" + result);
                //String textMark = PATH + File.separator + "textMark.mp4";
                //commandLine = FFmpegUtil.addWaterMark(appendVideo, photo, textMark);
                break;
            case 5://视频转成gif
                String Video2Gif = PATH + File.separator + "Video2Gif.gif";
                int gifStart = 1;
                int gifDuration = 5;
                commandLine = FFmpegUtil.generateGif(srcFile, gifStart, gifDuration, Video2Gif);
                break;
            case 6://屏幕录制
//                String screenRecord = PATH + File.separator + "screenRecord.mp4";
//                String screenSize = "320x240";
//                int recordTime = 10;
//                commandLine = FFmpegUtil.screenRecord(screenSize, recordTime, screenRecord);
                break;
            case 7://图片合成视频
                //图片所在路径，图片命名格式img+number.jpg
                String picturePath = PATH + File.separator + "img/";
                String combineVideo = PATH + File.separator + "combineVideo.mp4";
                commandLine = FFmpegUtil.pictureToVideo(picturePath, combineVideo);
                break;
            case 8://视频解码播放
                //startActivity(new Intent(VideoHandleActivity.this, VideoPlayerActivity.class));
                return;
            case 9://视频画面拼接:分辨率、时长、封装格式不一致时，先把视频源转为一致
                String input1 = PATH + File.separator + "input1.mp4";
                String input2 = PATH + File.separator + "input2.mp4";
                String outputFile = PATH + File.separator + "multi.mp4";
                //commandLine = FFmpegUtil.multiVideo(input1, input2, outputFile, VideoLayout.LAYOUT_HORIZONTAL);
                break;
            default:
                break;
        }
        executeFFmpegCmd(commandLine);
    }

    /**
     * 执行ffmpeg命令行
     * @param commandLine commandLine
     */
    private void executeFFmpegCmd(final String commandLine){
        if(commandLine == null){
            return;
        }
        FFmpegCmd.execute(commandLine, new FFmpegCmd.OnHandleListener() {
            @Override
            public void onBegin() {
                Log.i(TAG, "handle video onBegin...");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"开始", Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onEnd(int result) {
                Log.i(TAG, "handle video onEnd...");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"结束", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void initView() {
        mRecordVideo = findViewById(R.id.bt_record_video);
        mRecordVideo.setOnClickListener(this);
        mEditVideo = findViewById(R.id.bt_edit_video);
        mEditVideo.setOnClickListener(this);
        mCutVideo = findViewById(R.id.bt_video_cut);
        mCutVideo.setOnClickListener(this);
        mCutImage = findViewById(R.id.bt_cut_image);
        mCutImage.setOnClickListener(this);
        mVideoToGif = findViewById(R.id.tv_video_to_gif);
        mVideoToGif.setOnClickListener(this);
        mVideoChangeCode = findViewById(R.id.tv_video_change_code);
        mVideoChangeCode.setOnClickListener(this);
        mImageToVideo = findViewById(R.id.tv_image_to_video);
        mImageToVideo.setOnClickListener(this);
        mAddWater = findViewById(R.id.tv_video_add_water);
        mAddWater.setOnClickListener(this);
        mVideoToAudio = findViewById(R.id.tv_video_to_audio);
        mVideoToAudio.setOnClickListener(this);
        mAddBgMusic = findViewById(R.id.tv_video_add_bg);
        mAddBgMusic.setOnClickListener(this);
        mVideoPress = findViewById(R.id.tv_video_press);
        mVideoPress.setOnClickListener(this);
        mMore = findViewById(R.id.tv_more);
        mMore.setOnClickListener(this);
    }


}
