
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

uniform sampler2D positionTex;

uniform sampler2D shadowMap;

uniform mat4 shadowMatrixB;

uniform mat4 matrix_v;
uniform mat4 matrix_p;

uniform float fov;
uniform float aspect;

uniform mat4 matrix_v_inv;
uniform mat4 matrix_p_inv;

vec3 getPosition(sampler2D dt, vec2 coord) {
	float depth = texture(dt, coord).r;
	vec4 tran = matrix_p_inv * vec4(coord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	return (matrix_v_inv * vec4(tran.xyz / tran.w, 1.0)).xyz;
}
vec3 getPosition2(sampler2D dt, vec2 coord) {
	float depth = texture(dt, coord).r;
	vec4 tran = matrix_p_inv * vec4(coord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	return tran.xyz / tran.w;
}

bool isInTexture(vec3 pos) {
	return pos.x >= 0.0 && pos.x <= 1.0 && pos.y >= 0.0 && pos.y <= 1.0 && pos.z >= 0.0 && pos.z <= 1.0;
}

void main() {
	float step = 0.6;
	vec3 fragPos = getPosition(positionTex, texCoord);
	
	if(fragPos.x == 0.0 && fragPos.y == 0.0 && fragPos.z == 0.0) {
		fragOut = 1.0;
		return;
	}
	
	vec3 direction = normalize(fragPos);
	
	vec4 pos = vec4(direction * (fract(sin(dot(texCoord.xy ,vec2(12.9898,78.233))) * 42485.42524) + 1.0) * 1.0, 1.0);
	
	float accum = 1.0;
	float divide = 1.0;
	for(float i = 0.0; i < 50.0; ++i) {
		vec4 viewPos = matrix_v * pos;
		vec4 projPos = matrix_p * viewPos;
		projPos.xyz /= projPos.w;
		
		vec3 samplePos = getPosition2(positionTex, projPos.xy * 0.5 + 0.5);
		if(samplePos.z > viewPos.z || samplePos.z == 0.0) break;
		
		vec4 shadowPos = shadowMatrixB * pos;
		shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
		if(!isInTexture(shadowPos.xyz)) {
			accum += 1.0;
		}else {
			accum += (texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.25, 0.0)).r <= shadowPos.z) ? 1.0 : 0.0;
		}
		pos.xyz += direction * step;
		++divide;
	}
	
	fragOut = (accum / divide) * clamp(length(fragPos) * 0.02, 0.0, 1.0);
}

#endif
