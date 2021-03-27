
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

uniform mat4 matrix_mvp;

void main()
{
	texCoord = texIn;
    gl_Position = matrix_mvp * vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;

layout(location = 0) out vec4 fragOut;

uniform sampler2D tex;

void main()
{
    fragOut = texture(tex, texCoord);
}

#endif
