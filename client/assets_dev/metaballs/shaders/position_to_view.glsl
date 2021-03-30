
#ifdef VERT

layout(location = 0) in vec3 posIn;
layout(location = 1) in vec2 texIn;

out vec2 texCoord;

void main() {
	texCoord = texIn;
    gl_Position = vec4(posIn, 1.0);
}

#endif

#ifdef FRAG

in vec2 texCoord;

layout(location = 0) out vec3 fragOut;

uniform sampler2D positionTex;

uniform mat4 matrix_v;

void main() {
	vec3 posV = texture(positionTex, texCoord).rgb;
	if(posV.x == 0.00 && posV.y == 0.0 && posV.z == 0.0) discard;
	fragOut = (matrix_v * vec4(posV, 1.0)).xyz;
}

#endif
