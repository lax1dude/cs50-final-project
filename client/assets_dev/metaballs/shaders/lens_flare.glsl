
#ifdef VERT

layout(location = 0) in vec2 posIn;
layout(location = 1) in vec2 texIn;
layout(location = 2) in vec4 colorIn;

out vec2 v_tex;
out vec3 v_color;

uniform vec2 aspectRatio;

void main() {
	v_tex = texIn;
	v_color = colorIn.rgb;
    gl_Position = vec4(posIn, 0.0, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_tex;
in vec3 v_color;

uniform sampler2D flareTexture;
uniform float intensity;

uniform vec2 sunTexCoord;
uniform sampler2D depthTexture;

layout(location = 0) out vec3 colorOut;

void main() {
	if(texture(depthTexture, sunTexCoord).r == 0.0) {
		colorOut = pow(texture(flareTexture, v_tex).rgb, vec3(2.2)) * intensity;
	}else {
		colorOut = vec3(0.0);
	}
}

#endif