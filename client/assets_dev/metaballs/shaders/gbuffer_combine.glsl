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

uniform float irradianceMapBlend;

uniform mat4 matrix_v_inv;
uniform mat4 matrix_p_inv;

vec3 getPosition(sampler2D dt, vec2 coord) {
	float depth = texture(dt, coord).r;
	vec4 tran = matrix_p_inv * vec4(coord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	return (matrix_v_inv * vec4(tran.xyz / tran.w, 1.0)).xyz;
}

float invPI = 0.318309886;
vec2 clipSpaceFromDir(vec3 dir) {
    return vec2(
        atan(dir.x, dir.z) * invPI,
        acos(dir.y) * invPI * 2.0 - 1.0
    );
}

vec3 sampleIrradianceTexture(vec3 dir) {
	vec2 pos = clamp(clipSpaceFromDir(dir * vec3(1.0, -1.0, 1.0)) * 0.5 + 0.5, 0.01, 0.99);
	return mix(texture(irradianceMapA, pos).rgb, texture(irradianceMapB, pos).rgb, irradianceMapBlend);
}

void main() {

	diffuseV = texture(diffuse, v_texCoord);
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = getPosition(position, v_texCoord);
	lightDiffuseV = texture(lightDiffuse, v_texCoord).rgb;
	lightSpecularV = texture(lightSpecular, v_texCoord).rgb;
	normalC = normalV.xyz * 2.0 - 1.0;
	
	vec3 cubemap = texture(cubemap, reflect(normalize(positionV), normalC) * vec3(-1.0, -1.0, 1.0)).rgb;
	vec3 irradiance = mix(sampleIrradianceTexture(normalC), vec3(0.3), pow(min(length(positionV) / 32.0, 1.0), 1.0 / 3.0) * 0.5 + 0.5);
	
	vec3 color = (diffuseV.rgb * (lightDiffuseV + (irradiance * 0.3) + (normalV.a * 50.0)) * (texture(ssaoBuffer, v_texCoord).r * 0.8 + 0.2)) + lightSpecularV;
	fragOut = vec4(mix(color, cubemap * 0.5 + lightSpecularV, materialV.a), 1.0);
}

#endif
