#extension GL_OES_EGL_image_external : require

precision highp float;

uniform samplerExternalOES u_texture;
uniform float u_iGlobalTime;
uniform vec2 u_iResolution;
uniform vec2 u_tilt;
uniform vec2 u_translate;
uniform int u_cameraRotation;

uniform float u_scale;
uniform float u_mixFactor;

uniform int xyReverse;
uniform int drawLine;
uniform float rotation;
uniform int drawOuter;


const int ROTATION_0 = 0;
const int ROTATION_90 = 1;
const int ROTATION_180 = 2;
const int ROTATION_270 = 3;

float hueStep = 0.1;

vec2 rand2n(vec2 co, float sampleIndex) {
    vec2 seed = co * (sampleIndex + 1.0);
    seed+=vec2(-1,1);
    // implementation based on: lumina.sourceforge.net/Tutorials/Noise.html
    return vec2(fract(sin(dot(seed.xy ,vec2(12.9898,78.233))) * 43758.5453),
                fract(cos(dot(seed.xy ,vec2(4.898,7.23))) * 23421.631));
}

const float NO_ANSWER = -999999.;
vec2 cPos1 = vec2(1.2631, 0);
vec2 cPos2 = vec2(0, 1.2631);
float cr1 = 0.771643;
float cr2 = 0.771643;
const float PI = 3.14159265359;

vec2 g_pos;

vec2 circleInverse(vec2 pos, vec2 circlePos, float circleR){
    return ((pos - circlePos) * circleR * circleR)/(length(pos - circlePos) * length(pos - circlePos) ) + circlePos;
}

vec2 reverseStereoProject(vec3 pos){
    return vec2(pos.x / (1. - pos.z), pos.y / (1. - pos.z));
}

vec4 circleIntersection(vec2 cPos1, float r1, vec2 cPos2, float r2){
    float x = cPos1.x - cPos2.x;
    float y = cPos1.y - cPos2.y;
    float x2 = x * x;
    float y2 = y * y;
    float x2y2 = x2 + y2;
    float a = (x2y2 + r2 * r2 - r1 * r1) / 2.;
    float a2 = a * a;
    if(x2y2 * r2 * r2 - a2 < 0.) return vec4(NO_ANSWER);
    float numR = sqrt(x2y2 * r2 * r2 - a2);
    return vec4((a * x + y * numR) / x2y2 + cPos2.x, (a * y - x * numR) / x2y2 + cPos2.y,
                (a * x - y * numR) / x2y2 + cPos2.x, (a * y + x * numR) / x2y2 + cPos2.y);
}

vec3 stereoProject(vec2 pos){
    float x = pos.x;
    float y = pos.y;
    float x2y2 = x * x + y * y;
    return vec3((2. * x) / (1. + x2y2),
                (2. * y) / (1. + x2y2),
                (-1. + x2y2) / (1. + x2y2));
}

vec3 getCircleFromSphere(vec3 upper, vec3 lower){
    vec2 p1 = reverseStereoProject(upper);
    vec2 p2 = reverseStereoProject(lower);
    return vec3((p1 + p2) / 2., distance(p1, p2)/ 2.);
}

