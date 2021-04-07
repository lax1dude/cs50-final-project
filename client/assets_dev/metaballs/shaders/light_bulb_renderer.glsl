
#ifdef VERT

layout(location = 0) in vec2 vertIn;
layout(location = 1) in vec3 posIn;
layout(location = 2) in vec4 colorIn;
layout(location = 3) in vec2 emissionAndPointSizeIn;

out vec2 v_pos;
out vec3 v_color;
out float v_emission;
out float v_pointsize;
out float v_sprite;

uniform vec2 aspectRatio;

void main() {
	v_pos = vertIn;
	v_color = colorIn.rgb;
	v_emission = emissionAndPointSizeIn.x;
	v_pointsize = emissionAndPointSizeIn.y;
	v_sprite = colorIn.a;
    gl_Position = vec4(vec3(vertIn * 10.0 * aspectRatio * v_pointsize, 0.0) + posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_pos;
in vec3 v_color;
in float v_emission;
in float v_pointsize;
in float v_sprite;

uniform sampler2D lightBulbTexture;

layout(location = 0) out vec3 diffuse;

void main() {
    diffuse = pow(texture(lightBulbTexture, (v_pos * 0.5 + 0.5) * vec2(0.25, 1.0) + vec2(v_sprite, 0.0)).rgb, vec3(4.0)) * v_emission * v_color;
}

#endif