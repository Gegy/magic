#version 150

in vec2 Position;

uniform mat4 ModelViewProject;

uniform float Scale;

out vec2 uv;

void main() {
    uv = Position * 0.5 + 0.5;
    gl_Position = ModelViewProject * vec4(Position * Scale, 0.0, 1.0);
}
