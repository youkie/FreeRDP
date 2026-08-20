package com.freerdp.freerdpcore.presentation;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;


import com.freerdp.freerdpcore.R;
import com.freerdp.freerdpcore.application.FrameBus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VrRdpActivity extends Activity {
    private static final String TAG = "VrRdpActivity";
    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_rdp);
        mGlSurfaceView = findViewById(R.id.gl_surface);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setRenderer(new SimpleStereoRenderer());
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    private static class SimpleStereoRenderer implements GLSurfaceView.Renderer {
        private int mSurfaceWidth = 0;
        private int mSurfaceHeight = 0;
        private int mRdpTextureId = 0;
        private final FloatBuffer mVertexBuffer;
        private final FloatBuffer mTexCoordBuffer;

        public SimpleStereoRenderer() {
            float[] vertices = {
                    -1.0f, -1.0f, 0,
                    1.0f, -1.0f, 0,
                    -1.0f, 1.0f, 0,
                    1.0f, 1.0f, 0
            };
            float[] texCoords = {
                    0,1,
                    1,1,
                    0,0,
                    1,0
            };
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length *4);
            tbb.order(ByteOrder.nativeOrder());
            mTexCoordBuffer = tbb.asFloatBuffer();
            mTexCoordBuffer.put(texCoords);
            mTexCoordBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(0,0,0,1);
            int[] tex = new int[1];
            gl.glGenTextures(1, tex,0);
            mRdpTextureId = tex[0];
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mRdpTextureId);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            gl.glViewport(0,0,width,height);
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            GLU.gluOrtho2D(gl, -1,1,-1,1);
            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            Bitmap frame = FrameBus.INSTANCE.getLatestFrame();
            if(frame != null){
                gl.glBindTexture(GL10.GL_TEXTURE_2D,mRdpTextureId);
                android.opengl.GLUtils.texImage2D(GL10.GL_TEXTURE_2D,0,GL10.GL_RGBA,frame,0);
            }
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            // ========== 左眼：绘制屏幕左半部分 ==========
            gl.glViewport(0,0,  mSurfaceWidth/2, mSurfaceHeight);
            gl.glVertexPointer(3, GL10.GL_FLOAT,0,mVertexBuffer);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT,0,mTexCoordBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);

            // ========== 右眼：绘制屏幕右半部分 ==========
            gl.glViewport( mSurfaceWidth/2, 0,  mSurfaceWidth/2, mSurfaceHeight);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,4);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glDisable(GL10.GL_TEXTURE_2D);
        }
    }
}