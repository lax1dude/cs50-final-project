
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main()
{
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;

layout(location = 0) out vec3 fragOut;

uniform sampler2D tex;
uniform sampler2D diffuse;

uniform vec2 screenSizeInv;

void main() {
	vec3 color = texture(tex, texCoord).rgb;
	float dither = texture(diffuse, texCoord).a;
	if(dither > 0.0) {
		vec2 uv = texCoord - vec2(screenSizeInv.x, 0.0);
		fragOut = mix(color, texture(tex, uv).rgb, dither);
	}else {
		vec2 uv = texCoord + vec2(screenSizeInv.x, 0.0);
		float dither2 = texture(diffuse, uv).a;
		vec3 color2 = texture(tex, uv).rgb;
		if(dither2 > 0.0) {
			fragOut = mix(color2, color, dither2);
		}else {
			fragOut = color;
		}
	}
}

#endif
