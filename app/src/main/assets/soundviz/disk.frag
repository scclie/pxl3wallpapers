// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

/* Other Methods */
#include SHADER_METHODS

precision lowp float;

uniform sampler2D u_highlights;
uniform float u_outer_noise_amp;
uniform float u_inner_noise_amp;
uniform highp float u_aod;

uniform highp vec2 u_resolution;
uniform vec2 u_aspect_ratio;
uniform vec4 u_vignette_color;
uniform vec2 u_vignette_scale;
uniform vec2 u_vignette_center;

// highlight color
uniform vec4 u_highlight_color;
uniform float u_highlight_intensity;

#if SHOW_AUDIO_BLOCK == 1
uniform float u_audio_block[BLOCK_SIZE];
uniform float u_fft_bands[FFT_SIZE];
uniform float u_loudness;
#endif

// Color combination
in vec4 v_color_outer;
in vec4 v_color_1;
in vec4 v_color_2;
in vec4 v_color_3;
in vec4 v_color_4;
in vec4 v_color_inner;
in vec4 v_vignette_color;
in float v_time_osc;

in vec2 v_uv;
in vec4 v_pos_grad;
in float v_center_grad;

out highp vec4 fragColor;

const vec4 aod_color_bg = vec4(0., 0., 0., 1.);

// Include shader methods
#include <glsl/basic_dithering.glsl>

float getLineX(vec2 uv, float start, float end) {
    return step(start, uv.x) * (1. - step(end, uv.x));
}

float getLineY(vec2 uv, float start, float end) {
    return step(start, uv.y) * (1. - step(end, uv.y));
}

void main()  {
    highp vec2 st = gl_FragCoord.xy / u_resolution.yy;
    vec2 st2 = gl_FragCoord.xy / u_resolution.xy;

    /* Interactive Color */
    float val1 = smoothstep(0., v_pos_grad.x, v_uv.y);
    float val2 = smoothstep(v_pos_grad.x, v_pos_grad.y, v_uv.y);
    float val3 = smoothstep(v_pos_grad.y, v_pos_grad.z, v_uv.y);
    float val4 = smoothstep(v_pos_grad.z, v_pos_grad.w, v_uv.y);
    float val5 = smoothstep(v_pos_grad.w, 1.0, v_uv.y);

    float highlightCp1Int = clamp(u_highlight_intensity * texture(u_highlights, st2).r * (1. - u_aod),0., 1.);

    vec4 comb5 = mix(v_color_4, vec4(v_color_outer.rgb, 1.), val5);
    vec4 comb4 = mix(v_color_3, comb5, val4);
    float aodAlpha = smoothstep(0.17, 0.47, st2.x) - smoothstep(0.53, 0.83, st2.x);

    vec4 v_color_2_new = mix(v_color_2, u_highlight_color, highlightCp1Int);
    vec4 aodColor = mix(v_color_inner, v_color_2_new, aodAlpha); // edges
    aodColor = mix(v_color_2_new, aodColor, u_aod);

    vec4 comb3 = mix(aodColor, comb4, val3);
//    vec4 comb3 = mix(v_color_2, comb4, val3);
    vec4 comb2 = mix(mix(v_color_1, u_highlight_color, highlightCp1Int), comb3, val2);
//    vec4 comb2 = mix(v_color_1, comb3, val2);
    fragColor = mix(mix(vec4(v_color_inner.rgb, 1.), u_highlight_color, highlightCp1Int), comb2, val1);

    // Vignetting
    float vignetteRadius = smoothstep(0.3, 1.1, length(u_vignette_scale * (st2 - u_vignette_center)));
    fragColor = mix(fragColor, v_vignette_color, vignetteRadius * v_vignette_color.a);
    fragColor.rgb += basicDithering(st, 4.);

//    /* AOD Vignette */
//    float vRad = smoothstep(0., 1., length(u_vignette_intensity * (st2 - u_vignette_center)));
//    fragColor.rgb = mix(aodValue, 0., vRad);
//    aodValue = clamp(aodValue, 0., 1.);

    /* DEBUG */
#if SHOW_LINES == 1
    float ar = 4./5.;
    vec2 thickness = vec2(0.005);
    thickness.x *= ar;

//    fragColor = vec4(1., 0., 0., 1.);
    float lines = getLineY(v_uv, 1.0 - thickness.y, 1.0);
    lines += getLineY(v_uv, 0.0, thickness.y);
    lines += getLineX(v_uv, 1.0 - thickness.x, 1.0);
    lines += getLineX(v_uv, 0.0, thickness.x);
    lines += getLineY(v_uv, v_center_grad - thickness.y / 2., v_center_grad + thickness.y / 2.);

    lines += 0.25 * getLineY(v_uv, v_pos_grad.y - thickness.y / 2., v_pos_grad.y + thickness.y / 2.);
    lines += 0.25 * getLineY(v_uv, v_pos_grad.z - thickness.y / 2., v_pos_grad.z + thickness.y / 2.);

    lines += 0.05 * getLineY(v_uv, v_pos_grad.x - thickness.y / 2., v_pos_grad.x + thickness.y / 2.);
    lines += 0.05 * getLineY(v_uv, v_pos_grad.w - thickness.y / 2., v_pos_grad.w + thickness.y / 2.);

    fragColor.rgb = mix(fragColor.rgb, vec3(0., 0., 1.0), lines);
#endif

#if SHOW_AUDIO_BLOCK == 1
    vec2 thick = vec2(0.005);
    int idx = int(floor(st2.x * float(BLOCK_SIZE)));

    // Soundwave
    float valueBlock = u_audio_block[idx];
    valueBlock = valueBlock * 0.25 + 0.5;
    float soundWave = getLineY(st2, valueBlock - thick.y / 2., valueBlock + thick.y / 2.);

    // loudness
    float peak = u_loudness * 0.25 + 0.5;
    soundWave += 0.25 * getLineY(st2, peak - thick.y / 2., peak + thick.y / 2.);

    // fft
    int idxFFT = int(floor(st2.x * float(FFT_SIZE)));
    float valueFFT = u_fft_bands[idxFFT];
    float fft = valueFFT * 0.25;

    soundWave += (0.5 + 0.2 * float(idxFFT % 2)) * (1.-step(fft, st2.y));

    // combine
    fragColor.rgb = mix(fragColor.rgb, vec3(0., 1., 0.), soundWave);
#endif
}
