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

const float PI = 3.14159265359;

vec4 diffuseV;
vec4 materialV;
vec4 normalV;
vec3 positionV;
vec3 lightDiffuseV;
vec3 lightSpecularV;

vec3 normalC;

in vec2 v_texCoord;

layout(location = 0) out vec4 fragOut;

uniform sampler2D diffuse;  // diffuseRGB, ditherBlend
uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D position; // position

uniform sampler2D lightDiffuse;
uniform sampler2D lightSpecular;
uniform sampler2D ssaoBuffer;

uniform samplerCube cubemap;
uniform sampler2D irradianceMapA;
uniform sampler2D irradianceMapB;
uniform sampler2D specularIBL;

uniform sampler2D ssrBuffer;
uniform sampler2D brdfLUT;

uniform float irradianceMapBlend;
uniform int enableSSR;

uniform mat4 matrix_v_inv;
uniform mat4 matrix_p_inv;

uniform vec2 screenSizeInv;

vec3 getPosition(sampler2D dt, vec2 coord) {
	float depth = texture(dt, coord).r;
	vec4 tran = matrix_p_inv * vec4(coord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	if(tran.w == 0.0) return vec3(0.0);
	return (matrix_v_inv * vec4(tran.xyz / tran.w, 1.0)).xyz;
}

float invPI = 0.318309886;
vec2 clipSpaceFromDir(vec3 dir) {
    return vec2(
        atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 2.0 - 1.0
    );
}

vec2 clipSpaceFromDir2(vec3 dir) {
	dir.xz /= abs(dir.y) + 1.0;
	dir.xz = dir.xz * 0.5 + 0.5;
    if(dir.y < 0.0) {
		return dir.xz * vec2(0.5, 1.0);
	}else {
		return dir.xz * vec2(0.5, 1.0) + vec2(0.5, 0.0);
	}
}

vec3 sampleIrradianceTexture(vec3 dir) {
	vec2 pos = clipSpaceFromDir2(dir * vec3(-1.0, -1.0, -1.0));
	return mix(texture(irradianceMapA, pos).rgb, texture(irradianceMapB, pos).rgb, irradianceMapBlend);
}

vec3 sampleCubemap(vec3 normPos, vec3 normalC) {
	return texture(cubemap, reflect(normPos, normalC) * vec3(-1.0, -1.0, 1.0)).rgb;
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(max(1.0 - cosTheta, 0.0), 5.0);
} 

void main() {

	diffuseV = texture(diffuse, v_texCoord);
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = getPosition(position, v_texCoord);
	lightDiffuseV = texture(lightDiffuse, v_texCoord).rgb;
	lightSpecularV = texture(lightSpecular, v_texCoord).rgb;
	normalC = normalV.xyz * 2.0 - 1.0;
	vec4 reflection = vec4(0.0);
	
	vec3 normPos = normalize(positionV);
	
	if(materialV.a > 0.0) {
		if(enableSSR != 0) {
			reflection = texture(ssrBuffer, v_texCoord);
			if(reflection.a < 1.0) {
				reflection = vec4(reflection.rgb + sampleCubemap(normPos, normalC) * (1.0 - reflection.a), 1.0);
			}
		}else {
			reflection = vec4(sampleCubemap(normPos, normalC), 1.0);
		}
	}
	
	float r = materialV.g;
	vec2 specularIBLpos = clipSpaceFromDir2(reflect(normPos, normalC) * vec3(-1.0, -1.0, -1.0));
	vec3 specularIBLValue;
	if(r < 0.2) {
		specularIBLValue = materialV.a > 0.0 ? reflection.rgb : sampleCubemap(normPos, normalC);
	}else if(r < 0.4) {
		specularIBLValue = texture(specularIBL, specularIBLpos * vec2(1.0, 0.25)).rgb;
	}else if(r < 0.6) {
		specularIBLValue = texture(specularIBL, specularIBLpos * vec2(1.0, 0.25) + vec2(0.0, 0.25)).rgb;
	}else if(r < 0.8){
		specularIBLValue = texture(specularIBL, specularIBLpos * vec2(1.0, 0.25) + vec2(0.0, 0.50)).rgb;
	}else {
		specularIBLValue = texture(specularIBL, specularIBLpos * vec2(1.0, 0.25) + vec2(0.0, 0.75)).rgb;
	}
	
	float nDotV = 0.0;
	vec3 Ff = fresnelSchlickRoughness(nDotV = max(dot(normalC, normalize(positionV)), 0.0), vec3(1.0) * materialV.r, r);
	vec2 brdf = texture(brdfLUT, vec2(nDotV, r)).rg;
	vec3 specular2 = specularIBLValue * (Ff * brdf.x + brdf.y);
	
	vec3 irradiance = mix(sampleIrradianceTexture(normalC), vec3(0.3), pow(min(length(positionV) / 32.0, 1.0), 1.0 / 3.0) * 0.5 + 0.5);
	
	vec3 color = diffuseV.rgb * (lightDiffuseV + ((1.0 - materialV.r) * irradiance) + (normalV.a * 50.0)) * (texture(ssaoBuffer, v_texCoord).r * 0.8 + 0.2) + specular2 * materialV.b;
	
	fragOut = vec4(mix(color, reflection.rgb * 0.8, materialV.a * reflection.a) + lightSpecularV, 1.0);
}

#endif
	