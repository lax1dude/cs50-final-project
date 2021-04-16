#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 v_texCoord;

void main()
{
	v_texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_texCoord;

layout(location = 0) out vec4 fragOut;

uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D depth; // position

uniform sampler2D prevFrame;

uniform mat4 matrix_v_inv;
uniform mat4 matrix_v_invtrans;
uniform mat4 matrix_p_inv;
uniform mat4 matrix_v;
uniform mat4 matrix_p;

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

vec4 calculateSSR(vec3 pos, vec3 norm2) {
	vec3 viewDir = normalize(pos);
	vec3 norm = mat3(matrix_v_invtrans) * reflect(viewDir, norm2);
	
	const float step = 0.4;
	
	vec3 pos2 = (matrix_v * vec4(pos, 1.0)).xyz + norm * (0.3 * length(v_texCoord * 2.0 - 1.0) + 0.1) * step;
	
	vec4 uv; float dist;
	for(int i = 0; i < 100; ++i) {
		pos2 += norm * 0.25 * step;
		
		uv = matrix_p * vec4(pos2, 1.0);
		uv.xyz /= uv.w;
		uv.xyz = uv.xyz * 0.5 + 0.5;
		
		if(!isInTexture(uv.xyz)) return vec4(0.0);
		
		dist = pos2.z - getPosition2(depth, uv.xy).z;
		
		if(abs(dist) < 0.3 * step) {
		/*
			//if(dot(norm, mat3(matrix_v_invtrans) * (texture(normal, uv.xy).rgb * 2.0 - 1.0)) > -0.5) {
				vec3 pos3 = pos2;
				vec3 dir = norm;
				float dist2; vec4 uv2;
				for(int j = 0; j < 0; ++j) {
					uv2 = matrix_p * vec4(pos3, 1.0);
					uv2.xyz /= uv2.w;
					uv2.xyz = uv2.xyz * 0.5 + 0.5;
					if(!isInTexture(uv2.xyz)) return vec4(0.0);
		
					dist2 = pos3.z - getPosition2(depth, uv2.xy).z;
					dir *= 0.5;
					if(dist2 > 0.0)
						pos3 += dir * 0.25 * step;
					else
						pos3 -= dir * 0.25 * step;
				}
				uv2 = matrix_p * vec4(pos3, 1.0);
				uv2.xy /= uv2.w;
				uv2.xy = clamp(uv2.xy * 0.5 + 0.5, vec2(0.0), vec2(1.0));
				return vec4(texture(prevFrame, uv2.xy).rgb, 1.0);
			//}
		*/
			return vec4(texture(prevFrame, uv.xy).rgb, 1.0);
		}
	}
	return vec4(0.0);
}

void main() {
	float ssr = texture(material, v_texCoord).a;
	if(ssr > 0.0) {
		vec3 normalV = texture(normal, v_texCoord).xyz * 2.0 - 1.0;
		fragOut = calculateSSR(getPosition(depth, v_texCoord), normalV);
	}else {
		fragOut = vec4(0.0);
	}
}

#endif
