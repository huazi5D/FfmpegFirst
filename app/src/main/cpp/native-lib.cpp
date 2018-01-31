#include <jni.h>
#include <string>
#include<android/log.h>

extern "C" {

//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//过滤器
#include "libavfilter/avfilter.h"

JNIEXPORT jstring JNICALL Java_hz_ffmpegfirst_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jint JNICALL Java_hz_ffmpegfirst_MainActivity_getAvFormatInfo(JNIEnv *env, jobject obj, jstring filePath) {

    // 输入文件
    const char * input = env->GetStringUTFChars(filePath, nullptr);

    av_register_all();

    AVFormatContext* pFormatCtx = nullptr;

    if (avformat_open_input(&pFormatCtx, input, nullptr, nullptr) != 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG,"zhx","文件打开失败！！！");
        return -1;
    }

    if (avformat_find_stream_info(pFormatCtx, nullptr))
    {
        return -1;
    }

    int videoStream = -1;
    int audioStream = -1;

    for (int i = 0; i < pFormatCtx->nb_streams; ++i)
    {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO)
        {
            videoStream = i;
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","videoStream = %d", i);
        }
        else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
        {
            audioStream = i;
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","audioStream = %d", i);
        }
    }

    AVCodecParameters *pCodecParam = pFormatCtx->streams[videoStream]->codecpar;

    AVCodecContext *pCodecCtx = avcodec_alloc_context3(nullptr);

    if (avcodec_parameters_to_context(pCodecCtx, pCodecParam) < 0)
    {
        return -1;
    }

    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    if (avcodec_open2(pCodecCtx, pCodec, nullptr) < 0)
    {
        return -1;
    }

    AVFrame * pFrame, *pFrameYUV;
    pFrame = av_frame_alloc();
    pFrameYUV = av_frame_alloc();
    return 0;
}

}
