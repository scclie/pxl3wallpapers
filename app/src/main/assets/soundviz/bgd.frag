// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

uniform vec4 u_color;

in vec2 v_uv;
out vec4 fragColor;

void main()  {
    fragColor = u_color;
}
