// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

uniform lowp int glow;
uniform float globalAlpha;
uniform float notF, aodF, lockF;

in float alpha;
in vec3 color;
in float hide;
out vec4 fragColor;

void main() {
    if ( hide == 1.0 ) discard;
    fragColor = vec4( color, alpha * globalAlpha * (1.0-hide) );
}
