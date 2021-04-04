
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;
layout(location = 2) in vec2 texIn;

out vec2 v_texCoord;
out vec3 v_normal;
out vec3 v_pos;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m;
uniform mat4 matrix_m_invtrans;

void main() {
	v_texCoord = texIn;
	v_pos = (matrix_m * vec4(posIn, 1.0)).xyz;
	v_normal = (normIn.xyz * mat3(matrix_m_invtrans)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_texCoord;
in vec3 v_normal;
in vec3 v_pos;

//include cubemap_common.glsl
#line 31

layout(location = 0) out vec3 diffuse;

uniform sampler2D tex;

uniform sampler2D shadowMap;
uniform mat4 shadowMatrix;

uniform vec3 sunDirection;
uniform vec3 sunRGB;

uniform float metallic;
uniform float roughness;
uniform float specular;
uniform float emission;

void main() {
    diffuse = computeSunlight(
		pow(texture(tex, v_texCoord).rgb, vec3(2.2)),
		normalize(v_normal * 2.0 - 1.0),
		v_pos, sunDirection, sunRGB,
		vec4(metallic, roughness, specular, emission),
		shadowMap, shadowMatrix
	);
}

#endif