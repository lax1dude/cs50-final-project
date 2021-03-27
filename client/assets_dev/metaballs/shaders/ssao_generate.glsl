
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

uniform sampler2D normal;
uniform sampler2D originalDepth;

uniform mat4 matrix_v_invtrans;
uniform mat4 matrix_p;
uniform mat4 matrix_p_inv;

uniform float randomTime;

uniform vec3 kernel[32];

vec3 rand3(vec2 co){
	float sinDot = sin(dot(co.xy ,vec2(12.9898,78.233)) + randomTime);
    return normalize(vec3(fract(sinDot * 498.2335650) * 2.0 - 1.0, fract(sinDot * 9640.43935658) * 2.0 - 1.0, fract(sinDot * 32334.34356435)));
}

#define bias 0.00001
#define radius 2.0

void main() {
	vec3 normalV = texture(normal, texCoord).xyz;
	if(normalV.x > 0.1 || normalV.y > 0.1 || normalV.z > 0.1) {
		normalV = mat3(matrix_v_invtrans) * (normalV * 2.0 - 1.0); 
		
		float origDepth = texture(originalDepth, texCoord).r * 2.0 - 1.0;
		
		vec4 fragPos0 = matrix_p_inv * vec4(texCoord.xy * 2.0 - 1.0, origDepth, 1.0);
		vec3 fragPos = fragPos0.xyz / fragPos0.w;
		
		vec3 randomVec = rand3(texCoord);
		
		vec3 tangent = normalize(randomVec - normalV * dot(randomVec, normalV));
		vec3 bitangent = cross(normalV, tangent);
		mat3 TBN = mat3(tangent, bitangent, normalV);
		
		float occlusion = 0.0;
		
		for(int i = 0; i < 16; ++i) {
			vec3 samplePos = fragPos + (TBN * kernel[i]) * radius;
			 
			vec4 offset = vec4(samplePos, 1.0);
			offset = matrix_p * offset; // from view to clip-space
			offset.xyz /= offset.w; // perspective divide
			
			vec4 samplePos0 = matrix_p_inv * vec4(offset.xy, texture(originalDepth, offset.xy * 0.5 + 0.5).r * 2.0 - 1.0, 1.0);
			float sampleDepth = samplePos0.z / samplePos0.w;
			
			float rangeCheck = smoothstep(0.0, 1.0, radius / abs(fragPos.z - sampleDepth));
			occlusion += (sampleDepth >= samplePos.z ? 1.0 : 0.0) * rangeCheck;    
		}
		
		fragOut = 1.0 - (occlusion / 16.0);
	}else {
    	fragOut = 1.0;
	}
}

#endif
