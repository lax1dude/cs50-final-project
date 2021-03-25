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

in vec2 v_texCoord;

layout(location = 0) out float fragOut;

uniform sampler2D position;

uniform sampler2D shadowMapA;
uniform sampler2D shadowMapB;
uniform sampler2D shadowMapC;
uniform sampler2D shadowMapD;

uniform mat4 shadowMatrixA;
uniform mat4 shadowMatrixB;
uniform mat4 shadowMatrixC;
uniform mat4 shadowMatrixD;

uniform float randTimer;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233)) + randTimer) * 43758.5453);
}

bool isInTexture(vec3 pos){
	return pos.x >= 0.0 && pos.x <= 1.0 && pos.y >= 0.0 && pos.y <= 1.0 && pos.z >= 0.0 && pos.z <= 1.0;
}

void main() {
	vec4 pos = vec4(texture(position, v_texCoord).rgb, 1.0);
	
	vec4 shadowPos = shadowMatrixA * pos;
	shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
	if(!isInTexture(shadowPos.xyz)) {
		shadowPos = shadowMatrixB * pos;
		shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
		if(!isInTexture(shadowPos.xyz)) {
			shadowPos = shadowMatrixC * pos;
			shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
			if(!isInTexture(shadowPos.xyz)) {
				shadowPos = shadowMatrixD * pos;
				shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
				if(!isInTexture(shadowPos.xyz)) {
					fragOut = 1.0;
				}else {
					fragOut = (texture(shadowMapD, shadowPos.xy).r <= shadowPos.z) ? 1.0 : 0.0;
				}	
			}else {
				fragOut = (texture(shadowMapC, shadowPos.xy).r <= shadowPos.z) ? 1.0 : 0.0;
			}
		}else {
			fragOut = (texture(shadowMapB, shadowPos.xy).r <= shadowPos.z) ? 1.0 : 0.0;
		}
	} else {
		fragOut = (texture(shadowMapA, shadowPos.xy).r <= shadowPos.z) ? 1.0 : 0.0;
	}
}

#endif
