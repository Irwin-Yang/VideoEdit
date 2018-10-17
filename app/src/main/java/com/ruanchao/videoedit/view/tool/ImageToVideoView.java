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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.EditInfo;
import com.ruanchao.videoedit.bean.VideoInfo;

public class ImageToVideoView extends BaseToolLayout implements View.OnClickListener {

    private TextView mImagePathView;
    private Button mImageToVideoBtn;
    private VideoInfo mInputVideoInfo;
    private Context mContext;
    private RadioGroup mEffectRadioGroup;
    public final static int EFFECT_NO = 0;
    public final static int EFFECT_ZOOM = 1;
    private int mEffect = EFFECT_ZOOM;
    private EditText mImageTime;


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
        mEffectRadioGroup = findViewById(R.id.main_radiogroup_effect);
        mEffectRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.main_radiobutton_effect_zoom:
                        mEffect = EFFECT_ZOOM;
                        break;
                    case R.id.main_radiobutton_effect_no:
                        mEffect = EFFECT_NO;
                        break;
                        default:
                            break;
                }
            }
        });
        mImageTime = findViewById(R.id.et_image_time);
    }

    public void setInputVideoInfo(VideoInfo mInputVideoInfo) {
        this.mInputVideoInfo = mInputVideoInfo;
        mImagePathView.setText(mInputVideoInfo.getPath());
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
        EditInfo editInfo = new EditInfo();
        editInfo.editType = EditInfo.EDIT_TYPE_IMAGE_TO_VIDEO;
        editInfo.videoInfo = mInputVideoInfo;
        editInfo.imageInfo = new EditInfo.ImageInfo();
        editInfo.imageInfo.duration = Integer.parseInt(mImageTime.getText().toString());
        editInfo.imageInfo.effect = mEffect;
        if (mOnStartEditListener != null){
            mOnStartEditListener.onStartEdit(editInfo);
        }
    }
}
