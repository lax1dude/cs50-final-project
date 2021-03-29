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

#define k11 -0.0
#define k21 -0.05
#define k31 -0.08

#define p1 0.0
#define p2 0.0

in vec2 v_texCoord;

layout(location = 0) out vec3 fragOut;

uniform sampler2D tex;
uniform sampler2D bloom;

uniform float startRandom;
uniform float endRandom;
uniform float randomTransition;

vec3 rand3(vec2 co){
	float sinDot1 = sin(dot(co.xy ,vec2(12.9898,78.233)) + startRandom);
	float sinDot2 = sin(dot(co.xy ,vec2(12.9898,78.233)) + endRandom);
    vec3 start = vec3(fract(sinDot1 * 498.2335650) * 2.0 - 1.0, fract(sinDot1 * 9640.43935658) * 2.0 - 1.0, fract(sinDot1 * 32334.34356435));
    vec3 end = vec3(fract(sinDot2 * 498.2335650) * 2.0 - 1.0, fract(sinDot2 * 9640.43935658) * 2.0 - 1.0, fract(sinDot2 * 32334.34356435));
	return mix(start, end, randomTransition);
}

vec2 lensDistort(vec2 uv3, float fct) {
	vec2 uv2 = vec2(uv3.x, uv3.y);
	
	float k1 = fct * k11;
	float k2 = fct * k21;
	float k3 = fct * k31;
	
	vec2 t = uv2 - 0.5;
	
	float r = sqrt(t.x * t.x + t.y * t.y);
	float r2 = pow(r, 2.0);
	
	float f = (1.0f + r2 * k1) + (k2 * pow(r, 4.0)) + (k3 * pow(r, 6.0));
	uv2 = f * t + 0.5;
	
	float xy = uv2.x * uv2.y;
	//uv2.x += 2.0 * p1 * xy + p2 * (r2 + 2.0 * pow(uv2.x, 2.0));
	//uv2.y += p1 * (r2 + 2.0 * pow(uv2.y, 2.0)) + 2.0 * p2 * xy;
	
	return uv2;
}

void main() {
	vec3 color = vec3(texture(tex, lensDistort(v_texCoord,1.1)).r, texture(tex, lensDistort(v_texCoord,1.0)).gb);
	color += texture(bloom, v_texCoord * 0.25 + 1.0).rgb * 0.15;
	
	vec3 grain = rand3(v_texCoord) * 0.5;
	color *= (0.9 + grain * 0.1);
	color += grain * 0.01;
	
	fragOut = color;
}

#endif