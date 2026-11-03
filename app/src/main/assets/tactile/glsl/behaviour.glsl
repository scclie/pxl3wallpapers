// !!! this code belongs to google (probably), is already publicly available, it is not originally from the project's author and is simply used here.

vec3 wind( vec3 pos, float t, float bend ) {
    if ( bend == 0.0 ) return pos;
    float L = .024; //.064;

    float nx = snoise( vec2( pos.x * L, t * .28 ) ) * bend;
    float ny = snoise( vec2( pos.y * L, t * .28 ) ) * bend;
    float nz = 0.0;//snoise( vec2( pos.z * L, t * .24 ) ) * bend;

    return pos + vec3( nx, ny, nz );
}

vec3 attract( vec3 pos, vec4 F, float amp ) {
    if ( F.w == 0.0 ) return pos;

    float d = distance( pos, F.xyz );
    float R = F.w * amp;

    vec3 p = pos;

    if ( d < R ) {
        vec3 Fd = normalize( F.xyz - pos ) * ( 1.0 - d/R );
        p += Fd;
    }

    return p;
}

vec3 attract( vec3 pos, vec4 F ) {
    return attract( pos, F, 8.0 );
}

vec3 repell( vec3 pos, vec4 F, float amp ) {
    //if ( F.w == 0.0 ) return pos;

    float d = distance( pos, F.xyz );
    float R = F.w * amp;

    vec3 p = pos;

    if ( d < R ) {
        vec3 Fd = normalize( F.xyz - pos ) * ( 1.0 - d/R );
        p -= Fd;
    }

    return p;
}

vec3 repell( vec3 pos, vec4 F ) {
    return repell( pos, F, 8.0 );
}

vec3 applySwipeForce( vec3 pos, float F ) {
    if ( F == 0.0 ) return pos;

    /*float d = distance( pos.xz, swipe.xz );
    float R = 16.0;

    if ( d < R ) {
        vec2 dir = normalize(swipe.xz - pos.xz);
        pos.x += dir.x * (1.0 - d/R) * F;
        pos.z += dir.y * (1.0 - d/R) * F;
    }*/

    pos.x += sin( M_PI * .5 * F + pos.y * .1 ) * .47 * F * swipe.y;
    pos.y += sin( M_PI * .5 * F + pos.y * .1 ) * .26 * F;
    /*pos.x += .17 * F * swipe.y;
    pos.y += .06 * F;*/

    return pos;
}

vec3 applyBehaviours( vec3 pos, float t, float bend, float weight ) {
    pos = wind( pos, t, bend );
    pos = repell( pos, F1 );
    pos = repell( pos, F2 );
    pos = repell( pos, F3 );
    pos = applySwipeForce( pos, swipe.w * weight );

    for(int i=0;i<TOUCH_POINTS;i++) {
        pos = attract( pos, vec4((counterSwipe * vec4(touch[i].xyz,1.0)).xyz, touch[i].w), 16.0 );
    }

    return pos;
}

float getAODLight( vec3 pos, vec3 L, float R ) {
    float d = distance( pos, L );
    float A = 0.0;

    if ( d < R ) {
        A = 1.0 - d/R;
    }

    return A;
}

float getAODLight( vec3 pos, vec3 L ) {
    return getAODLight( pos, L, 16.0 ); // default
}
