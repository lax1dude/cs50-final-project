
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

vec3 dirFromLongAndLat(vec2 texIn) {
    if(texIn.x < 0.5) {
        vec2 latLong = vec2(texIn.x * 4.0 - 1.0, texIn.y * 2.0 - 1.0);
        return normalize(vec3(latLong.x, -(1.0 - length(latLong)), -latLong.y));
    }else {
        vec2 latLong = vec2((texIn.x - 0.5) * 4.0 - 1.0, texIn.y * 2.0 - 1.0);
        return normalize(vec3(latLong.x, (1.0 - length(latLong)), -latLong.y));
    }
}

float PI = 3.14159265;
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
	vec2 texCoord = texInV.xy;
	vec3 rayDirection = dirFromLongAndLat(texCoord);
	fragOut = sampl(rayDirection); //texture(cubeMap, rayDirection).rgb;
}

#endif
