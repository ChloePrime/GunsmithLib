#version 150

const float BRIGHTNESS_THRESHOLD = 0.257;

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;

out vec4 fragColor;

float luma(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

bool is_white(float saturation, float brightness) {
    return saturation <= 0.01 && brightness >= 0.5;
}

int get_bg_type(vec4 color) {
    vec3 hsv = rgb2hsv(color.rgb);
    float saturation = hsv.y;
    float brightness = luma(color.rgb);
    if (is_white(saturation, brightness)) {
        return 0;
    }
    return brightness > BRIGHTNESS_THRESHOLD ? 1 : -1;
}

vec4 fix_color(vec4 color) {
    vec3 hsv = rgb2hsv(color.rgb);
    float saturation = hsv.y;
    float brightness = luma(color.rgb);
    bool  this_is_darker;
    if (is_white(saturation, brightness)) {
        vec2 textureSize = textureSize(Sampler0, 0);
        vec2 pixelCoord0 = floor(texCoord0 * textureSize) / textureSize;
        vec2 core_size = 1.0 / textureSize;
        vec4 r = texture(Sampler0, pixelCoord0 + vec2(core_size.x, 0));
        vec4 u = texture(Sampler0, pixelCoord0 + vec2(0, core_size.y));
        vec4 l = texture(Sampler0, pixelCoord0 - vec2(core_size.x, 0));
        vec4 d = texture(Sampler0, pixelCoord0 - vec2(0, core_size.y));
        int bg_type = get_bg_type(r) + get_bg_type(u) + get_bg_type(l) + get_bg_type(d);
        this_is_darker = bg_type >= 0;
    } else {
        this_is_darker = brightness <= BRIGHTNESS_THRESHOLD;
    }
    float value = this_is_darker ? 0.57 : 0.875;
    return vec4(value, value, value, color.a);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.5) {
        discard;
    }
    fragColor = fix_color(color) * ColorModulator;
}
