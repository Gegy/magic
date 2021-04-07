#version 150

in vec2 Position;

uniform float Distance;
uniform float Scale;

uniform mat4 ModelViewProject;

out vec2 uv;

void main() {
    uv = (Position + 1.0) * 0.5;

    gl_Position = ModelViewProject * vec4(Position * Scale, Distance, 1.0);
}
