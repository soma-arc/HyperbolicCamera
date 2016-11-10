package net.soma_arc.hyperboliccamera;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created by soma on 2016/11/07.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    private  final GLRenderer renderer;
    private GestureDetector gestureDetector;

    public MyGLSurfaceView(Context context){
        super(context);

        setEGLContextClientVersion(2);

        renderer = new GLRenderer(this, (Activity) context);

        setRenderer(renderer);

        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        gestureDetector = new GestureDetector(context, onGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return true;
    }

    private final GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDoubleTap(MotionEvent e){
            Toast.makeText(getContext(), "Saving...", Toast.LENGTH_LONG).show();

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.savePNG();
                }
            });
            return super.onDoubleTap(e);
        }
    };
}
