package com.freerdp.freerdpcore.application;

import android.graphics.Bitmap;

public enum FrameBus {
    INSTANCE;

    private Bitmap mLatestFrame = null;

    public synchronized void setLatestFrame(Bitmap bitmap) {
        mLatestFrame = bitmap;
    }

    public synchronized Bitmap getLatestFrame() {
        return mLatestFrame;
    }

    public synchronized void clear() {
        mLatestFrame = null;
    }
}