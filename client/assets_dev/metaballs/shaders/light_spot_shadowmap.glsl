#ifdef VERT

layout(location = 0) in vec3 posIn;

uniform mat4 matrix_mvp;

void main() {
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

#define PI 3.14159265

//include pbr_common.glsl
#line 19

layout(location = 0) out vec3 diffuseOut;
layout(location = 1) out vec3 specularOut;

uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D position; // position
uniform sampler2D shadowMap;

uniform vec3 lightPosition;
uniform vec3 lightDirection;
uniform vec3 lightColor;
uniform float emission;
uniform float radiusF;
uniform float size;

uniform mat4 shadowMatrix;
uniform float shadowMapIndex;

uniform vec2 screenSize;

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

void main() {
	vec4 diffuseV;
	vec4 materialV;
	vec4 normalV;
	vec3 positionV;
	vec3 normalC;
	
	vec2 v_texCoord = gl_FragCoord.xy / screenSize;
	
	positionV = texture(position, v_texCoord).rgb;
	
	vec3 L = normalize(lightPosition - positionV);
	
	float spotCutoff = max(dot(-lightDirection, L), 0.0);
	float outercutoff = 1.0 - radiusF;
	float cutoff = outercutoff + (size / 360.0);
		
	if(spotCutoff > outercutoff) {
		normalV = texture(normal, v_texCoord);
		normalC = normalize(normalV.xyz * 2.0 - 1.0);
		
		vec4 shadowPos = shadowMatrix * vec4(positionV - lightPosition, 1.0);
		shadowPos.xyz /= shadowPos.w;
		shadowPos.xyz *= 0.5; shadowPos.xyz += 0.5;
		
		float shadow; 
		if(!isInTexture(shadowPos.xyz)) {
			shadow = 1.0;
		}else {
			vec2 xy = vec2(mod(shadowMapIndex, 6.0) / 6.0, (floor(shadowMapIndex / 6.0)) / 6.0);
			
			float accum = 0.0;
			float sampleWeight = 1.0 / 15.0;
		
			vec4 rotate90 = rotationMatrix(vec3(1.0,0.0,0.0), 90.0 * 0.017453293) * vec4(normalC, 1.0);
			
			for(float i = 0.0; i < 15.0; ++i) {
				vec4 rot = rotationMatrix(normalC, i * (360.0 / 7.0) * 0.017453293) * rotate90;
				vec4 sampleLoc = shadowMatrix * vec4((positionV.xyz - lightPosition) + rot.xyz * (i < 7.0 ? 0.007 : 0.014) * min(size, 7.0), 1.0);
				sampleLoc.xyz /= sampleLoc.w;
				sampleLoc.xyz *= 0.5; sampleLoc.xyz += 0.5;
				accum += (texture(shadowMap, clamp(sampleLoc.xy * vec2(0.25, 0.25) + xy, vec2(0.000001), vec2(0.999999))).r <= sampleLoc.z) ? sampleWeight : 0.0;
			}
		
			shadow = max(accum * 2.0 - 1.0, 0.0);
		}	
		
		if(shadow > 0.01) {
			materialV = texture(material, v_texCoord);
	
			float ep = max(cutoff - outercutoff, 0.01);
			
			vec3 V = normalize(-positionV);
			vec3 H = normalize(V + L);
			
			vec3 F0 = vec3(0.04);
			F0 = mix(F0, vec3(1.0), materialV.r);
			
			float distance = length(lightPosition - positionV);
    		float attenuation = 1.0 / (distance * distance);
    		vec3 radiance = lightColor * max(attenuation * emission - 0.2, 0.0) * clamp((spotCutoff - outercutoff) / ep, 0.0, 1.0) * shadow;
			
			float roughness = materialV.g;
			float NDF = DistributionGGX(normalC, H, roughness);   
			float G   = GeometrySmith(normalC, V, L, roughness);      
			vec3  F   = fresnelSchlick(clamp(dot(H, V), 0.0, 1.0), F0);
			
			vec3 kS = F;
			vec3 kD = vec3(1.0) - kS;
			kD *= 1.0 - materialV.r;
		
			vec3 nominator    = NDF * G * F; 
			float denominator = 0.25 * max(dot(normalC, V), 0.0) * max(dot(normalC, L), 0.0);
			vec3 specular = nominator / max(denominator, 0.001);
		
			float NdotL = max(dot(normalC, L), 0.0);
			diffuseOut = radiance * (kD / PI * NdotL);// + vec3(0.05);
			specularOut = radiance * specular * NdotL * materialV.b;
		}else {
			diffuseOut = vec3(0.0);
			specularOut = vec3(0.0);
		}
	}else {
		diffuseOut = vec3(0.0);
		specularOut = vec3(0.0);
	}
}

#endif
