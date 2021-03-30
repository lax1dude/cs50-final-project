
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
uniform float fogDensity;

uniform int enableLightShafts;

void main() {
	if(enableLightShafts == 1) {
		float b = 0.0;
		for(float i = -1.0; i <= 1.0; ++i) {
			for(float j = -1.0; j <= 1.0; ++j) {
				b += texture(lightShaftTex, texCoord + (vec2(j, i) * invTextureSize)).r;
			}
		}
		b = b / 9.0;
		
		float dist = max(length(texture(positionTex, texCoord).xyz), 0.0);
		float shaft = b;//texture(lightShaftTex, texCoord).r;
		
		float fog = (1.0 - clamp(exp(-fogDensity * dist), 0.5, 1.0));
		
		fragOut = vec4(fogColor * vec3(pow(shaft, 2.0)), fog * pow(shaft, 3.0));
	}else {
		float dist = max(length(texture(positionTex, texCoord).xyz) - 30.0, 0.0);
		float fog = (1.0 - clamp(exp(-fogDensity * dist), 0.5, 1.0));
		fragOut = vec4(fogColor, fog);
	}
}

#endif
