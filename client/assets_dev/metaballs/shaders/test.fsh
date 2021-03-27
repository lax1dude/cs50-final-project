
in vec2 texCoord;

layout(location = 0) out vec4 fragOut;

uniform sampler2D tex;

void main()
{
    fragOut = texture(tex, texCoord);
} 