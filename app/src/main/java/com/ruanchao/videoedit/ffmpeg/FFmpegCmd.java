package com.ruanchao.videoedit.ffmpeg;

import android.util.Log;

public class FFmpegCmd {

    public interface OnHandleListener{
        void onBegin();
        void onEnd(int result);
    }

    static{
        System.loadLibrary("media-handle");
    }

    //开子线程调用native方法进行音视频处理
    public static void execute(final String commands, final OnHandleListener onHandleListener){
        String regulation="[ \\t]+";
        final String[] split = commands.split(regulation);

        Log.i("FFmpegCmd","FFmpegCmd:" + commands );
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(onHandleListener != null){
                    onHandleListener.onBegin();
                }
                //调用ffmpeg进行处理
                int result = handle(split);
                if(onHandleListener != null){
                    onHandleListener.onEnd(result);
                }
            }
        }).start();
    }

    public static int execute(final String commands) {
        String regulation = "[ \\t]+";
        final String[] split = commands.split(regulation);
        int result = handle(split);
        return result;
    }
    private native static int handle(String[] commands);

    public native static String getVideoInfo(String videoPath);

    public native static long getVideoDuration(String videoPath);

}