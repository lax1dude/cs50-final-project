
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

uniform samplerCube cubeMap;

float PI = 3.14159265;
vec3 dirFromLongAndLat(vec2 texIn) {
/*
    vec2 angles = vec2(longAndLat.x * PI, (longAndLat.y + 1.0) * PI * 0.5);
    return vec3(sin(angles.x) * sin(angles.y), cos(angles.y), cos(angles.x) * sin(angles.y));
*/
	if(texIn.x < 0.5) {
		vec2 latLong = vec2(texIn.x * 4.0 - 1.0, texIn.y * 2.0 - 1.0);
		return normalize(vec3(latLong.x, -(1.0 - length(latLong)), -latLong.y));
	}else {
		vec2 latLong = vec2((texIn.x - 0.5) * 4.0 - 1.0, texIn.y * 2.0 - 1.0);
		return normalize(vec3(latLong.x, (1.0 - length(latLong)), -latLong.y));
	}
}

void main() {
	fragOut = texture(cubeMap, dirFromLongAndLat(texInV)).rgb;
}

#endif
