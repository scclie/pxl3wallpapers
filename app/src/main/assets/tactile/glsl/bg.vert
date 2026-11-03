// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

in vec3 a_position;
in vec2 a_texCoord0;

uniform vec3 color1;
uniform vec3 color2;
uniform float aodF;

out vec3 color;
out vec2 uv;

void main() {
    uv = a_texCoord0;
    color = mix( color1, color2, 1.0 - a_texCoord0.y );
    color = mix( color, vec3(0.), aodF );
    gl_Position = vec4(a_position, 1.0);
}
