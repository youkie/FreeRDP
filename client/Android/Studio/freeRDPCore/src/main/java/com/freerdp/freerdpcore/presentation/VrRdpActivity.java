package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.freerdp.freerdpcore.application.FrameBus;
import com.google.cardboard.sdk.CardboardViewApi;
import com.google.cardboard.sdk.nativetypes.EyeTextureDescription;

import javax.microedition.khronos.EGLConfigChooser;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VrRdpActivity extends Activity {
    private static final String TAG = "VrRdpActivity";

    private GLSurfaceView mGlSurfaceView;
    private CardboardViewApi mCardboardApi;

    private static class VrRenderer implements GLSurfaceView.Renderer {
        private final CardboardViewApi mApi;
        private int mRdpTextureId = 0;

        public VrRenderer(CardboardViewApi api) {
            mApi = api;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // ⚠️必须在GL渲染线程初始化Render模块，UI线程调用直接崩溃
            mApi.initializeRenderThread();

            // 创建2D纹理
            int[] tex = new int[1];
            gl.glGenTextures(1, tex, 0);
            mRdpTextureId = tex[0];

            gl.glBindTexture(GL10.GL_TEXTURE_2D, mRdpTextureId);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mApi.setScreenParams(width, height);
            mApi.loadDeviceParams();
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Bitmap frame = FrameBus.INSTANCE.getLatestFrame();
            if (frame != null) {
                gl.glBindTexture(GL10.GL_TEXTURE_2D, mRdpTextureId);
                android.opengl.GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, frame, 0);
            }

            EyeTextureDescription leftEye = new EyeTextureDescription();
            leftEye.texture = mRdpTextureId;
            leftEye.leftU = 0.0f;
            leftEye.rightU = 1.0f;
            leftEye.topV = 0.0f;
            leftEye.bottomV = 1.0f;

            EyeTextureDescription rightEye = new EyeTextureDescription();
            rightEye.texture = mRdpTextureId;
            rightEye.leftU = 0.0f;
            rightEye.rightU = 1.0f;
            rightEye.topV = 0.0f;
            rightEye.bottomV = 1.0f;

            mApi.renderEyeToDisplay(leftEye, rightEye);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_rdp);
        mGlSurfaceView = findViewById(R.id.gl_surface);

        mCardboardApi = new CardboardViewApi(this);
        VrRenderer renderer = new VrRenderer(mCardboardApi);
        mGlSurfaceView.setEGLConfigChooser((EGLConfigChooser) null);
        mGlSurfaceView.setRenderer(renderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
        mCardboardApi.resumeHeadTracker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCardboardApi.pauseHeadTracker();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close必须投递到GL渲染线程执行，禁止UI线程直接close
        mGlSurfaceView.queueEvent(() -> {
            if (mCardboardApi != null) {
                mCardboardApi.close();
            }
        });
    }
}