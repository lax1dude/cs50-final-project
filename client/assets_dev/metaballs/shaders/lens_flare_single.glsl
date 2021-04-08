
#ifdef VERT

out vec2 v_tex;

layout(location = 0) in vec2 posIn;

uniform highp vec3 position;
uniform vec2 size;
uniform sampler2D depthTexture;

void main() {
	v_tex = posIn * 0.5 + 0.5;
	if(texture(depthTexture, position.xy * 0.5 + 0.5).r <= (position.z * 0.5 + 0.5) + 0.0005) {
		gl_Position = vec4(posIn * size + position.xy, 0.0, 1.0);
	}else {
		gl_Position = vec4(vec3(-2.0), 1.0);
	}
}

#endif

#ifdef FRAG

in vec2 v_tex;

uniform sampler2D flareTexture;
uniform vec3 color;
uniform float flareTextureSelection;

layout(location = 0) out vec3 colorOut;

void main() {
	vec2 uv = v_tex.yx;
	uv *= vec2(1.0, 48.0 / 256.0);
	uv += vec2(0.0, (512.0 - 48.0 - (48.0 * flareTextureSelection)) / 256.0);
	colorOut = pow(texture(flareTexture, uv).rgb, vec3(2.2)) * color;
}

#endif