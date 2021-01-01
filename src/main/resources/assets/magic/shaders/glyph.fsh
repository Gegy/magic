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

struct Edge {
    vec2 from;
    vec2 to;
    int bit;
};

const Edge EDGES[EDGE_COUNT] = Edge[](
    Edge(CENTER, CENTER_UPPER, 1 << 0),
    Edge(CENTER, CENTER_LOWER, 1 << 1),
    Edge(CENTER, SIDE_UPPER, 1 << 2),
    Edge(CENTER, SIDE_LOWER, 1 << 3),

    Edge(CENTER_UPPER, SIDE_UPPER, 1 << 4),
    Edge(CENTER_UPPER, SIDE_LOWER, 1 << 5),
    Edge(CENTER_LOWER, SIDE_UPPER, 1 << 6),
    Edge(CENTER_LOWER, SIDE_LOWER, 1 << 7),

    Edge(TOP, CENTER_UPPER, 1 << 8),
    Edge(TOP, SIDE_UPPER, 1 << 9),
    Edge(TOP, SIDE_LOWER, 1 << 10),

    Edge(BOTTOM, CENTER_LOWER, 1 << 11),
    Edge(BOTTOM, SIDE_UPPER, 1 << 12),
    Edge(BOTTOM, SIDE_LOWER, 1 << 13),

    Edge(SIDE_LOWER, SIDE_UPPER, 1 << 14)
);

// from: https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float get_distance_to_edge(vec2 p, Edge edge) {
    vec2 pa = p - edge.from;
    vec2 ba = edge.to - edge.from;
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
        Edge edge = EDGES[edge_idx];
        if ((edges & edge.bit) != 0) {
            distance = min(distance, get_distance_to_edge(pos, edge));
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
