#version 150

uniform vec3 Color;
uniform float Time;

in vec2 uv;

out vec4 fragColor;

#moj_import <noise.glsl>
#moj_import <beam_color.glsl>

float temperature_noise(vec2 p) {
    p = p * vec2(0.5, 1.5);
    float t = Time * 21.6;
    float a = noise(p * 1.7 + t * vec2(-1.6, 0.5));
    float b = noise(p * 4.2 + t * vec2(-1.5, -0.3));
    float c = noise(p * 3.1 + t * vec2(-1.8, 0.2));
    return (a + b + c) / 3.0;
}

float sample_temperature(vec2 point) {
    float pinch = 1.0 / min(point.x * point.x * 2.0, 1.0);
    point.y *= pinch;

    float jitter = (noise(Time * 50.0 - point.x) * 2.0 - 1.0) * 0.1;

    float main = 1.0 - abs(point.y + jitter);
    float noise = (temperature_noise(point) * 2.0 - 1.0) * 0.3 * pinch;

    return main + noise;
}

void main() {
    float temperature = sample_temperature(uv);
    fragColor = beam_color(temperature);
}
