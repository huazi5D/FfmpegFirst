cmake_minimum_required(VERSION 3.4.1)

find_library( log-lib log )

add_library( native-lib SHARED src/main/cpp/native-lib.cpp )

set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

add_library( ffmpeg SHARED IMPORTED)
set_target_properties( ffmpeg PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/armeabi-v7a/libffmpeg.so)

include_directories(src/main/cpp/include)

target_link_libraries( native-lib
                       ffmpeg

                       ${log-lib} )
