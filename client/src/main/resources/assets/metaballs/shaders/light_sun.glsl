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

#define PI 3.14159265

//include pbr_common.glsl
#line 21

in vec2 v_texCoord;

layout(location = 0) out vec3 diffuseOut;
layout(location = 1) out vec3 specularOut;

uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D position; // position

uniform vec3 sunRGB;
uniform vec3 sunDirection;

void main() {
	vec4 diffuseV;
	vec4 materialV;
	vec4 normalV;
	vec3 positionV;
	vec3 normalC;
	
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = texture(position, v_texCoord).rgb;
	normalC = normalize(normalV.xyz * 2.0 - 1.0);
	
	vec3 V = normalize(-positionV);
	
	vec3 F0 = vec3(0.04);
	F0 = mix(F0, vec3(1.0), materialV.r);
	
	vec3 L = normalize(sunDirection * 100.0 + V);
	vec3 H = normalize(V + L);
	
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
	diffuseOut = sunRGB * (kD / PI * NdotL) + vec3(0.1);
	specularOut = sunRGB * specular * NdotL * materialV.b;
}

#endif
