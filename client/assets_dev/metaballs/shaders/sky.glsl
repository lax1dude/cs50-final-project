
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;

out vec3 normalv;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m_invtrans;

void main() {
	normalv = (normIn.xyz * mat3(matrix_m_invtrans)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec3 normalv;

layout(location = 0) out vec3 fragOut;

uniform vec3 skyColor;
uniform vec3 sunColor;
uniform vec3 sunDirection;
uniform float sunSize;

void main() {
	vec3 normal = normalize(normalv);
	
	float sunBrightness = max(dot(normal, -sunDirection), 0.0);
	sunBrightness = pow(sunBrightness, 300.0f / sunSize);
	
	float skyBrightness = pow(dot(sunDirection, vec3(0.0, 1.0, 0.0)) + 1.0, 0.25);
	
    fragOut = mix(skyColor * skyBrightness, sunColor, sunBrightness);
}

#endif
