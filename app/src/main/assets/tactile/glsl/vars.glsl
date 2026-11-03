// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

#define M_PI 3.1415926535897932384626433832795
#define TOUCH_POINTS 16

uniform float time;
uniform float turbulence;
uniform vec4 F1,F2,F3,swipe;
uniform vec4 touch[TOUCH_POINTS];
uniform vec3 aodColor;
uniform mat4 swipeRotation;
uniform mat4 counterSwipe;
