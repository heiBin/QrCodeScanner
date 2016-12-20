package com.duoyi.qrdecode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DecodeEntry {
    static {
        System.loadLibrary("qrscan");
    }

    public static String decodeFromFile(String filename, BarcodeFormat barcodeFormat) {
        Bitmap bitmap = BitmapFactory.decodeFile(filename);
        return getPixelsByBitmap(bitmap, barcodeFormat);
    }

    public static String getPixelsByBitmap(Bitmap bitmap, BarcodeFormat barcodeFormat) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = width * height;

        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();
        if (barcodeFormat != null) {
            return decodeFileFromJNI(barcodeFormat.get(), pixels, width, height);
        } else {
            return decodeFileFromJNI(BarcodeFormat.BARCODE | BarcodeFormat.QRCODE, pixels, width, height);
        }
    }

    public static String getDecodeResult(BarcodeFormat barcodeFormat, byte[] data, int dataWidth,
                                         int dataHeight, int left, int top, int width, int height) {
        if (barcodeFormat != null) {
            return decodeFromJNI(barcodeFormat.get(), data, dataWidth, dataHeight, left, top, width, height);
        } else {
            return decodeFromJNI(BarcodeFormat.BARCODE | BarcodeFormat.QRCODE, data, dataWidth, dataHeight, left, top, width, height);
        }

    }

    public native static String decodeFromJNI(int decodeCode, byte[] data, int dataWidth,
                                              int dataHeight, int left, int top, int width, int height);

    public native static String decodeFileFromJNI(int decodeCode, int[] pixels, int width, int height);

}
