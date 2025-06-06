#pragma name "GummyFireflies2"
// #iUniform color3 iColorRGB = vec3(0.964, 0.144, 0.519)
// #iUniform color3 iColor2RGB = vec3(0.226, 0.046, 0.636)
#iUniform float iRotationAngle = 0.0 in {0.0, 6.28}
#iUniform float iSpeed = 0.5 in {0.0, 4.0}
#iUniform float iScale = 0.05 in {0.01, 1.0}
#iUniform float iQuantity = 3.0 in {1.0, 9.0}
#iUniform float iWow2 = 2.5 in {1.0, 3.0}
#iUniform float iWow1 = 0.9 in {0.0, 2.0}

#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.141592653589793
#define HALF_PI 1.5707963267948966
#define TWO_PI 6.28318530718
#define TAU 6.28318530718

vec3 col1 = vec3(0.964, 0.144, 0.519);
vec3 col2 = vec3(0.226, 0.046, 0.636);

/****************************************************
 * Palettes                                         *
 ****************************************************/

vec3 palette( float t )
{
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263, 0.416, 0.557);

    return a + b*cos( 6.28318*(c*t*d) );
}

vec3 mixPalette( vec3 c1, vec3 c2, float t )
{
    float mixFactor = 0.5 + sin(t);
    return mix(c1, c2, mixFactor);
}

vec3 rgb2hsb( in vec3 c ){
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz),
                 vec4(c.gb, K.xy),
                 step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r),
                 vec4(c.r, p.yzx),
                 step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)),
                d / (q.x + e),
                q.x);
}

vec3 hsb2rgb( in vec3 c ){
    vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),
                             6.0)-3.0)-1.0,
                     0.0,
                     1.0 );
    rgb = rgb*rgb*(3.0-2.0*rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

vec3 getPaletteColor(float t) {
    vec3 c1hsb = rgb2hsb(iColorRGB);
    vec3 c2hsb = rgb2hsb(iColor2RGB);
    float mixFactor = 0.5 + sin(t);
    return hsb2rgb(mixPalette(c1hsb, c2hsb, mixFactor));

    //// for inside the loop:
    // float mixFactor = fract((i + N*0.5*iTime) / N);
}

/****************************************************
 * SDFs                                             *
 ****************************************************/

float sdCircle(vec2 st) {
    // NOTE: modified to reference zero
    return length(st-.0)*2.;
}

float sdRect(vec2 st, vec2 s) {
    // st = st*2.-1.;
    //// NOTE: modified to reference zero (commented line above)
    return max(abs(st.x/s.x), abs(st.y/s.y));
}

/****************************************************
 * Drawing                                          *
 ****************************************************/

float stroke(float x, float s, float w) {
    float d = step(s, x + w * .5) - step(s, x - w * .5);
    return clamp(d, 0., 1.);
}

float fill(float x, float size) {
    return 1. - step(size, x);
}

void debugGrid(in vec2 pt, out vec3 color) {
    color.r += 0.2*(1.0 - smoothstep(0.0, 0.02, abs(fract(pt.x))));
    color.g += 0.2*(1.0 - smoothstep(0.0, 0.02, abs(fract(pt.y))));
    // color.r += 1.0 - smoothstep(0.0, 0.02, abs(pt.x));
    // color.g += 1.0 - smoothstep(0.0, 0.02, abs(pt.y));
}

void debugRect(in float rect, out vec3 color) {
    if (rect < 1.) {
        color.b += .4*(rect);
    }
    if (rect < 2.) {
        color.g += .4*(rect / 2.);
    }
    color.b += 0.3*stroke(rect, 1., 0.01);
    color.g += 0.2*stroke(rect, 2., 0.01);
}

/****************************************************
 * Utils                                            *
 ****************************************************/

float flip(float v, float pct) {
    return mix(v, 1.-v, pct);
}

vec2 rotate(vec2 st, float a) {
    st = mat2(cos(a), -sin(a), sin(a), cos(a)) * (st-.5);
    return st + .5;
}

vec2 random2( vec2 p ) {
    return fract(sin(vec2(dot(p,vec2(127.1,311.7)),dot(p,vec2(269.5,183.3))))*43758.5453);
}

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))
                 * 43758.5453123);
}

// 2D Noise based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    // Smooth Interpolation

    // Cubic Hermine Curve.  Same as SmoothStep()
    vec2 u = f*f*(3.0-2.0*f);
    // u = smoothstep(0.,1.,f);

    // Mix 4 coorners percentages
    return mix(a, b, u.x) +
            (c - a)* u.y * (1.0 - u.x) +
            (d - b) * u.x * u.y;
}

