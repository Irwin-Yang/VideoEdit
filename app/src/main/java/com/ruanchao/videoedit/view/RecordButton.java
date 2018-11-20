package com.ruanchao.videoedit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.ruanchao.videoedit.R;


public class RecordButton extends TextView {


    private int mBackgroundColor;

    private Paint mPaint;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackground(null);
        mBackgroundColor= getResources().getColor(R.color.record_bg_color);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBackgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                getMeasuredWidth() / 2, mPaint);
        super.onDraw(canvas);
    }
}
