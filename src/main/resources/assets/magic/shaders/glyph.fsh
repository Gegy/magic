#version 130

const float LINE_RADIUS = 0.1;
const float NODE_RADIUS = 0.05;

uniform float form_progress;
uniform vec3 color;
uniform int edges;

uniform vec4 stroke;

varying vec2 texture;

// sqrt(0.75)
const float SIDE_CENTER_X = 0.866025;

const vec2 TOP = vec2(0.0, 1.0);
const vec2 BOTTOM = vec2(0.0, -1.0);
const vec2 SIDE_UPPER = vec2(SIDE_CENTER_X, 0.5);
const vec2 SIDE_LOWER = vec2(SIDE_CENTER_X, -0.5);
const vec2 CENTER_UPPER = vec2(0.0, 0.5);
const vec2 CENTER = vec2(0.0, 0.0);
const vec2 CENTER_LOWER = vec2(0.0, -0.5);

const int NODE_COUNT = 7;
const int EDGE_COUNT = 15;

const vec2 NODES[NODE_COUNT] = vec2[](TOP, BOTTOM, SIDE_UPPER, SIDE_LOWER, CENTER_UPPER, CENTER, CENTER_LOWER);

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

const int STROKE_BIT = 1 << 15;

// from: https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float get_distance_to_edge(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

float get_outline_intensity_at(vec2 pos) {
    float radius2 = dot(pos, pos);
    float radius = sqrt(radius2);

    float radius_distance = abs(radius - 1.0);
    return 1.0 - clamp(radius_distance / LINE_RADIUS, 0.0, 1.0);
}

float get_edge_intensity_at(vec2 pos) {
    float distance = 999.0;

    for (int edge_idx = 0; edge_idx < EDGE_COUNT; edge_idx++) {
        Edge edge = EDGES[edge_idx];
        if ((edges & edge.bit) != 0) {
            distance = min(distance, get_distance_to_edge(pos, edge.from, edge.to));
        }
    }

    if ((edges & STROKE_BIT) != 0) {
        distance = min(distance, get_distance_to_edge(pos, stroke.xy, stroke.zw));
    }

    return 1.0 - clamp(distance / LINE_RADIUS, 0.0, 1.0);
}

// TODO: optimize
float get_intensity_at(vec2 pos) {
    float outline_intensity = get_outline_intensity_at(pos);
    float edge_intensity = get_edge_intensity_at(pos);

    return max(outline_intensity, edge_intensity);
}

vec4 apply_node_glow(vec2 pos, vec4 color) {
    float distance = 999.0;
    for (int i = 0; i < NODE_COUNT; i++) {
        vec2 node = NODES[i];
        distance = min(distance, length(node - pos));
    }

    float glow = 1.0 - clamp(distance / NODE_RADIUS, 0.0, 1.0);
    return color + vec4(glow);
}

void main() {
    vec2 pos = texture;
    pos.x = abs(pos.x);

    float intensity = get_intensity_at(pos) * form_progress;
    vec4 edges = vec4(color, intensity);

    vec4 edges_with_nodes = apply_node_glow(pos, edges);
    if (edges_with_nodes.a < 0.01) {
        discard;
    }

    gl_FragColor = edges_with_nodes;
}
