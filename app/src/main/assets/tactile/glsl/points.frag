// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

uniform vec2 resolution;
uniform vec3 bgColor1;
uniform vec3 bgColor2;
#include <tactile/glsl/vars.glsl>

in float depth;
in vec3 color;

out vec4 fragColor;

void main () {
    float alpha = 1.0-depth;
    if ( alpha < .1 ) discard;

    vec3 bgColor = mix( bgColor1, bgColor2, 1.0 - gl_FragCoord.y/resolution.y );
    fragColor = vec4(mix(color, bgColor, depth), 1.0);
}
