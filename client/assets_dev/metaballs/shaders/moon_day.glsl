
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

uniform vec3 moonColor;
uniform vec2 moonTexXY;

uniform sampler2D cloudTextureA;
uniform sampler2D cloudTextureB;
uniform float cloudTextureBlend;

#define MOONTEX_WH 1024.0

uniform sampler2D tex;

void main()
{
    fragOut = vec4(pow(texture(tex, moonTexXY + texCoord * (155.0 / MOONTEX_WH)).rgb * moonColor, vec3(2.2)), 1.0);
}

#endif
