package net.soma_arc.hyperboliccamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

/**
 * Created by soma on 2016/11/07.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    private GLRenderer renderer;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    public MyGLSurfaceView(Context context){
        super(context);
        if(!isInEditMode())
            init(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        if(!isInEditMode())
            init(context);
    }

    private void init(Context context){
        setEGLContextClientVersion(2);

        renderer = new GLRenderer(this, (Activity) context);

        setRenderer(renderer);

        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        gestureDetector = new GestureDetector(context, onGestureListener);
        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        scaleGestureDetector.onTouchEvent(e);
        return true;
    }

    private final GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e){
            renderer.onDown(e);
            return true;
        }

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

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy){
            renderer.handleScroll(e1, e2, dx, dy);
            return super.onScroll(e1, e2, dx, dy);
        }
    };

    private final ScaleGestureDetector.SimpleOnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            renderer.scaleBegin();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            renderer.handleScaling(detector.getScaleFactor());
            renderer.scaleEnd();
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            renderer.handleScaling(detector.getScaleFactor());
            return super.onScale(detector);
        }
    };
}
