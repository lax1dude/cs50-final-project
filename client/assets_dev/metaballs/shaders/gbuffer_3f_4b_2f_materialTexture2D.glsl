
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;
layout(location = 2) in vec2 texIn;

out vec2 v_texCoord;
out vec3 v_normal;
out vec3 v_viewdir;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m;
uniform mat4 matrix_m_invtrans;

void main() {
	v_texCoord = texIn;
	v_normal = (mat3(matrix_m_invtrans) * normIn.xyz);
	v_viewdir = (matrix_m * vec4(posIn, 1.0)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 v_texCoord;
in vec3 v_normal;
in vec3 v_viewdir;

layout(location = 0) out vec4 diffuse;  // diffuseRGB, ditherBlend
layout(location = 1) out vec4 material; // metallic, roughness, specular, ssr
layout(location = 2) out vec4 normal;   // normalXYZ, emission

uniform sampler2D tex;

uniform float ditherBlend;

mat3 cotangent_frame(vec3 N, vec3 p, vec2 uv) {
    vec3 dp1 = dFdx(p);
    vec3 dp2 = dFdy(p);
    vec2 duv1 = dFdx(uv);
    vec2 duv2 = dFdy(uv);
    vec3 dp2perp = cross(dp2, N);
    vec3 dp1perp = cross(N, dp1);
    vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
    vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;
    float invmax = inversesqrt(max(dot(T,T), dot(B,B)));
    return mat3(T * invmax, B * invmax, N);
}

void main() {
	if(ditherBlend > 0.0 && mod(gl_FragCoord.x + gl_FragCoord.y, 2.0) == 0.0) discard;
	
	vec2 ttex = fract(v_texCoord);
	vec4 materialA = texture(tex, vec2(ttex.x * 0.333333, ttex.y));
	vec4 materialB = texture(tex, vec2(ttex.x * 0.333333 + 0.333333, ttex.y));
	vec4 materialC = texture(tex, vec2(ttex.x * 0.333333 + 0.666666, ttex.y));
	
    diffuse = vec4(pow(materialA.rgb, vec3(2.2)), ditherBlend);
	material = materialC;
	
	vec3 bump = materialB.xyz * 2.0 - 1.0;
	normal = vec4((cotangent_frame(normalize(v_normal.xyz), normalize(v_viewdir), v_texCoord) * bump) * 0.5 + 0.5, materialA.a);
}

#endif
