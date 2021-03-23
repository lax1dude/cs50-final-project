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

#define PI 3.14159265

// ----------------------------------------------------------------------------
float DistributionGGX(vec3 N, vec3 H, float roughness) {
	float a = roughness*roughness;
	float a2 = a*a;
	float NdotH = max(dot(N, H), 0.0);
	float NdotH2 = NdotH*NdotH;

	float nom   = a2;
	float denom = (NdotH2 * (a2 - 1.0) + 1.0);
	denom = PI * denom * denom;

	return nom / max(denom, 0.0000001); // prevent divide by zero for roughness=0.0 and NdotH=1.0
}
float GeometrySchlickGGX(float NdotV, float roughness) {
	float r = (roughness + 1.0);
	float k = (r*r) / 8.0;

	float nom   = NdotV;
	float denom = NdotV * (1.0 - k) + k;

	return nom / denom;
}
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
	float NdotV = max(dot(N, V), 0.0);
	float NdotL = max(dot(N, L), 0.0);
	float ggx2 = GeometrySchlickGGX(NdotV, roughness);
	float ggx1 = GeometrySchlickGGX(NdotL, roughness);

	return ggx1 * ggx2;
}
vec3 fresnelSchlick(float cosTheta, vec3 F0) {
	return F0 + (1.0 - F0) * pow(max(1.0 - cosTheta, 0.0), 5.0);
}
// ----------------------------------------------------------------------------

vec4 diffuseV;
vec4 materialV;
vec4 normalV;
vec3 positionV;

vec3 normalC;

in vec2 v_texCoord;

layout(location = 0) out vec3 diffuseOut;
layout(location = 1) out vec3 specularOut;

uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D position; // position

uniform vec3 sunRGB;
uniform vec3 sunDirection;
uniform vec3 viewDirection;

void main() {
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = texture(position, v_texCoord).rgb;
	normalC = normalize(normalV.xyz * 2.0 - 1.0);
	
	vec3 V = normalize(-positionV);
	
	vec3 F0 = vec3(0.04);
	F0 = mix(F0, vec3(1.0), materialV.r);
	
	vec3 L = sunDirection;
	vec3 H = normalize(V + L);
	
	float roughness = materialV.g;
	float NDF = DistributionGGX(normalC, H, roughness);   
	float G   = GeometrySmith(normalC, V, L, roughness);      
	vec3  F   = fresnelSchlick(clamp(dot(H, V), 0.0, 1.0), F0);
	
	vec3 nominator    = NDF * G * F; 
	float denominator = 4.0 * max(dot(normalC, V), 0.0) * max(dot(normalC, L), 0.0);
	vec3 specular = nominator / max(denominator, 0.001);
	
	vec3 kS = F;
	vec3 kD = vec3(1.0) - kS;
	kD *= 1.0 - materialV.r;
	float NdotL = max(dot(normalC, L), 0.0);
	
	diffuseOut = (kD / PI * NdotL) + vec3(0.1);
	specularOut = specular * (NdotL * materialV.b);
}

#endif
