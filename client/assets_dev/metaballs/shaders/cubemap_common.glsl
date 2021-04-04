
bool isInTexture(vec3 pos) {
	return pos.x >= 0.0 && pos.x <= 1.0 && pos.y >= 0.0 && pos.y <= 1.0 && pos.z >= 0.0 && pos.z <= 1.0;
}

vec3 computeSunlight(vec3 p_diffuse, vec3 p_normal, vec3 p_position, vec3 p_sunDirection, vec3 p_sunRGB, vec4 p_metallicRoughnessSpecular, sampler2D p_shadowTexture, mat4 p_shadowMatrix) {
	
	vec4 shadowPos = p_shadowMatrix * vec4(p_position, 1.0);
	shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
	if(!isInTexture(shadowPos.xyz) || texture(p_shadowTexture, shadowPos.xy * vec2(0.25, 1.0)).r <= shadowPos.z - 0.001) {
		vec3 lightDir = p_sunDirection;
		float diff = max(dot(lightDir, p_normal), 0.0);
		
		vec3 viewDir = normalize(p_position);
		vec3 halfwayDir = normalize(lightDir + viewDir);
		float spec = pow(max(dot(p_normal, halfwayDir), 0.0), max(32.0 - p_metallicRoughnessSpecular.y * 32.0, 0.0));
		
		return p_diffuse * (diff + 0.1 + p_metallicRoughnessSpecular.w) * p_sunRGB + spec * p_sunRGB * p_metallicRoughnessSpecular.z;
	}else {
		return p_diffuse * 0.1;
	}
	
}
