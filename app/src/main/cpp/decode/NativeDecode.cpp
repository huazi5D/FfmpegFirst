//
// Created by zhx on 19-5-24.
//

extern "C" {

#include <libavutil/imgutils.h>
#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavformat/avformat.h>
#include "NativeDecode.h"

}

jint NativeDecode::decode_mp4_to_yuv(JNIEnv *env, jclass clazz, jstring file_path) {
    int ret = 0;

    FILE * yuv = fopen("sdcard/111/av/video.yuv", "wb+");
    const char *file = env->GetStringUTFChars(file_path, nullptr);

    AVFormatContext *formatContext = nullptr;
    ret = avformat_open_input(&formatContext, file, nullptr, nullptr);
    if (ret != 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "zhx", "file is open faild ...");
        return -1;
    }

    ret = avformat_find_stream_info(formatContext, nullptr);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "zhx", "avformat_find_stream_info faild ...");
        return -1;
    }

    int videoStream = -1;
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        if (formatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoStream = i;
        }
    }

    AVCodecParameters* codecParameters = formatContext->streams[videoStream]->codecpar;
    AVCodecContext* codecContext = avcodec_alloc_context3(nullptr);
    avcodec_parameters_to_context(codecContext, codecParameters);

    avcodec_open2(codecContext, avcodec_find_decoder(codecContext->codec_id), nullptr);

    AVFrame *originFrame = av_frame_alloc();
    AVFrame *yuvFrame = av_frame_alloc();

    uint8_t* outBuffer = (uint8_t *) av_malloc(static_cast<size_t>(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, codecContext->width, codecContext->height, 1)));
    av_image_fill_arrays(originFrame->data, originFrame->linesize, outBuffer, AV_PIX_FMT_YUV420P, codecContext->width, codecContext->height, 1);

    SwsContext * swsContext = sws_getContext(yuvFrame->width, yuvFrame->height, codecContext->pix_fmt, yuvFrame->width, yuvFrame->height, AV_PIX_FMT_YUV420P, SWS_BICUBIC,
                                             nullptr, nullptr, nullptr);

    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    while (av_read_frame(formatContext, packet) == 0) {
        if (packet->stream_index == videoStream) {

            ret = avcodec_send_packet(codecContext, packet);
            if (ret < 0 && ret != AVERROR(EAGAIN) && ret != AVERROR_EOF) {
                __android_log_print(ANDROID_LOG_DEBUG,"zhx","avcodec_send_packet: faild");
                return -1;
            }

            char* buf[100];
            ret = avcodec_receive_frame(codecContext, yuvFrame);
            if (ret != 0) {
                __android_log_print(ANDROID_LOG_DEBUG,"zhx","avcodec_receive_frame: faild");
                return -1;
            }

            sws_scale(swsContext, (const uint8_t *const *) originFrame->data, originFrame->linesize, 0, codecContext->height, yuvFrame->data, yuvFrame->linesize);

            size_t size = static_cast<size_t>(codecContext->width * codecContext->height);
            fwrite(yuvFrame->data[0], 1, size, yuv);
        }
    }

    return 0;
}

