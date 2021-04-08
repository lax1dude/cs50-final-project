
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;

out vec3 colorv;
out vec3 normalv;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m_invtrans;

uniform lowp vec3 sunDirection;

uniform float altitude;

//include dependencies/glsl-atmosphere.glsl

void main() {
	normalv = normalize((normIn.xyz * mat3(matrix_m_invtrans)).xyz);
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
	colorv = atmosphere(
        -normalv,                       // normalized ray direction
        vec3(0,6373e3,0),               // ray origin
        sunDirection,                   // position of the sun
        100.0,                          // intensity of the sun
        6371e3,                         // radius of the planet in meters
        6471e3,                         // radius of the atmosphere in meters
        vec3(5.5e-6, 13.0e-6, 22.4e-6), // Rayleigh scattering coefficient
        21e-6,                          // Mie scattering coefficient
        8e3,                            // Rayleigh scale height
        1.2e3,                          // Mie scale height
        0.758                           // Mie preferred scattering direction
    );
}

#endif

#ifdef FRAG

in vec3 colorv;
in vec3 normalv;

layout(location = 0) out vec3 fragOut;
	
uniform vec3 sunColor;
uniform vec3 cloudColor;
uniform lowp vec3 sunDirection;
uniform float sunSize;

uniform sampler2D cloudTextureA;
uniform sampler2D cloudTextureB;
uniform sampler2D starsTexture;

uniform float cloudTextureBlend;

float invPI = 0.318309886;
vec2 clipSpaceFromDir180(vec3 dir) {
	return vec2(
		atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 4.0 - 1.0
	);
}
vec2 clipSpaceFromDir360(vec3 dir) {
    return vec2(
        atan(-dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 2.0 - 1.0
    );
}

void main() {
	vec3 normal = normalize(normalv);
	
	float sunBrightness = max(dot(normal, -sunDirection), 0.0);
	vec2 cloudMapPos = clipSpaceFromDir180(-normal) * 0.5 + 0.5;
	
	float riseFac = clamp((sunDirection.y + 0.1), 0.1, 1.0);
	riseFac *= riseFac;
	
	float cloudMapSample = mix(texture(cloudTextureA, cloudMapPos).r, texture(cloudTextureB, cloudMapPos).r, cloudTextureBlend) * max(-normal.y, 0.0) * 0.001 * riseFac;
	
	float darkness = pow(cloudMapSample * 100.0, 4.0) * 30.0;
	darkness /= darkness + 2.0;
	darkness = clamp(darkness, 0.0, 1.0);
	
	vec3 vecc1 = -sunDirection;
	vec3 vecc2 = vec3(0.0, 0.0, -1.0);
	vec3 tangent = normalize(vecc1 - vecc2 * dot(vecc1, vecc2));
	vec3 bitangent = cross(vecc2, tangent);
	mat3 TBN = mat3(tangent, bitangent, vecc2);
	
	riseFac = clamp(-sunDirection.y * 5.0, 0.0, 1.0);
	riseFac *= riseFac;
	
    fragOut = mix(
		colorv +
		sunColor * (pow(sunBrightness, 300.0f / sunSize) * pow(max(1.0 - cloudMapSample * 100.0 - darkness * 10.0, 0.0), 2.0)) +
		cloudColor * (cloudMapSample * clamp(pow(sunBrightness, 2.0f / sunSize) * 2.0, 1.0, 100.0)) +
		pow(texture(starsTexture, clipSpaceFromDir360(TBN * -normal) * 0.5 + 0.5).rgb, vec3(4.0)) * 0.5 * riseFac,
		vec3(0.0),
		darkness * 0.9
	);
	
}

#endif
