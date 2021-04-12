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

uniform sampler2D normal;
uniform sampler2D position;

uniform sampler2D shadowMap;

uniform highp mat4 shadowMatrixA;
uniform highp mat4 shadowMatrixB;
uniform highp mat4 shadowMatrixC;
uniform highp mat4 shadowMatrixD;

uniform mat4 matrix_v_inv;
uniform mat4 matrix_p_inv;

uniform float randTimer;

uniform int softShadow;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233)) + randTimer) * 43758.5453);
}

bool isInTexture(vec3 pos) {
	return pos.x >= 0.0 && pos.x <= 1.0 && pos.y >= 0.0 && pos.y <= 1.0 && pos.z >= 0.0 && pos.z <= 1.0;
}

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

float calcSmoothShadow(mat4 pmatrix, float pmap, vec3 pnormal, vec3 ppos, float amount) {
	float sampleWeight = 1.0 / 16.0;
	float accum = sampleWeight;
	
	vec4 rotate90 = rotationMatrix(vec3(1.0,0.0,0.0), 90.0 * 0.017453293) * vec4(pnormal, 1.0);
	
	for(float i = 0.0; i < 15.0; ++i) {
		vec4 rot = rotationMatrix(pnormal, i * (360.0 / 7.0) * 0.017453293) * rotate90;
		vec4 sampleLoc = pmatrix * vec4(ppos + rot.xyz * (i < 7.0 ? 0.07 : 0.14) * amount, 1.0);
		
		sampleLoc.xyz *= 0.5; sampleLoc.xyz += 0.5;
		if(isInTexture(sampleLoc.xyz)) accum += (texture(shadowMap, sampleLoc.xy * vec2(0.25, 1.0) + vec2(0.25 * pmap, 1.0)).r <= sampleLoc.z) ? sampleWeight : 0.0;
		else accum += sampleWeight;
	}
	
	return max(accum * 2.0 - 1.0, 0.0);
}

void main() {
	vec4 pos = vec4(texture(position, v_texCoord).rgb, 1.0);
	vec3 normalC = normalize(texture(normal, v_texCoord).xyz * 2.0 - 1.0);
	
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
					if(softShadow == 1) {
						fragOut = calcSmoothShadow(shadowMatrixD, 3.0, normalC, pos.xyz, 15.0);
					}else {
						fragOut = (texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.75, 0.0)).r <= shadowPos.z) ? 1.0 : 0.0;
					}
				}	
			}else {
				if(softShadow == 1) {
					fragOut = calcSmoothShadow(shadowMatrixC, 2.0, normalC, pos.xyz, 5.0);
				}else {
					fragOut = (texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.50, 0.0)).r <= shadowPos.z) ? 1.0 : 0.0;
				}
			}
		}else {
			if(softShadow == 1) {
				fragOut = calcSmoothShadow(shadowMatrixB, 1.0, normalC, pos.xyz, 1.0);
			}else {
				fragOut = (texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.25, 0.0)).r <= shadowPos.z) ? 1.0 : 0.0;
			}
		}
	} else {
		if(softShadow == 1) {
			fragOut = calcSmoothShadow(shadowMatrixA, 0.0, normalC, pos.xyz, 1.0);
		}else {
			fragOut = (texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.0, 0.0)).r <= shadowPos.z) ? 1.0 : 0.0;
		}
	}
}

#endif
