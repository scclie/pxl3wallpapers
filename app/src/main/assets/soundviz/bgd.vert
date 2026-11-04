// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#version 310 es
#define PI 3.1415926535897932384626433832795

in vec3 a_position;
in vec2 a_texCoord0;

precision highp float;

uniform mat4 u_projTrans;
uniform mat4 u_viewTrans;

out highp vec2 v_uv;

void main()  {
  v_uv = a_texCoord0;
  v_uv.y = 1.0 - v_uv.y;

  gl_Position = u_viewTrans * vec4(a_position, 1.0);
}
