#version 150

uniform vec2 Scale;

in vec2 Position;
out vec2 uv;

void main() {
    uv = vec2(Position.x, Position.y - 0.5) * Scale;

    gl_Position = vec4(Position * 2.0 - 1.0, 0.2, 1.0);
}
