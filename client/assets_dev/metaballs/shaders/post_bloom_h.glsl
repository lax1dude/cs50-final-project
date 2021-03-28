#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 v_texCoord;

void main() {
	v_texCoord = texIn * 0.25;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_texCoord;

layout(location = 0) out vec3 fragOut;

uniform sampler2D tex;

uniform vec2 screenSizeInv;

vec3 brightPass(vec3 color) {
/*
	float b = dot(color, vec3(0.2126, 0.7152, 0.0722));
	return b > 5.0 ? max(color - 2.0, vec3(0.0)) : vec3(0.0);
*/
	return color;
}

void main() {
	float offset = 0.001;
	float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
	vec3 result = texture(tex, v_texCoord).rgb * weight[0];
	float j = 1.0;
	for(int i = 1; i < 5; ++i) {
		result += brightPass(texture(tex, clamp(v_texCoord + vec2(screenSizeInv.x * j, 0.0), vec2(0.0), vec2(1.0))).rgb) * weight[i];
		result += brightPass(texture(tex, clamp(v_texCoord - vec2(screenSizeInv.x * j, 0.0), vec2(0.0), vec2(1.0))).rgb) * weight[i];
		j += 1.0;
	}
	fragOut = result;
}

#endif