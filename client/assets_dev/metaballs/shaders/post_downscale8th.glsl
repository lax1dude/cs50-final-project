#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 v_texCoord;

void main() {
	v_texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_texCoord;

layout(location = 0) out float fragOut;

uniform sampler2D tex;

uniform vec2 textureSize;

void main() {
	vec2 textureFac = vec2(1.0) / textureSize;
	vec3 W = vec3(0.2125, 0.7154, 0.0721);
	float w = floor(textureSize.x / 8.0);
	float h = floor(textureSize.y / 8.0);
	float accum = 0.0;
	vec3 color;
	for(float y = 0.0; y < h; ++y) {
		for(float x = 0.0; x < w; ++x) {
			color = texture(tex, vec2(x * textureFac.x, y * textureFac.y)).rgb;
			accum += dot(color, W);
		}
	}
	fragOut = accum / (w * h);
}

#endif