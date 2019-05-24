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

#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>

JNIEXPORT jstring JNICALL Java_hz_ffmpegfirst_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jint JNICALL Java_hz_ffmpegfirst_MainActivity_getAvFormatInfo(JNIEnv *env, jobject obj, jstring filePath) {

    // 输入文件
    const char * input = env->GetStringUTFChars(filePath, nullptr);
    __android_log_print(ANDROID_LOG_DEBUG, "zhx", "%s", input);
    FILE * yuv = fopen("sdcard/111/av/video.yuv", "wb+");

    AVFormatContext* pFormatCtx = nullptr;

    int err_code;
    char buf[1024];
    if ((err_code = avformat_open_input(&pFormatCtx, input, nullptr, nullptr)) != 0)
    {
        av_strerror(err_code, buf, 1024);
        __android_log_print(ANDROID_LOG_DEBUG,"zhx","文件打开失败！！！ %d(%s)", err_code, buf);
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
        __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu");
        return -1;
    }

    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    if (avcodec_open2(pCodecCtx, pCodec, nullptr) < 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu");
        return -1;
    }

    AVFrame * pFrame, *pFrameYUV;
    pFrame = av_frame_alloc();
    pFrameYUV = av_frame_alloc();

    uint8_t * out_buffer = (uint8_t *) av_malloc(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pCodecParam->width, pCodecParam->height, 1));
    av_image_fill_arrays(pFrame->data, pFrame->linesize, out_buffer, AV_PIX_FMT_YUV420P, pCodecParam->width, pCodecParam->height, 1);

    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));

    SwsContext * swsContext = sws_getContext(pFrameYUV->width, pFrameYUV->height, pCodecCtx->pix_fmt, pFrameYUV->width, pFrameYUV->height, AV_PIX_FMT_YUV420P, SWS_BICUBIC,
                                             nullptr, nullptr, nullptr);

    int ret;
    int size;
    while (av_read_frame(pFormatCtx, packet) >= 0)
    {
        __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu1");
        if (packet->stream_index == videoStream)
        {
            ret = avcodec_send_packet(pCodecCtx, packet);
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu2");
            if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF)
            {
                av_packet_unref(packet);
                return -1;
            }

            ret = avcodec_receive_frame(pCodecCtx, pFrameYUV);
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu3");
            if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF)
            {
                av_packet_unref(packet);
                return -1;
            }
            else
            {
                // TODO 可以显示YUV
            }

            if (pFrameYUV->pict_type == AV_PICTURE_TYPE_I)
            {
                // 获取当前帧持续时间
                const int64_t frameDuration = av_frame_get_pkt_duration(pFrameYUV);
            }

            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu4");
            sws_scale(swsContext, (const uint8_t *const *) pFrame->data, pFrame->linesize, 0, pCodecCtx->height, pFrameYUV->data, pFrameYUV->linesize);
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu5");
            size = pCodecCtx->width * pCodecCtx->height;
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu6");
            fwrite(pFrameYUV->data[0], 1, size, yuv);
            __android_log_print(ANDROID_LOG_DEBUG,"zhx","cuo wu7");
        }
        else
        {
            // TODO audio
        }

        av_packet_unref(packet);
    }

    av_frame_free(&pFrameYUV);
    av_frame_free(&pFrame);
    avcodec_close(pCodecCtx);
    avcodec_parameters_free(&pCodecParam);
    return 0;
}

}
