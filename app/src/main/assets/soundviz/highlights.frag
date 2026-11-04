#version 310 es
// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

precision highp float;

uniform float u_time;
uniform float u_highlight_noise_period;
uniform vec2 u_highlight_noise_scale;
uniform vec3 u_highlight_noise_speed;
uniform vec2 u_resolution;

out vec4 fragColor;

#define OCTAVES 3
#define LACUNARITY 0.580
#define GAIN 0.432

#define NOISE_TYPE NOISE_PERLIN

// Include shader methods
#include <glsl/perlin_noise.glsl>
#include <glsl/fractal_brownian_motion.glsl>

float getNoise(vec3 st) {
    float time = u_time;
    // Scale the space in order to see the function
    vec3 noisePos = vec3(st.xy, u_highlight_noise_period);
    noisePos.xy *= u_highlight_noise_scale;
	float f = fbm(noisePos + u_highlight_noise_speed * u_time);
    float fExp = 0.5 * f * f + 0.5 * f;
    float fNorm = (fExp - 0.000) / (0.700 - 0.008);
	fNorm = fNorm;
    return smoothstep(0.05, 1., fNorm);
}

void main()  {
    vec3 st = vec3(gl_FragCoord.xy / u_resolution.yy, 1.0);
    float noise = getNoise(st);
    fragColor = vec4(noise, noise, noise, 1.);
}
