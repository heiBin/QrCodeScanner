# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.


file(GLOB CPP_FILES "src/main/cpp/*.cpp"
                    "src/main/cpp/bigint/*.cc"
                    "src/main/cpp/zxing/*.cpp"
                    "src/main/cpp/zxing/aztec/*.cpp"
                    "src/main/cpp/zxing/aztec/decoder/*.cpp"
                    "src/main/cpp/zxing/aztec/detector/*.cpp"
                    "src/main/cpp/zxing/common/*.cpp"
                    "src/main/cpp/zxing/common/detector/*.cpp"
                    "src/main/cpp/zxing/common/reedsolomon/*.cpp"
                    "src/main/cpp/zxing/datamatrix/*.cpp"
                    "src/main/cpp/zxing/datamatrix/decoder/*.cpp"
                    "src/main/cpp/zxing/datamatrix/detector/*.cpp"
                    "src/main/cpp/zxing/multi/*.cpp"
                    "src/main/cpp/zxing/multi/qrcode/*.cpp"
                    "src/main/cpp/zxing/multi/qrcode/detector/*.cpp"
                    "src/main/cpp/zxing/oned/*.cpp"
                    "src/main/cpp/zxing/pdf417/*.cpp"
                    "src/main/cpp/zxing/pdf417/decoder/*.cpp"
                    "src/main/cpp/zxing/pdf417/decoder/ec/*.cpp"
                    "src/main/cpp/zxing/pdf417/detector/*.cpp"
                    "src/main/cpp/zxing/qrcode/*.cpp"
                    "src/main/cpp/zxing/qrcode/decoder/*.cpp"
                    "src/main/cpp/zxing/qrcode/detector/*.cpp"
                    "src/main/cpp/decoder/*.c"
                    #"src/main/cpp/libiconv/*.c"
                    "src/main/cpp/video/*.c"
                    "src/main/cpp/window/*.c"
                    "src/main/cpp/zbar/*.c")

add_library( # Sets the name of the library.
             qrscan

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             ${CPP_FILES}
             src/main/cpp/DecodeEntry.cpp)
include_directories(src/main/cpp/)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
link_libraries(iconv)
find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       qrscan

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
