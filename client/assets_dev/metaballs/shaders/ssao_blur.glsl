
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

layout(location = 0) out float fragOut;

uniform sampler2D ssaoBuffer;
uniform sampler2D linearDepth;

uniform vec2 blurDirection;

#define depthLimit 0.001

void main() {
	
	float divisor = 1.0;
	float accum = 0.0;
	
	float fragDepth = texture(linearDepth, texCoord).r;
	
	accum += texture(ssaoBuffer, texCoord).r;
	
	vec2 coord = clamp(texCoord.xy + blurDirection * -4.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 4.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * -3.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 3.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * -2.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 2.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * -1.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 1.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * 1.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 1.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * 2.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 2.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * 3.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 3.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	coord = clamp(texCoord.xy + blurDirection * 4.0, 0.00001, 0.99999);
	if(abs(texture(linearDepth, coord).r - fragDepth) < depthLimit * 4.0) {
		divisor += 1.0;
		accum += texture(ssaoBuffer, coord).r;
	}
	
	fragOut = accum / divisor;
	
}

#endif
