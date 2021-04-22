
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

void main() {
	if(texInV.x < 0.5) {
        vec2 latLong = vec2(texInV.x * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		float dotL = dot(latLong, latLong);
		if(dotL > 1.05) discard;
        fragOut = texture(cubeMap, normalize(vec3(latLong.x, -(1.0 - sqrt(dotL)), -latLong.y))).rgb;
    }else {
        vec2 latLong = vec2((texInV.x - 0.5) * 4.0 - 1.0, texInV.y * 2.0 - 1.0);
		float dotL = dot(latLong, latLong);
		if(dotL > 1.05) discard;
        fragOut = texture(cubeMap, normalize(vec3(latLong.x, (1.0 - sqrt(dotL)), -latLong.y))).rgb;
    }
}

#endif
