package com.freerdp.freerdpcore.presentation;

import com.freerdp.freerdpcore.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class VrRdpActivity extends Activity implements SessionActivity.VrFrameListener {

    public static VrRdpActivity sInstance = null;

    private ImageView leftEyeView;
    private ImageView rightEyeView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Bitmap lastFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);
        leftEyeView = findViewById(R.id.left_eye);
        rightEyeView = findViewById(R.id.right_eye);
        sInstance = this;
    }

    @Override
    public void onNewFrame(@NonNull Bitmap frameBitmap) {
        mainHandler.post(() -> {
            // 回收上一帧，避免内存暴涨
            if(lastFrame != null && !lastFrame.isRecycled()){
                lastFrame.recycle();
            }
            lastFrame = frameBitmap;
            leftEyeView.setImageBitmap(frameBitmap);
            rightEyeView.setImageBitmap(frameBitmap);
        });
    }

    @Override
    protected void onDestroy() {
        sInstance = null;
        // 退出时清空图片释放内存
        leftEyeView.setImageBitmap(null);
        rightEyeView.setImageBitmap(null);
        if(lastFrame != null && !lastFrame.isRecycled()){
            lastFrame.recycle();
            lastFrame = null;
        }
        super.onDestroy();

    }
}