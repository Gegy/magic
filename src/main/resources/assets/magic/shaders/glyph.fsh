#version 130

const float RADIUS = 0.9F;
const float LINE_RADIUS = 0.1F;

uniform float form_progress;
uniform vec3 color;
uniform int edges;

varying vec2 texture;

// sqrt(0.75)
const float SIDE_CENTER_X = RADIUS * 0.866025;

const vec2 TOP = vec2(0.0, RADIUS);
const vec2 BOTTOM = vec2(0.0, -RADIUS);
const vec2 SIDE_UPPER = vec2(SIDE_CENTER_X, RADIUS * 0.5);
const vec2 SIDE_LOWER = vec2(SIDE_CENTER_X, RADIUS * -0.5);
const vec2 CENTER_UPPER = vec2(0.0, RADIUS * 0.5);
const vec2 CENTER = vec2(0.0, 0.0);
const vec2 CENTER_LOWER = vec2(0.0, RADIUS * -0.5);

const int EDGE_COUNT = 15;
const int EDGE_VERTEX_COUNT = 30;

const vec2 EDGES[EDGE_VERTEX_COUNT] = vec2[](
    CENTER, CENTER_UPPER,
    CENTER, CENTER_LOWER,
    CENTER, SIDE_UPPER,
    CENTER, SIDE_LOWER,

    CENTER_UPPER, SIDE_UPPER,
    CENTER_UPPER, SIDE_LOWER,
    CENTER_LOWER, SIDE_UPPER,
    CENTER_LOWER, SIDE_LOWER,

    TOP, CENTER_UPPER,
    TOP, SIDE_UPPER,
    TOP, SIDE_LOWER,

    BOTTOM, CENTER_LOWER,
    BOTTOM, SIDE_UPPER,
    BOTTOM, SIDE_LOWER,

    SIDE_LOWER, SIDE_UPPER
);

// from: https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float get_distance_to_line(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

float get_outline_intensity_at(vec2 pos) {
    float radius2 = dot(pos, pos);
    float radius = sqrt(radius2);

    float radius_distance = abs(radius - RADIUS);
    return 1.0 - clamp(radius_distance / LINE_RADIUS, 0.0, 1.0);
}

float get_edge_intensity_at(vec2 pos) {
    pos.x = abs(pos.x);

    float distance = 999.0;

    for (int edge_idx = 0; edge_idx < EDGE_COUNT; edge_idx++) {
        if (((edges >> edge_idx) & 1) != 0) {
            int vertex_idx = edge_idx * 2;
            vec2 from = EDGES[vertex_idx];
            vec2 to = EDGES[vertex_idx + 1];
            distance = min(distance, get_distance_to_line(pos, from, to));
        }
    }

    return 1.0 - clamp(distance / LINE_RADIUS, 0.0, 1.0);
}

// TODO: optimize
float get_intensity_at(vec2 pos) {
    float outline_intensity = get_outline_intensity_at(pos);
    float edge_intensity = get_edge_intensity_at(pos);

    return max(outline_intensity, edge_intensity);
}

void main() {
    float intensity = get_intensity_at(texture) * form_progress;
    if (intensity < 0.01) {
        discard;
    }

    gl_FragColor = vec4(color, intensity);
}
