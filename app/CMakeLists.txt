cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
             ffmpegcmd

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
              src/main/cpp/ffmpeg/include/cmdutils.c
              src/main/cpp/ffmpeg/include/ffmpeg.c
              src/main/cpp/ffmpeg/include/ffmpeg_filter.c
              src/main/cpp/ffmpeg/include/ffmpeg_hw.c
              src/main/cpp/ffmpeg/include/ffmpeg_opt.c
              src/main/cpp/ffmpeg_cmd_utils.c
              )

#添加libavcodec-57.so
add_library( avcodec
             SHARED
             IMPORTED)
set_target_properties( avcodec
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libavcodec.so)

#添加libavdevice-57.so
add_library( avdevice
             SHARED
             IMPORTED)
set_target_properties( avdevice
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libavdevice.so)

add_library( avfilter
             SHARED
             IMPORTED)
set_target_properties( avfilter
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libavfilter.so)

add_library( avformat
             SHARED
             IMPORTED)
set_target_properties( avformat
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libavformat.so)

add_library( avutil
             SHARED
             IMPORTED)
set_target_properties( avutil
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libavutil.so)

add_library( swresample
             SHARED
             IMPORTED)
set_target_properties( swresample
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libswresample.so)

add_library( swscale
             SHARED
             IMPORTED)
set_target_properties( swscale
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/cpp/ffmpeg/armeabi-v7a/libswscale.so)



include_directories(src/main/cpp/ffmpeg/include)



find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

target_link_libraries( # Specifies the target library.

                       ffmpegcmd
                       avcodec
                       avdevice
                       avfilter
                       avformat
                       avutil
                       swresample
                       swscale

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )