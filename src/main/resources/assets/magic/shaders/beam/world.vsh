#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewProject;

uniform vec2 Scale;
uniform float Distance;

out vec2 uv;

void main() {
    uv = UV0;

    vec3 local_position = vec3(
            Position.x * Scale.y,
            Position.y * Scale.y,
            Position.z * Scale.x + Distance
    );
    gl_Position = ModelViewProject * vec4(local_position, 1.0);
}
