#include <jni.h>
#include "ffmpeg/ffmpeg.h"
#include<android/log.h>
#include "stdlib.h"
#ifndef LOG_TAG
#define LOG_TAG "FFMPEG_CMD"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG ,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG ,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG ,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG ,__VA_ARGS__) // 定义LOGE类型
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,LOG_TAG ,__VA_ARGS__) // 定义LOGF类型
#endif

static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl)
{
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);
    //sanitize((uint8_t *)line);

    if (level <= AV_LOG_WARNING)
    {
        LOGI("%s", line);
    }
    else
    {
        LOGI("%s", line);
    }
}

//com.ruanchao.videoedit.ffmpeg
JNIEXPORT jint JNICALL Java_com_ruanchao_videoedit_ffmpeg_FFmpegCmd_handle
(JNIEnv *env, jclass obj, jobjectArray commands){
    int argc = (*env)->GetArrayLength(env, commands);
    char **argv = (char**)malloc(argc * sizeof(char*));
    int i;
    int result;
    for (i = 0; i < argc; i++) {
        jstring jstr = (jstring) (*env)->GetObjectArrayElement(env, commands, i);
        char* temp = (char*) (*env)->GetStringUTFChars(env, jstr, 0);
        argv[i] = malloc(1024);
        strcpy(argv[i], temp);
        (*env)->ReleaseStringUTFChars(env, jstr, temp);
    }
    av_log_set_callback(log_callback_null);
    //执行ffmpeg命令
    result =  run(argc, argv);
    //释放内存
    for (i = 0; i < argc; i++) {
        free(argv[i]);
    }
    free(argv);
    return result;
}

/*
 * Class:     com_ruanchao_videoedit_ffmpeg_FFmpegCmd
 * Method:    getVideoInfo
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_ruanchao_videoedit_ffmpeg_FFmpegCmd_getVideoInfo
        (JNIEnv *env, jclass obj, jstring videoPath){
    LOGI("LOG from JNI");
    //jstring参数不能直接被C++程序使用，先做了如下转换
    const char* filePath = (*env)->GetStringUTFChars(env, videoPath, NULL);
    //1.注册
    av_register_all();
    //2.获取上下文
    AVFormatContext *pFormatContext = avformat_alloc_context();
    if (!pFormatContext) {
        return "pFormatContext=null";
    }
    //3.打开视频文件
    int openResult = avformat_open_input(&pFormatContext,filePath,NULL,NULL);

    if(openResult == -1){
        LOGE("Couldn’t open file");
        return -1;
    }
    int64_t duration = pFormatContext->duration;
    avformat_find_stream_info(pFormatContext, NULL);
    //4.找到视频流
    int i = 0;
    int V_stream_idx = -1;
    //av_find_best_stream()
    for (i ; i<pFormatContext->nb_streams; i++){
        if (pFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO){
            V_stream_idx  =i;
            break;
        }
    }
    if (V_stream_idx == -1){
        //没找到视频流
        return -1;
    }
    //5.解码器,目前没用到
    AVCodecContext *pCodecCtx = pFormatContext->streams[V_stream_idx]->codec;
    int with = pCodecCtx->width;
    int height = pCodecCtx->height;

    LOGI("---------- dumping stream info ----------");

    LOGI("input format: %s", pFormatContext->iformat->name);
    LOGI("nb_streams: %d", pFormatContext->nb_streams);

    int64_t start_time = pFormatContext->start_time / AV_TIME_BASE;
    LOGI("start_time: %lld", start_time);

    int64_t duration2 = pFormatContext->duration / AV_TIME_BASE;
    LOGI("duration: %lld s", duration2);
    int video_stream_idx = av_find_best_stream(pFormatContext, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
    if (video_stream_idx >= 0) {
        AVStream *video_stream = pFormatContext->streams[video_stream_idx];
        LOGI("video nb_frames: %lld", video_stream->nb_frames);
        LOGI("video codec_id: %d", video_stream->codec->codec_id);
        LOGI("video codec_name: %s", avcodec_get_name(video_stream->codec->codec_id));
        LOGI("video width x height: %d x %d", video_stream->codec->width, video_stream->codec->height);
        LOGI("video pix_fmt: %d", video_stream->codec->pix_fmt);
        LOGI("video bitrate %lld kb/s", (int64_t) video_stream->codec->bit_rate / 1000);
        LOGI("video avg_frame_rate: %d fps", video_stream->avg_frame_rate.num/video_stream->avg_frame_rate.den);
    }

    int audio_stream_idx = av_find_best_stream(pFormatContext, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
    if (audio_stream_idx >= 0) {
        AVStream *audio_stream = pFormatContext->streams[audio_stream_idx];
        LOGI("audio codec_id: %d", audio_stream->codec->codec_id);
        LOGI("audio codec_name: %s", avcodec_get_name(audio_stream->codec->codec_id));
        LOGI("audio sample_rate: %d", audio_stream->codec->sample_rate);
        LOGI("audio channels: %d", audio_stream->codec->channels);
        LOGI("audio sample_fmt: %d", audio_stream->codec->sample_fmt);
        LOGI("audio frame_size: %d", audio_stream->codec->frame_size);
        LOGI("audio nb_frames: %lld", audio_stream->nb_frames);
        LOGI("audio bitrate %lld kb/s", (int64_t) audio_stream->codec->bit_rate / 1000);
    }

    LOGI("---------- dumping stream info ----------");


    //6.获取视频元数据
//    AVDictionaryEntry *tag = NULL;
//    tag = av_dict_get(pFormatContext->streams[V_stream_idx]->metadata,"rotate",tag,0);
//    int angle = -1;
//    if (tag != NULL){
//         angle = atoi(tag->value);
//    }
    //7.释放资源
    avformat_free_context(pFormatContext);
    char *result = (char *)malloc(sizeof(char) * (10000));


    //返回的时候，要生成一个jstring类型的对象，也必须通过如下方式，必须要做转化
    jstring rtstr = (*env)->NewStringUTF(env,result);
    return rtstr;
}

JNIEXPORT jlong JNICALL
Java_com_ruanchao_videoedit_ffmpeg_FFmpegCmd_getVideoDuration(JNIEnv *env, jclass type,
                                                              jstring videoPath_) {
    //jstring参数不能直接被C++程序使用，先做了如下转换
    const char* filePath = (*env)->GetStringUTFChars(env, videoPath_, NULL);
    //1.注册
    av_register_all();
    //2.获取上下文
    AVFormatContext *pFormatContext = avformat_alloc_context();
    if (!pFormatContext) {
        return "pFormatContext=null";
    }
    //3.打开视频文件
    int openResult = avformat_open_input(&pFormatContext,filePath,NULL,NULL);

    if(openResult == -1){
        LOGE("Couldn’t open file");
        return -1;
    }
    int64_t duration = pFormatContext->duration/ AV_TIME_BASE;

    return duration;
}