// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

in vec3 a_position;
in vec3 a_seed;
in float a_alpha;

uniform vec3 glowColor;
uniform vec3 dotColor;
uniform float notF;

uniform float globalAlpha;

#include <tactile/glsl/vars.glsl>

uniform mat4 projectionViewMatrix;
uniform mat4 modelViewMatrix;

out float depth;
out vec3 color;

#include <glsl/simplex.glsl>
#include <glsl/random.glsl>
#include <tactile/glsl/behaviour.glsl>

void main() {
    float bend = turbulence;
    float t = time;

    color = mix( dotColor, glowColor, notF );

    vec3 pos = applyBehaviours( a_seed, t, bend, 1.0 );

    vec4 mvPosition = modelViewMatrix * swipeRotation * vec4( pos, 1.0 );

    float distToCamera = -mvPosition.z;

    depth = 0.0;
    if ( distToCamera > 33.0 ) depth = smoothstep( 33.0, 39.0, distToCamera );

    if ( a_seed.x == 0.0 && a_seed.y == 0.0 ) depth = 1.0;

    /* face screen */
    vec4 rpos = counterSwipe * vec4(a_position, 1.0);

    float pA = globalAlpha > .98 ? 1.0 : smoothstep( a_alpha, a_alpha +.3, globalAlpha );
    pos = pos + rpos.xyz * (1.0-depth) * pA;

    gl_Position = projectionViewMatrix * swipeRotation * vec4(pos, 1.0);
}
