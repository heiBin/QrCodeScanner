/*
 * Copyright (C) 2010 ZXing authors
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

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.duoyi.provider.qrscan.R;
import com.duoyi.provider.qrscan.activity.MipcaActivityCapture;
import com.duoyi.provider.qrscan.camera.CameraManager;
import com.duoyi.qrdecode.BarcodeFormat;
import com.duoyi.qrdecode.DecodeEntry;

final class DecodeHandler extends Handler {


    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final MipcaActivityCapture activity;
    private BarcodeFormat barcodeFormat;

    DecodeHandler(MipcaActivityCapture activity, BarcodeFormat barcodeFormat) {
        this.activity = activity;
        this.barcodeFormat = barcodeFormat;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        String result = null;
        Rect rect = CameraManager.get().getRealFramingRect();
        result = DecodeEntry.getDecodeResult(barcodeFormat, data, width, height, rect.left, rect.top,
                rect.width(), rect.height());


        if (!TextUtils.isEmpty(result)) {
            Message message = Message.obtain(activity.getHandler(),
                    R.id.decode_succeeded, result);
            message.sendToTarget();
        } else {
            Message message = Message.obtain(activity.getHandler(),
                    R.id.decode_failed);
            message.sendToTarget();
        }

    }


}