#define N 100.
#define ALPHA 1. / N

vec3 drawSquiggle(
    in vec2 st,
    in vec2 dims,
    in vec2 offset,
    in float max_n,
    in vec4 wave,
    in float radius
) {
    vec3 color = vec3(0.);

    // vec2 x_wave = wave.xy;
    float x_freq = wave.x;
    float x_speed = wave.y;

    // vec2 y_wave = wave.zw;
    float y_freq = wave.z;
    float y_speed = wave.w;

    vec2 crop = dims.xy - vec2(.5 * radius);
    vec3 pal = getPaletteColor(iTime);

    vec2 step = vec2(0.);  // x and y step size (for next circle)
    vec2 acc = vec2(0.);   // accumulated offset, relative to (st), incl all past steps
    vec2 dir = vec2(1.);   // direction of the squiggle

    vec2 currOffset = offset;

    for (float i = 0.; i < N; i++) {
        if (i > max_n) {
            break;
        }

        vec2 limit = dir * crop;
        step = dir * currOffset;

        float noiseStep = noise(i* (st + acc));

        step.y += noiseStep * sin(y_freq*i + y_speed*iTime);
        // step.y *= sin(y_freq*i + y_speed*iTime);
        // step.y *= clamp(i, 0., 10.) * 0.05; // fix the base

        if (dir.y > 0. ? acc.y > limit.y : acc.y < limit.y) {
            offset.x += dir.y * 0.02;
            dir.y *= -1.;
        }

        step.x *= noiseStep * sin(x_freq*i + x_speed*iTime);
        // step.x *= clamp(i, 0., 10.) * 0.05; // fix the base

        if (dir.x > 0. ? acc.x > limit.x : acc.x < limit.x) {
            offset.y += dir.x * 0.02;
            dir.x *= -1.;
        }

        acc += step;

        color += fill(sdCircle(st + acc), radius) * (N-i) * pal * ALPHA;
    }

    return color;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    vec3 color = vec3(0.);
    vec2 st = fragCoord.xy/iResolution.xy;
    st.x *= iResolution.x/iResolution.y;
    st = rotate(st, iRotationAngle);
    st -= vec2(0.5);
    st *= iQuantity;

    // blob size pulses with drums, amount controlled by levelReact
    float blobScale = (stemOther > 0.1) ? -1.0 + 2.0 * stemOther : 0.0;
    float radius = iScale + (levelReact * iScale * blobScale);

    // inter-firefly spacing changes w/stem value, controlled by frequencyReact
    // turned up, this gets really fun!
    float spacing = max(0.05,(stemVocals * frequencyReact) / 3.0);

    // offset the start time a little so that the fireflies don't start
    // grouped tightly together
    float flyTime = iTime + 3.;

    for (float i = 0.; i < iQuantity; i++) {
        // distance between fireflies;
        float t = flyTime + i * spacing;
        float d = 0.05 * i;

        // v is the current position of the firefly - looks random, but is actually a deterministic
        // wobbly circular path.
        vec2 v = 1.5*cos(t*d)*vec2(cos(t*1.5), sin(t*3.) ) + 1.5*sin(t*d) * vec2( cos(t*2.), sin(t*.75) );

        vec2 gradient_v = -d*sin(t*d) * vec2(cos(t*1.5), sin(t*3.))
                + cos(t*d) * vec2(-1.5*sin(t*1.5), 3.0*cos(t*3.))
                + d*cos(t*d) * vec2(cos(t*2.), sin(t*.75))
                + sin(t*d) * vec2(-2.0*sin(t*2.), 0.75*cos(t*.75));

        float w = .4;
        float h = .4;
        vec2 bounds = vec2(w, h);

        vec2 rand2 = random2(vec2(i));
        float r = rand2.x + rand2.y;

        float freq = .1 + .1 * r;
        float speed = 1.5 + r;

        float len = N*(0.5 + .5*sin(iTime + 2.*r));

        vec2 step_size = vec2(
            0.02 + 0.02 * r,
            2. / N
        );
        vec4 wave = vec4(freq, speed, 0., 0.);

        color += drawSquiggle(
            st+v,
            bounds,
            step_size * gradient_v,
            len,
            wave,
            radius
        );
    }
    fragColor = vec4(0.);
    if (iWowTrigger) {
        // cool the entire backbuffer by a small amount
        fragColor = max(vec4(0.),texelFetch(iBackbuffer, ivec2(gl_FragCoord.xy), 0) - iWow1 / 10.);
    }
    fragColor += vec4(color,1.0);
}