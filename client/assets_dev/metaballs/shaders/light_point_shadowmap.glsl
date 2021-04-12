#ifdef VERT

layout(location = 0) in vec3 posIn;

uniform mat4 matrix_mvp;

void main() {
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

#define PI 3.14159265

//include dependencies/pbr_common.glsl
#line 19

layout(location = 0) out vec3 diffuseOut;
layout(location = 1) out vec3 specularOut;

uniform sampler2D material; // metallic, roughness, specular, ssr
uniform sampler2D normal;   // normalXYZ, emission
uniform sampler2D position; // position

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform float emission;
uniform vec2 screenSize;
uniform float size;
uniform float shadowMapIndex;

uniform sampler2D shadowMap;
uniform highp mat4 shadowMatrix;

void main() {
	vec4 diffuseV;
	vec4 materialV;
	vec4 normalV;
	vec3 positionV;
	vec3 normalC;
	
	vec2 v_texCoord = gl_FragCoord.xy / screenSize;
	
	materialV = texture(material, v_texCoord);
	normalV = texture(normal, v_texCoord);
	positionV = texture(position, v_texCoord).rgb;
	normalC = normalize(normalV.xyz * 2.0 - 1.0);
	
	vec3 V = normalize(-positionV);
	
	vec3 F0 = vec3(0.04);
	F0 = mix(F0, vec3(1.0), materialV.r);
	
	vec3 L = normalize(lightPosition - positionV);
	vec3 H = normalize(V + L);
	
	float distance = length(lightPosition - positionV);
    float attenuation = 1.0 / (distance * distance);
    vec3 radiance = lightColor * max(attenuation * emission - 0.2, 0.0);
	
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
}

#endif
