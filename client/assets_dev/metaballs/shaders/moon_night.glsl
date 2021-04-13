
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;
out vec3 pos;

uniform mat4 matrix_m;
uniform mat4 matrix_mvp;

void main()
{
	texCoord = texIn;
	pos = (matrix_m * vec4(posIn, 1.0)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;
in vec3 pos;

layout(location = 0) out vec4 fragOut;

uniform vec3 moonColor;
uniform vec2 moonTexXY;

uniform vec3 cloudColor;

uniform sampler2D cloudTextureA;
uniform sampler2D cloudTextureB;
uniform float cloudTextureBlend;

#define MOONTEX_WH 1024.0

uniform sampler2D tex;

float invPI = 0.318309886;
vec2 clipSpaceFromDir180(vec3 dir) {
	return vec2(
		atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 4.0 - 1.0
	);
}

void main()
{
	vec3 normal = normalize(-pos);
	vec2 cloudMapPos = clipSpaceFromDir180(-normal) * 0.5 + 0.5;
	
	float cloudMapSample = mix(texture(cloudTextureA, cloudMapPos).r, texture(cloudTextureB, cloudMapPos).r, cloudTextureBlend) * max(-normal.y, 0.0) * 0.00001;
	
	float darkness = pow(cloudMapSample * 100.0, 4.0) * 30.0;
	darkness /= darkness + 2.0;
	darkness = clamp(darkness, 0.0, 1.0);
	
    vec3 color = mix(
		pow(texture(tex, moonTexXY + texCoord * (155.0 / MOONTEX_WH)).rgb * moonColor, vec3(2.2)) +
		cloudColor * cloudMapSample,
		vec3(0.0),
		darkness * 0.9
	);
	
    fragOut = vec4(color, texture(tex, texCoord * (155.0 / MOONTEX_WH)).r);
}

#endif
