package com.ruanchao.videoedit.view.tool;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;
import com.ruanchao.videoedit.interf.OnStartEditListener;

public class VideoToGifLayout extends BaseToolLayout implements View.OnClickListener {

    private Button mVideoToGif;
    private TextView mGifStartTime;
    private TextView mGifEndTime;
    private EditText mFrameRateView;
    private Toast mToast;

    private VideoInfo mInputVideoInfo;

    public VideoToGifLayout(Context context) {
        super(context);
        init(context);
    }

    public VideoToGifLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoToGifLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoToGifLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.video_to_gif_layout,this,true);
        mVideoToGif = findViewById(R.id.btn_video_to_gif);
        mVideoToGif.setOnClickListener(this);
        mGifStartTime = findViewById(R.id.et_gif_start_time);
        mGifEndTime = findViewById(R.id.et_gif_end_time);
        mFrameRateView = findViewById(R.id.et_gif_frame_rate);
        mToast = Toast.makeText(context,"", Toast.LENGTH_LONG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_video_to_gif:
                videoToGif();
                default:
                    break;
        }
    }

    public void setInputVideoInfo(VideoInfo mInputVideoInfo) {
        this.mInputVideoInfo = mInputVideoInfo;
        mGifEndTime.setText(String.valueOf(mInputVideoInfo.getDuration()));
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
        if (mOnStartEditListener != null){
            mOnStartEditListener.onStartEdit(editInfo);
        }
    }
}
