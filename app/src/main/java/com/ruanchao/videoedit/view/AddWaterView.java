package com.ruanchao.videoedit.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ruanchao.videoedit.R;
import com.ruanchao.videoedit.bean.WaterInfo;

public class AddWaterView extends LinearLayout implements View.OnClickListener {

    private int mPosition;
    private double mDuration = 10;
    TextView mImagePathView;
    EditText mEndTimeView;
    EditText mStartTimeView;
    double startTime = 0;
    double endTime = 10;
    String imagePath;
    Context context;
    TextView mTipView;

    public AddWaterView(Context context) {
        super(context);
        init(context);
    }

    public AddWaterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AddWaterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AddWaterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.add_water_layout, this);
        Spinner mWaterChoose = view.findViewById(R.id.sp_water_choose);
        mWaterChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                mPosition = pos;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Button submit = view.findViewById(R.id.submit_edit);
        submit.setOnClickListener(this);
        Button innerChoose = view.findViewById(R.id.btn_inner_choose);
        innerChoose.setOnClickListener(this);
        Button photoChoose = view.findViewById(R.id.btn_photos_choose);
        photoChoose.setOnClickListener(this);
        mStartTimeView = view.findViewById(R.id.tv_start_time);
        mEndTimeView = view.findViewById(R.id.tv_end_time);
        mTipView = view.findViewById(R.id.tv_tip);

        mEndTimeView.setText(String.valueOf(mDuration));
        mImagePathView = view.findViewById(R.id.tv_image_path);
    }

    public void setPhotoImagePath(String path){
        imagePath = path;
        mImagePathView.setText(path);
    }

    public void setDefaultEndTime(double endTime){
        mEndTimeView.setText(String.valueOf(endTime/1000));
        this.endTime = endTime/1000;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_inner_choose:
                break;
            case R.id.btn_photos_choose:
                if (mOnSubmitListener != null){
                    mOnSubmitListener.onChoosePhoto();
                }
                break;
            case R.id.submit_edit:
                submitWater();
                break;
            default:
                break;
        }
    }

    private void submitWater() {
        if (mImagePathView.getText() == null || TextUtils.isEmpty(mImagePathView.getText().toString())){
           mTipView.setVisibility(VISIBLE);
           return;
        }

        try {
            startTime = Double.parseDouble(mStartTimeView.getText().toString());
        }catch (Exception e){
            startTime = 0;
        }
        try {
            endTime = Double.parseDouble(mEndTimeView.getText().toString());
        }catch (Exception e){
            startTime = 10;
        }
        String x = "10";
        String y = "10";
        switch (mPosition){
            case 0:
                x = "10";
                y = "10";
                break;
            case 1:
                x = "main_w-overlay_w-10";
                y = "10";
                break;
            case 2:
                x = "10";
                y = "main_h-overlay_h-10";
                break;
            case 3:
                x = "main_w-overlay_w-10";
                y = "main_h-overlay_h-10";
                break;
            default:
                break;
        }
        if (startTime>0){
            x =String.format("if(gte(t,%f),if(lte(t,%f),%s,NAN),NAN)",startTime,endTime,x);
        }

        WaterInfo mWaterInfo = new WaterInfo();
        mWaterInfo.setStartTime(startTime);
        mWaterInfo.setEndTime(endTime);
        mWaterInfo.setWaterPath(imagePath);
        mWaterInfo.setxPosition(x);
        mWaterInfo.setyPosition(y);
        if (mOnSubmitListener != null){
            mOnSubmitListener.onSubmit(mWaterInfo);
        }
    }

    private OnSubmitListener mOnSubmitListener;

    public interface OnSubmitListener{
        void onSubmit(WaterInfo waterInfo);
        void onChoosePhoto();
        void onChooseFile();
    }

    public void setOnSubmitListener(OnSubmitListener onSubmitListener){
        mOnSubmitListener = onSubmitListener;
    }

}
