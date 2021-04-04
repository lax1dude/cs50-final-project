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
uniform sampler2D irradianceMap;

float invPI = 0.318309886;
vec2 clipSpaceFromDir(vec3 dir) {
	return vec2(
		atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 4.0 - 1.0
	);
}

vec3 sampleIrradianceTexture(vec3 dir) {
	if(dir.y < 0.0) {
		return texture(irradianceMap, (clipSpaceFromDir(dir * vec3(1.0, -1.0, 1.0)) * 0.5 + 0.5) * vec2(0.5, 1.0)).rgb;
	}else {
		return texture(irradianceMap, (clipSpaceFromDir(dir * vec3(1.0, -1.0, 1.0)) * 0.5 + 0.5) * vec2(0.5, 1.0) + vec2(0.5, 0.0)).rgb;
	}
}

void main() {

	diffuseV = texture(diffuse, v_texCoord);
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = texture(position, v_texCoord).rgb;
	lightDiffuseV = texture(lightDiffuse, v_texCoord).rgb;
	lightSpecularV = texture(lightSpecular, v_texCoord).rgb;
	normalC = normalV.xyz * 2.0 - 1.0;
	
	//vec3 cubemap = texture(cubemap, reflect(normalize(positionV), normalC) * vec3(-1.0, -1.0, 1.0)).rgb;
	vec3 cubemap = sampleIrradianceTexture(reflect(normalize(positionV), normalC));
	
	vec3 color = (diffuseV.rgb * lightDiffuseV * (texture(ssaoBuffer, v_texCoord).r * 0.8 + 0.2)) + lightSpecularV;
	fragOut = vec4(color, 1.0);
}

#endif
