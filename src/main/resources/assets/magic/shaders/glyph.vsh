#version 130

attribute vec2 Position;

uniform vec2 center;
uniform float radius;

uniform mat4 glyph_to_world;
uniform mat4 world_to_screen;

varying vec2 texture;

void main() {
    texture = Position;

    vec4 world_position = glyph_to_world * vec4((Position * radius) + center, 1.0, 1.0);
    gl_Position = world_to_screen * world_position;
}
