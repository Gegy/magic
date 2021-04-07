#version 150

uniform vec2 Scale;

in vec2 Position;
out vec2 uv;

void main() {
    vec2 position = Position * 2.0 - 1.0;
    uv = position * 0.5 * Scale;
    gl_Position = vec4(position, 0.2, 1.0);
}
