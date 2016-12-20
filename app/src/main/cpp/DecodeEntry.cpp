// -*- mode:c++; tab-width:2; indent-tabs-mode:nil; c-basic-offset:2 -*-
/*
 *  Copyright 2010-2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file excep t in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <iostream>
#include <zxing/common/Counted.h>
#include <zxing/Binarizer.h>
#include <zxing/MultiFormatReader.h>
#include <zxing/ReaderException.h>
#include <zxing/common/GlobalHistogramBinarizer.h>
#include <zxing/common/HybridBinarizer.h>
#include <zxing/common/GreyscaleLuminanceSource.h>

#include <zxing/qrcode/QRCodeReader.h>
#include <zxing/multi/qrcode/QRCodeMultiReader.h>
#include <zxing/multi/ByQuadrantReader.h>
#include <zxing/multi/GenericMultipleBarcodeReader.h>

extern "C" {
#include "zbar/zbar_entry.h"
}


using namespace std;
using namespace zxing;
using namespace zxing::multi;
using namespace zxing::qrcode;

namespace {

    bool more = false;
}
const static int QRCODE = 1;
const static int BARCODE = 2;

const char *decodeZxing(int dataWidth, int dataHeight, int left, int top, int width, int height,
                        char *rotateData) {

    try {
        ArrayRef<char> data(rotateData, dataWidth * dataHeight);
        Ref<LuminanceSource> source(
                new GreyscaleLuminanceSource(data, dataWidth, dataHeight, left,
                                             top, width, height));

        Ref<Binarizer> binarizer(new HybridBinarizer(source));
        Ref<BinaryBitmap> image(new BinaryBitmap(binarizer));

        DecodeHints hints(DecodeHints::DEFAULT_QR_HINT);
        MultiFormatReader reader;
        Ref<Result> result(reader.decode(image, hints));

        return result->getText()->getText().c_str();
    }

    catch (zxing::Exception &e) {
    }
    return NULL;
}

extern "C" jstring Java_com_duoyi_qrdecode_DecodeEntry_decodeFromJNI(JNIEnv *env, jobject thiz,
                                                                     jint decodeCode,
                                                                     jbyteArray data,
                                                                     jint dataWidth,
                                                                     jint dataHeight,
                                                                     jint left, jint top,
                                                                     jint width, jint height) {

    char *buffer = (char *) env->GetByteArrayElements(data, JNI_FALSE);
    char *rotateData = new char[dataWidth * dataHeight];
    for (int y = 0; y < dataHeight; y++) {
        for (int x = 0; x < dataWidth; x++) {
            rotateData[x * dataHeight + dataHeight - y - 1] = buffer[x + y * dataWidth];
        }
    }
    int tmp = dataWidth;
    dataWidth = dataHeight;
    dataHeight = tmp;

    jstring s = NULL;
    if ((decodeCode & QRCODE) == QRCODE) {
        const char *result = decodeZxing(dataWidth, dataHeight, left, top, width, height,
                                         rotateData);
        s = env->NewStringUTF(result);
    }
    if (s == NULL && ((decodeCode & BARCODE) == BARCODE)) {
        char *resultBar = decodeZbar(dataWidth, dataHeight, left, top, width, height, rotateData);
        s = env->NewStringUTF(resultBar);
    }

    env->ReleaseByteArrayElements(data, (jbyte *) buffer, 0);
    free(rotateData);
    return s;

}

extern "C" jstring Java_com_duoyi_qrdecode_DecodeEntry_decodeFileFromJNI(JNIEnv *env, jobject thiz,
                                                                         jint decodeCode,
                                                                         jintArray pixels,
                                                                         jint width, int height) {
    int *pixelsData = env->GetIntArrayElements(pixels, JNI_FALSE);
    char *yuv = new char[width * height];
    int y;
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            int rgb = pixelsData[i * width + j] & 0x00FFFFFF;
            int r = rgb & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = (rgb >> 16) & 0xFF;
            y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
            y = y < 0 ? 0 : (y > 255 ? 255 : y);
            yuv[i * width + j] = (byte) y;

        }
    }
    jstring s = NULL;
    if ((decodeCode & QRCODE) == QRCODE) {
        const char *result = decodeZxing(width, height, 0, 0, width, height, yuv);
        s = env->NewStringUTF(result);
    }

    if (s == NULL && ((decodeCode & BARCODE) == BARCODE)) {
        char *resultBar = decodeZbar(width, height, 0, 0, width, height, yuv);
        s = env->NewStringUTF(resultBar);
    }

    env->ReleaseIntArrayElements(pixels, pixelsData, JNI_FALSE);
    free(yuv);
    return s;
}






