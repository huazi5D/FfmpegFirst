#!/bin/bash

rm -rf build
rm ffbuild/config*

NDK=~/Android/Ndk/android-ndk-r14b
PLATFORM=$NDK/platforms/android-16/arch-arm
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64

X264_PREFIX=`pwd`/../x264/build/armv7-a

echo $X264_PREFIX

ADD_H264_FEATURE="--enable-gpl \
    --enable-version3 \
    --enable-encoder=libx264 \
    --enable-libx264 \
    --extra-cflags=-I$X264_PREFIX/include \
    --extra-ldflags=-L$X264_PREFIX/lib" 

function build_one
{
    ./configure \
	--prefix=$PREFIX \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--sysroot=$PLATFORM \
	--extra-cflags="-I$PLATFORM/usr/include" \
	--cc=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \
	--nm=$TOOLCHAIN/bin/arm-linux-androideabi-nm \
	--disable-everything \
	--disable-shared \
	--enable-static \
	--enable-nonfree \
	--disable-symver \
        --enable-ffmpeg \
        --enable-ffplay \
        --enable-ffprobe \
	--disable-avdevice \
	--target-os=linux \
	--arch=arm \
	--enable-cross-compile \
	--disable-parsers \
	--enable-parser=h264 \
	--enable-parser=hevc \
	--enable-parser=aac \
	--disable-encoders \
	--enable-encoder=aac \
	--disable-decoders \
	--enable-decoder=h264 \
	--enable-decoder=hevc \
	--enable-decoder=aac \
	--disable-network \
	--disable-protocols \
	--disable-muxers \
	--enable-muxer=mp4 \
	--disable-demuxers \
	--enable-demuxer=aac \
	--enable-demuxer=mp3 \
	--enable-demuxer=mov \
	--enable-demuxer=concat \
	--enable-demuxer=data \
	--enable-demuxer=mpegps \
	--enable-demuxer=mpegts \
	--disable-filters \
	--enable-filter=aformat \
	--enable-filter=volume \
	--enable-filter=amix \
	--enable-filter=aresample \
	--disable-debug \
	$ADD_H264_FEATURE

make clean
make -j4
make install

$TOOLCHAIN/bin/arm-linux-androideabi-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
    libavcodec/libavcodec.a \
    libavfilter/libavfilter.a \
    libswresample/libswresample.a \
    libavformat/libavformat.a \
    libavutil/libavutil.a \
    libswscale/libswscale.a \
    ../x264/build/armv7-a/lib/libx264.a \
    -lc -lm -lz -ldl -llog \
    $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a \

}

# arm v7vfp
CPU=armv7-a
PREFIX=`pwd`/build/$CPU
build_one

