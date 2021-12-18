#version 150

uniform sampler2D Sampler;

in vec2 uv;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler, uv);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = vec4(color.rgb / color.a, color.a);
}
