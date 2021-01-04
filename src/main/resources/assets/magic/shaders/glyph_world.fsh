#version 130

uniform sampler2D sampler;

varying vec2 uv;

void main() {
    vec4 color = texture(sampler, uv);
    if (color.a < 0.5) {
        discard;
    }
    gl_FragColor = color;
}
