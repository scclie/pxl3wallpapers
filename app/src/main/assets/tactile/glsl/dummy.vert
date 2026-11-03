// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es

precision lowp float;

in vec3 a_position;

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;

void main() {
    gl_Position = projectionViewMatrix * modelMatrix * vec4(a_position, 1.0);
}
