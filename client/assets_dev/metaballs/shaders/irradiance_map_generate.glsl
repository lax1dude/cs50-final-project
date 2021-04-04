
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
vec3 dirFromLongAndLat(vec2 longAndLat) {
	vec2 angles = vec2(longAndLat.x * PI, (longAndLat.y + 1.0) * PI * 0.25);
	return vec3(sin(angles.x) * sin(angles.y), cos(angles.y), cos(angles.x) * sin(angles.y));
}

vec3 sampl(vec3 dir) {
	vec3 irradiance = vec3(0.0);   

	vec3 up    = vec3(0.0, 1.0, 0.0);
	vec3 right = cross(up, dir);
	up         = cross(dir, right);

	float sampleDelta = 0.025;
	float nrSamples = 0.0;
	for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta)
	{
		for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta)
		{
			vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));
			vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * dir; 

			irradiance += texture(cubeMap, sampleVec).rgb * cos(theta) * sin(theta);
			++nrSamples;
		}
	}
    return PI * irradiance * (1.0 / nrSamples);
}

void main() {
	if(texInV.x > 0.5) {
		vec2 texCoord = ((texInV.xy - vec2(0.5, 1.0)) * vec2(2.0, 1.0)) * 2.0 - 1.0;
		vec3 rayDirection = dirFromLongAndLat(texCoord) * vec3(1.0, -1.0, -1.0);
		fragOut = sampl(rayDirection);
	}else {
		vec2 texCoord = ((texInV.xy) * vec2(2.0, 1.0)) * 2.0 - 1.0;
		vec3 rayDirection = dirFromLongAndLat(texCoord) * vec3(-1.0, 1.0, 1.0);
		fragOut = sampl(rayDirection);
	}
}

#endif