bool revCircle = false;
bool revCircle2 = false;
const int ITERATIONS = 50;
float colCount = 0.;
bool outer = false;
int IIS(vec2 pos){
    colCount = 0.;
    //if(length(pos) > 1.) return 0;

    bool fund = true;
    int invCount = 1;
    for(int i = 0 ; i < ITERATIONS ; i++){
        fund = true;
        if(xyReverse == 1){
            if (pos.x < 0.){
                pos *= vec2(-1, 1);
                invCount++;
                fund = false;
            }
            if(pos.y < 0.){
                pos *= vec2(1, -1);
                invCount++;
                fund = false;
            }
        }
        if(revCircle){
            if(distance(pos, cPos1) > cr1 ){
                pos = circleInverse(pos, cPos1, cr1);
                invCount++;
                colCount++;
                fund = false;
            }
            if(distance(pos, -cPos1) > cr1 ){
                pos = circleInverse(pos, -cPos1, cr1);
                invCount++;
                colCount++;
                fund = false;
            }
        }else{
            if(distance(pos, cPos1) < cr1 ){
                pos = circleInverse(pos, cPos1, cr1);
                invCount++;
                colCount++;
                fund = false;
            }
            if(distance(pos, -cPos1) < cr1 ){
                pos = circleInverse(pos, -cPos1, cr1);
                invCount++;
                colCount++;
                fund = false;
            }
        }

        if(revCircle2){
            if(distance(pos, cPos2) > cr2 ){
                pos = circleInverse(pos, cPos2, cr2);
                invCount++;
                colCount++;
                fund = false;
            }
            if(distance(pos, -cPos2) > cr2 ){
                pos = circleInverse(pos, -cPos2, cr2);
                invCount++;
                colCount++;
                fund = false;
            }
        }else{
            if(distance(pos, cPos2) < cr2 ){
                pos = circleInverse(pos, cPos2, cr2);
                invCount++;
                colCount++;
                fund = false;
            }
            if(distance(pos, -cPos2) < cr2 ){
                pos = circleInverse(pos, -cPos2, cr2);
                invCount++;
                colCount++;
                fund = false;
            }
        }

        if(fund){
            g_pos = pos;
            if(length(pos) > 1.5){
                outer = true;
                if(drawOuter == 1){
                    return invCount;
                }else{

                    return 0;
                }
            }
            g_pos = pos;
            return invCount;
        }
    }

    g_pos = pos;
    return invCount;
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 calcCircleFromLine(vec4 line){
    float a = line.x;
    float b = line.y;
    float c = line.z;
    float d = line.w;

    float bcad = b * c - a * d;
    float a2 = a * a;
    float b2 = b * b;
    float c2 = c * c;
    float d2 = d * d;
    float c2d2 = (1. + c2 + d2);
    vec2 pos = vec2(((1. + a2) * d + b2 * d - b * c2d2)/(-2. * bcad),
                    (a2 * c + (1. + b2) * c - a * c2d2)/ (2. * bcad));
    return vec3(pos, distance(pos, line.xy));
}

vec3 getCameraColor(vec2 p){
    if(u_cameraRotation == ROTATION_270){
        return  texture2D(u_texture, p.yx).rgb;
    }else if(u_cameraRotation == ROTATION_0){
        return  texture2D(u_texture, abs(vec2(1, 0)- p.xy)).rgb;
    }else if(u_cameraRotation == ROTATION_180){
        return texture2D(u_texture, abs(vec2(0, 1)- p.xy)).rgb;
    }else{
        return texture2D(u_texture, abs(vec2(1, 0)- p.xy)).rgb;
    }
}

const float sampleNum = 1.;
void main( ){
    float t = mod(u_iGlobalTime, 10.);
    t = abs(t - 5.) / 5.;

    float ratio = u_iResolution.x / u_iResolution.y / 2.0;
    vec3 sum = vec3(0);
    float x = 0.57735;

    float bendX =  u_tilt.x;//1. * abs(sin(u_iGlobalTime));;//PI / 6.;
    mat3 xRotate = mat3(1, 0, 0,
                        0, cos(bendX), -sin(bendX),
                        0, sin(bendX), cos(bendX));
    float bendY = u_tilt.y;
    //     float bendY = PI/6.5;//-abs(0.8 * sin(u_iGlobalTime));
    mat3 yRotate = mat3(cos(bendY), 0, sin(bendY),
                        0, 1, 0,
                        -sin(bendY), 0, cos(bendY));
    float y = .57735;
    vec3 c1 = getCircleFromSphere(vec3(0, y, sqrt(1. - y * y))* xRotate,
                                  vec3(0, y, -sqrt(1. - y * y))* xRotate);
    vec3 c2 = getCircleFromSphere(vec3(x, 0, sqrt(1. - x * x)) * yRotate,
                                  vec3(x, 0, -sqrt(1. - x * x)) * yRotate);

    cr1 = c1.z;
    cr2 = c2.z;
    cPos1 = c1.xy;
    cPos2 = c2.xy;
    if(y > cPos1.y){
        revCircle = true;
    }
    if(x > cPos2.x){
        revCircle2 = true;
    }

    vec4 intersection = circleIntersection(cPos1, cr1, cPos2, cr2);
    vec2 p1 = intersection.xy;
    vec2 p2 = intersection.zw;

    vec2 corner = p2;

    if(revCircle){
        corner = p1;
    }
    if(p1.x == NO_ANSWER && p1.y == NO_ANSWER &&
       p2.x == NO_ANSWER && p2.y == NO_ANSWER){
        p1 = vec2(1.5);
        corner = vec2(1.5);
    }
    vec2 texTranslate = corner;
    vec2 tile = corner * 2.;
    for(float i = 0. ; i < sampleNum ; i++){
        vec2 position = ( (gl_FragCoord.xy + rand2n(gl_FragCoord.xy, i)) / u_iResolution.yy ) - vec2(ratio, 0.5);
        mat2 m = mat2(cos(rotation), -sin(rotation),
                      sin(rotation), cos(rotation));

        position *= u_scale;//( 2.2 + ( t * 8.));

        position += u_translate;
        position = m * position;
        int d = IIS(position);

        if(d == 0){
            sum += vec3(0.,0.,0.);
        }else{
            vec3 ss;
            if(drawLine == 1){
                if(abs(distance(g_pos, cPos1) - cr1) < 0.01 ||
                   abs(distance(g_pos, cPos2) - cr2) < 0.01 ||
                   abs(distance(g_pos, -cPos1) - cr1) < 0.01 ||
                   abs(distance(g_pos, -cPos2) - cr2) < 0.01){
                    sum += vec3(0, 0, 0);
                }else{
                    ss = getCameraColor(abs( (g_pos + texTranslate) / tile));
                }
            }else{
                ss = getCameraColor(abs( (g_pos + texTranslate) / tile));
            }
            //continue;
            vec3 ss2;
            if(mod(float(d), 2.) == 0.){
                if(outer){
                    ss2 = hsv2rgb(vec3(0.5 + hueStep * colCount, 1., 1.));
                }else{
                    ss2 = hsv2rgb(vec3(hueStep * colCount, 1., 1.));
                }
            }else{
                if(outer){
                    ss2 = hsv2rgb(vec3(0.9 + hueStep * colCount, 1., 1.));
                }else{
                    ss2 = hsv2rgb(vec3(0.7 + hueStep * colCount, 1., 1.));
                }
            }
            sum += mix(ss, ss2, u_mixFactor);
        }
    }
    gl_FragColor = vec4(sum/sampleNum, 1.);
}
