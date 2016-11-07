package net.soma_arc.hyperboliccamera;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by soma on 2016/11/07.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    private  final GLRenderer renderer;

    public MyGLSurfaceView(Context context){
        super(context);

        setEGLContextClientVersion(2);

        renderer = new GLRenderer(this, (Activity) context);

        setRenderer(renderer);

        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
