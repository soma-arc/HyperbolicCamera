package net.soma_arc.hyperboliccamera;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by soma on 2016/11/07.
 */

public final class GLUtil {
    public static int loadShader(final GLSurfaceView context,
                                 final int shaderType,
                                 final int resourceId){
        return compileShader(shaderType, readTextFileFromRawResource(context, resourceId));
    }

    public static String readTextFileFromRawResource(final GLSurfaceView context,
                                                     final int resourceId){
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        }catch (IOException e) {
            return null;
        }
        return body.toString();
    }

    public static int compileShader(final int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int logLength[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_INFO_LOG_LENGTH, logLength, 0);
        if (logLength[0] > 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            Log.e("GLRenderer", "Shader compile log:" + log);
        }
        int status[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e("GLRender", "Couldn't compile shader");
            GLES20.glDeleteShader(shader);
            return -1;
        }
        return shader;
    }
}
