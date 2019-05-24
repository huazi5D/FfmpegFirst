//
// Created by zhx on 19-5-24.
//

#include <jni.h>
#include "decode/NativeDecode.h"

#define JNI_CLASS_DECODE     "hz/ffmpegfirst/Decode"

static JNINativeMethod g_methods[] = {
        {"decodeMp4ToYUV",        "(Ljava/lang/String;)I", (void *) NativeDecode::decode_mp4_to_yuv}
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    jclass clazz = env->FindClass(JNI_CLASS_DECODE);

    env->RegisterNatives(clazz, g_methods, sizeof(g_methods) / sizeof(g_methods[0]));

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved) {
    JNIEnv *env = nullptr;

    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return;
    }
}
