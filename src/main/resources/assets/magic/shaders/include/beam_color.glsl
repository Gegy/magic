float quantize(float x, float s) {
    return round(x / s) * s;
}

// from: <https://gist.github.com/ForeverZer0/f4f3ce84fe8a58d3ab8d16feb73b3509>
vec3 hue_shift(vec3 color, float hue) {
    const vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cos_angle = cos(hue);
    return vec3(color * cos_angle + cross(k, color) * sin(hue) + k * dot(k, color) * (1.0 - cos_angle));
}

vec4 beam_color(float temperature) {
    temperature = quantize(temperature, 1.0 / 8.0);
    if (temperature <= 0.0) {
        discard;
    }

    float factor = clamp(temperature, 0.0, 1.0);

    vec3 color = mix(Color, vec3(1.2), factor);
    color = hue_shift(color, factor * 0.4);

    return vec4(color, 1.0);
}
