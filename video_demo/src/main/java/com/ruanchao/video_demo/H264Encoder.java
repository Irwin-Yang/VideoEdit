package com.ruanchao.video_demo;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class H264Encoder {

    MediaCodec mMediaCodec;
    private static final String MIME_TYPE = "video/avc";
    //视频数据存储队列
    public ArrayBlockingQueue<byte[]> yuv420Queue = new ArrayBlockingQueue<>(10);
    BufferedOutputStream outputStream;
    volatile boolean isRunning = false;
    public byte[] configbyte;
    int width;
    int height;
    private final static int TIMEOUT_USEC = 12000;

    public H264Encoder(int width, int height){
        this.width = width;
        this.height = height;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,width,height);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            //第四个参数,编码H264的时候,固定CONFIGURE_FLAG_ENCODE, 播放的时候传入0即可;API文档有解释
            //CONFIGURE_FLAG_ENCODE  编码h264关键
            mMediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 存放视频数据
     * @param data
     */
    public void putData(byte[] data){
        if (yuv420Queue.size() >= 10){
            yuv420Queue.poll();
        }
        yuv420Queue.add(data);
    }

    /**
     * 开始编码
     */
    public void startEncoder(final String outPath){
        if (isRunning){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //创建输出文件流
                createFile(outPath);
                isRunning = true;
                try {
                    dataStorage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop(){
        isRunning = false;
    }

    /**
     * 编解码 并且存储数据
     */
    private void dataStorage() throws IOException {
        while (isRunning) {
            byte[] input = null;
            long generateIndex = 0;
            if (yuv420Queue.size() > 0) {
                input = yuv420Queue.poll();
                byte[] yuv420sp = new byte[width * height * 3 / 2];
                // 必须要转格式，否则录制的内容播放出来为绿屏
                NV21ToNV12(input, yuv420sp, width, height);
                //存储转化后的数据
                input = yuv420sp;
            }
            if (input != null) {
                ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();

                //1.编码的过程
                //dequeueInputBuffer返回一个填充了有效数据的input buffer的索引，
                // 如果没有可用的buffer则返回-1.当timeoutUs==0时，该方法立即返回；
                // 当timeoutUs<0时，无限期地等待一个可用的input buffer;
                // 当timeoutUs>0时，至多等待timeoutUs微妙。
                int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    //往编码器中写入数据
                    inputBuffer.put(input);
                    //queueInputbuffer给指定索引的input buffer填充数据后，将其提交给codec组件。一旦一个input buffer在codec中排队，它就不再可用直到通过getInputBuffer(int)重新获取，getInputBuffer(int)是对dequeueInputbuffer(long)的返回值或onInputBufferAvailable(MediaCodec, int)回调的响应。
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
                    generateIndex++;
                }

                //2.解码的过程
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                while (outBufferIndex >= 0) {

                    ByteBuffer outputBuffer = outputBuffers[outBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        configbyte = new byte[bufferInfo.size];
                        configbyte = outData;
                    } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME) {
                        byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        outputStream.write(keyframe, 0, keyframe.length);
                    } else {
                        outputStream.write(outData, 0, outData.length);
                    }
                    mMediaCodec.releaseOutputBuffer(outBufferIndex,false);
                    outBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
                }

            }else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        mMediaCodec.stop();
        mMediaCodec.release();
        outputStream.flush();
        outputStream.close();
    }

    private void createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对输入数据进行转化
     * @param nv21
     * @param nv12
     * @param width
     * @param height
     */
    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }
}
