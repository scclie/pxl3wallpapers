// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es
#define PI 3.1415926535897932384626433832795
#define TWO_PI 2. * PI

precision highp float;

in vec3 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform mat4 u_transform;
uniform float u_time;
uniform float u_seed;

uniform vec2 u_resolution;

uniform float u_aod;
uniform float u_locked;
uniform float u_aod_vignette;

uniform float u_center;
uniform float u_amp;
uniform float u_freq;
uniform float aod_amp;
uniform float u_phase;

uniform float u_pull_noise_speed;
uniform float u_pull_noise_freq;
uniform float u_pull_noise_amp;

uniform float u_outer_noise_speed;
uniform float u_outer_noise_freq;
uniform float u_outer_noise_amp;

uniform float u_inner_noise_speed;
uniform float u_inner_noise_freq;
uniform float u_inner_noise_freq_2;
uniform float u_inner_noise_amp;
uniform float u_inner_noise_variation;

uniform float u_color_var_freq;
uniform float u_color_var_speed;

uniform float u_opacity_max;
uniform float u_opacity_min;

uniform float u_opacity_thr_min;
uniform float u_opacity_thr_max;

// Color combination A
uniform vec4 u_color_a_outer;
uniform vec4 u_color_a_cp1;
uniform vec4 u_color_a_cp2;
uniform vec4 u_color_a_cp3;
uniform vec4 u_color_a_cp4;
uniform vec4 u_color_a_inner;

// Color combination B
uniform vec4 u_color_b_outer;
uniform vec4 u_color_b_cp1;
uniform vec4 u_color_b_cp2;
uniform vec4 u_color_b_cp3;
uniform vec4 u_color_b_cp4;
uniform vec4 u_color_b_inner;

// Vignette color
uniform vec4 u_vignette_color;

out highp vec2 v_uv;
out highp float v_center_grad;
out highp vec4 v_pos_grad;

out vec4 v_color_outer;
out vec4 v_color_1;
out vec4 v_color_2;
out vec4 v_color_3;
out vec4 v_color_4;
out vec4 v_color_inner;
out vec4 v_vignette_color;

const vec4 aod_color_bg = vec4(0., 0., 0., 1.);
const vec4 aod_color_fg = vec4(0.35, 0.35, 0.35, 1.);

#define NOISE_TYPE NOISE_PERLIN

// Include shader methods
#include <glsl/simplex.glsl>

float sinNormalized(float rad, float size) {
    float halfSize = size / 2.;
    return (halfSize + halfSize * sin(rad * TWO_PI));
}

void updateGradientPoints(  in vec2 uv,
                            in float time,
                            out vec4 posGrad,
                            out float centerGrad) {
    /* Times */
    float time_1 = time * u_pull_noise_speed;
    float time_2 = time * u_outer_noise_speed + 352.3;
    float time_3 = time * u_inner_noise_speed + 35.8;

    /* Noises */
    float noise_1 = snoise(vec2(uv.x * u_pull_noise_freq + time_1, 3.4 + u_seed));
    float noise_2 = snoise(vec2(uv.x * u_outer_noise_freq, time_2 + u_seed));
    float noise_3 = snoise(vec2(uv.x * u_inner_noise_freq + time_3, 74.8 + u_seed));

    float centerNoise = u_pull_noise_amp * noise_1;
    float variation = centerNoise + u_amp * sin((uv.x + centerNoise) * aod_amp * u_freq * TWO_PI + u_phase);

    centerGrad = u_center + variation;
    float innerAmpGrad = u_inner_noise_amp * (1.49 + u_inner_noise_variation * (sin((uv.x * aod_amp + time * 0.03 + 0.15 * u_inner_noise_freq_2 * noise_3) * u_freq * 1.1 * TWO_PI)));
    float outerAmpGrad = u_outer_noise_amp * (noise_2 * 0.5 + 0.5);

    /* Locked animation: pinch in the center */
    innerAmpGrad *= mix(1., 1. - (smoothstep(0.30, 0.49, uv.x) - smoothstep(0.51, 0.7, uv.x)), u_locked);
    // Set the positions for the gradients, that goes from the innerRad (0.0) point to tue outerRad (1.0)
    posGrad.y = centerGrad * (1. - max(innerAmpGrad, 0.0005));
    posGrad.z = centerGrad + max((1. - centerGrad) * innerAmpGrad, 0.0005);

    posGrad.x = max(posGrad.y - outerAmpGrad, 0.05);
    posGrad.w = min(posGrad.z + outerAmpGrad, 0.95);
}

void updateGradientColors(  in vec2 uv,
                            in vec4 posGrad,
                            in float time,
                            out vec4 colorInner,
                            out vec4 color1,
                            out vec4 color2,
                            out vec4 color3,
                            out vec4 color4,
                            out vec4 colorOuter) {

    float scaledTime = time * u_color_var_speed + 53.89;

    float colorVariation = sinNormalized(uv.x * u_color_var_freq + scaledTime, 1.);

    // Make that when the second and third point are really close, the opacity is 1 for the second color.
    float diff = abs(posGrad.z - posGrad.y);
    float opacity = mix(u_opacity_max, u_opacity_min, smoothstep(u_opacity_thr_min, u_opacity_thr_max, diff));

    colorInner = mix(u_color_a_inner, u_color_b_inner, colorVariation);
    color1 = mix(u_color_a_cp1, u_color_b_cp1, colorVariation);
    color2 = mix(u_color_a_cp2, u_color_b_cp2, colorVariation);
    color3 = mix(u_color_a_cp3, u_color_b_cp3, colorVariation);
    color4 = mix(u_color_a_cp4, u_color_b_cp4, colorVariation);
    colorOuter = mix(u_color_a_outer, u_color_b_outer, colorVariation);

    /* Apply opacity. */
    color3 = mix(colorOuter, color3, opacity);
    color4 = mix(colorOuter, color4, opacity);

    /* Apply AOD colors. */
    colorInner = mix(colorInner, aod_color_bg, u_aod);
    color1 = mix(color1, aod_color_bg, u_aod);
    color2 = mix(color2, aod_color_bg, u_aod * 0.3);
    color3 = mix(color3, aod_color_bg, u_aod);
    color4 = mix(color4, aod_color_bg, u_aod);
    colorOuter = mix(colorOuter, aod_color_bg, u_aod);

//    v_vignette_color = u_vignette_color;
    vec4 vignetteColorAod = u_vignette_color;
    vignetteColorAod.a = 0.;
    v_vignette_color = mix(u_vignette_color, vignetteColorAod, u_aod_vignette);
}


void main()  {
    // Fix UV coordinates
    v_uv = a_texCoord0;
    v_uv.y = 1.0 - v_uv.y;

    // Oscilate gradient positions
    updateGradientPoints(v_uv, u_time, v_pos_grad, v_center_grad);

    // Update colors
    updateGradientColors(v_uv, v_pos_grad, u_time, v_color_inner, v_color_1, v_color_2, v_color_3, v_color_4, v_color_outer);

    gl_Position = u_projTrans * u_transform * vec4(a_position, 1.0);
}
