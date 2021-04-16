
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texInV;

void main() {
    texInV = texIn;
	gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texInV;

layout(location = 0) out vec3 fragOut;

uniform sampler2D tex;
uniform vec2 screenSizeInv;

vec2 sampleColorWrap(vec2 pos) {
	if(texInV.x < 0.5) {
		vec2 pos2 = pos * vec2(2.0, 1.0);
		pos2.x = mod(pos2.x, 2.0);
		if(pos2.y > 1.0) {
			pos2.y = fract(pos2.y);
			pos2.x += 0.5;
		}else if(pos2.y < 0.0) {
			pos2.y = fract(pos2.y);
			pos2.x = 1.0 - pos2.x;
		}
		return pos2 * vec2(0.5, 1.0);
	}else {
		vec2 pos2 = pos * vec2(2.0, 1.0) - vec2(1.0, 0.0);
		pos2.x = mod(pos2.x, 2.0);
		if(pos2.y > 1.0) {
			pos2.y = fract(pos2.y);
			pos2.x = -pos2.x;
		}else if(pos2.y < 0.0) {
			pos2.y = fract(pos2.y);
			pos2.x -= 0.5;
		}
		return pos2 * vec2(0.5, 1.0) + vec2(0.5, 0.0);
	}
}

void main() {
	float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
	vec3 result = texture(tex, sampleColorWrap(texInV)).rgb * weight[0];
	float j = 1.0;
	for(int i = 1; i < 5; ++i) {
		result += texture(tex, sampleColorWrap(texInV + vec2(screenSizeInv.x * j, screenSizeInv.y * j))).rgb * weight[i];
		result += texture(tex, sampleColorWrap(texInV - vec2(screenSizeInv.x * j, screenSizeInv.y * j))).rgb * weight[i];
		++j;
	}
	fragOut = result;
}

#endif
