#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform float u_iGlobalTime;
uniform vec2 u_iResolution;
uniform samplerExternalOES u_texture;
uniform int u_cameraRotation;

const int ROTATION_0 = 0;
const int ROTATION_90 = 1;
const int ROTATION_180 = 2;
const int ROTATION_270 = 3;

void main(){
    vec4 texCoord =  vec4(gl_FragCoord.xy / u_iResolution, 0, 0);
    if(u_cameraRotation == ROTATION_270){
        gl_FragColor =  texture2D(u_texture, texCoord.yx);
    }else if(u_cameraRotation == ROTATION_0){
        gl_FragColor =  texture2D(u_texture, abs(vec2(1, 0)- texCoord.xy));
    }else if(u_cameraRotation == ROTATION_180){
        gl_FragColor =  texture2D(u_texture, abs(vec2(0, 1)- texCoord.xy));
    }else{
        gl_FragColor =  texture2D(u_texture, abs(vec2(1, 0)- texCoord.xy));
    }
}
