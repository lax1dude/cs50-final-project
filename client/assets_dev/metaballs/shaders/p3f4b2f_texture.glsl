
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec4 normIn;
layout(location = 2) in vec2 texIn;

out vec2 texCoord;
out vec3 normal;

uniform mat4 matrix_mvp;
uniform mat4 matrix_m_invtrans;

void main()
{
	texCoord = texIn;
	normal = (normIn.xyz * mat3(matrix_m_invtrans)).xyz;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;
in vec3 normal;

layout(location = 0) out vec4 fragOut;

uniform sampler2D tex;

void main()
{
	float intensity = max(dot(normalize(normal),normalize(vec3(1.0, 1.0, 0.0))), 0.0);
	
    fragOut = max(pow(intensity, 1.0 / 2.2), 0.4) * texture(tex, texCoord);
}

#endif
