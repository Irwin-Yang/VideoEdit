package com.ruanchao.videoedit.view.tool;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ruanchao.videoedit.interf.OnStartEditListener;

public class BaseToolLayout extends LinearLayout{
    public BaseToolLayout(Context context) {
        super(context);
    }

    public BaseToolLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseToolLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseToolLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected OnStartEditListener mOnStartEditListener;

    public void setOnStartEditListener(OnStartEditListener onStartEditListener){
        mOnStartEditListener = onStartEditListener;
    }
}
