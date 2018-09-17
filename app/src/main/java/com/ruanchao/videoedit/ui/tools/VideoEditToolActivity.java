package com.ruanchao.videoedit.ui.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.base.BaseMvpActivity;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.event.EditFinishMsg;
import com.ruanchao.videoedit.ffmpeg.FFmpegCmd;
import com.ruanchao.videoedit.ui.video.MusicListActivity;
import com.ruanchao.videoedit.ui.video.VideoEditActivity;
import com.ruanchao.videoedit.util.Constans;
import com.ruanchao.videoedit.util.DateUtil;
import com.ruanchao.videoedit.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import rx.Subscriber;

public class VideoEditToolActivity extends BaseMvpActivity<IVideoEditToolView,VideoEditToolPresenter> implements IVideoEditToolView, View.OnClickListener {

    private static final String EDIT_TYPE = "edit_type";
    private int mEditType;
    private Button mChooseVideo;
    private static final int CHOOSE_VIDEO_CODE = 100;
    private VideoInfo mInputVideoInfo;
    private String TAG = VideoEditToolActivity.class.getSimpleName();
    private Button mVideoToGif;
    private TextView mGifStartTime;
    private TextView mGifEndTime;
    private EditText mFrameRateView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_tool);
        mEditType = getIntent().getIntExtra(EDIT_TYPE,-1);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOOSE_VIDEO_CODE:
                Uri uri = data.getData();
                String path = FileUtil.getFilePathByUri(this, uri);
                File file = new File(path);
                mInputVideoInfo = new VideoInfo();
                mInputVideoInfo.setVideoPath(path);
                mInputVideoInfo.setVideoName(file.getName());
                mInputVideoInfo.setVideoTime(file.lastModified());
                mInputVideoInfo.setVideoName(DateUtil.timeToDate(file.lastModified()));
                long videoDuration = FFmpegCmd.getVideoDuration(mInputVideoInfo.getVideoPath());
                mInputVideoInfo.setDuration(videoDuration);
                mGifEndTime.setText(String.valueOf(mInputVideoInfo.getDuration()));
                break;
            default:
                break;
        }
    }

    private void initView() {
        mChooseVideo = findViewById(R.id.btn_choose_video);
        mChooseVideo.setOnClickListener(this);
        mVideoToGif = findViewById(R.id.btn_video_to_gif);
        mVideoToGif.setOnClickListener(this);
        mGifStartTime = findViewById(R.id.et_gif_start_time);
        mGifEndTime = findViewById(R.id.et_gif_end_time);
        mFrameRateView = findViewById(R.id.et_gif_frame_rate);
    }

    public static void start(Context context,int type){
        Intent intent = new Intent(context, VideoEditToolActivity.class);
        intent.putExtra(EDIT_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    public VideoEditToolPresenter createPresenter() {
        return new VideoEditToolPresenter(this);
    }

    @Override
    public void showToast(String msg) {

    }

    @Override
    public void showErr() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_choose_video:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, CHOOSE_VIDEO_CODE);
                break;
            case R.id.btn_video_to_gif:
                videoToGif();
            default:
                    break;
        }
    }

    private void videoToGif() {
        if (mInputVideoInfo == null){
            mToast.setText("请先选择视频");
            mToast.show();
            return;
        }
        double startTime = Double.parseDouble(mGifStartTime.getText().toString());
        double endTime = Double.parseDouble(mGifEndTime.getText().toString());
        int frameRate = Integer.parseInt(mFrameRateView.getText().toString());
        if (startTime < 0 || startTime > mInputVideoInfo.getDuration()
                || endTime < 0 || endTime>mInputVideoInfo.getDuration() || endTime < startTime){
            mToast.setText("视频转化起始时间输入有误，请重新输入");
            mToast.show();
        }
        if (frameRate >= 15 || frameRate <= 7){
            mToast.setText("帧率建议在7~15帧之间");
            mToast.show();
        }
        EditInfo editInfo = new EditInfo();
        editInfo.editType = EditInfo.EDIT_TYPE_VIDEO_TO_GIF;
        editInfo.videoInfo = mInputVideoInfo;
        editInfo.gifInfo = new EditInfo.GifInfo();
        editInfo.gifInfo.startTime = startTime;
        editInfo.gifInfo.endTime = endTime;
        editInfo.gifInfo.frameRate = frameRate;
        startEdit(editInfo);
    }

    private void startEdit(EditInfo editInfo) {
        mPresenter.doEditVideo(editInfo,new Subscriber<VideoInfo>() {
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
                Toast.makeText(VideoEditToolActivity.this,"执行失败", Toast.LENGTH_LONG).show();
                if (mProgressDialog != null){
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onNext(VideoInfo videoInfo) {
                Toast.makeText(VideoEditToolActivity.this,"执行结束", Toast.LENGTH_LONG).show();
                if (videoInfo != null) {
                    if (videoInfo.getType() == VideoInfo.TYPE_GIF){
                        mToast.setText("Gif生成成功，保存在"+videoInfo.getVideoPath());
                        mToast.show();
                    }
                }else {
                    mToast.setText("视频编辑失败");
                    mToast.show();
                }
                //EventBus.getDefault().post(new EditFinishMsg(videoInfo));
                finish();
            }
        });
    }
}
