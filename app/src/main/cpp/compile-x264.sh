#!/bin/bash

NDK=~/Android/Ndk/android-ndk-r14b
PLATFORM=$NDK/platforms/android-16/arch-arm
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
# arm v7vfp
CPU=armv7-a
PREFIX=`pwd`/build/$CPU

EXTRA_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=neon -D__ARM_ARCH_7__ -D__ARM_ARCH_7A__"
EXTRA_LDFLAGS="-nostdlib"

function build_one
{
    	./configure \
	--prefix=$PREFIX \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--sysroot=$PLATFORM \
	--extra-cflags=$EXTRA_CFLAGS \
	--extra-ldflags=$EXTRA_LDFLAGS \
	--enable-static \
	--enable-strip \
	--enable-pic \
	--disable-cli \
	--host=arm-linux \

make clean
make -j4
make install

}

build_one

