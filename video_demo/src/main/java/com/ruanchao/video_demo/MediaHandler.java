package com.ruanchao.video_demo;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.ByteBuffer;

public class MediaHandler {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean splitVideo(String source, String out){
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(source);
            int mVideoTrackIndex = -1;
            int frameRate = 0;
            MediaMuxer mediaMuxer = null;
            for (int i = 0; i<mediaExtractor.getTrackCount(); i++){

                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (!mime.startsWith("video/")){
                    continue;
                }
                frameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                mediaExtractor.selectTrack(i);
                mediaMuxer = new MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                mVideoTrackIndex = mediaMuxer.addTrack(trackFormat);
                mediaMuxer.start();
            }

            if (mediaMuxer == null){
                return false;
            }

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500*1024);
            int sampleSize = 0;
            while ((sampleSize = mediaExtractor.readSampleData(byteBuffer,0)) > 0){
                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs += 1000*1000/frameRate;

                mediaMuxer.writeSampleData(mVideoTrackIndex,byteBuffer, info);
                mediaExtractor.advance();
            }
            mediaExtractor.release();

            mediaMuxer.stop();
            mediaMuxer.release();

        }catch (Exception e){

            return false;
        }
        return true;
    }
}
