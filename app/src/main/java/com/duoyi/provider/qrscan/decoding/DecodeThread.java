/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.duoyi.provider.qrscan.decoding;

import android.os.Handler;
import android.os.Looper;


import com.duoyi.provider.qrscan.activity.MipcaActivityCapture;
import com.duoyi.qrdecode.BarcodeFormat;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {

    private final MipcaActivityCapture activity;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;
    private BarcodeFormat barcodeFormat;

    DecodeThread(MipcaActivityCapture activity,
                 BarcodeFormat barcodeFormat) {

        this.activity = activity;
        this.barcodeFormat = barcodeFormat;
        handlerInitLatch = new CountDownLatch(1);

    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity, barcodeFormat);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
