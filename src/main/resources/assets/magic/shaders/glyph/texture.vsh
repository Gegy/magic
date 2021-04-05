#version 150

uniform float texel_size;

in vec2 Position;
out vec2 texel;

void main() {
    vec2 uv = Position * 0.5;
    texel = uv / texel_size;

    gl_Position = vec4(Position, 0.2, 1.0);
}
