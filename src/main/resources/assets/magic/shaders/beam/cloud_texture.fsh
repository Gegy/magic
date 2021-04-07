#version 150

uniform vec3 Color;
uniform float Time;

in vec2 uv;

out vec4 fragColor;

#moj_import <noise.glsl>
#moj_import <beam_color.glsl>

float temperature_noise(vec3 p) {
    p *= vec3(0.5, 1.5, 1.5);
    float t = Time * 6.5;
    float a = noise(p * 0.4 + t * vec3(1.2, 0.2, -0.1));
    float b = noise(p * 2.3 + t * vec3(1.8, -0.1, 0.3));
    float c = noise(p * 1.6 + t * vec3(1.3, -0.3, 0.1));
    return (a + b + c) / 3.0;
}

vec2 polar(vec2 p) {
    return vec2(length(p), atan(p.y, p.x));
}

float sample_temperature(vec2 point) {
    point *= 2.0;

    vec2 polar = polar(point);
    float distance = polar.x;
    vec2 direction = point / distance;

    float noise = temperature_noise(vec3(polar.x, direction.xy));
    noise = pow(noise, 3.0) * 1.6 - 0.1;

    return (1.0 - distance) + noise;
}

void main() {
    float temperature = sample_temperature(uv);
    fragColor = beam_color(temperature);
}
