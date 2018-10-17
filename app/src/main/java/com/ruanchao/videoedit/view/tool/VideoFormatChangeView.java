package com.ruanchao.videoedit.view.tool;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;

public class VideoFormatChangeView extends BaseToolLayout implements View.OnClickListener {

    private TextView mVideoPathView;
    private Button mVideoFormatChangeBtn;
    private VideoInfo mInputVideoInfo;
    private Context mContext;
    private Spinner mFormatSpinner;
    private String mFormat_type = "mp4";

    public VideoFormatChangeView(Context context) {
        super(context);
        init(context);
    }

    public VideoFormatChangeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoFormatChangeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoFormatChangeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.video_format_change_layout,this,true);
        mVideoPathView = findViewById(R.id.tv_image_path);
        mVideoFormatChangeBtn = findViewById(R.id.btn_video_format_change);
        mVideoFormatChangeBtn.setOnClickListener(this);

        mFormatSpinner = findViewById(R.id.format_spinner);
        mFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] languages = getResources().getStringArray(R.array.format_type);
                mFormat_type = languages[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void setInputVideoInfo(VideoInfo mInputVideoInfo) {
        this.mInputVideoInfo = mInputVideoInfo;
        mVideoPathView.setText(mInputVideoInfo.getPath());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_video_format_change:
                startVideoFormatChange();
                break;
                default:
                    break;
        }
    }

    private void startVideoFormatChange() {
        if (mInputVideoInfo == null) {
            Toast.makeText(mContext, "请先选择视频", Toast.LENGTH_LONG).show();
            return;
        }
        EditInfo editInfo = new EditInfo();
        editInfo.editType = EditInfo.EDIT_TYPE_VIDEO_FORMAT_CHANGE;
        editInfo.videoInfo = mInputVideoInfo;
        editInfo.videoFormat = mFormat_type;
        if (mOnStartEditListener != null){
            mOnStartEditListener.onStartEdit(editInfo);
        }
    }
}
