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

uniform sampler2D depthTexture;
uniform sampler2D cloudTextureA;
uniform sampler2D cloudTextureB;

uniform vec3 sunDirection;
uniform mat4 matrix_vp;

uniform vec4 sampleNoise[32];
uniform float cloudTextureBlend;

float invPI = 0.318309886;
vec2 clipSpaceFromDir(vec3 dir) {
	return vec2(
		atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 4.0 - 1.0
	);
}

float sampleDirection(vec3 dir) {
	vec4 vpt = matrix_vp * vec4(dir, 1.0);
	vec2 texCoord = (vpt.xy / vpt.w) * 0.5 + 0.5;
	if(texCoord.x > 1.1 || texCoord.x < -0.1 || texCoord.y > 1.1 || texCoord.y < -0.1) return 0.0;
	float depth = (texture(depthTexture, clamp(texCoord, vec2(0.00001), vec2(0.99990))).r == 0.0 ? 1.0 : 0.0);
	if(depth == 1.0) {
		vec2 cloudMapPos = clipSpaceFromDir(dir) * 0.5 + 0.5;
		float cloudMapSample = mix(texture(cloudTextureA, cloudMapPos).r, texture(cloudTextureB, cloudMapPos).r, cloudTextureBlend);
		float darkness = pow(cloudMapSample * 0.2, 4.0);
		darkness /= darkness + 2.0;
		return 1.0 - clamp(darkness, 0.0, 1.0);
	}else {
		return 0.0;
	}
}

void main() {
	float s = 0.0;
	float div = 0.0;
	for(int i = 0; i < 32; ++i) {
		s += sampleDirection(sunDirection + sampleNoise[i].xyz * 0.03) / sampleNoise[i].w;
		++div;
	}
	fragOut = clamp(s / div, 0.0, 1.0);
}

#endif