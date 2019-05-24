//
// Created by zhx on 19-5-24.
//

#ifndef FFMPEGFIRST_MASTER_NATIVEDECODE_H
#define FFMPEGFIRST_MASTER_NATIVEDECODE_H


#include <jni.h>
#include <android/log.h>

class NativeDecode {

public:
    static jint decode_mp4_to_yuv(JNIEnv *env, jclass clazz, jstring file_path);

};


#endif //FFMPEGFIRST_MASTER_NATIVEDECODE_H
