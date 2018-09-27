package com.ruanchao.videoedit.view.tool;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.VideoInfo;

public class ImageToVideoView extends LinearLayout implements View.OnClickListener {

    private TextView mImagePathView;
    private Button mImageToVideoBtn;
    private VideoInfo mInputVideoInfo;
    private Context mContext;

    public ImageToVideoView(Context context) {
        super(context);
        init(context);
    }

    public ImageToVideoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageToVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageToVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.image_to_video_layout,this,true);
        mImagePathView = findViewById(R.id.tv_image_path);

        mImageToVideoBtn = findViewById(R.id.btn_image_to_video);
        mImageToVideoBtn.setOnClickListener(this);
    }

    public void setInputVideoInfo(VideoInfo mInputVideoInfo) {
        this.mInputVideoInfo = mInputVideoInfo;
        mImagePathView.setText(mInputVideoInfo.getVideoPath());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_image_to_video:
                startImageToVideo();
                break;
                default:
                    break;
        }
    }

    private void startImageToVideo() {
        if (mInputVideoInfo == null) {
            Toast.makeText(mContext, "请先选择照片", Toast.LENGTH_LONG).show();
            return;
        }

    }
}
