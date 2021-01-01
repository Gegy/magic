#version 130

const float QUAD_SCALE = 1.2;

attribute vec2 Position;

uniform float radius;

uniform mat4 glyph_to_world;
uniform mat4 world_to_screen;

varying vec2 texture;

void main() {
    texture = Position * QUAD_SCALE;

    vec4 world_position = glyph_to_world * vec4(Position * radius * QUAD_SCALE, 1.0, 1.0);
    gl_Position = world_to_screen * world_position;
}
