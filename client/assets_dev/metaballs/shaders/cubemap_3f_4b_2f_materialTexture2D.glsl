
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
	v_normal = (mat3(matrix_m_invtrans) * normIn.xyz).xyz;
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

uniform vec3 sunDirection;
uniform vec3 sunRGB;

void main() {
	vec2 ttex = fract(v_texCoord);
	vec4 materialA = texture(tex, vec2(ttex.x * 0.333333, ttex.y));
	vec4 materialC = texture(tex, vec2(ttex.x * 0.333333 + 0.666666, ttex.y));
	
    vec4 diffuse2 = computeSunlight(
		pow(materialA.rgb, vec3(2.2)),
		normalize(v_normal * 2.0 - 1.0),
		v_pos, sunDirection, sunRGB,
		vec4(materialC.r, materialC.g, materialC.b, materialA.a)
	);
	if(diffuse2.a == 0.0) discard;
	diffuse = diffuse2.rgb;
}

#endif