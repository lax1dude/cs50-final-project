
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main()
{
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;

layout(location = 0) out float fragOut;

uniform sampler2D tex;
uniform float farPlane;

void main()
{
    fragOut = (2.0 * 0.1) / (farPlane + 0.1 - (1.0 - texture(tex, texCoord).r) * (farPlane - 0.1));
}

#endif
