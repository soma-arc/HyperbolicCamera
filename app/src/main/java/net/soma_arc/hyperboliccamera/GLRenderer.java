package net.soma_arc.hyperboliccamera;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by soma on 2016/11/07.
 */

public class GLRenderer implements GLSurfaceView.Renderer {
    private GLSurfaceView view;
    private Activity activity;
    private Rect rect;
    private float[] resolution = new float[2];
    long startTime;

    public GLRenderer(GLSurfaceView view, Activity activity){
        this.view = view;
        this.activity = activity;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        rect = new Rect(view);
        startTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        resolution[0] = width;
        resolution[1] = height;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long time = SystemClock.uptimeMillis() - startTime;
        rect.draw(resolution, 0);
    }
}
