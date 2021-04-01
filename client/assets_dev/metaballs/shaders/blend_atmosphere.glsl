
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main() {
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;

layout(location = 0) out vec4 fragOut;

uniform sampler2D positionTex;
uniform sampler2D lightShaftTex;

uniform vec2 invTextureSize;
uniform vec3 fogColor;
uniform vec3 shaftColor;
uniform float fogDensity;

uniform int enableLightShafts;

void main() {
	float dist0 = length(texture(positionTex, texCoord).xyz);
	float dist = max(dist0 - 30.0, 0.0);
	float shaft = 0.0;
	if(enableLightShafts == 1) shaft = pow(max(30.0 - dist0, 0.0) / 10.0, 2.0) * texture(lightShaftTex, texCoord).r;
	float fog = (1.0 - clamp(exp(-fogDensity * dist), 0.0, 1.0));
	fragOut = vec4(mix(fogColor, shaftColor, shaft), max(fog, shaft) * clamp(dist0 * 0.05, 0.0, 1.0));
}

#endif
