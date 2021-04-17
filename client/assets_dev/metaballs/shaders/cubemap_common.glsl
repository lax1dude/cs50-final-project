
uniform sampler2D shadowMap;
uniform highp mat4 shadowMatrix;

bool isInTexture(vec3 pos) {
	return pos.x >= 0.0 && pos.x <= 1.0 && pos.y >= 0.0 && pos.y <= 1.0 && pos.z >= 0.0 && pos.z <= 1.0;
}

vec4 computeSunlight(vec3 p_diffuse, vec3 p_normal, vec3 p_position, vec3 p_sunDirection, vec3 p_sunRGB, vec4 p_metallicRoughnessSpecularEmission) {
	vec3 lightDir = p_sunDirection;
	float diff = max(dot(lightDir, p_normal), 0.0);
	
	if(diff > 0.0) {
		vec4 shadowPos = shadowMatrix * vec4(p_position, 1.0);
		shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
		
		bool isShadowed;
		if(!isInTexture(shadowPos.xyz)) {
			return vec4(0.0);
		}else {
			isShadowed = texture(shadowMap, shadowPos.xy * vec2(0.25, 1.0) + vec2(0.25, 0.0)).r > shadowPos.z - 0.001;
		}
		
		if(!isShadowed) {
			float diff = max(dot(lightDir, p_normal), 0.0);
			
			vec3 viewDir = normalize(p_position);
			vec3 halfwayDir = normalize(lightDir + viewDir);
			float spec = pow(max(dot(p_normal, halfwayDir), 0.0), max(32.0 - p_metallicRoughnessSpecularEmission.y * 32.0, 0.0));
			
			return vec4(p_diffuse * (diff + 0.1 + (p_metallicRoughnessSpecularEmission.w * 50.0)) * p_sunRGB + spec * p_sunRGB * p_metallicRoughnessSpecularEmission.z, 1.0);
		}else {
			return vec4(p_diffuse * (0.1 + (p_metallicRoughnessSpecularEmission.w * 50.0)), 1.0);
		}
	}else {
		return vec4(p_diffuse * (0.1 + (p_metallicRoughnessSpecularEmission.w * 50.0)), 1.0);
	}
	
}
