
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec3 posV;

void main() {
    posV = posIn;
	gl_Position = vec4(posV, 1.0);
}

#endif

#ifdef FRAG

//include dependencies/glsl-worley.glsl
#line 18

in vec3 posV;

layout(location = 0) out float fragOut;

uniform float cloudDensity;
uniform vec2 cloudOffset;
uniform float cloudMorph;
uniform vec3 sunPosition;

float noiseFunction(vec3 pos) {
	vec4 bufferA = mainImage_old(pos);
	float perlinWorley = bufferA.x;
	vec3 worley = bufferA.yzw;
	float wfbm = worley.x * 0.625 + worley.y * 0.125 + worley.z * 0.25;
	return remap(remap(perlinWorley, wfbm - 1.0, 1.0, 0.0, 1.0), (1.0 - cloudDensity), 1.0, 0.0, 1.0);
}

float PI = 3.14159265;
vec3 dirFromLongAndLat(vec2 longAndLat) {
	vec2 angles = vec2(longAndLat.x * PI, (longAndLat.y + 1.0) * PI * 0.25);
	return vec3(sin(angles.x) * sin(angles.y), cos(angles.y), cos(angles.x) * sin(angles.y));
}

void main() {
	
	float scale = 0.01;
	
	vec3 rayDirection = dirFromLongAndLat(posV.xy);
	
	float density = 0.0;
	
	vec3 ray = rayDirection * 20.0;
	float v;
	for(int i = 0; i < 40; ++i) {
		v = noiseFunction(ray * 0.01 + vec3(cloudOffset, cloudMorph));
		density += clamp(v, 0.0, 1.0);
		ray += rayDirection;
	}
	
	fragOut = density;
}

#endif
