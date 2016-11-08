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
    private long startTime;
    private Camera camera;

    private boolean configured = false;

    public GLRenderer(GLSurfaceView view, Activity activity){
        this.view = view;
        this.activity = activity;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int cameraTexture = GLUtil.createTexture();
        camera = new Camera(activity, cameraTexture);
        camera.open();

        rect = new Rect(view, cameraTexture, camera);
        startTime = SystemClock.uptimeMillis();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        resolution[0] = width;
        resolution[1] = height;
        GLES20.glViewport(0, 0, width, height);
        configured = false;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!configured) {
            if (configured = camera.getInitialized()) {
                camera.setCameraRotation();
            } else {
                return;
            }
        }

        camera.updateTexture();

        long time = SystemClock.uptimeMillis() - startTime;
        rect.draw(resolution, 0);
    }

}
