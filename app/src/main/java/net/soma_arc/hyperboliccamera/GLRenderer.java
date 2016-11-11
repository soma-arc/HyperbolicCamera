package net.soma_arc.hyperboliccamera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public void handleScroll(MotionEvent e1, MotionEvent e2, float dx, float dy){
        rect.scroll(e1.getX(), e1.getY(), e2.getX(), e2.getY());
    }

    public void onDown(MotionEvent e){
        rect.onDown(e.getX(), e.getY());
    }

    public void scaleBegin(){
        rect.scaleBegin();
    }

    public void handleScaling(float scaleFactor){
        rect.tweakScale(scaleFactor);
    }

    public static Bitmap capture(final int w, final int h){
        final int pixels[] = new int[w * h];
        final int result[] = new int[w * h];
        IntBuffer buffer = IntBuffer.wrap(pixels);
        buffer.position(0);
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        // This code is based on http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html
        // See also http://d.hatena.ne.jp/orangesignal/20120814/1344923993
        for(int i=0; i<h; i++) {
            for(int j=0; j<w; j++) {
                int pix=pixels[i*w+j];
                int pb=(pix>>16)&0xff;
                int pr=(pix<<16)&0x00ff0000;
                int pix1=(pix&0xff00ff00) | pr | pb;
                result[(h-i-1)*w+j]=pix1;
            }
        }
        return Bitmap.createBitmap(result, w, h, Bitmap.Config.ARGB_8888);
    }

    public void savePNG()
    {
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            Bitmap bmp = capture((int) resolution[0], (int) resolution[1]);

            Date date = new Date();
            SimpleDateFormat name = new SimpleDateFormat("yyyyMMdd_HHmmss");
            final int quality = 100;
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            try {
                path.mkdir();
                File file = new File(path, name.format(date) +".png");
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, quality, fos);
                fos.flush();
                fos.close();

                // from  https://developer.android.com/reference/android/os/Environment.html
                MediaScannerConnection.scanFile(activity,
                        new String[] { file.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                savePNG();
            }
        }

    }
}
