package com.ruanchao.video_demo;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * MediaCodec可以处理具体的视频流，主要有这几个方法：

 getInputBuffers：获取需要编码数据的输入流队列，返回的是一个ByteBuffer数组
 queueInputBuffer：输入流入队列
 dequeueInputBuffer：从输入流队列中取数据进行编码操作
 getOutputBuffers：获取编解码之后的数据输出流队列，返回的是一个ByteBuffer数组
 dequeueOutputBuffer：从输出队列中取出编码操作之后的数据
 releaseOutputBuffer：处理完成，释放ByteBuffer数据
 */

public class AvcEncoder {

    private int mWidth;
    private int mHeight;
    private MediaCodec mMediaCodec;
    private static final String MIME = "video/avc";
    private byte[] yuv420;
    private byte[] m_info = null;

    /*对于有些机器上报错，修改 MediaFormat.KEY_COLOR_FORMAT值即可
   case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
       case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
       case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
       case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
       case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:*/
    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int framerate, int bitrate) throws IOException {
        mWidth = width;
        mHeight = height;
        //初始化编码器
        mMediaCodec = MediaCodec.createEncoderByType(MIME);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME, width, height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        //第四个参数,编码H264的时候,固定CONFIGURE_FLAG_ENCODE, 播放的时候传入0即可;API文档有解释
        //CONFIGURE_FLAG_ENCODE  编码h264关键
        mMediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void close(){
        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 把相机摄像头数据传给编码器，获取编码后的数据
     */
    @SuppressLint("NewApi")
    public int offerEncoder(byte[] input, byte[] output)
    {
        //Log.d("Fuck", "input lenght: " + input.length);
        int pos = 0;
        //swapYV12toI420(input, yuv420, m_width, m_height);
        swapYV12toYUV420SemiPlanar(input, yuv420, mWidth, mHeight);
        try {
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0)
            {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(yuv420);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, 0, 0);
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo,0);
            while (outputBufferIndex >= 0)
            {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                if(m_info != null)
                {
                    System.arraycopy(outData, 0,  output, pos, outData.length);
                    pos += outData.length;
                }
                else
                {
                    ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
                    if (spsPpsBuffer.getInt() == 0x00000001)
                    {
                        m_info = new byte[outData.length];
                        System.arraycopy(outData, 0, m_info, 0, outData.length);
                    }
                    else
                    {
                        return -1;
                    }
                }

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }

            if(output[4] == 0x65) //key frame
            {
                Log.d("Fuck", "key frame");
                System.arraycopy(output, 0,  yuv420, 0, pos);
                System.arraycopy(m_info, 0,  output, 0, m_info.length);
                Log.d("Fuck", "m_info.length: " + m_info.length);
                System.arraycopy(yuv420, 0,  output, m_info.length, pos);
                pos += m_info.length;
            }else{
                //Log.d("Fuck", "NOT key frame");
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return pos;
    }

    private void swapYV12toYUV420SemiPlanar(byte[] yv12bytes, byte[] i420bytes, int width, int height){
        System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
        int startPos = width*height;
        int yv_start_pos_v =  width*height+width;
        int yv_start_pos_u =  width*height+width*height/4;
        for(int i = 0; i < width*height/4; i++){
            i420bytes[startPos + 2 * i + 0] = yv12bytes[yv_start_pos_u + i];
            i420bytes[startPos + 2 * i + 1] = yv12bytes[yv_start_pos_v + i];
        }
    }
}
