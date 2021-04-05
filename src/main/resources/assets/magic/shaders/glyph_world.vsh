#version 150

in vec2 Position;

uniform float radius;
uniform float render_scale;

uniform mat4 glyph_to_world;
uniform mat4 world_to_screen;

out vec2 uv;

void main() {
    uv = (Position + 1.0) * 0.5;

    vec4 world_position = glyph_to_world * vec4(Position * render_scale * radius, 1.0, 1.0);
    gl_Position = world_to_screen * world_position;
}
