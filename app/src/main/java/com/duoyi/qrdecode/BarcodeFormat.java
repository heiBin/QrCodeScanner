package com.duoyi.qrdecode;


public class BarcodeFormat {

    private int requstCode = 0;
    /**
     * 条形码
     */
    public static final int QRCODE = 1;
    /**
     * 二维码
     */
    public static final int BARCODE = 2;

    public void add(int code) {
        requstCode = requstCode | code;
    }

    public void set(int code) {
        requstCode = code;
    }

    public int get() {
        return requstCode;
    }
}
