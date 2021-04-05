#version 150

uniform sampler2D sampler;

in vec2 uv;

out vec4 fragColor;

void main() {
    vec4 color = texture(sampler, uv);
    if (color.a < 0.5) {
        discard;
    }
    fragColor = color;
}
