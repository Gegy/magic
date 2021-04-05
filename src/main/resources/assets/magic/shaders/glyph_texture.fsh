#version 150

uniform float texel_size;
uniform float render_size;

uniform float form_progress;
uniform vec3 primary_color;
uniform vec3 secondary_color;
uniform int flags;

uniform vec4 stroke;

in vec2 texel;

out vec4 fragColor;

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

vec2 glyph_to_texel(vec2 glyph) {
    return glyph * 0.5 * render_size;
}

// TODO: optimize! a lot of branching here
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
int get_lines_at(vec2 texel) {
    float circle_radius = render_size / 2.0;
    float line = circle_outline(texel, circle_radius);

    for (int edge_idx = 0; edge_idx < EDGE_COUNT; edge_idx++) {
        Edge edge = EDGES[edge_idx];
        if ((flags & edge.bit) != 0) {
            line = min(line, line_segment(texel, glyph_to_texel(edge.from), glyph_to_texel(edge.to)));
        }
    }

    if ((flags & STROKE_BIT) != 0) {
        line = min(line, line_segment(texel, glyph_to_texel(stroke.xy), glyph_to_texel(stroke.zw)));
    }

    return int(round(line));
}

bool should_highlight_node(vec2 texel) {
    if ((flags & HIGHLIGHT_NODES_BIT) == 0) {
        return false;
    }

    for (int i = 0; i < NODE_COUNT; i++) {
        vec2 node = floor(glyph_to_texel(NODES[i]));
        if (texel.x == node.x && texel.y == node.y) {
            return true;
        }
    }

    return false;
}

void main() {
    vec2 mirrored_texel = floor(texel);
    mirrored_texel.x = abs(mirrored_texel.x);

    if (should_highlight_node(mirrored_texel)) {
        fragColor = vec4(vec3(1.0), form_progress);
        return;
    }

    int lines = get_lines_at(mirrored_texel);

    vec3 result = vec3(0.0);
    if (lines == 0) {
        result = secondary_color;
    } else if (lines == 1) {
        result = primary_color;
    } else {
        discard;
    }

    fragColor = vec4(result, form_progress);
}
