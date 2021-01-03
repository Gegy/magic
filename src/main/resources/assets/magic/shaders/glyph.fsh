#version 130

const int TEXTURE_RADIUS = 16;
const float PIXEL_SIZE = 1.0 / float(TEXTURE_RADIUS);

const float LINE_RADIUS = PIXEL_SIZE * 1.5;
const float NODE_RADIUS = PIXEL_SIZE / 2.0;

uniform float form_progress;
uniform vec3 color;
uniform int flags;

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
const int HIGHLIGHT_NODES_BIT = 1 << 16;

float line_segment(vec2 p, vec2 a, vec2 b) {
    vec2 ba = b - a;
    vec2 pa = p - a;
    float m = ba.y / ba.x;
    if (m >= -1.0 && m <= 1.0) {
        // distance along the line as per x
        float h = pa.x / ba.x;
        float ty = a.y + ba.y * clamp(h, 0.0, 1.0);
        float ey = abs(p.y - ty);
        if (h < 0.0) ey += abs(a.x - p.x);
        else if (h > 1.0) ey += abs(p.x - b.x);
        return ey;
    } else {
        // distance along the line as per y
        float h = pa.y / ba.y;
        float tx = a.x + ba.x * clamp(h, 0.0, 1.0);
        float ex = abs(p.x - tx);
        if (h < 0.0) ex += abs(a.y - p.y);
        else if (h > 1.0) ex += abs(p.y - b.y);
        return ex;
    }
}

float circle_outline(vec2 p, float r) {
    float r2 = r * r;
    float m = p.y / p.x;
    if (m >= -1.0 && m <= 1.0) {
        float y2 = p.y * p.y;
        float x2 = abs(r2 - y2);
        return abs(sqrt(x2) - abs(p.x));
    } else {
        float x2 = p.x * p.x;
        float y2 = abs(r2 - x2);
        return abs(sqrt(y2) - abs(p.y));
    }
}

// TODO: optimize
int get_lines_at(vec2 pos) {
    float line = circle_outline(pos, 1.0);

    for (int edge_idx = 0; edge_idx < EDGE_COUNT; edge_idx++) {
        Edge edge = EDGES[edge_idx];
        if ((flags & edge.bit) != 0) {
            line = min(distance, line_segment(pos, edge.from, edge.to));
        }
    }

    if ((flags & STROKE_BIT) != 0) {
        line = min(distance, line_segment(pos, stroke.xy, stroke.zw));
    }

    return int(round(line / PIXEL_SIZE));
}

bool should_highlight_node(vec2 pos) {
    if ((flags & HIGHLIGHT_NODES_BIT) == 0) {
        return false;
    }

    for (int i = 0; i < NODE_COUNT; i++) {
        vec2 node = NODES[i];
        // TODO: not good
        if (floor(pos.x / PIXEL_SIZE) == floor(node.x / PIXEL_SIZE) && floor(pos.y / PIXEL_SIZE) == floor(node.y / PIXEL_SIZE)) {
            return true;
        }
    }

    return false;
}

// TODO: render to framebuffer first
vec2 remap_texture(vec2 texture) {
    return vec2(
        abs(floor(texture.x / PIXEL_SIZE)) * PIXEL_SIZE,
        (floor(texture.y / PIXEL_SIZE)) * PIXEL_SIZE
    );
}

void main() {
    vec2 pos = remap_texture(texture);

    if (should_highlight_node(pos)) {
        gl_FragColor = vec4(vec3(1.0), form_progress);
        return;
    }

    vec3 outline_color = color * 0.5;

    int lines = get_lines_at(pos);

    vec3 result = vec3(0.0);
    if (lines == 0) {
        result = color;
    } else if (lines == 1) {
        result = outline_color;
    } else {
        discard;
    }

    gl_FragColor = vec4(result, form_progress);
}
