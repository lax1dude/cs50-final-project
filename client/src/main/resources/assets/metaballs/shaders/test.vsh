
layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main()
{
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}