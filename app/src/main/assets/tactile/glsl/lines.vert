// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

#include SHADER_METHODS

in vec3 a_position;
in float a_weight;

uniform vec3 strandColor;
uniform vec3 aodColor2;
uniform vec3 centerColor;
uniform vec3 glowColor;
uniform lowp int glow;
uniform float notF, lockF;
uniform float aodF;
uniform float aodLightArea;
uniform vec3 aodL, aodL2;

#if COLOR_MODE == 1
    uniform vec3 tipColor;
    uniform float centerStep;
    uniform float midStep;
#endif

#include <tactile/glsl/vars.glsl>

out vec3 color;
out float alpha;
out float hide;

uniform mat4 modelViewMatrix;
uniform mat4 projectionViewMatrix;

#include <glsl/simplex.glsl>
#include <tactile/glsl/behaviour.glsl>

float cubicOut(float t) {
  float f = t - 1.0;
  return f * f * f + 1.0;
}

void main() {
    #if ALPHA_MODE == 0
        alpha = mix( 1.0, 0.4, smoothstep(.8, 1.0, a_weight) );
    #else
        alpha = 0.85;
    #endif


    color = mix( centerColor, glowColor, notF );

    float bend = a_weight * turbulence;
    float t = time;

    vec3 pos = applyBehaviours( a_position, t, bend, a_weight );

    float w = min( 1.0, aodF+lockF );
    float lAlpha = smoothstep( .83, 1.0, a_weight );
    float fAlpha = smoothstep( .25, 1.0, a_weight );
    float aL = min( 1.0, getAODLight( pos, aodL, aodLightArea ) + getAODLight( pos, aodL2, aodLightArea ) );

    alpha = mix( alpha, fAlpha, w );
    alpha = mix( alpha, lAlpha*aL, aodF );

    #if COLOR_MODE == 0
        color = mix( color, strandColor, a_weight );
        color = mix( color, mix(aodColor,aodColor2,notF), aodF );
    #else
        color = mix( mix(color, strandColor, smoothstep(centerStep, midStep, a_weight)), mix( strandColor, tipColor, (a_weight-midStep)/(1.0-midStep) ), step(midStep, a_weight) );
        color = mix( color, mix(aodColor,aodColor2,notF) * lAlpha*aL, aodF );
    #endif

    // discard lines behind
    vec4 mvPosition = modelViewMatrix * swipeRotation * vec4( pos, 1.0 );
    float distToCamera = -mvPosition.z;

    hide = step( 35., distToCamera );

    gl_Position = projectionViewMatrix * swipeRotation * vec4(pos, 1.0);
}
