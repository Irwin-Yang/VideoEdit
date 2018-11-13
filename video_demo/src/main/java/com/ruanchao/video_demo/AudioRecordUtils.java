package com.ruanchao.video_demo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 音频录制与转化
 */
public class AudioRecordUtils {

    static final String TAG = AudioRecordUtils.class.getSimpleName();
    AudioRecord mAudioRecord;
    int SAMPLE_RATE_INHZ  = 44100;
    int CHANNEL_CONFIG  = AudioFormat.CHANNEL_IN_MONO;
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSizeInBytes;
    byte[] buffer;
    Context mContext;
    volatile boolean isRecording = false;
    File file;

    public AudioRecordUtils(Context context){
        mContext = context;
    }

    public void createAudioRecord(){
        bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ,CHANNEL_CONFIG,AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSizeInBytes);
        buffer = new byte[bufferSizeInBytes];
        file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+ "test.pcm");
        try {
            if (!file.createNewFile()) {
                Log.e(TAG, "Directory not created");
            }
        }catch (Exception e){

        }
    }

    public void startRecord(){

        if (isRecording) {
            Toast.makeText(mContext,"请先停止录制", Toast.LENGTH_LONG).show();
            return;
        }
        isRecording = true;
        if (mAudioRecord == null) {
            createAudioRecord();
        }
        mAudioRecord.startRecording();
        //开启线程从buffer中读取数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        int read = mAudioRecord.read(buffer, 0, bufferSizeInBytes);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(buffer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i(TAG, "run: close file output stream !");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    public void covert(){
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        File wavFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+ "test.wav");
        pcmToWavUtil.pcmToWav(file.getAbsolutePath(), wavFile.getAbsolutePath());
    }

    public void stopRecord(){
        isRecording = false;
        if (mAudioRecord != null && isRecording) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }
}
