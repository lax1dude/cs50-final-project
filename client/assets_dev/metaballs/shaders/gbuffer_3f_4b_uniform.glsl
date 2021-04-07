
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;

out vec3 v_normal;
out vec3 v_pos;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m;
uniform mat4 matrix_m_invtrans;

void main() {
	v_pos = (matrix_m * vec4(posIn, 1.0)).xyz;
	v_normal = (normIn.xyz * mat3(matrix_m_invtrans)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec3 v_normal;
in vec3 v_pos;

layout(location = 0) out vec4 diffuse;  // diffuseRGB, ditherBlend
layout(location = 1) out vec4 material; // metallic, roughness, specular, ssr
layout(location = 2) out vec4 normal;   // normalXYZ, emission
layout(location = 3) out vec3 position; // view space x y z

uniform sampler2D tex;

uniform float ditherBlend;
uniform float metallic;
uniform float roughness;
uniform float specular;
uniform float ssr;
uniform float emission;

uniform vec3 diffuseColor;

void main() {
    diffuse = vec4(diffuseColor, ditherBlend);
	material = vec4(metallic, roughness, specular, ssr);
	normal = vec4(normalize(v_normal) * 0.5 + 0.5, emission);
	position = v_pos;
}

#endif
