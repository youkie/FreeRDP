package com.freerdp.freerdpcore.application;

import android.graphics.Bitmap;

/**
 * 跨Activity帧数据中转，仅传递引用，不拷贝像素
 * volatile 保证多线程可见性，UI线程写，GL渲染线程读
 */
public class FrameBus {
    public static final FrameBus INSTANCE = new FrameBus();

    private volatile Bitmap mLatestFrame;

    private FrameBus() {
    }

    public void setLatestFrame(Bitmap bitmap) {
        mLatestFrame = bitmap;
    }

    public Bitmap getLatestFrame() {
        return mLatestFrame;
    }
}