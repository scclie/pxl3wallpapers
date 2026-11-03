// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision highp float;

uniform int mode;
uniform sampler2D map;
uniform vec2 resolution;
uniform lowp float aodF, notF;

in vec3 color;
in vec2 uv;

out vec4 fragColor;

#include <glsl/basic_dithering.glsl>

void main() {
    vec3 c = color;
    float r1 = notF * min( resolution.x, resolution.y ) / 6.0;
    float r2 = notF * min( resolution.x, resolution.y ) / 2.5;

    vec2 d;
    d.x = gl_FragCoord.x - resolution.x/2.0;
    d.y = gl_FragCoord.y - resolution.y/1.8;

    float circle = 0.0;

    float dist = distance( d, vec2(0.) );

    if ( dist < r1 ) circle = 1.0;
    else if ( dist < r2 ) circle = 1.0 - (dist-r1)/(r2-r1);

    fragColor = vec4( c, 1.0 );
    fragColor.rgb += mix(basicDithering(uv, 2.), 0., aodF);
}
