
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
        vec3(0,6373e3 + altitude * 30.0,0),// ray origin
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
uniform lowp vec3 sunDirection;
uniform float sunSize;

uniform sampler2D cloudTextureA;
uniform sampler2D cloudTextureB;

uniform float cloudTextureBlend;

void main() {
	vec3 normal = normalize(normalv);
	
	float sunBrightness = max(dot(normal, -sunDirection), 0.0);
	sunBrightness = pow(sunBrightness, 300.0f / sunSize);
	
	vec2 cloudMapPos = vec2(normal.x, normal.z) * 0.5 + 0.5;
	
	float cloudMapSample = mix(texture(cloudTextureA, cloudMapPos).r, texture(cloudTextureB, cloudMapPos).r, cloudTextureBlend) * max(dot(normal, vec3(0.0, -1.0, 0.0)), 0.0) * 0.001;
	
    fragOut = colorv + (sunColor * sunBrightness) + (sunColor * cloudMapSample);
}

#endif
