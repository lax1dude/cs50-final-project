
#ifdef VERT

layout(location = 0) in vec2 posIn;
layout(location = 1) in vec2 texIn;
layout(location = 2) in vec4 colorIn;

out vec2 v_tex;
out vec3 v_color;

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

uniform sampler2D sunOcclusionTexture;

layout(location = 0) out vec3 colorOut;

void main() {
	float occlud = texture(sunOcclusionTexture, vec2(0.0)).r;
	if(occlud > 0.0) {
		colorOut = max(pow(texture(flareTexture, v_tex).rgb, vec3(2.2)) * intensity * v_color * occlud, vec3(0.0));
	}else {
		colorOut = vec3(0.0);
	}
}

#endif