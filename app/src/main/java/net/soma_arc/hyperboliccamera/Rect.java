package net.soma_arc.hyperboliccamera;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by soma on 2016/11/07.
 */

public class Rect {
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int program;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static final float SQUARE_COORDS[] = {
            -1.0f,  1.0f,   // top left
            -1.0f, -1.0f,   // bottom left
            1.0f, -1.0f,   // bottom right
            1.0f,  1.0f,}; // top right

    private final short SQUARE_DRAW_ORDER[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private int attribLocation;

    private ArrayList<Integer> uniLocation = new ArrayList<>();

    private int cameraTexureId;
    private Camera camera;

    public Rect(GLSurfaceView context, int cameraTextureId, Camera camera){
        this.cameraTexureId = cameraTextureId;
        this.camera = camera;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                SQUARE_COORDS.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SQUARE_COORDS);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                SQUARE_DRAW_ORDER.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(SQUARE_DRAW_ORDER);
        drawListBuffer.position(0);

        int vertexShader = GLUtil.loadShader(context, GLES20.GL_VERTEX_SHADER, R.raw.rect_vert);
        int fragmentShader = GLUtil.loadShader(context, GLES20.GL_FRAGMENT_SHADER,  R.raw.hyperbolic_tessellator_frag);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        uniLocation.add(GLES20.glGetUniformLocation(program, "u_iResolution"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_iGlobalTime"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_texture"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_cameraRotation"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_tilt"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_translate"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_scale"));
        uniLocation.add(GLES20.glGetUniformLocation(program, "u_mixFactor"));

        attribLocation = GLES20.glGetAttribLocation(program, "a_vertex");
        GLES20.glEnableVertexAttribArray(attribLocation);
        GLES20.glVertexAttribPointer(attribLocation, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);

        GLES20.glUseProgram(program);
    }

    private float[] tilt  =  {0.7f, 0.f};
    private float[] translate =  {0.f, 0.f};
    private float scale = 7.f;
    private float mixFactor = 0.f;

    public void draw(float[] resolution, int time){
        int index = 0;
        GLES20.glUniform2fv(uniLocation.get(index++), 1, resolution, 0);
        GLES20.glUniform1f(uniLocation.get(index++), time);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTexureId);
        GLUtil.checkGlError("bindCameraTexture");
        GLES20.glUniform1i(uniLocation.get(index++), 0);
        GLES20.glUniform1i(uniLocation.get(index++), camera.getRotation());
        GLES20.glUniform2fv(uniLocation.get(index++), 1, tilt, 0);
        GLES20.glUniform2fv(uniLocation.get(index++), 1, translate, 0);
        GLES20.glUniform1f(uniLocation.get(index++), scale);
        GLES20.glUniform1f(uniLocation.get(index++), mixFactor);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, SQUARE_DRAW_ORDER.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }
}
