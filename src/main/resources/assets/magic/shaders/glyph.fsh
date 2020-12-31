#version 110

const float OUTLINE_RADIUS = 0.9F;
const float OUTLINE_FADE = 0.1F;

uniform vec3 color;
uniform int edges;

varying vec2 texture;

// TODO: optimize
float get_intensity_at(vec2 pos) {
    float radius2 = dot(pos, pos);
    float radius = sqrt(radius2);

    float radius_dist = abs(radius - OUTLINE_RADIUS);
    return 1.0 - clamp(radius_dist / OUTLINE_FADE, 0.0, 1.0);
}

void main() {
    float intensity = get_intensity_at(texture);
    if (intensity < 0.1) {
        discard;
    }

    gl_FragColor = vec4(color, intensity);
}
