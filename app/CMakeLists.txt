cmake_minimum_required(VERSION 3.4.1)

set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

#link_libraries(${distribution_DIR}/armeabi-v7a/libffmpeg.so)
add_library( ffmpeg SHARED IMPORTED)
set_target_properties( ffmpeg PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/armeabi-v7a/libffmpeg.so)
include_directories(src/main/cpp/include)

find_library( log-lib log )

#获取cpp文件下的cpp文件
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp CPP)
aux_source_directory(${CMAKE_SOURCE_DIR}/src/main/cpp/decode DECODE)

add_library(ff-decode SHARED ${CPP} ${DECODE})

target_link_libraries( ff-decode
                       ffmpeg

                       ${log-lib} )
