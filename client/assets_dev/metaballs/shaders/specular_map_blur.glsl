
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

vec3 dirFromTexCoords(vec2 texCoords) {
	vec2 texCoords2 = texCoords;
	if (texCoords2.x < 0.5) texCoords2 -= vec2(0.5, 0.0);
	texCoords2 /= vec2(0.5, 1.0);
	vec3 dir = vec3(0.0, 0.0, 0.0);
	float b = (2.0 * texCoords2.x + 2.0 * texCoords2.y - 2.0);
	dir.y = (-b + sqrt(b * b + 12.0) * ((texCoords.x > 0.5) ? -1.0 : 1.0)) / 2.0;
	dir.xz = (texCoords2.xy * 2.0 - 1.0) * (abs(dir.y) + 1.0);
	return dir;
}

vec2 sampl(vec3 dir) {
	dir.xz /= abs(dir.y) + 1.0;
	dir.xz = dir.xz * 0.5 + 0.5;
	if(dir.y < 0.0) {
		return dir.xz * vec2(0.5, 1.0);
	}else {
		return dir.xz * vec2(0.5, 1.0) + vec2(0.5, 0.0);
	}
}

void main() {

	if(texInV.x < 0.5) {
		vec2 latLong = vec2(texInV.x * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		if(dot(latLong, latLong) > 1.07) discard;
	}else {
		vec2 latLong = vec2((texInV.x - 0.5) * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		if(dot(latLong, latLong) > 1.07) discard;
	}
	
	float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
	vec3 result = texture(tex, texInV).rgb * weight[0];
	
	vec3 dir = dirFromTexCoords(texInV);
	
	float w = 7.0;
	
	float j = 1.0;
	for(int i = 1; i < 5; ++i) {
		result += texture(tex, sampl(dir + vec3(j * screenSizeInv.x, 0.0, j * screenSizeInv.y) * w)).rgb * weight[i];
		result += texture(tex, sampl(dir + vec3(j * -screenSizeInv.x, 0.0, j * -screenSizeInv.y) * w)).rgb * weight[i];
		++j;
	}
	
	fragOut = result;
	
}

/*
vec2 sampl(vec2 offset) {
	vec3 dir = dirFromTexCoords(texInV + offset);
	dir.xz /= abs(dir.y) + 1.0;
	dir.xz = dir.xz * 0.5 + 0.5;
	if(dir.y < 0.0) {
		return dir.xz * vec2(0.5, 1.0);
	}else {
		return dir.xz * vec2(0.5, 1.0) + vec2(0.5, 0.0);
	}
}

void main() {

	if(texInV.x < 0.5) {
		vec2 latLong = vec2(texInV.x * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		if(dot(latLong, latLong) > 1.05) discard;
	}else {
		vec2 latLong = vec2((texInV.x - 0.5) * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		if(dot(latLong, latLong) > 1.05) discard;
	}
	
	float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
	vec3 result = texture(tex, sampl(vec2(0.0))).rgb * weight[0];
	float j = 1.0;
	for(int i = 1; i < 5; ++i) {
		result += texture(tex, sampl(vec2(screenSizeInv.x * j, screenSizeInv.y * j))).rgb * weight[i];
		result += texture(tex, sampl(vec2(-screenSizeInv.x * j, -screenSizeInv.y * j))).rgb * weight[i];
		++j;
	}
	
	fragOut = result;
	
}
*/

#endif
