package com.example.cj.videoeditor;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.cj.videoeditor.media.VideoInfo;
import com.example.cj.videoeditor.mediacodec.VideoRunnable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by cj on 2017/8/6.
 *
 */

public class VideoFilterApplication {

    private static Context mContext;

    public static void setContext(Context context){
        mContext = context;
    }

    public static Context getContext() {
        return mContext;
    }
}
