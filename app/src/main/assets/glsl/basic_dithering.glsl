// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

// Basic Dithering
//
highp float basicDithering (in highp vec2 st, highp float intensity) {
    return mix(-intensity/ 255., intensity/255., fract(sin(dot(st.xy, vec2(12.9898,78.233)))* 43758.5453123));
}
