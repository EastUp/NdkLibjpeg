
cmake_minimum_required(VERSION 3.4.1)

# include 头文件
include_directories(libjpeg_turbo/include)

# 设置输出路径为项目目录下 (PROJECT_SOURCE_DIR : cmake.txt所在目录)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../../../../jniLibs/${CMAKE_ANDROID_ARCH_ABI})

add_library( # Sets the name of the library.
             native-lib
             SHARED
             native-lib.cpp
             jpegcompress.cpp
        )

add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libjpeg_turbo/libs/${CMAKE_ANDROID_ARCH_ABI}/libturbojpeg.a)

find_library( # Sets the name of the path variable.
              log-lib
              log )


target_link_libraries( # Specifies the target library.
                       native-lib
                       # Android Bitmap需要的库
                       jnigraphics
                       # libjpeg-turbo 压缩图片
                       libjpeg
                       ${log-lib} )